package com.afd.backend.controller;

import com.afd.backend.dto.IncidentDTO;
import com.afd.backend.dto.PageResponseDTO;
import com.afd.backend.service.IncidentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentController.class)
@ContextConfiguration(classes = {IncidentController.class, IncidentControllerTest.TestCacheConfig.class})
class IncidentControllerTest {

    // if we don't configure this, The Application can't start when running the test
    // because it don't find the bean CacheManager.
    @Configuration
    static class TestCacheConfig {
        @Bean
        public CacheManager cacheManager() {
            return new NoOpCacheManager();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidentService incidentService;

    @Test
    void searchIncidents_withFilters_shouldReturnIncidents() throws Exception {
        // Given
        IncidentDTO incident = new IncidentDTO(1, "Bug", "Description", "HIGH", 
                LocalDateTime.now(), 1, "Doe", "John", "john@example.com");
        PageResponseDTO<IncidentDTO> response = new PageResponseDTO<>(List.of(incident), 1, 1, 0, 10, false, false);
        
        when(incidentService.searchIncidents(eq("Bug"), isNull(), eq("HIGH"), isNull(), 
                eq(0), eq(10), eq("createdAt"), eq("desc")))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/incidents")
                        .param("title", "Bug")
                        .param("severity", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].title").value("Bug"))
                .andExpect(jsonPath("$.items[0].severity").value("HIGH"))
                .andExpect(jsonPath("$.totalElements").value(1));
        
        verify(incidentService).searchIncidents("Bug", null, "HIGH", null, 0, 10, "createdAt", "desc");
    }

    @Test
    void searchIncidents_shouldReturnCacheHeaders() throws Exception {
        // Given
        PageResponseDTO<IncidentDTO> emptyResponse = new PageResponseDTO<>(List.of(), 0, 0, 0, 10, false, false);
        when(incidentService.searchIncidents(any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(emptyResponse);

        // When & Then
        mockMvc.perform(get("/incidents"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=300, must-revalidate, private"));
    }

    @Test
    void searchIncidents_withDefaultParams_shouldUsePagination() throws Exception {
        // Given
        PageResponseDTO<IncidentDTO> response = new PageResponseDTO<>(List.of(), 0, 0, 0, 10, false, false);
        when(incidentService.searchIncidents(isNull(), isNull(), isNull(), isNull(), 
                eq(0), eq(10), eq("createdAt"), eq("desc")))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
        
        verify(incidentService).searchIncidents(null, null, null, null, 0, 10, "createdAt", "desc");
    }

    @Test
    void searchIncidents_withCustomPagination_shouldApplyParams() throws Exception {
        // Given
        PageResponseDTO<IncidentDTO> response = new PageResponseDTO<>(List.of(), 0, 3, 2, 20, true, true);
        when(incidentService.searchIncidents(any(), any(), any(), any(), 
                eq(2), eq(20), eq("title"), eq("asc")))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/incidents")
                        .param("page", "2")
                        .param("size", "20")
                        .param("sort", "title")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.pageSize").value(20));
        
        verify(incidentService).searchIncidents(null, null, null, null, 2, 20, "title", "asc");
    }
}
