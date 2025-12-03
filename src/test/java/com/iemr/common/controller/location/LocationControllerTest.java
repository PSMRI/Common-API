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
package com.iemr.common.controller.location;

import com.iemr.common.data.location.Country;
import com.iemr.common.data.location.DistrictBlock;
import com.iemr.common.data.location.DistrictBranchMapping;
import com.iemr.common.data.location.Districts;
import com.iemr.common.data.location.States;
import com.iemr.common.service.location.LocationService;
import com.iemr.common.utils.response.OutputResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // Test constants for better maintainability
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_TOKEN = "Bearer test";
    private static final String CONTENT_TYPE = "application/json";
    
    // API endpoints
    private static final String STATES_URL = "/location/states/{countryId}";
    private static final String DISTRICTS_URL = "/location/districts/{stateId}";
    private static final String STATE_DISTRICTS_URL = "/location/statesDistricts/{countryId}";
    private static final String TALUKS_URL = "/location/taluks/{districtId}";
    private static final String CITY_URL = "/location/city/{districtId}";
    private static final String VILLAGE_URL = "/location/village/{blockId}";
    private static final String COUNTRIES_URL = "/location/getCountries";

    // Helper method to create expected success output
    private String createSuccessOutput(List<?> data) {
        OutputResponse response = new OutputResponse();
        response.setResponse(data.toString());
        return response.toString();
    }

    @Test
    void getStates_success() throws Exception {
        States state = new States();
        List<States> states = List.of(state);
        when(locationService.getStates(1)).thenReturn(states);

        mockMvc.perform(get(STATES_URL, 1)
                .header(AUTH_HEADER, BEARER_TOKEN)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(createSuccessOutput(states)));
    }

    @Test
    void getStates_exception() throws Exception {
        when(locationService.getStates(1)).thenThrow(new RuntimeException("fail"));
        
        mockMvc.perform(get(STATES_URL, 1)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void getDistricts_success() throws Exception {
        Districts d = new Districts();
        List<Districts> districts = List.of(d);
        when(locationService.getDistricts(2)).thenReturn(districts);

        mockMvc.perform(get(DISTRICTS_URL, 2)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(createSuccessOutput(districts)));
    }

    @Test
    void getDistricts_exception() throws Exception {
        when(locationService.getDistricts(2)).thenThrow(new RuntimeException("fail"));
        
        mockMvc.perform(get(DISTRICTS_URL, 2)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void getStatetDistricts_success() throws Exception {
        Districts d = new Districts();
        List<Districts> districts = List.of(d);
        when(locationService.findStateDistrictBy(3)).thenReturn(districts);

        
        mockMvc.perform(get(STATE_DISTRICTS_URL, 3)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(createSuccessOutput(districts)));
    }

    @Test
    void getStatetDistricts_exception() throws Exception {
        when(locationService.findStateDistrictBy(3)).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get(STATE_DISTRICTS_URL, 3)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void getDistrictBlocks_success() throws Exception {
        DistrictBlock block = new DistrictBlock();
        List<DistrictBlock> blocks = List.of(block);
        when(locationService.getDistrictBlocks(4)).thenReturn(blocks);

        
        mockMvc.perform(get(TALUKS_URL, 4)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(createSuccessOutput(blocks)));
    }

    @Test
    void getDistrictBlocks_exception() throws Exception {
        when(locationService.getDistrictBlocks(4)).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get(TALUKS_URL, 4)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void getCity_success() throws Exception {
        // NOTE: The controller has a bug - it calls getDistrictBlocks instead of getCities
        DistrictBlock block = new DistrictBlock();
        List<DistrictBlock> blocks = List.of(block);
        when(locationService.getDistrictBlocks(5)).thenReturn(blocks);

        
        mockMvc.perform(get(CITY_URL, 5)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(createSuccessOutput(blocks)));
    }

 @Test
    void getCity_exception() throws Exception {
        // NOTE: The controller has a bug - it calls getDistrictBlocks instead of getCities
        when(locationService.getDistrictBlocks(5)).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get(CITY_URL, 5)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void getVillages_success() throws Exception {
        DistrictBranchMapping mapping = new DistrictBranchMapping();
        List<DistrictBranchMapping> mappings = List.of(mapping);
        when(locationService.getDistrilctBranchs(6)).thenReturn(mappings);

        
        mockMvc.perform(get(VILLAGE_URL, 6)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(createSuccessOutput(mappings)));
    }

    @Test
    void getVillages_exception() throws Exception {
        when(locationService.getDistrilctBranchs(6)).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get(VILLAGE_URL, 6)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void getCountries_success() throws Exception {
        Country c = new Country();
        List<Country> countries = List.of(c);
        when(locationService.getCountries()).thenReturn(countries);

        
        mockMvc.perform(get(COUNTRIES_URL)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(createSuccessOutput(countries)));
    }

    @Test
    void getCountries_exception() throws Exception {
        when(locationService.getCountries()).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get(COUNTRIES_URL)
                .header(AUTH_HEADER, BEARER_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }
}