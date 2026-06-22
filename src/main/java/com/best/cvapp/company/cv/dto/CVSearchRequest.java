package com.best.cvapp.company.cv.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CVSearchRequest {

    private String keyword;

    private String skill;

    private String location;
}