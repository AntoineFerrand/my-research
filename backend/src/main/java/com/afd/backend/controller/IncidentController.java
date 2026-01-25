package com.afd.backend.controller;

import com.afd.backend.dto.IncidentDTO;
import com.afd.backend.dto.PageResponseDTO;
import com.afd.backend.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/incidents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<IncidentDTO>> searchIncidents(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        PageResponseDTO<IncidentDTO> incidents = incidentService.searchIncidents(title, description, severity, owner, page, size);
        return ResponseEntity.ok(incidents);
    }
}
