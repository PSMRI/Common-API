/*
 * AMRIT – Accessible Medical Records via Integrated Technology
 * Integrated EHR (Electronic Health Records) Solution
 *
 * Copyright (C) "Piramal Swasthya Management and Research Institute"
 *
 * This file is part of AMRIT.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 */
package com.iemr.common.service.platform_feedback;

import com.iemr.common.data.platform_feedback.Feedback;
import com.iemr.common.data.platform_feedback.FeedbackCategory;
import com.iemr.common.dto.platform_feedback.CategoryResponse;
import com.iemr.common.dto.platform_feedback.FeedbackRequest;
import com.iemr.common.dto.platform_feedback.FeedbackResponse;
import com.iemr.common.repository.platform_feedback.PlatformFeedbackCategoryRepository;
import com.iemr.common.repository.platform_feedback.PlatformFeedbackRepository;
import com.iemr.common.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PlatformFeedbackService {

    private final PlatformFeedbackRepository feedbackRepo;
    private final PlatformFeedbackCategoryRepository categoryRepo;

    public PlatformFeedbackService(PlatformFeedbackRepository feedbackRepo,
                                   PlatformFeedbackCategoryRepository categoryRepo) {
        this.feedbackRepo = feedbackRepo;
        this.categoryRepo = categoryRepo;
    }

    @Transactional
    public FeedbackResponse submitFeedback(FeedbackRequest req) {
        // basic validations
        if (req.rating() < 1 || req.rating() > 5) {
            throw new BadRequestException("rating must be between 1 and 5");
        }
        if (!req.isAnonymous() && req.userId() == null) {
            throw new BadRequestException("userId required when isAnonymous=false");
        }

        FeedbackCategory category = resolveCategory(req.categoryId(), req.categorySlug(), req.serviceLine());

        Feedback fb = new Feedback();
        fb.setFeedbackId(UUID.randomUUID().toString());
        fb.setCreatedAt(LocalDateTime.now());
        fb.setUpdatedAt(LocalDateTime.now());
        fb.setRating(req.rating());
        fb.setComment(req.comment() == null ? "" : req.comment());
        fb.setServiceLine(req.serviceLine());
        fb.setAnonymous(req.isAnonymous()); 
        fb.setCategory(category);
        fb.setUserId(req.userId());

        feedbackRepo.save(fb);
        return new FeedbackResponse(fb.getFeedbackId(), fb.getCreatedAt());
    }

    private FeedbackCategory resolveCategory(String categoryId, String categorySlug, String serviceLine) {
        if (categoryId != null && categorySlug != null) {
            FeedbackCategory byId = categoryRepo.findById(categoryId)
                    .orElseThrow(() -> new BadRequestException("invalid categoryId"));
            if (!byId.getSlug().equalsIgnoreCase(categorySlug)) {
                throw new BadRequestException("categoryId and categorySlug mismatch");
            }
            if (!byId.isActive()) throw new BadRequestException("category inactive");
            // optional: check scope matches serviceLine or GLOBAL
            return byId;
        }

        if (categoryId != null) {
            FeedbackCategory byId = categoryRepo.findById(categoryId)
                    .orElseThrow(() -> new BadRequestException("invalid categoryId"));
            if (!byId.isActive()) throw new BadRequestException("category inactive");
            return byId;
        }

        if (categorySlug != null) {
            FeedbackCategory bySlug = categoryRepo.findBySlugIgnoreCase(categorySlug)
                    .orElseThrow(() -> new BadRequestException("invalid categorySlug"));
            if (!bySlug.isActive()) throw new BadRequestException("category inactive");
            return bySlug;
        }

        throw new BadRequestException("categoryId or categorySlug required");
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories(String serviceLine) {
        List<FeedbackCategory> all = categoryRepo.findByActiveTrueOrderByLabelAsc();
        // filter by scope or return all; FE can filter further
        return all.stream()
                .filter(c -> "GLOBAL".equals(c.getScope()) || c.getScope().equalsIgnoreCase(serviceLine))
                .map(c -> new CategoryResponse(c.getCategoryId(), c.getSlug(), c.getLabel(), c.getScope(), c.isActive()))
                .toList();
    }
}
