package com.best.cvapp.company;

import com.best.cvapp.cv.CV;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favorite_cvs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteCV {

    @EmbeddedId
    private FavoriteCVId id;

    @ManyToOne
    @MapsId("companyId")
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne
    @MapsId("cvId")
    @JoinColumn(name = "cv_id")
    private CV cv;
}