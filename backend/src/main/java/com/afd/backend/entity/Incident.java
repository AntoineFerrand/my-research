package com.afd.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "incident")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "severity", nullable = false)
    private String severity;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Person owner;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
