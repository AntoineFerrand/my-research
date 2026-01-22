package com.afd.backend.controller;

import com.afd.backend.dto.IncidentDTO;
import com.afd.backend.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/incidents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    public ResponseEntity<List<IncidentDTO>> searchIncidents(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String owner
    ) {
        List<IncidentDTO> incidents = incidentService.searchIncidents(title, description, severity, owner);
        return ResponseEntity.ok(incidents);
    }
}
