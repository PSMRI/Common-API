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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FeedbackCategory maps to the m_feedback_category table.
 */
@Entity
@Table(name = "m_feedback_category", uniqueConstraints = {
    @UniqueConstraint(name = "uq_category_slug", columnNames = "Slug")
})
public class FeedbackCategory {

    @Id
    @Column(name = "CategoryID", length = 36, updatable = false, nullable = false)
    private String categoryId;

    @NotBlank
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase alphanumeric with optional single dashes")
    @Size(max = 64)
    @Column(name = "Slug", nullable = false, length = 64)
    private String slug;

    @NotBlank
    @Size(max = 128)
    @Column(name = "Label", nullable = false, length = 128)
    private String label;

    @NotBlank
    @Size(max = 20)
    @Column(name = "Scope", nullable = false, length = 20)
    private String scope;

    @Column(name = "Active", nullable = false)
    private boolean active;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    // ===== Constructors =====
    public FeedbackCategory() {
        // default ctor for JPA
    }

    public FeedbackCategory(String slug, String label, String scope, boolean active) {
        this.slug = slug;
        this.label = label;
        this.scope = scope;
        this.active = active;
    }

    // ===== JPA lifecycle hooks =====
    @PrePersist
    protected void onCreate() {
        if (this.categoryId == null) {
            this.categoryId = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== Getters & Setters =====
    public String getCategoryId() {
        return categoryId;
    }

    // categoryId is generated at persist; setter kept for migration/tests
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // CreatedAt normally set by lifecycle; setter available for tests/migrations
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // UpdatedAt normally managed by lifecycle
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
