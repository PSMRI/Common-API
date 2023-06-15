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
package com.iemr.common.mapper;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.iemr.common.data.beneficiary.BenRelationshipType;
import com.iemr.common.model.beneficiary.BenRelationshipTypeModel;

@Mapper(componentModel = "spring")
@DecoratedWith(RelationshipMapperDecorator.class)
public interface RelationshipMapper
{
	RelationshipMapper INSTANCE = Mappers.getMapper(RelationshipMapper.class);

	BenRelationshipTypeModel createRelationshipTypes(BenRelationshipType relationship);

	List<BenRelationshipTypeModel> createRelationshipTypes(List<BenRelationshipType> relationship);

	BenRelationshipTypeModel createRelationshipTypesByID(Integer relationship);

	List<BenRelationshipTypeModel> createRelationshipTypesByID(List<Integer> relationship);
}
