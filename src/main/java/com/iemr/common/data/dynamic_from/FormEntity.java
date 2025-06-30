package com.iemr.common.data.dynamic_from;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "form_types",schema = "db_imer")
public class FormEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "form_name",columnDefinition = "TEXT")
    private String formName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private ModuleEntity module;

    @Column(name = "fields",columnDefinition = "TEXT")
    private String  fields ;
}
