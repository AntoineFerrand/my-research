package com.afd.backend.controller;

import com.afd.backend.dto.IncidentDTO;
import com.afd.backend.dto.PageResponseDTO;
import com.afd.backend.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Incidents", description = "API de gestion des incidents")
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    @Operation(summary = "Rechercher des incidents", description = "Recherche paginée des incidents avec filtres optionnels")
    public ResponseEntity<@NonNull PageResponseDTO<IncidentDTO>> searchIncidents(
            @Parameter(description = "Filtrer par titre") @RequestParam(required = false) String title,
            @Parameter(description = "Filtrer par description") @RequestParam(required = false) String description,
            @Parameter(description = "Filtrer par sévérité") @RequestParam(required = false) String severity,
            @Parameter(description = "Filtrer par propriétaire") @RequestParam(required = false) String owner,
            @Parameter(description = "Numéro de page (commence à 0)") @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Taille de la page") @RequestParam(required = false, defaultValue = "10") int size,
            @Parameter(description = "Champ de tri") @RequestParam(required = false, defaultValue = "createdAt") String sort,
            @Parameter(description = "Direction du tri (asc ou desc)") @RequestParam(required = false, defaultValue = "desc") String direction
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
