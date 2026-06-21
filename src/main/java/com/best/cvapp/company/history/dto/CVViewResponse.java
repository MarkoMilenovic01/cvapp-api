package com.best.cvapp.company.history.dto;

import java.time.LocalDateTime;

public record CVViewResponse(
        Long cvId,
        String firstName,
        String lastName,
        LocalDateTime viewedAt
) {
}