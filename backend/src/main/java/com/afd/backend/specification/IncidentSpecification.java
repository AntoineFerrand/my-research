package com.afd.backend.specification;

import com.afd.backend.entity.Incident;
import com.afd.backend.entity.Person;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class IncidentSpecification {

    public static Specification<Incident> withFilters(String title, String description, String severity, String owner) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by title
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + title.toLowerCase() + "%"
                ));
            }

            // Filter by description
            if (description != null && !description.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + description.toLowerCase() + "%"
                ));
            }

            // Filter by severity
            if (severity != null && !severity.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("severity")),
                    "%" + severity.toLowerCase() + "%"
                ));
            }

            // Filter by owner (last_name OR first_name OR email)
            if (owner != null && !owner.trim().isEmpty()) {
                Join<Incident, Person> personJoin = root.join("owner");
                String ownerPattern = "%" + owner.toLowerCase() + "%";
                
                Predicate lastNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(personJoin.get("lastName")),
                    ownerPattern
                );
                Predicate firstNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(personJoin.get("firstName")),
                    ownerPattern
                );
                Predicate emailPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(personJoin.get("email")),
                    ownerPattern
                );
                
                predicates.add(criteriaBuilder.or(lastNamePredicate, firstNamePredicate, emailPredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
