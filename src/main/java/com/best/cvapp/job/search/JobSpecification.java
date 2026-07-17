package com.best.cvapp.job.search;

import com.best.cvapp.job.core.Job;
import com.best.cvapp.job.search.dto.JobSearchFilter;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public class JobSpecification implements Specification<Job> {

    private static final char LIKE_ESCAPE = '\\';

    private final JobSearchFilter filter;

    @Override
    public Predicate toPredicate(Root<Job> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // only active jobs
        predicates.add(cb.isTrue(root.get("active")));
        predicates.add(cb.or(
                cb.isNull(root.get("deadline")),
                cb.greaterThanOrEqualTo(root.get("deadline"), LocalDate.now())
        ));

        if (hasText(filter.keyword())) {
            String pattern = likePattern(filter.keyword());
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")),        pattern, LIKE_ESCAPE),
                    cb.like(cb.lower(root.get("description")),  pattern, LIKE_ESCAPE),
                    cb.like(cb.lower(root.get("requirements")), pattern, LIKE_ESCAPE)
            ));
        }

        if (hasText(filter.location())) {
            predicates.add(cb.like(
                    cb.lower(root.get("location")),
                    likePattern(filter.location()),
                    LIKE_ESCAPE
            ));
        }

        if (filter.workMode() != null) {
            predicates.add(cb.equal(root.get("workMode"), filter.workMode()));
        }

        if (filter.employmentType() != null) {
            predicates.add(cb.equal(root.get("employmentType"), filter.employmentType()));
        }

        if (hasText(filter.companyName())) {
            predicates.add(cb.like(
                    cb.lower(root.get("company").get("name")),
                    likePattern(filter.companyName()),
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
