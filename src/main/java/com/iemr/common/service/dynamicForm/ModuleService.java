package com.iemr.common.service.dynamicForm;

import com.iemr.common.data.dynamic_from.ModuleEntity;
import com.iemr.common.dto.dynamicForm.ModuleEntityDTO;
import com.iemr.common.repository.dynamic_form.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ModuleService {
    @Autowired
    ModuleRepository repo;

    public ModuleEntity save(ModuleEntityDTO module) {
        ModuleEntity moduleEntity = new ModuleEntity();
        moduleEntity.setModuleName(module.getModuleName());
        return repo.save(moduleEntity);
    }

    public Optional<ModuleEntity> findById(Long id) {
        return repo.findById(id);
    }
}

