package com.afd.backend.controller;

import com.afd.backend.dto.IncidentDTO;
import com.afd.backend.dto.PageResponseDTO;
import com.afd.backend.service.IncidentService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/incidents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    public ResponseEntity<@NonNull PageResponseDTO<IncidentDTO>> searchIncidents(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String owner,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sort,
            @RequestParam(required = false, defaultValue = "desc") String direction
    ) {
        PageResponseDTO<IncidentDTO> incidents = incidentService.searchIncidents(title, description, severity, owner, page, size, sort, direction);
        
        // HTTP Cache browser side : 5 minutes, private (not shared between users)
        CacheControl cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES)
                .cachePrivate()
                .mustRevalidate();
        
        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .body(incidents);
    }
}
