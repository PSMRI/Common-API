package com.iemr.common.repository.dynamic_form;
import com.iemr.common.data.dynamic_from.FormFieldOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormFieldOptionRepository
    extends JpaRepository<FormFieldOption, Integer> {

    List<FormFieldOption> findByOptionKeyOrderBySortOrderAsc(String optionKey);

}