package com.iemr.common.service.dynamicForm;

import com.iemr.common.data.dynamic_from.FormDefinition;
import com.iemr.common.data.dynamic_from.FormField;
import com.iemr.common.data.dynamic_from.FormModule;
import com.iemr.common.dto.dynamicForm.FieldDTO;
import com.iemr.common.dto.dynamicForm.FormDTO;
import com.iemr.common.dto.dynamicForm.FormResponseDTO;
import com.iemr.common.dto.dynamicForm.ModuleDTO;

import java.util.List;

public interface FormMasterService {
    FormModule createModule(ModuleDTO dto);
    FormDefinition createForm(FormDTO dto);
    List<FormField> createField(List<FieldDTO> dto);
    FormField updateField(FieldDTO dto);
    FormResponseDTO getStructuredFormByFormId(String formId,String lang,String token);
    void deleteField(Long fieldId);
}
