package com.example.exe2update.dto.Response;

import org.springframework.data.domain.Page;

import java.util.List;

public class PageResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    public PageResponse() {}

    public PageResponse(List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public PageResponse(Page<T> result) {
        this.content = result.getContent();
        this.pageNumber = result.getNumber();
        this.pageSize = result.getSize();
        this.totalElements = result.getTotalElements();
        this.totalPages = result.getTotalPages();
    }

    @Override
    public String toString() {
        // Đảm bảo đủ 5 tham số cho 5 placeholder %d
        return String.format(
                "PageResponse{page=%d/%d, size=%d, totalElements=%d, contentSize=%d}",
                pageNumber,
                totalPages,
                pageSize,
                totalElements,
                content != null ? content.size() : 0
        );
    }

    // Getters và setters
    public List<T> getContent() {
        return content;
    }
    public void setContent(List<T> content) {
        this.content = content;
    }
    public int getPageNumber() {
        return pageNumber;
    }
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
    public int getPageSize() {
        return pageSize;
    }
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    public long getTotalElements() {
        return totalElements;
    }
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    public int getTotalPages() {
        return totalPages;
    }
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
