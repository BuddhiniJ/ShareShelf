package com.shareshelf.backend.repository;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PagedResponse<T> {

    private List<T> content;
    private int page;           // current page number (0-based)
    private int size;           // page size
    private long totalElements; // total records in DB
    private int totalPages;
    private boolean last;       // is this the last page?
}