package com.best.cvapp.company.cv;

import com.best.cvapp.company.cv.dto.CVSearchRequest;
import com.best.cvapp.cv.profile.CV;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class CVSpecification implements Specification<CV> {

    private static final char LIKE_ESCAPE = '\\';

    private final CVSearchRequest request;

    @Override
    public Predicate toPredicate(Root<CV> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // ── keyword: search across firstName, lastName, summary ───────────────
        if (hasText(request.keyword())) {
            String pattern = likePattern(request.keyword());

            // Join to skills and experience for broader keyword matching
            Join<Object, Object> skillsJoin = root.join("skills", JoinType.LEFT);
            Join<Object, Object> experienceJoin = root.join("experience", JoinType.LEFT);
            Join<Object, Object> educationJoin = root.join("education", JoinType.LEFT);

            // Avoid duplicates from multiple joins
            query.distinct(true);

            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("firstName")),  pattern, LIKE_ESCAPE),
                    cb.like(cb.lower(root.get("lastName")),   pattern, LIKE_ESCAPE),
                    cb.like(cb.lower(root.get("summary")),    pattern, LIKE_ESCAPE),
                    cb.like(cb.lower(skillsJoin.get("name")), pattern, LIKE_ESCAPE),
                    cb.like(cb.lower(experienceJoin.get("position")),    pattern, LIKE_ESCAPE),
                    cb.like(cb.lower(experienceJoin.get("companyName")), pattern, LIKE_ESCAPE),
                    cb.like(cb.lower(educationJoin.get("institution")),  pattern, LIKE_ESCAPE),
                    cb.like(cb.lower(educationJoin.get("fieldOfStudy")), pattern, LIKE_ESCAPE)
            ));
        }

        // ── skill: exact skill name match ─────────────────────────────────────
        if (hasText(request.skill())) {
            Join<Object, Object> skillsJoin = root.join("skills", JoinType.LEFT);
            query.distinct(true);
            predicates.add(cb.like(
                    cb.lower(skillsJoin.get("name")),
                    likePattern(request.skill()),
                    LIKE_ESCAPE
            ));
        }

        // ── location: match against address field ─────────────────────────────
        if (hasText(request.location())) {
            predicates.add(cb.like(
                    cb.lower(root.get("address")),
                    likePattern(request.location()),
                    LIKE_ESCAPE
            ));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String likePattern(String value) {
        String escaped = value.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
