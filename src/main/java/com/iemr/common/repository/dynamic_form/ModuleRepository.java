package com.iemr.common.repository.dynamic_form;

import com.iemr.common.data.dynamic_from.FormModule;
import com.iemr.common.data.dynamic_from.ModuleEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepository extends CrudRepository<FormModule, Long> {}
