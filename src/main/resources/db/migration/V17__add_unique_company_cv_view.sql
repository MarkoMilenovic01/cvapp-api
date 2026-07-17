ALTER TABLE cv_views
    ADD CONSTRAINT uk_cv_views_company_cv UNIQUE (company_id, cv_id);
