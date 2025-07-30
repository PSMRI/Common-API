package com.iemr.common.dto.dynamicForm;

import lombok.Data;

@Data
public class FormDTO {
    private String formId;
    private String formName;
    private Long moduleId;
}