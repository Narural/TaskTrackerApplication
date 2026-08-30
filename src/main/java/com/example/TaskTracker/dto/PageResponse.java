package com.example.TaskTracker.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Стабильная форма страничного ответа.
 * Нужна, чтобы наружу не уезжала внутренняя структура Spring Data:
 * её поля определяет версия библиотеки, а не наш контракт.
 */
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }

    public List<T> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
