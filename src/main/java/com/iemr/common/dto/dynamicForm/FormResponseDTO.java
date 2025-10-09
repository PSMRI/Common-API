package com.iemr.common.dto.dynamicForm;

import lombok.Data;

import java.util.List;

@Data
public class FormResponseDTO {
    private Integer version;
    private String formId;
    private String formName;
    private List<GroupedFieldResponseDTO> sections;
}
