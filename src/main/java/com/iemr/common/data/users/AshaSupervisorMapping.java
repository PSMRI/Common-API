package com.iemr.common.data.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "asha_supervisor_mapping")
@Data
@NoArgsConstructor
public class AshaSupervisorMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "supervisorUserID")
	private Integer supervisorUserID;

	@Column(name = "ashaUserID")
	private Integer ashaUserID;

	@Column(name = "facilityID")
	private Integer facilityID;

	@Column(name = "deleted", insertable = false, updatable = true)
	private Boolean deleted;
}
