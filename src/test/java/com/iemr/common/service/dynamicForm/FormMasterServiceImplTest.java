package com.iemr.common.service.dynamicForm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.iemr.common.data.dynamic_from.FormDefinition;
import com.iemr.common.data.dynamic_from.FormField;
import com.iemr.common.data.dynamic_from.FormFieldOption;
import com.iemr.common.data.dynamic_from.FormModule;
import com.iemr.common.data.translation.Translation;
import com.iemr.common.data.users.UserServiceRole;
import com.iemr.common.dto.dynamicForm.FieldDTO;
import com.iemr.common.dto.dynamicForm.FieldResponseDTO;
import com.iemr.common.dto.dynamicForm.FormDTO;
import com.iemr.common.dto.dynamicForm.FormResponseDTO;
import com.iemr.common.dto.dynamicForm.ModuleDTO;
import com.iemr.common.repository.dynamic_form.FieldRepository;
import com.iemr.common.repository.dynamic_form.FormFieldOptionRepository;
import com.iemr.common.repository.dynamic_form.FormRepository;
import com.iemr.common.repository.dynamic_form.ModuleRepository;
import com.iemr.common.repository.translation.TranslationRepo;
import com.iemr.common.repository.users.UserServiceRoleRepo;
import com.iemr.common.utils.JwtUtil;

