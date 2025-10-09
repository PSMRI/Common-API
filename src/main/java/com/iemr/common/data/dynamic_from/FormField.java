package com.iemr.common.data.dynamic_from;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "form_fields")
public class FormField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", referencedColumnName = "form_id")
    private FormDefinition form;

    @Column(name = "section_title")
    private String sectionTitle;

    @Column(name = "field_id")
    private String fieldId;

    @Column(name = "label")
    private String label;

    @Column(name = "type")
    private String type;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "is_visible")
    private Boolean isVisible;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "placeholder")
    private String placeholder;

    @Column(name = "options", columnDefinition = "json")
    private String options;

    @Column(name = "validation", columnDefinition = "json")
    private String validation; // includes error messages now

    @Column(name = "conditional", columnDefinition = "json")
    private String conditional;

    @Column(name = "sequence")
    private Integer sequence;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
