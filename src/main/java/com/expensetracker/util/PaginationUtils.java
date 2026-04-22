package com.expensetracker.util;

import com.expensetracker.dto.response.PaginatedResponse;
import com.expensetracker.dto.response.PaginationLinks;
import com.expensetracker.dto.response.PaginationMetadata;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * PaginationUtils — utility for converting Spring Data Page to PaginatedResponse.
 *
 * Converts 0-indexed Spring Page to 1-indexed user-friendly pagination response.
 * Generates next/prev links automatically based on current request URL.
 */
@Component
public class PaginationUtils {

    /**
     * Converts Spring Data Page to PaginatedResponse with metadata and links.
     *
     * @param page the Spring Data Page object
     * @param <T> the type of data items
     * @return PaginatedResponse with data and pagination metadata
     */
    public <T> PaginatedResponse<T> toPaginatedResponse(Page<T> page) {
        List<T> data = page.getContent();
        
        // Convert 0-indexed page number to 1-indexed for user-friendly response
        int currentPage = page.getNumber() + 1;
        int pageSize = page.getSize();
        long totalItems = page.getTotalElements();
        int totalPages = page.getTotalPages();
        boolean hasNext = page.hasNext();

        // Build navigation links
        PaginationLinks links = buildLinks(page);

        // Build pagination metadata
        PaginationMetadata metadata = PaginationMetadata.builder()
                .currentPage(currentPage)
                .pageSize(pageSize)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .links(links)
                .build();

        return PaginatedResponse.<T>builder()
                .data(data)
                .pagination(metadata)
                .build();
    }

    /**
     * Builds next and prev links for pagination based on current request URL.
     *
     * @param page the Spring Data Page object
     * @return PaginationLinks with next and prev URLs
     */
    private PaginationLinks buildLinks(Page<?> page) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("page")
                .replaceQueryParam("size")
                .toUriString();

        String nextLink = null;
        String prevLink = null;

        int currentPageZeroIndexed = page.getNumber();
        int pageSize = page.getSize();
        int totalPages = page.getTotalPages();

        // next: only if not on last page
        if (page.hasNext() && (currentPageZeroIndexed + 1) < totalPages) {
            // next page for user is currentPage+2 (since user-facing is 1-indexed)
            nextLink = baseUrl + "?page=" + (currentPageZeroIndexed + 1) + "&size=" + pageSize;
        }

        // prev: only if not on first page
        if (page.hasPrevious() && currentPageZeroIndexed > 0) {
            prevLink = baseUrl + "?page=" + (currentPageZeroIndexed - 1) + "&size=" + pageSize;
        }

        return PaginationLinks.builder()
                .next(nextLink)
                .prev(prevLink)
                .build();
    }
}
