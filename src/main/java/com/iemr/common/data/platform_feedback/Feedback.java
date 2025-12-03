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
package com.iemr.common.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "m_platform_feedback")
public class Feedback {

    @Id
    @Column(name = "FeedbackID", length = 36, updatable = false, nullable = false)
    private String feedbackId;

    @Column(name = "CreatedAt", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Min(1)
    @Max(5)
    @Column(name = "Rating", nullable = false)
    private int rating;

    @Size(max = 2000)
    @Column(name = "Comment", columnDefinition = "TEXT", nullable = true)
    private String comment;

    @Column(name = "ServiceLine", nullable = false, length = 10)
    private String serviceLine;

    @Column(name = "IsAnonymous", nullable = false)
    private boolean isAnonymous = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CategoryID", referencedColumnName = "CategoryID", nullable = false)
    private FeedbackCategory category;

    @Column(name = "UserID", nullable = true)
    private Integer userId;

    public Feedback() {
        this.feedbackId = UUID.randomUUID().toString();
    }

    public Feedback(int rating, String comment, String serviceLine, boolean isAnonymous, FeedbackCategory category, Integer userId) {
        this(); // ensures feedbackId
        this.setRating(rating);
        this.setComment(comment);
        this.setServiceLine(serviceLine);
        this.isAnonymous = isAnonymous;
        this.category = category;
        this.userId = userId;
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    // Don't usually set feedbackId externally, but keep setter for testing/migration if needed
    public void setFeedbackId(String feedbackId) {
        this.feedbackId = feedbackId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        if (comment != null && comment.length() > 2000) {
            throw new IllegalArgumentException("Comment cannot exceed 2000 characters");
        }
        this.comment = (comment == null || comment.trim().isEmpty()) ? null : comment.trim();
    }

    public String getServiceLine() {
        return serviceLine;
    }

    public void setServiceLine(String serviceLine) {
        if (serviceLine == null || serviceLine.trim().isEmpty()) {
            throw new IllegalArgumentException("ServiceLine must not be null or empty.");
        }
        this.serviceLine = serviceLine;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
        if (anonymous) {
            this.userId = null;
        }
    }

    public FeedbackCategory getCategory() {
        return category;
    }

    public void setCategory(FeedbackCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Category must not be null.");
        }
        this.category = category;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
        if (userId != null) {
            this.isAnonymous = false;
        }
    }
}
