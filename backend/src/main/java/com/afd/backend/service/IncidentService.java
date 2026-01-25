package com.afd.backend.service;

import com.afd.backend.dto.IncidentDTO;
import com.afd.backend.dto.PageResponseDTO;
import com.afd.backend.entity.Incident;
import com.afd.backend.repository.IncidentRepository;
import com.afd.backend.specification.IncidentSpecification;
import lombok.RequiredArgsConstructor;
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

    @Transactional(readOnly = true)
    public PageResponseDTO<IncidentDTO> searchIncidents(String title, String description, String severity, String owner, int page, int size) {
        Specification<Incident> spec = IncidentSpecification.withFilters(title, description, severity, owner);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Incident> incidentsPage = incidentRepository.findAll(spec, pageable);

        Page<IncidentDTO> incidentDTOPage = incidentsPage.map(IncidentDTO::fromEntity);
        return PageResponseDTO.fromPage(incidentDTOPage);
    }
}
