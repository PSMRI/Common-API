package com.iemr.common.controller.dynamicForm;

import com.iemr.common.dto.dynamicForm.FieldDTO;
import com.iemr.common.dto.dynamicForm.FormDTO;
import com.iemr.common.dto.dynamicForm.ModuleDTO;
import com.iemr.common.service.dynamicForm.FormMasterService;
import com.iemr.common.utils.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = "dynamicForm")
@RestController
public class DynamicFormController {
    @Autowired
    private FormMasterService formMasterService;

    @PostMapping(value = "createModule")
    public ResponseEntity<ApiResponse<?>> createModule(@Valid @RequestBody ModuleDTO moduleDTO) {
        try {
            Object result = formMasterService.createModule(moduleDTO);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success("Module created successfully", HttpStatus.OK.value(), result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid module data: " + e.getMessage(), HttpStatus.BAD_REQUEST.value(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create module", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @PostMapping(value = "createForm")
    public ResponseEntity<ApiResponse<?>> createForm(@Valid @RequestBody FormDTO dto) {
        try {
            Object result = formMasterService.createForm(dto);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success("Form created successfully", HttpStatus.OK.value(), result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create form", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @PostMapping(value = "createFields")
    public ResponseEntity<ApiResponse<?>> createField(@Valid @RequestBody List<FieldDTO> dto) {
        try {
            Object result = formMasterService.createField(dto);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success("Fields created successfully", HttpStatus.OK.value(), result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create fields", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @PostMapping(value = "field/update",headers = "Authorization")
    public ResponseEntity<ApiResponse<?>> updateField(@Valid @RequestBody FieldDTO dto) {
        try {
            Object result = formMasterService.updateField(dto);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success("Field updated successfully", HttpStatus.OK.value(), result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update field", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @DeleteMapping(value = "delete/{fieldId}/field",headers = "Authorization")
    public ResponseEntity<ApiResponse<?>> deleteField(@PathVariable Long fieldId) {
        try {
            formMasterService.deleteField(fieldId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success("Field deleted successfully", HttpStatus.OK.value(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete field", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @GetMapping(value = "form/{formId}/fields")
    public ResponseEntity<ApiResponse<?>> getStructuredForm(@PathVariable String formId, @RequestParam(name = "lang", defaultValue = "en") String lang,@RequestHeader(value = "JwtToken") String token) {
        try {
            Object result = formMasterService.getStructuredFormByFormId(formId,lang,token);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success("Form structure fetched successfully", HttpStatus.OK.value(), result));
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch form structure:"+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e));
        }
    }


}
