package com.iemr.common.controller.dynamicForm;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.iemr.common.data.dynamic_from.FormDefinition;
import com.iemr.common.data.dynamic_from.FormField;
import com.iemr.common.data.dynamic_from.FormModule;
import com.iemr.common.dto.dynamicForm.FieldDTO;
import com.iemr.common.dto.dynamicForm.FormDTO;
import com.iemr.common.dto.dynamicForm.FormResponseDTO;
import com.iemr.common.dto.dynamicForm.ModuleDTO;
import com.iemr.common.service.dynamicForm.FormMasterService;

/**
 * Covers the dynamic form authoring and rendering endpoints.
 *
 * <p>Each endpoint wraps the service call in a success or error envelope, so the tests
 * assert the HTTP status, the success flag and the message for both outcomes.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DynamicFormControllerTest {

	@Mock
	private FormMasterService formMasterService;

	@InjectMocks
	private DynamicFormController controller;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Nested
	@DisplayName("createModule")
	class CreateModule {

		@Test
		@DisplayName("reports the created module")
		void reportsTheCreatedModule() throws Exception {
			when(formMasterService.createModule(any(ModuleDTO.class))).thenReturn(new FormModule());

			mockMvc.perform(post("/dynamicForm/createModule").contentType(MediaType.APPLICATION_JSON)
					.content("{\"moduleName\":\"ANC\"}")).andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.message").value("Module created successfully"));
		}

		@Test
		@DisplayName("reports invalid module data as a bad request")
		void reportsInvalidData() throws Exception {
			when(formMasterService.createModule(any(ModuleDTO.class)))
					.thenThrow(new IllegalArgumentException("name required"));

			mockMvc.perform(post("/dynamicForm/createModule").contentType(MediaType.APPLICATION_JSON)
					.content("{\"moduleName\":\"\"}")).andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.success").value(false))
					.andExpect(jsonPath("$.message").value("Invalid module data: name required"));
		}

		@Test
		@DisplayName("reports an unexpected failure as a server error")
		void reportsUnexpectedFailure() throws Exception {
			when(formMasterService.createModule(any(ModuleDTO.class))).thenThrow(new RuntimeException("db down"));

			mockMvc.perform(post("/dynamicForm/createModule").contentType(MediaType.APPLICATION_JSON)
					.content("{\"moduleName\":\"ANC\"}")).andExpect(status().isInternalServerError())
					.andExpect(jsonPath("$.message").value("Failed to create module"));
		}
	}

	@Nested
	@DisplayName("createForm")
	class CreateForm {

		@Test
		@DisplayName("reports the created form")
		void reportsTheCreatedForm() throws Exception {
			when(formMasterService.createForm(any(FormDTO.class))).thenReturn(new FormDefinition());

			mockMvc.perform(post("/dynamicForm/createForm").contentType(MediaType.APPLICATION_JSON)
					.content("{\"formId\":\"anc\",\"formName\":\"ANC\",\"moduleId\":1}")).andExpect(status().isOk())
					.andExpect(jsonPath("$.message").value("Form created successfully"));
		}

		@Test
		@DisplayName("reports a failure as a server error")
		void reportsFailure() throws Exception {
			when(formMasterService.createForm(any(FormDTO.class)))
					.thenThrow(new IllegalArgumentException("Invalid module ID"));

			mockMvc.perform(post("/dynamicForm/createForm").contentType(MediaType.APPLICATION_JSON)
					.content("{\"moduleId\":99}")).andExpect(status().isInternalServerError())
					.andExpect(jsonPath("$.message").value("Failed to create form"));
		}
	}

	@Nested
	@DisplayName("createFields")
	class CreateFields {

		@Test
		@DisplayName("reports the created fields")
		void reportsTheCreatedFields() throws Exception {
			when(formMasterService.createField(any())).thenReturn(List.of(new FormField()));

			mockMvc.perform(post("/dynamicForm/createFields").contentType(MediaType.APPLICATION_JSON)
					.content("[{\"formId\":\"anc\",\"fieldId\":\"age\"}]")).andExpect(status().isOk())
					.andExpect(jsonPath("$.message").value("Fields created successfully"));
		}

		@Test
		@DisplayName("reports a failure as a server error")
		void reportsFailure() throws Exception {
			when(formMasterService.createField(any())).thenThrow(new IllegalArgumentException("Invalid form ID"));

			mockMvc.perform(post("/dynamicForm/createFields").contentType(MediaType.APPLICATION_JSON)
					.content("[{\"formId\":\"nope\"}]")).andExpect(status().isInternalServerError())
					.andExpect(jsonPath("$.message").value("Failed to create fields"));
		}
	}

	@Nested
	@DisplayName("updateField")
	class UpdateField {

		@Test
		@DisplayName("reports the updated field")
		void reportsTheUpdatedField() throws Exception {
			when(formMasterService.updateField(any(FieldDTO.class))).thenReturn(new FormField());

			mockMvc.perform(post("/dynamicForm/field/update").contentType(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer token").content("{\"id\":1,\"label\":\"Age\"}"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.message").value("Field updated successfully"));
		}

		@Test
		@DisplayName("reports a failure as a server error")
		void reportsFailure() throws Exception {
			when(formMasterService.updateField(any(FieldDTO.class)))
					.thenThrow(new IllegalArgumentException("Field not found: 99"));

			mockMvc.perform(post("/dynamicForm/field/update").contentType(MediaType.APPLICATION_JSON)
					.header("Authorization", "Bearer token").content("{\"id\":99}"))
					.andExpect(status().isInternalServerError())
					.andExpect(jsonPath("$.message").value("Failed to update field"));
		}
	}

	@Nested
	@DisplayName("deleteField")
	class DeleteField {

		@Test
		@DisplayName("reports the deletion")
		void reportsTheDeletion() throws Exception {
			mockMvc.perform(delete("/dynamicForm/delete/7/field").header("Authorization", "Bearer token"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.message").value("Field deleted successfully"));

			verify(formMasterService).deleteField(7L);
		}

		@Test
		@DisplayName("reports a failure as a server error")
		void reportsFailure() throws Exception {
			doThrow(new RuntimeException("db down")).when(formMasterService).deleteField(anyLong());

			mockMvc.perform(delete("/dynamicForm/delete/7/field").header("Authorization", "Bearer token"))
					.andExpect(status().isInternalServerError())
					.andExpect(jsonPath("$.message").value("Failed to delete field"));
		}
	}

	@Nested
	@DisplayName("getStructuredForm")
	class GetStructuredForm {

		@Test
		@DisplayName("returns the rendered form structure")
		void returnsTheStructure() throws Exception {
			FormResponseDTO response = new FormResponseDTO();
			response.setFormId("anc");
			when(formMasterService.getStructuredFormByFormId("anc", "en", "a-token")).thenReturn(response);

			mockMvc.perform(get("/dynamicForm/form/anc/fields").header("jwttoken", "a-token"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.message").value("Form structure fetched successfully"))
					.andExpect(jsonPath("$.data.formId").value("anc"));
		}

		@Test
		@DisplayName("defaults the language to English")
		void defaultsLanguageToEnglish() throws Exception {
			when(formMasterService.getStructuredFormByFormId(anyString(), anyString(), anyString()))
					.thenReturn(new FormResponseDTO());

			mockMvc.perform(get("/dynamicForm/form/anc/fields").header("jwttoken", "a-token"))
					.andExpect(status().isOk());

			verify(formMasterService).getStructuredFormByFormId("anc", "en", "a-token");
		}

		@Test
		@DisplayName("honours an explicit language")
		void honoursExplicitLanguage() throws Exception {
			when(formMasterService.getStructuredFormByFormId(anyString(), anyString(), anyString()))
					.thenReturn(new FormResponseDTO());

			mockMvc.perform(get("/dynamicForm/form/anc/fields").param("lang", "hi").header("jwttoken", "a-token"))
					.andExpect(status().isOk());

			verify(formMasterService).getStructuredFormByFormId("anc", "hi", "a-token");
		}

		@Test
		@DisplayName("reports a rendering failure with its cause")
		void reportsRenderingFailure() throws Exception {
			when(formMasterService.getStructuredFormByFormId(anyString(), anyString(), anyString()))
					.thenThrow(new RuntimeException("Failed to build form structure"));

			mockMvc.perform(get("/dynamicForm/form/anc/fields").header("jwttoken", "a-token"))
					.andExpect(status().isInternalServerError())
					.andExpect(jsonPath("$.success").value(false));
		}
	}
}
