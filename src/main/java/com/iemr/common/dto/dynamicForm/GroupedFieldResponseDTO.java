package com.iemr.common.dto.dynamicForm;

import lombok.Data;

import java.util.List;

@Data
public class GroupedFieldResponseDTO {
    private String sectionTitle;
    private List<FieldResponseDTO> fields;
}
