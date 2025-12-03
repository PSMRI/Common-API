package com.iemr.common.data.dynamic_from;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "form_module")
public class FormModule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "module_name")
    private String moduleName;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
