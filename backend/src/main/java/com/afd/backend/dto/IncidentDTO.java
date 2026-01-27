package com.afd.backend.dto;

import com.afd.backend.entity.Incident;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDTO {

    private Integer id;
    private String title;
    private String description;
    private String severity;
    private LocalDateTime createdAt;
    
    // Owner attributes
    private Integer ownerId;
    private String ownerLastName;
    private String ownerFirstName;
    private String ownerEmail;

    public static IncidentDTO fromEntity(Incident incident) {
        IncidentDTO dto = new IncidentDTO();
        dto.setId(incident.getId());
        dto.setTitle(incident.getTitle());
        dto.setDescription(incident.getDescription());
        dto.setSeverity(incident.getSeverity());
        dto.setCreatedAt(incident.getCreatedAt());

        dto.setOwnerId(incident.getOwner().getId());
        dto.setOwnerLastName(incident.getOwner().getLastName());
        dto.setOwnerFirstName(incident.getOwner().getFirstName());
        dto.setOwnerEmail(incident.getOwner().getEmail());

        return dto;
    }
}
