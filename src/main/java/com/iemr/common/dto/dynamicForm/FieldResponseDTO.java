package com.iemr.common.dto.dynamicForm;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FieldResponseDTO {
    private Long id;
    private String formId;
    private String sectionTitle;
    private String fieldId;
    private String label;
    private Boolean visible;
    private String type;
    private Boolean isRequired;
    private String defaultValue;
    private String placeholder;
    private Integer sequence;
    private List<String> options;
    private Map<String, Object> validation;
    private Map<String, Object> conditional;
}