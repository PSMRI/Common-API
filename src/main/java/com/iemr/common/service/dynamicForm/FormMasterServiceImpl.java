package com.iemr.common.service.dynamicForm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.dynamic_from.FormDefinition;
import com.iemr.common.data.dynamic_from.FormField;
import com.iemr.common.data.dynamic_from.FormFieldOption;
import com.iemr.common.data.dynamic_from.FormModule;
import com.iemr.common.data.translation.Translation;
import com.iemr.common.data.users.UserServiceRole;
import com.iemr.common.dto.dynamicForm.*;
import com.iemr.common.repository.dynamic_form.FieldRepository;
import com.iemr.common.repository.dynamic_form.FormFieldOptionRepository;
import com.iemr.common.repository.dynamic_form.FormRepository;
import com.iemr.common.repository.dynamic_form.ModuleRepository;
import com.iemr.common.repository.translation.TranslationRepo;
import com.iemr.common.repository.users.UserServiceRoleRepo;
import com.iemr.common.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FormMasterServiceImpl implements FormMasterService {
    final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private ModuleRepository moduleRepo;
    @Autowired
    private FormRepository formRepo;
    @Autowired
    private FieldRepository fieldRepo;

    @Autowired
    private TranslationRepo translationRepo;

    @Autowired
    private UserServiceRoleRepo userServiceRoleRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private FormFieldOptionRepository formFieldOptionRepo ;

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
    public FormResponseDTO getStructuredFormByFormId(String formId, String lang, String token) {
        Integer stateId = 0;
        try {
            String username = jwtUtil.getUsernameFromToken(token);

            stateId = userServiceRoleRepo.findByUserName(username)
                    .stream()
                    .findFirst()
                    .map(UserServiceRole::getStateId)
                    .filter(Objects::nonNull)
                    .orElse(null);


            FormDefinition form = formRepo.findByFormId(formId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid form ID"));

            List<FormField> fields = fieldRepo.findByForm_FormIdOrderBySequenceAsc(formId);
            ObjectMapper objectMapper = new ObjectMapper();

            Integer finalStateId = stateId;
            List<FieldResponseDTO> fieldDtos = fields.stream().filter(formField -> (formField.getStateCode().equals(0) || formField.getStateCode().equals(finalStateId)))
                    .map(field -> {
                        String labelKey = field.getFieldId();

                        Translation label = translationRepo.findByLabelKeyAndIsActive(labelKey, true)
                                .orElse(null);

                        Translation placeHolder = translationRepo.findByLabelKeyAndIsActive("placeholder_"+labelKey, true)
                                .orElse(null);

                        String translatedLabel = field.getLabel();
                        String translatedPlaceHolder = field.getPlaceholder();

                        if (label != null) {
                            if ("hi".equalsIgnoreCase(lang)) {
                                translatedLabel = label.getHindiTranslation();
                            } else if ("as".equalsIgnoreCase(lang)) {
                                translatedLabel = label.getAssameseTranslation();
                            } else if ("en".equalsIgnoreCase(lang)) {
                                translatedLabel = label.getEnglish();

                            }
                        }

                        if (placeHolder != null) {
                            if ("hi".equalsIgnoreCase(lang)) {
                                translatedPlaceHolder= placeHolder.getHindiTranslation();
                            } else if ("as".equalsIgnoreCase(lang)) {
                                translatedPlaceHolder = placeHolder.getAssameseTranslation();
                            } else if ("en".equalsIgnoreCase(lang)) {
                                translatedPlaceHolder = placeHolder.getEnglish();

                            }
                        }


                        FieldResponseDTO dto = new FieldResponseDTO();
                        dto.setId(field.getId());
                        dto.setIsEditable(field.getIsEditable());
                        dto.setStateCode(field.getStateCode());
                        dto.setVisible(field.getIsVisible());
                        dto.setFormId(field.getForm().getFormId());
                        dto.setSectionTitle(field.getSectionTitle());
                        dto.setFieldId(field.getFieldId());
                        dto.setLabel(translatedLabel);
                        dto.setType(field.getType());
                        dto.setIsRequired(field.getIsRequired());
                        dto.setDefaultValue(field.getDefaultValue());
                        dto.setPlaceholder(translatedPlaceHolder);
                        dto.setSequence(field.getSequence());


                        try {
                            if (field.getOptionKey() != null && !field.getOptionKey().isBlank()) {
                                List<FormFieldOption> dbOptions = formFieldOptionRepo
                                        .findByOptionKeyOrderBySortOrderAsc(field.getOptionKey());

                                List<Map<String, Object>> translatedOptions = dbOptions.stream()
                                        .map(opt -> {
                                            Map<String, Object> map = new LinkedHashMap<>();
                                            map.put("id", opt.getId());
                                            map.put("value", opt.getValue());
                                            if ("hi".equalsIgnoreCase(lang))      map.put("label", opt.getLabelHi());
                                            else if ("as".equalsIgnoreCase(lang)) map.put("label", opt.getLabelAs());
                                            else                                   map.put("label", opt.getLabelEn());
                                            return map;
                                        })
                                        .collect(Collectors.toList());

                                dto.setOptions(translatedOptions.isEmpty() ? null : translatedOptions);

                            } else {
                                dto.setOptions(null);
                            }
                            if (field.getValidation() != null && !field.getValidation().isBlank()) {
                                Map<String, Object> validation = objectMapper.readValue(field.getValidation(), new TypeReference<>() {
                                });
                                dto.setValidation(validation.isEmpty() ? null : validation);
                            } else {
                                dto.setValidation(null);
                            }

                            if (field.getConditional() != null && !field.getConditional().isBlank()) {
                                Map<String, Object> conditional = objectMapper.readValue(field.getConditional(), new TypeReference<>() {
                                });
                                dto.setConditional(conditional.isEmpty() ? null : conditional);
                            } else {
                                dto.setConditional(null);
                            }
                        } catch (Exception e) {

                            System.err.println("JSON Parsing Error in field: " + field.getFieldId());
                            throw new RuntimeException("Failed to parse JSON for field: " + field.getFieldId(), e);
                        }

                        return dto;
                    })
                    .sorted(Comparator.comparing(FieldResponseDTO::getId))
                    .collect(Collectors.toList());


            GroupedFieldResponseDTO singleSection = new GroupedFieldResponseDTO();
            singleSection.setFields(fieldDtos);
            singleSection.setSectionTitle(
                    Objects.requireNonNullElse(singleSection.getSectionTitle(), "Section Title")
            );
            FormResponseDTO response = new FormResponseDTO();
            response.setVersion(form.getVersion());
            response.setFormId(form.getFormId());
            response.setFormName(form.getFormName());
            response.setSections(List.of(singleSection));
            return response;

        } catch (Exception e) {
            logger.error("Exception while building form response", e);
            throw new RuntimeException("Failed to build form structure");
        }

    }


    @Override
    public void deleteField(Long fieldId) {
        fieldRepo.deleteById(fieldId);
    }

}