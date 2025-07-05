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
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = LocationController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = {LocationController.class})
@AutoConfigureMockMvc(addFilters = false)
class LocationControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    @MockBean
    private Logger logger; // If you want to verify logging, otherwise remove

    @BeforeEach
    void setup() {
        // No-op, but can be used for setup if needed
    }

    @Test
    void getStates_success() throws Exception {
        States state = new States();
        List<States> states = List.of(state);
        when(locationService.getStates(1)).thenReturn(states);

        String expectedJson = "{\"data\":[{}],\"statusCode\":200,\"errorMessage\":\"Success\",\"status\":\"Success\"}";
        mockMvc.perform(get("/location/states/1")
                .header("Authorization", "Bearer test")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void getStates_exception() throws Exception {
        when(locationService.getStates(1)).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get("/location/states/1")
                .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void getDistricts_success() throws Exception {
        Districts d = new Districts();
        List<Districts> districts = List.of(d);
        when(locationService.getDistricts(2)).thenReturn(districts);

        String expectedJson = "{\"data\":[{}],\"statusCode\":200,\"errorMessage\":\"Success\",\"status\":\"Success\"}";
        mockMvc.perform(get("/location/districts/2")
                .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void getDistricts_exception() throws Exception {
        when(locationService.getDistricts(2)).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get("/location/districts/2")
                .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void getStatetDistricts_success() throws Exception {
        Districts d = new Districts();
        List<Districts> districts = List.of(d);
        when(locationService.findStateDistrictBy(3)).thenReturn(districts);

        String expectedJson = "{\"data\":[{}],\"statusCode\":200,\"errorMessage\":\"Success\",\"status\":\"Success\"}";
        mockMvc.perform(get("/location/statesDistricts/3")
                .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void getStatetDistricts_exception() throws Exception {
        when(locationService.findStateDistrictBy(3)).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get("/location/statesDistricts/3")
                .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void getDistrictBlocks_success() throws Exception {
        DistrictBlock block = new DistrictBlock();
        List<DistrictBlock> blocks = List.of(block);
        when(locationService.getDistrictBlocks(4)).thenReturn(blocks);

        String expectedJson = "{\"data\":[{}],\"statusCode\":200,\"errorMessage\":\"Success\",\"status\":\"Success\"}";
        mockMvc.perform(get("/location/taluks/4")
                .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void getDistrictBlocks_exception() throws Exception {
        when(locationService.getDistrictBlocks(4)).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get("/location/taluks/4")
                .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void getCity_success() throws Exception {
        DistrictBlock block = new DistrictBlock();
        List<DistrictBlock> blocks = List.of(block);
        when(locationService.getDistrictBlocks(5)).thenReturn(blocks);

        String expectedJson = "{\"data\":[{}],\"statusCode\":200,\"errorMessage\":\"Success\",\"status\":\"Success\"}";
        mockMvc.perform(get("/location/city/5")
                .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void getCity_exception() throws Exception {
        when(locationService.getDistrictBlocks(5)).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get("/location/city/5")
                .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void getVillages_success() throws Exception {
        DistrictBranchMapping mapping = new DistrictBranchMapping();
        List<DistrictBranchMapping> mappings = List.of(mapping);
        when(locationService.getDistrilctBranchs(6)).thenReturn(mappings);

        String expectedJson = "{\"data\":[{}],\"statusCode\":200,\"errorMessage\":\"Success\",\"status\":\"Success\"}";
        mockMvc.perform(get("/location/village/6")
                .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void getVillages_exception() throws Exception {
        when(locationService.getDistrilctBranchs(6)).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get("/location/village/6")
                .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }

    @Test
    void getCountries_success() throws Exception {
        Country c = new Country();
        List<Country> countries = List.of(c);
        when(locationService.getCountries()).thenReturn(countries);

        String expectedJson = "{\"data\":[{}],\"statusCode\":200,\"errorMessage\":\"Success\",\"status\":\"Success\"}";
        mockMvc.perform(get("/location/getCountries")
                .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void getCountries_exception() throws Exception {
        when(locationService.getCountries()).thenThrow(new RuntimeException("fail"));
        mockMvc.perform(get("/location/getCountries")
                .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fail")));
    }
}