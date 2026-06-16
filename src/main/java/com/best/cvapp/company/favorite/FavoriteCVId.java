package com.best.cvapp.company.favorite;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FavoriteCVId implements Serializable {
    private Long companyId;
    private Long cvId;
}