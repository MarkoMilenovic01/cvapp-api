package com.best.cvapp.admin;

import com.best.cvapp.admin.exception.AdminPageSizeExceededException;
import org.springframework.data.domain.Pageable;

public final class AdminPageSizeValidator {

    public static final int MAX_PAGE_SIZE = 100;

    private AdminPageSizeValidator() {
    }

    public static void validate(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new AdminPageSizeExceededException(MAX_PAGE_SIZE);
        }
    }
}
