/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.common.repository.nhm_dashboard;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.common.data.nhm_dashboard.DetailedCallReport;

@Repository
public interface DetailedCallReportRepo extends CrudRepository<DetailedCallReport, Long> {
	List<DetailedCallReport> findByCallStartTimeBetween(Timestamp startDate, Timestamp endDate);

	/**
	 * Call dates for which data has already been pulled from CTI. Used to detect
	 * the days that were missed by earlier scheduler runs, so that they can be
	 * pulled again instead of staying permanently empty.
	 */
	@Query(value = "select distinct date(Call_Start_Time) from t_DetailedCallReport "
			+ "where Call_Start_Time between :startDate and :endDate", nativeQuery = true)
	List<Date> findExistingCallDates(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);
}
