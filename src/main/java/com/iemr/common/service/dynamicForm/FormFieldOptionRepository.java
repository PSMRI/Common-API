package com.iemr.common.service.dynamicForm;

import com.iemr.common.data.dynamic_from.FormFieldOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormFieldOptionRepository
    extends JpaRepository<FormFieldOption, Integer> {

    List<FormFieldOption> findByOptionKeyOrderBySortOrderAsc(String optionKey);

}