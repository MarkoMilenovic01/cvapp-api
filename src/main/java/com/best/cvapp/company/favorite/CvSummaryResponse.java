package com.best.cvapp.company.favorite;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CvSummaryResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String summary;
    private boolean isFavorite;
}