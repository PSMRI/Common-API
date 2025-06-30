package com.iemr.common.dto.dynamicForm;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FormFieldDTO {
    private String name;
    private String label;
    private String type;
    private boolean required;
    private String inputType;
    private String fileType;
    private String buttonAction;
    private String visibleIfField;
    private String visibleIfValue;
    private Map<String, Object> validations;
    private List<Map<String, String>> options;

    // Getters and Setters
}
