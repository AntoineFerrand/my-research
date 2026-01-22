package com.afd.backend.service;

import com.afd.backend.dto.IncidentDTO;
import com.afd.backend.entity.Incident;
import com.afd.backend.repository.IncidentRepository;
import com.afd.backend.specification.IncidentSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;

    @Transactional(readOnly = true)
    public List<IncidentDTO> searchIncidents(String title, String description, String severity, String owner) {
        Specification<Incident> spec = IncidentSpecification.withFilters(title, description, severity, owner);
        List<Incident> incidents = incidentRepository.findAll(spec);
        return incidents.stream()
                .map(IncidentDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
