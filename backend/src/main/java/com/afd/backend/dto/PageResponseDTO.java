package com.afd.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic DTO for encapsulating paginated responses.
 * Contains pagination data and metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO<T> {

    private List<T> items;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;

    /**
     * Map Page to PageResponseDTO
     */
    public static <T> PageResponseDTO<T> fromPage(Page<T> page) {
        PageResponseDTO<T> response = new PageResponseDTO<>();
        response.setItems(page.getContent());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setCurrentPage(page.getNumber());
        response.setPageSize(page.getSize());
        response.setHasNext(page.hasNext());
        response.setHasPrevious(page.hasPrevious());
        return response;
    }
}
