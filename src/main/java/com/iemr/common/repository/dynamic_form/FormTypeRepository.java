package com.iemr.common.repository.dynamic_form;

import com.iemr.common.data.dynamic_from.FormEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormTypeRepository extends CrudRepository<FormEntity, Long> {}
