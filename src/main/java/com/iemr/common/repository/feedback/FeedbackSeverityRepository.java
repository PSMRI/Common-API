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
package com.iemr.common.repository.feedback;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.common.data.feedback.FeedbackSeverity;

@Repository
public interface FeedbackSeverityRepository extends CrudRepository<FeedbackSeverity, Long>
{
	@Query("select severityID, severityTypeName from FeedbackSeverity where deleted = false "
			+ "order by severityTypeName asc")
	Set<Object[]> getActiveFeedbackSeverity();

	@Query("select new FeedbackSeverity(severityID, severityTypeName) from FeedbackSeverity where "
			+ "providerServiceMapID = :providerServiceMapID and deleted = false order by severityTypeName asc")
	List<FeedbackSeverity> getActiveFeedbackSeverity(@Param("providerServiceMapID") Integer providerServiceMapID);
}