/**
 * Covers the dynamic form master: authoring modules, forms and fields, and rendering a
 * form for a caller in their own language.
 *
 * <p>Rendering is the interesting part — it filters fields by the caller's state, resolves
 * label and placeholder translations per language, expands option keys into translated
 * option lists, and parses the JSON validation and conditional blocks.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FormMasterServiceImplTest {

	private static final String FORM_ID = "anc-registration";
	private static final String TOKEN = "a-jwt-token";

	@Mock
	private ModuleRepository moduleRepo;
	@Mock
	private FormRepository formRepo;
	@Mock
	private FieldRepository fieldRepo;
	@Mock
	private TranslationRepo translationRepo;
	@Mock
	private UserServiceRoleRepo userServiceRoleRepo;
	@Mock
	private JwtUtil jwtUtil;
	@Mock
	private FormFieldOptionRepository formFieldOptionRepo;

	@InjectMocks
	private FormMasterServiceImpl service;

	@BeforeEach
	void setUp() {
		when(fieldRepo.save(any(FormField.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(formRepo.save(any(FormDefinition.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(moduleRepo.save(any(FormModule.class))).thenAnswer(invocation -> invocation.getArgument(0));
	}

	private FormDefinition form() {
		FormDefinition form = new FormDefinition();
		form.setFormId(FORM_ID);
		form.setFormName("ANC Registration");
		form.setVersion(3);
		return form;
	}

	private FormField field(String fieldId, Integer stateCode) {
		FormField field = new FormField();
		field.setId(1L);
		field.setForm(form());
		field.setFieldId(fieldId);
		field.setLabel("English label");
		field.setPlaceholder("English placeholder");
		field.setType("text");
		field.setIsRequired(true);
		field.setIsVisible(true);
		field.setIsEditable(true);
		field.setStateCode(stateCode);
		field.setSequence(1);
		return field;
	}

	private Translation translation(String key) {
		Translation translation = new Translation();
		translation.setLabelKey(key);
		translation.setEnglish("English text");
		translation.setHindiTranslation("Hindi text");
		translation.setAssameseTranslation("Assamese text");
		translation.setIsActive(true);
		return translation;
	}

	private void callerIsInState(Integer stateId) {
		UserServiceRole role = new UserServiceRole();
		role.setStateId(stateId);
		when(jwtUtil.getUsernameFromToken(TOKEN)).thenReturn("asha");
		when(userServiceRoleRepo.findByUserName("asha")).thenReturn(List.of(role));
	}

	@Nested
	@DisplayName("authoring")
	class Authoring {

		@Test
		@DisplayName("creates a module under the given name")
		void createsAModule() {
			ModuleDTO dto = new ModuleDTO();
			dto.setModuleName("ANC");

			assertThat(service.createModule(dto).getModuleName()).isEqualTo("ANC");
		}

		@Test
		@DisplayName("creates a form attached to its module")
		void createsAForm() {
			FormModule module = new FormModule();
			module.setModuleName("ANC");
			when(moduleRepo.findById(5L)).thenReturn(Optional.of(module));
			FormDTO dto = new FormDTO();
			dto.setFormId(FORM_ID);
			dto.setFormName("ANC Registration");
			dto.setModuleId(5L);

			FormDefinition created = service.createForm(dto);

			assertThat(created.getFormId()).isEqualTo(FORM_ID);
			assertThat(created.getFormName()).isEqualTo("ANC Registration");
			assertThat(created.getModule()).isSameAs(module);
		}

		@Test
		@DisplayName("rejects a form for an unknown module")
		void rejectsUnknownModule() {
			when(moduleRepo.findById(anyLong())).thenReturn(Optional.empty());
			FormDTO dto = new FormDTO();
			dto.setModuleId(99L);

			assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> service.createForm(dto))
					.withMessageContaining("Invalid module ID");
		}

		@Test
		@DisplayName("creates every field in the batch against its form")
		void createsFields() {
			when(formRepo.findByFormId(FORM_ID)).thenReturn(Optional.of(form()));

			List<FormField> created = service.createField(List.of(fieldDto("age"), fieldDto("weight")));

			assertThat(created).hasSize(2);
			assertThat(created).extracting(FormField::getFieldId).containsExactly("age", "weight");
			verify(fieldRepo, times(2)).save(any(FormField.class));
		}

		@Test
		@DisplayName("carries every field property through to the stored field")
		void carriesFieldPropertiesThrough() {
			when(formRepo.findByFormId(FORM_ID)).thenReturn(Optional.of(form()));

			FormField created = service.createField(List.of(fieldDto("age"))).get(0);

			assertThat(created.getSectionTitle()).isEqualTo("Section for age");
			assertThat(created.getLabel()).isEqualTo("Label for age");
			assertThat(created.getType()).isEqualTo("number");
			assertThat(created.getIsVisible()).isTrue();
			assertThat(created.getIsRequired()).isTrue();
			assertThat(created.getDefaultValue()).isEqualTo("0");
			assertThat(created.getPlaceholder()).isEqualTo("Enter age");
			assertThat(created.getSequence()).isEqualTo(2);
			assertThat(created.getValidation()).isEqualTo("{\"min\":0}");
			assertThat(created.getConditional()).isEqualTo("{\"showIf\":\"always\"}");
		}

		@Test
		@DisplayName("rejects a field for an unknown form")
		void rejectsUnknownForm() {
			when(formRepo.findByFormId(anyString())).thenReturn(Optional.empty());

			assertThatExceptionOfType(IllegalArgumentException.class)
					.isThrownBy(() -> service.createField(List.of(fieldDto("age"))))
					.withMessageContaining("Invalid form ID");
		}

		@Test
		@DisplayName("creates nothing for an empty batch")
		void createsNothingForAnEmptyBatch() {
			assertThat(service.createField(List.of())).isEmpty();
		}

		@Test
		@DisplayName("updates an existing field in place")
		void updatesAField() {
			when(fieldRepo.findById(7L)).thenReturn(Optional.of(field("age", 0)));
			FieldDTO dto = fieldDto("age");
			dto.setId(7L);
			dto.setLabel("Updated label");

			FormField updated = service.updateField(dto);

			assertThat(updated.getId()).isEqualTo(7L);
			assertThat(updated.getLabel()).isEqualTo("Updated label");
			assertThat(updated.getType()).isEqualTo("number");
		}

		@Test
		@DisplayName("rejects an update to a field that does not exist")
		void rejectsUnknownField() {
			when(fieldRepo.findById(anyLong())).thenReturn(Optional.empty());
			FieldDTO dto = fieldDto("age");
			dto.setId(99L);

			assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> service.updateField(dto))
					.withMessageContaining("Field not found: 99");
		}

		@Test
		@DisplayName("deletes a field by id")
		void deletesAField() {
			service.deleteField(7L);

			verify(fieldRepo).deleteById(7L);
		}

		private FieldDTO fieldDto(String fieldId) {
			FieldDTO dto = new FieldDTO();
			dto.setFormId(FORM_ID);
			dto.setFieldId(fieldId);
			dto.setSectionTitle("Section for " + fieldId);
			dto.setLabel("Label for " + fieldId);
			dto.setType("number");
			dto.setIsVisible(true);
			dto.setIsRequired(true);
			dto.setDefaultValue("0");
			dto.setPlaceholder("Enter " + fieldId);
			dto.setSequence(2);
			dto.setOptions("[]");
			dto.setValidation("{\"min\":0}");
			dto.setConditional("{\"showIf\":\"always\"}");
			return dto;
		}
	}

	@Nested
	@DisplayName("getStructuredFormByFormId")
	class StructuredForm {

		@BeforeEach
		void formExists() {
			when(formRepo.findByFormId(FORM_ID)).thenReturn(Optional.of(form()));
			callerIsInState(29);
		}

		@Test
		@DisplayName("returns the form identity and version with a single section")
		void returnsTheFormIdentity() {
			when(fieldRepo.findByForm_FormIdOrderBySequenceAsc(FORM_ID)).thenReturn(List.of(field("age", 0)));

			FormResponseDTO response = service.getStructuredFormByFormId(FORM_ID, "en", TOKEN);

			assertThat(response.getFormId()).isEqualTo(FORM_ID);
			assertThat(response.getFormName()).isEqualTo("ANC Registration");
			assertThat(response.getVersion()).isEqualTo(3);
			assertThat(response.getSections()).hasSize(1);
			assertThat(response.getSections().get(0).getSectionTitle()).isEqualTo("Section Title");
		}

		@Test
		@DisplayName("includes fields shared across states and fields for the caller's state")
		void filtersFieldsByState() {
			when(fieldRepo.findByForm_FormIdOrderBySequenceAsc(FORM_ID))
					.thenReturn(List.of(field("shared", 0), field("karnataka", 29), field("assam", 18)));

			List<FieldResponseDTO> fields = service.getStructuredFormByFormId(FORM_ID, "en", TOKEN).getSections()
					.get(0).getFields();

			assertThat(fields).extracting(FieldResponseDTO::getFieldId).containsExactlyInAnyOrder("shared",
					"karnataka");
		}

		@ParameterizedTest(name = "language {0} renders {1}")
		@CsvSource({ "en, English text", "hi, Hindi text", "as, Assamese text" })
		@DisplayName("renders the label in the requested language")
		void rendersLabelInRequestedLanguage(String language, String expectedLabel) {
			when(fieldRepo.findByForm_FormIdOrderBySequenceAsc(FORM_ID)).thenReturn(List.of(field("age", 0)));
			when(translationRepo.findByLabelKeyAndIsActive("age", true))
					.thenReturn(Optional.of(translation("age")));
			when(translationRepo.findByLabelKeyAndIsActive("placeholder_age", true))
					.thenReturn(Optional.of(translation("placeholder_age")));

			FieldResponseDTO field = firstField(language);

			assertThat(field.getLabel()).isEqualTo(expectedLabel);
			assertThat(field.getPlaceholder()).isEqualTo(expectedLabel);
		}

		@Test
		@DisplayName("keeps the authored label when no translation exists")
		void keepsAuthoredLabelWithoutTranslation() {
			when(fieldRepo.findByForm_FormIdOrderBySequenceAsc(FORM_ID)).thenReturn(List.of(field("age", 0)));
			when(translationRepo.findByLabelKeyAndIsActive(anyString(), anyBoolean())).thenReturn(Optional.empty());

			FieldResponseDTO field = firstField("en");

			assertThat(field.getLabel()).isEqualTo("English label");
			assertThat(field.getPlaceholder()).isEqualTo("English placeholder");
		}

		@Test
		@DisplayName("keeps the authored label for an unrecognised language")
		void keepsAuthoredLabelForUnknownLanguage() {
			when(fieldRepo.findByForm_FormIdOrderBySequenceAsc(FORM_ID)).thenReturn(List.of(field("age", 0)));
			when(translationRepo.findByLabelKeyAndIsActive(anyString(), anyBoolean()))
					.thenReturn(Optional.of(translation("age")));

			assertThat(firstField("fr").getLabel()).isEqualTo("English label");
		}

		@Test
		@DisplayName("carries the field metadata through to the response")
		void carriesFieldMetadataThrough() {
			when(fieldRepo.findByForm_FormIdOrderBySequenceAsc(FORM_ID)).thenReturn(List.of(field("age", 0)));

			FieldResponseDTO field = firstField("en");

			assertThat(field.getId()).isEqualTo(1L);
			assertThat(field.getFormId()).isEqualTo(FORM_ID);
			assertThat(field.getType()).isEqualTo("text");
			assertThat(field.getIsRequired()).isTrue();
			assertThat(field.getVisible()).isTrue();
			assertThat(field.getIsEditable()).isTrue();
			assertThat(field.getStateCode()).isZero();
			assertThat(field.getSequence()).isEqualTo(1);
		}

		@ParameterizedTest(name = "language {0} labels the option {1}")
		@CsvSource({ "en, Yes", "hi, Haan", "as, Hoi" })
		@DisplayName("expands an option key into options labelled in the requested language")
		void expandsOptionKey(String language, String expectedLabel) {
			FormField withOptions = field("consent", 0);
			withOptions.setOptionKey("yes-no");
			when(fieldRepo.findByForm_FormIdOrderBySequenceAsc(FORM_ID)).thenReturn(List.of(withOptions));
			when(formFieldOptionRepo.findByOptionKeyOrderBySortOrderAsc("yes-no")).thenReturn(List.of(option()));

			List<Map<String, Object>> options = firstField(language).getOptions();

			assertThat(options).hasSize(1);
			assertThat(options.get(0)).containsEntry("value", "Y").containsEntry("label", expectedLabel);
			assertThat(options.get(0)).containsKey("id");
		}

		@Test
		@DisplayName("reports no options when the field carries no option key")
		void noOptionsWithoutAnOptionKey() {
			when(fieldRepo.findByForm_FormIdOrderBySequenceAsc(FORM_ID)).thenReturn(List.of(field("age", 0)));

			assertThat(firstField("en").getOptions()).isNull();
		}

		@Test
		@DisplayName("reports no options when the option key resolves to nothing")
		void noOptionsWhenOptionKeyIsEmpty() {
			FormField withOptions = field("consent", 0);
			withOptions.setOptionKey("yes-no");
			when(fieldRepo.findByForm_FormIdOrderBySequenceAsc(FORM_ID)).thenReturn(List.of(withOptions));
			when(formFieldOptionRepo.findByOptionKeyOrderBySortOrderAsc("yes-no")).thenReturn(List.of());

			assertThat(firstField("en").getOptions()).isNull();
		}

		@Test
		@DisplayName("parses the validation and conditional blocks")
		void parsesValidationAndConditional() {
			FormField withJson = field("age", 0);
			withJson.setValidation("{\"min\":0,\"max\":120}");
			withJson.setConditional("{\"showIf\":\"pregnant\"}");
			when(fieldRepo.findByForm_FormIdOrderBySequenceAsc(FORM_ID)).thenReturn(List.of(withJson));

			FieldResponseDTO field = firstField("en");

			assertThat(field.getValidation()).containsEntry("min", 0).containsEntry("max", 120);
			assertThat(field.getConditional()).containsEntry("showIf", "pregnant");
		}

		@Test
		@DisplayName("reports no validation or conditional when the blocks are empty")
		void emptyJsonBlocksBecomeAbsent() {
			FormField withEmptyJson = field("age", 0);
			withEmptyJson.setValidation("{}");
			withEmptyJson.setConditional("   ");
			when(fieldRepo.findByForm_FormIdOrderBySequenceAsc(FORM_ID)).thenReturn(List.of(withEmptyJson));

			FieldResponseDTO field = firstField("en");

			assertThat(field.getValidation()).isNull();
			assertThat(field.getConditional()).isNull();
		}

		@Test
		@DisplayName("fails when a field carries unparseable JSON")
		void failsOnUnparseableJson() {
			FormField broken = field("age", 0);
			broken.setValidation("{not json");
			when(fieldRepo.findByForm_FormIdOrderBySequenceAsc(FORM_ID)).thenReturn(List.of(broken));

			assertThatExceptionOfType(RuntimeException.class)
					.isThrownBy(() -> service.getStructuredFormByFormId(FORM_ID, "en", TOKEN))
					.withMessageContaining("Failed to build form structure");
		}

		@Test
		@DisplayName("fails for an unknown form id")
		void failsForUnknownForm() {
			when(formRepo.findByFormId(anyString())).thenReturn(Optional.empty());

			assertThatExceptionOfType(RuntimeException.class)
					.isThrownBy(() -> service.getStructuredFormByFormId("nope", "en", TOKEN))
					.withMessageContaining("Failed to build form structure");
		}

		@Test
		@DisplayName("treats a caller with no state mapping as having no state")
		void callerWithoutStateMapping() {
			when(userServiceRoleRepo.findByUserName(anyString())).thenReturn(List.of());
			when(fieldRepo.findByForm_FormIdOrderBySequenceAsc(FORM_ID))
					.thenReturn(List.of(field("shared", 0), field("karnataka", 29)));

			List<FieldResponseDTO> fields = service.getStructuredFormByFormId(FORM_ID, "en", TOKEN).getSections()
					.get(0).getFields();

			assertThat(fields).extracting(FieldResponseDTO::getFieldId).containsExactly("shared");
		}

		@Test
		@DisplayName("returns an empty section when the form has no fields")
		void emptySectionWithoutFields() {
			when(fieldRepo.findByForm_FormIdOrderBySequenceAsc(FORM_ID)).thenReturn(List.of());

			assertThat(service.getStructuredFormByFormId(FORM_ID, "en", TOKEN).getSections().get(0).getFields())
					.isEmpty();
		}

		private FieldResponseDTO firstField(String language) {
			return service.getStructuredFormByFormId(FORM_ID, language, TOKEN).getSections().get(0).getFields().get(0);
		}

		private FormFieldOption option() {
			FormFieldOption option = new FormFieldOption();
			option.setId(1);
			option.setOptionKey("yes-no");
			option.setValue("Y");
			option.setLabelEn("Yes");
			option.setLabelHi("Haan");
			option.setLabelAs("Hoi");
			option.setSortOrder(1);
			return option;
		}
	}
}
