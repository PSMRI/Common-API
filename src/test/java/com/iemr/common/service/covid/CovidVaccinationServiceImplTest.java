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
package com.iemr.common.service.covid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.iemr.common.data.covid.CovidDoseType;
import com.iemr.common.data.covid.CovidVaccinationStatus;
import com.iemr.common.data.covid.CovidVaccineType;
import com.iemr.common.repository.covid.CovidDoseTypeRepo;
import com.iemr.common.repository.covid.CovidVaccinationRepo;
import com.iemr.common.repository.covid.CovidVaccineTypeRepo;
import com.iemr.common.utils.exception.IEMRException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigInteger;

class CovidVaccinationServiceImplTest {

    @Mock
    private CovidDoseTypeRepo covidDoseTypeRepo;
    @Mock
    private CovidVaccineTypeRepo covidVaccineTypeRepo;
    @Mock
    private CovidVaccinationRepo covidVaccinationRepo;

    @InjectMocks
    private CovidVaccinationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getVaccinationTypeAndDoseTaken_returnsJson() {
        ArrayList<CovidVaccineType> vaccineTypes = new ArrayList<>();
        ArrayList<CovidDoseType> doseTypes = new ArrayList<>();
        vaccineTypes.add(new CovidVaccineType());
        doseTypes.add(new CovidDoseType());

        when(covidVaccineTypeRepo.findAll()).thenReturn(vaccineTypes);
        when(covidDoseTypeRepo.findAll()).thenReturn(doseTypes);

        String result = service.getVaccinationTypeAndDoseTaken();
        assertNotNull(result);
        assertTrue(result.contains("vaccineType"));
        assertTrue(result.contains("doseType"));
    }

    @Test
    void getCovidVaccinationDetails_returnsJson() throws IEMRException {

        CovidVaccinationStatus status = new CovidVaccinationStatus();
        status.setCovidVSID(BigInteger.valueOf(1));
        when(covidVaccinationRepo.findByBeneficiaryRegID(123L)).thenReturn(status);

        String result = service.getCovidVaccinationDetails(123L);
        assertNotNull(result);
        assertTrue(result.contains("covidVSID"));
    }

    @Test
    void getCovidVaccinationDetails_noDataFound() throws IEMRException {
        when(covidVaccinationRepo.findByBeneficiaryRegID(123L)).thenReturn(null);
        String result = service.getCovidVaccinationDetails(123L);
        assertEquals("No data found", result);

        CovidVaccinationStatus status = new CovidVaccinationStatus();
        status.setCovidVSID(null);
        when(covidVaccinationRepo.findByBeneficiaryRegID(456L)).thenReturn(status);
        result = service.getCovidVaccinationDetails(456L);
        assertEquals("No data found", result);
    }

    @Test
    void getCovidVaccinationDetails_exceptionThrown() {
        when(covidVaccinationRepo.findByBeneficiaryRegID(anyLong())).thenThrow(new RuntimeException("fail"));
        IEMRException ex = assertThrows(IEMRException.class, () -> service.getCovidVaccinationDetails(1L));
        assertTrue(ex.getMessage().contains("fail"));
    }

    @Test
    void saveBenCovidVaccinationDetails_successUpdate() throws Exception {
        CovidVaccinationStatus status = new CovidVaccinationStatus();
        status.setCovidVSID(BigInteger.valueOf(1));
        status.setProcessed(null);

        // Simulate save
        when(covidVaccinationRepo.save(any())).thenReturn(status);

        String request = new Gson().toJson(status);
        String result = service.saveBenCovidVaccinationDetails(request);
        assertNotNull(result);
        assertTrue(result.contains("covidVSID"));
    }

    @Test
    void saveBenCovidVaccinationDetails_saveFails() {
        CovidVaccinationStatus status = new CovidVaccinationStatus();
        status.setCovidVSID(BigInteger.valueOf(1));

        // Simulate save returns null
        when(covidVaccinationRepo.save(any())).thenReturn(null);

        String request = new Gson().toJson(status);
        IEMRException ex = assertThrows(IEMRException.class, () -> service.saveBenCovidVaccinationDetails(request));
        assertTrue(ex.getMessage().contains("Failed to save"));
    }

    @Test
    void saveBenCovidVaccinationDetails_exceptionThrown() {
        String badJson = "not a json";
        IEMRException ex = assertThrows(IEMRException.class, () -> service.saveBenCovidVaccinationDetails(badJson));
        assertNotNull(ex.getMessage());
    }
}
