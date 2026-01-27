package com.afd.backend.service;

import com.afd.backend.dto.IncidentDTO;
import com.afd.backend.dto.PageResponseDTO;
import com.afd.backend.entity.Incident;
import com.afd.backend.repository.IncidentRepository;
import com.afd.backend.specification.IncidentSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;

    @Cacheable(value = "incidents", key = "#title + '_' + #description + '_' + #severity + '_' + #owner + '_' + #page + '_' + #size + '_' + #sort + '_' + #direction")
    @Transactional(readOnly = true)
    public PageResponseDTO<IncidentDTO> searchIncidents(String title, String description, String severity, String owner, int page, int size, String sort, String direction) {
        Specification<Incident> spec = IncidentSpecification.withFilters(title, description, severity, owner);

        String sortField = mapSortField(sort);
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<Incident> incidentsPage = incidentRepository.findAll(spec, pageable);

        Page<IncidentDTO> incidentDTOPage = incidentsPage.map(IncidentDTO::fromEntity);
        return PageResponseDTO.fromPage(incidentDTOPage);
    }
    
    /**
     * Maps DTO field names to Entity field names for sorting.
     * Supports owner fields using nested property paths.
     */
    private String mapSortField(String sortField) {
        return switch (sortField) {
            case "ownerLastName" -> "owner.lastName";
            case "ownerFirstName" -> "owner.firstName";
            case "ownerEmail" -> "owner.email";
            default -> sortField; // id, title, description, severity, createdAt
        };
    }
}
