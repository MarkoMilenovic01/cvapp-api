package com.best.cvapp.job.search;

import com.best.cvapp.job.core.Job;
import com.best.cvapp.job.search.dto.JobSearchFilter;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class JobSpecification implements Specification<Job> {

    private final JobSearchFilter filter;

    @Override
    public Predicate toPredicate(Root<Job> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // only active jobs
        predicates.add(cb.isTrue(root.get("active")));

        if (hasText(filter.getKeyword())) {
            String pattern = likePattern(filter.getKeyword());
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")),        pattern),
                    cb.like(cb.lower(root.get("description")),  pattern),
                    cb.like(cb.lower(root.get("requirements")), pattern)
            ));
        }

        if (hasText(filter.getLocation())) {
            predicates.add(cb.like(
                    cb.lower(root.get("location")),
                    likePattern(filter.getLocation())
            ));
        }

        if (filter.getWorkMode() != null) {
            predicates.add(cb.equal(root.get("workMode"), filter.getWorkMode()));
        }

        if (filter.getEmploymentType() != null) {
            predicates.add(cb.equal(root.get("employmentType"), filter.getEmploymentType()));
        }

        if (hasText(filter.getCompanyName())) {
            predicates.add(cb.like(
                    cb.lower(root.get("company").get("name")),
                    likePattern(filter.getCompanyName())
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