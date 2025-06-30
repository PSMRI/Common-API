package com.iemr.common.service.dynamicForm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.dynamic_from.FormEntity;
import com.iemr.common.data.dynamic_from.ModuleEntity;
import com.iemr.common.dto.dynamicForm.FormTypeEntityDTO;
import com.iemr.common.repository.dynamic_form.FormTypeRepository;
import com.iemr.common.repository.dynamic_form.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FormTypeService {
    @Autowired
    ModuleRepository moduleRepo;
    @Autowired
    FormTypeRepository formTypeRepo;
    @Autowired
    ObjectMapper objectMapper;

    public FormEntity createFormType(Long moduleId, FormTypeEntityDTO formTypeDTO) {
        ModuleEntity module = moduleRepo.findById(moduleId).orElseThrow();
        FormEntity formType = new FormEntity();
        formType.setFormName(formTypeDTO.getFormName());
        formType.setModule(module);
        try {
            String fieldsJson = objectMapper.writeValueAsString(formTypeDTO.getFields());
            formType.setFields(fieldsJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize form fields", e);
        }
        return formTypeRepo.save(formType);
    }
}