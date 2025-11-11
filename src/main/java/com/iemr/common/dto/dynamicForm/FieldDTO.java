package com.iemr.common.dto.dynamicForm;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FieldDTO {
    private Long id;
    private String formId;
    private String sectionTitle;
    private String fieldId;
    private String label;
    private String type;
    private Boolean isVisible;
    private Boolean isRequired;
    private String defaultValue;
    private String placeholder;
    private Integer sequence;
    private String options; // ⬅️ changed from String to List<String>
    private String validation; // ⬅️ changed from String to Map
    private String conditional; // ⬅️ changed from String to Map
}

