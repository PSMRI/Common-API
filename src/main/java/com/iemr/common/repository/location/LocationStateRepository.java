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
package com.iemr.common.repository.location;

import java.util.ArrayList;
import java.util.Objects;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iemr.common.data.location.States;

@Repository
public interface LocationStateRepository extends CrudRepository<States, Integer>
{

	@Query("select stateID, stateName from States where countryID = :id order by stateName asc")
	public ArrayList<Object[]> findBy(@Param("id") int id);
	
	@Query("select state from States state where state.stateID = :id")
	public States findByStateID(@Param("id") int id);
	
	//For everwell stateID
	@Query("select stateID from States t where t.stateName = :stateName ")
	public Integer getStateID(@Param("stateName") String stateName);
	
	// for everwell - fetching the langugae according to state.
	@Query("select t.language from States t where t.stateName = :stateName ")
	public String getStateLanguage(@Param("stateName") String stateName);
	
	States findByStateID(Integer stateID);
	
	@Query("select state.state from States state where state.stateID = :id")
	public String findByStateIDForGrievance(@Param("id") int id);
	
	
}
