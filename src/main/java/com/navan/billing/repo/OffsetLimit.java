package com.navan.billing.repo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record OffsetLimit(int offset, int pageSize) implements Pageable {
    @Override
    public int getPageNumber() {
        return offset / pageSize;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return Sort.unsorted();
    }

    @Override
    public Pageable next() {
        return new OffsetLimit(offset + pageSize, pageSize);
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? new OffsetLimit(Math.max(0, offset - pageSize), pageSize) : first();
    }

    @Override
    public Pageable first() {
        return new OffsetLimit(0, pageSize);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero");
        }
        return new OffsetLimit(pageNumber * pageSize, pageSize);
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }
}
