package com.best.cvapp.cv;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CVSpecification implements Specification<CV> {

    private final CVSearchRequest request;

    @Override
    public Predicate toPredicate(Root<CV> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // ── keyword: search across firstName, lastName, summary ───────────────
        if (hasText(request.getKeyword())) {
            String pattern = likePattern(request.getKeyword());

            // Join to skills and experience for broader keyword matching
            Join<Object, Object> skillsJoin = root.join("skills", JoinType.LEFT);
            Join<Object, Object> experienceJoin = root.join("experience", JoinType.LEFT);
            Join<Object, Object> educationJoin = root.join("education", JoinType.LEFT);

            // Avoid duplicates from multiple joins
            query.distinct(true);

            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("firstName")),  pattern),
                    cb.like(cb.lower(root.get("lastName")),   pattern),
                    cb.like(cb.lower(root.get("summary")),    pattern),
                    cb.like(cb.lower(skillsJoin.get("name")), pattern),
                    cb.like(cb.lower(experienceJoin.get("position")),    pattern),
                    cb.like(cb.lower(experienceJoin.get("companyName")), pattern),
                    cb.like(cb.lower(educationJoin.get("institution")),  pattern),
                    cb.like(cb.lower(educationJoin.get("fieldOfStudy")), pattern)
            ));
        }

        // ── skill: exact skill name match ─────────────────────────────────────
        if (hasText(request.getSkill())) {
            Join<Object, Object> skillsJoin = root.join("skills", JoinType.LEFT);
            query.distinct(true);
            predicates.add(cb.like(
                    cb.lower(skillsJoin.get("name")),
                    likePattern(request.getSkill())
            ));
        }

        // ── location: match against address field ─────────────────────────────
        if (hasText(request.getLocation())) {
            predicates.add(cb.like(
                    cb.lower(root.get("address")),
                    likePattern(request.getLocation())
            ));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String likePattern(String value) {
        return "%" + value.toLowerCase() + "%";
    }
}