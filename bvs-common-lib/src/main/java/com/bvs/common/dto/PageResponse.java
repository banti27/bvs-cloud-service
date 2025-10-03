package com.bvs.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Pagination response wrapper
 * 
 * @param <T> Type of data in the page
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    
    /**
     * List of items in current page
     */
    private List<T> content;
    
    /**
     * Current page number (0-indexed)
     */
    private int pageNumber;
    
    /**
     * Number of items per page
     */
    private int pageSize;
    
    /**
     * Total number of elements across all pages
     */
    private long totalElements;
    
    /**
     * Total number of pages
     */
    private int totalPages;
    
    /**
     * Whether this is the first page
     */
    private boolean first;
    
    /**
     * Whether this is the last page
     */
    private boolean last;
    
    /**
     * Whether there are more pages
     */
    private boolean hasNext;
    
    /**
     * Whether there are previous pages
     */
    private boolean hasPrevious;
}
