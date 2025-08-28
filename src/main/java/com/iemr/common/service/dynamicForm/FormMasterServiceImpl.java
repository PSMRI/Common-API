package com.iemr.common.service.dynamicForm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.dynamic_from.FormDefinition;
import com.iemr.common.data.dynamic_from.FormField;
import com.iemr.common.data.dynamic_from.FormModule;
import com.iemr.common.dto.dynamicForm.*;
import com.iemr.common.repository.dynamic_form.FieldRepository;
import com.iemr.common.repository.dynamic_form.FormRepository;
import com.iemr.common.repository.dynamic_form.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FormMasterServiceImpl implements FormMasterService {

    @Autowired
    private ModuleRepository moduleRepo;
    @Autowired private FormRepository formRepo;
    @Autowired private FieldRepository fieldRepo;

    @Override
    public FormModule createModule(ModuleDTO dto) {
        FormModule module = new FormModule();
        module.setModuleName(dto.getModuleName());
        return moduleRepo.save(module);
    }

    @Override
    public FormDefinition createForm(FormDTO dto) {
        FormModule module = moduleRepo.findById(dto.getModuleId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid module ID"));

        FormDefinition form = new FormDefinition();
        form.setFormId(dto.getFormId());
        form.setFormName(dto.getFormName());
        form.setModule(module);
        return formRepo.save(form);
    }

    @Override
    public List<FormField> createField(List<FieldDTO> dtoList) {
        List<FormField> savedFields = new ArrayList<>();

        for (FieldDTO dto : dtoList) {
            FormDefinition form = formRepo.findByFormId(dto.getFormId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid form ID"));

            FormField field = new FormField();
            field.setForm(form);
            field.setSectionTitle(dto.getSectionTitle());
            field.setFieldId(dto.getFieldId());
            field.setLabel(dto.getLabel());
            field.setType(dto.getType());
            field.setIsVisible(dto.getIsVisible());
            field.setIsRequired(dto.getIsRequired());
            field.setDefaultValue(dto.getDefaultValue());
            field.setPlaceholder(dto.getPlaceholder());
            field.setOptions(dto.getOptions());
            field.setValidation(dto.getValidation());
            field.setConditional(dto.getConditional());
            field.setSequence(dto.getSequence());

            savedFields.add(fieldRepo.save(field));
        }

        return savedFields;
    }

    @Override
    public FormField updateField(FieldDTO dto) {
        FormField field = fieldRepo.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("Field not found: " + dto.getId()));
         field.setId(dto.getId());
        field.setSectionTitle(dto.getSectionTitle());
        field.setLabel(dto.getLabel());
        field.setType(dto.getType());
        field.setIsVisible(dto.getIsVisible());
        field.setIsRequired(dto.getIsRequired());
        field.setDefaultValue(dto.getDefaultValue());
        field.setPlaceholder(dto.getPlaceholder());
        field.setSequence(dto.getSequence());
        field.setOptions(dto.getOptions());
        field.setValidation(dto.getValidation());
        field.setConditional(dto.getConditional());


        return fieldRepo.save(field);
    }

    @Override
    public FormResponseDTO getStructuredFormByFormId(String formId) {
        FormDefinition form = formRepo.findByFormId(formId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid form ID"));

        List<FormField> fields = fieldRepo.findByForm_FormIdOrderBySequenceAsc(formId);
        ObjectMapper objectMapper = new ObjectMapper();

        List<FieldResponseDTO> fieldDtos = fields.stream()
                .map(field -> {
                    FieldResponseDTO dto = new FieldResponseDTO();
                    dto.setId(field.getId());
                    dto.setVisible(field.getIsVisible());
                    dto.setFormId(field.getForm().getFormId());
                    dto.setSectionTitle(field.getSectionTitle());
                    dto.setFieldId(field.getFieldId());
                    dto.setLabel(field.getLabel());
                    dto.setType(field.getType());
                    dto.setIsRequired(field.getIsRequired());
                    dto.setDefaultValue(field.getDefaultValue());
                    dto.setPlaceholder(field.getPlaceholder());
                    dto.setSequence(field.getSequence());

                    try {
                        // Handle options
                        if (field.getOptions() != null && !field.getOptions().isBlank()) {
                            List<String> options = objectMapper.readValue(field.getOptions(), new TypeReference<>() {});
                            dto.setOptions(options.isEmpty() ? null : options);
                        } else {
                            dto.setOptions(null);
                        }

                        // Handle validation
                        if (field.getValidation() != null && !field.getValidation().isBlank()) {
                            Map<String, Object> validation = objectMapper.readValue(field.getValidation(), new TypeReference<>() {});
                            dto.setValidation(validation.isEmpty() ? null : validation);
                        } else {
                            dto.setValidation(null);
                        }

                        // Handle conditional
                        if (field.getConditional() != null && !field.getConditional().isBlank()) {
                            Map<String, Object> conditional = objectMapper.readValue(field.getConditional(), new TypeReference<>() {});
                            dto.setConditional(conditional.isEmpty() ? null : conditional);
                        } else {
                            dto.setConditional(null);
                        }
                    } catch (JsonProcessingException e) {
                        System.err.println("JSON Parsing Error in field: " + field.getFieldId());
                        throw new RuntimeException("Failed to parse JSON for field: " + field.getFieldId(), e);
                    }

                    return dto;
                })
                .sorted(Comparator.comparing(FieldResponseDTO::getId))
                .collect(Collectors.toList());


        GroupedFieldResponseDTO singleSection = new GroupedFieldResponseDTO();
        singleSection.setSectionTitle("HBNC Form Fields"); // your custom section title
        singleSection.setFields(fieldDtos);

        FormResponseDTO response = new FormResponseDTO();
        response.setVersion(form.getVersion());
        response.setFormId(form.getFormId());
        response.setFormName(form.getFormName());
        response.setSections(List.of(singleSection));

        return response;
    }


    @Override
    public void deleteField(Long fieldId) {
        fieldRepo.deleteById(fieldId);
    }

}