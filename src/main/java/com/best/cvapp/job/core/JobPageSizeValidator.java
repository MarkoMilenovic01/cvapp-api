package com.best.cvapp.job.core;

import com.best.cvapp.job.core.exception.JobPageSizeExceededException;
import org.springframework.data.domain.Pageable;

public final class JobPageSizeValidator {

    public static final int MAX_PAGE_SIZE = 100;

    private JobPageSizeValidator() {
    }

    public static void validate(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new JobPageSizeExceededException(MAX_PAGE_SIZE);
        }
    }
}
