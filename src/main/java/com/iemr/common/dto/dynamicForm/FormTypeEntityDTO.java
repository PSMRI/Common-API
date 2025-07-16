package com.iemr.common.dto.dynamicForm;

import lombok.Data;

import java.util.List;

@Data
public class FormTypeEntityDTO {

    private Long id;

    private String formName;

    private ModuleEntityDTO module;

    private List<FormFieldDTO> fields;

}
