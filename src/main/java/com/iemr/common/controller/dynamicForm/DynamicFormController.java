package com.iemr.common.controller.dynamicForm;

import com.iemr.common.data.dynamic_from.FormEntity;
import com.iemr.common.data.dynamic_from.ModuleEntity;
import com.iemr.common.dto.dynamicForm.FormTypeEntityDTO;
import com.iemr.common.dto.dynamicForm.ModuleEntityDTO;
import com.iemr.common.service.dynamicForm.FormTypeService;
import com.iemr.common.service.dynamicForm.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(value = "masterFrom")
@RestController
public class DynamicFormController {
    @Autowired  ModuleService moduleService;
    @Autowired
    private FormTypeService formTypeService;

    @PostMapping("/modules")
    public ResponseEntity<ModuleEntity> createModule(@RequestBody ModuleEntityDTO module) {
        return ResponseEntity.ok(moduleService.save(module));
    }

    @PostMapping("/modules/{moduleId}/form-types")
    public ResponseEntity<FormEntity> createFormType(@PathVariable Long moduleId, @RequestBody FormTypeEntityDTO formType) {
        return ResponseEntity.ok(formTypeService.createFormType(moduleId, formType));
    }


}
