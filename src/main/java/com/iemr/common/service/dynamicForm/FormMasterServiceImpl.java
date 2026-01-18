package com.iemr.common.service.dynamicForm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iemr.common.data.dynamic_from.FormDefinition;
import com.iemr.common.data.dynamic_from.FormField;
import com.iemr.common.data.dynamic_from.FormModule;
import com.iemr.common.data.translation.Translation;
import com.iemr.common.data.users.UserServiceRole;
import com.iemr.common.data.users.UserServiceRoleMapping;
import com.iemr.common.dto.dynamicForm.*;
import com.iemr.common.repository.dynamic_form.FieldRepository;
import com.iemr.common.repository.dynamic_form.FormRepository;
import com.iemr.common.repository.dynamic_form.ModuleRepository;
import com.iemr.common.repository.translation.TranslationRepo;
import com.iemr.common.repository.users.UserRoleMappingRepository;
import com.iemr.common.repository.users.UserServiceRoleRepo;
import com.iemr.common.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FormMasterServiceImpl implements FormMasterService {
    final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private ModuleRepository moduleRepo;
    @Autowired private FormRepository formRepo;
    @Autowired private FieldRepository fieldRepo;

    @Autowired
    private TranslationRepo translationRepo;

    @Autowired
    private UserServiceRoleRepo userServiceRoleRepo;

    @Autowired
    private JwtUtil jwtUtil;

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
    public FormResponseDTO getStructuredFormByFormId(String formId,String lang,String token) {
        int  stateId =0 ;
        try {
            UserServiceRole userServiceRole=  userServiceRoleRepo.findByUserName(jwtUtil.getUsernameFromToken(token));
            if(userServiceRole!=null){
                stateId = userServiceRole.getStateId();
            }
            FormDefinition form = formRepo.findByFormId(formId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid form ID"));

            List<FormField> fields = fieldRepo.findByForm_FormIdAndStateCodeOrderBySequenceAsc(formId,stateId);
            ObjectMapper objectMapper = new ObjectMapper();

            List<FieldResponseDTO> fieldDtos = fields.stream()
                    .map(field -> {
                        String labelKey = field.getFieldId();  // field label already contains label_key

                        Translation t = translationRepo.findByLabelKeyAndIsActive(labelKey, true)
                                .orElse(null);

                        String translatedLabel = field.getLabel(); // fallback

                        if (t != null) {
                            if ("hi".equalsIgnoreCase(lang)) {
                                translatedLabel = t.getHindiTranslation();
                            } else if("as".equalsIgnoreCase(lang)){
                                translatedLabel = t.getAssameseTranslation();
                            }else if("en".equalsIgnoreCase(lang)){
                                translatedLabel = t.getEnglish();

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
                        dto.setPlaceholder(field.getPlaceholder());
                        dto.setSequence(field.getSequence());

                        try {
                            // Handle options
                            if (field.getOptions() != null && !field.getOptions().isBlank()) {
                                JsonNode node = objectMapper.readTree(field.getOptions());
                                List<String> options = null;
                                if (node.isArray()) {
                                    options = objectMapper.convertValue(node, new TypeReference<>() {});
                                } else if (node.has("options")) {
                                    options = objectMapper.convertValue(node.get("options"), new TypeReference<>() {});
                                }
                                dto.setOptions(options == null || options.isEmpty() ? null : options);
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
                        } catch (Exception e) {

                            System.err.println("JSON Parsing Error in field: " + field.getFieldId());
                            throw new RuntimeException("Failed to parse JSON for field: " + field.getFieldId(), e);
                        }

                        return dto;
                    })
                    .sorted(Comparator.comparing(FieldResponseDTO::getId))
                    .collect(Collectors.toList());


            GroupedFieldResponseDTO singleSection = new GroupedFieldResponseDTO();
            singleSection.setSectionTitle(singleSection.getSectionTitle()); // your custom section title
            singleSection.setFields(fieldDtos);

            FormResponseDTO response = new FormResponseDTO();
            response.setVersion(form.getVersion());
            response.setFormId(form.getFormId());
            response.setFormName(form.getFormName());
            response.setSections(List.of(singleSection));
            return response;

        }catch (Exception e){
         logger.error("Exception:"+e.getMessage());
        }

       return  null;

    }


    @Override
    public void deleteField(Long fieldId) {
        fieldRepo.deleteById(fieldId);
    }

}