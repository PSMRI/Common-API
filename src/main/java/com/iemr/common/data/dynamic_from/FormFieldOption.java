package com.iemr.common.data.dynamic_from;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "form_field_options")
@Data
public class FormFieldOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "option_key")
    private String optionKey;

    @Column(name = "value")
    private String value;

    @Column(name = "label_en")
    private String labelEn;

    @Column(name = "label_hi")
    private String labelHi;

    @Column(name = "label_as")
    private String labelAs;

    @Column(name = "label_bn")
    private String labelBn;

    @Column(name = "sort_order")
    private Integer sortOrder;

    // getters/setters
}