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
package com.iemr.common.controller.platform_feedback;

import com.iemr.common.dto.platform_feedback.CategoryResponse;
import com.iemr.common.dto.platform_feedback.FeedbackRequest;
import com.iemr.common.dto.platform_feedback.FeedbackResponse;
import com.iemr.common.service.platform_feedback.PlatformFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Platform Feedback", description = "Feedback ingestion and category listing for platform-wide feedback")
@RestController
@RequestMapping("/platform-feedback")
@Validated
public class PlatformFeedbackController {

    private final PlatformFeedbackService service;

    public PlatformFeedbackController(PlatformFeedbackService service) {
        this.service = service;
    }

    @Operation(summary = "Submit feedback (public endpoint)",
               description = "Accepts feedback (anonymous or identified). Accepts categoryId or categorySlug; slug is preferred.")
    @ApiResponse(responseCode = "201", description = "Feedback accepted")
    @ApiResponse(responseCode = "400", description = "Validation or business error", content = @Content)
    @PostMapping
    public ResponseEntity<FeedbackResponse> submit(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Feedback payload")
            @Valid @RequestBody FeedbackRequest req) {
        FeedbackResponse resp = service.submitFeedback(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @Operation(summary = "List active categories",
               description = "Returns active categories. Optionally filter by serviceLine (frontend convenience).")
    @ApiResponse(responseCode = "200", description = "List of categories")
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> list(
            @Parameter(description = "Optional serviceLine to prefer scopes (1097|104|AAM|MMU|TM|ECD)")
            @RequestParam(required = false) String serviceLine) {
        if (serviceLine == null) serviceLine = "GLOBAL";
        List<CategoryResponse> list = service.listCategories(serviceLine);
        return ResponseEntity.ok(list);
    }
}
