package com.best.cvapp.job;

import com.best.cvapp.job.dto.JobSearchRequest;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class JobSpecification implements Specification<Job> {

    private final JobSearchRequest request;

    @Override
    public Predicate toPredicate(Root<Job> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // ── Only show active jobs to users ────────────────────────────────────
        predicates.add(cb.isTrue(root.get("active")));

        // ── keyword: search across title, description, requirements ───────────
        if (hasText(request.getKeyword())) {
            String pattern = likePattern(request.getKeyword());
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")),        pattern),
                    cb.like(cb.lower(root.get("description")),  pattern),
                    cb.like(cb.lower(root.get("requirements")), pattern)
            ));
        }

        // ── location ──────────────────────────────────────────────────────────
        if (hasText(request.getLocation())) {
            predicates.add(cb.like(
                    cb.lower(root.get("location")),
                    likePattern(request.getLocation())
            ));
        }

        // ── employmentType: exact enum match ──────────────────────────────────
        if (request.getEmploymentType() != null) {
            predicates.add(cb.equal(root.get("employmentType"), request.getEmploymentType()));
        }

        // ── workMode: exact enum match ────────────────────────────────────────
        if (request.getWorkMode() != null) {
            predicates.add(cb.equal(root.get("workMode"), request.getWorkMode()));
        }

        // ── companyName: join to company and match name ───────────────────────
        if (hasText(request.getCompanyName())) {
            Join<Object, Object> companyJoin = root.join("company", JoinType.LEFT);
            predicates.add(cb.like(
                    cb.lower(companyJoin.get("name")),
                    likePattern(request.getCompanyName())
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