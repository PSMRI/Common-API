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
package com.iemr.common.data.email;



import com.google.gson.annotations.Expose;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="v_emailstockalert")
public class StockAlertData {
	

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "uuid")
	private String uuid;	

	@Column(name = "FacilityId")
	private Integer facilityId;
	
	@Column(name = "ItemID")
	private Integer itemID;
	
	@Column(name = "FacilityName")
	@Expose
	private String facilityName;
	
	@Column(name = "ItemName")
	@Expose
	private String itemName;
	
	@Column(name = "Totalquantity")
	@Expose
	private Integer Totalquantity;
	
	@Column(name = "Quantityinhand")
	private Integer quantityInHand;
	
	@Column(name = "CreatedBy")
	@Expose
	private String createdBy;
	
	@Column(name = "Emailid")
	@Expose
	private String emailid;
	
	@Column(name = "DistrictName")
	@Expose
	private String districtName;
	
	@Column(name = "QuantityConsumedPercent")
	@Expose
	private Double quantityinhandPercent;
}
