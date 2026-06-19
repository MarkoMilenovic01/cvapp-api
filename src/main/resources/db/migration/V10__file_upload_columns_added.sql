-- CVs: profile photo of the user + optional custom PDF upload
ALTER TABLE cvs
    ADD COLUMN profile_photo_url  VARCHAR(500),
    ADD COLUMN profile_photo_id   VARCHAR(300),
    ADD COLUMN pdf_url            VARCHAR(500),
    ADD COLUMN pdf_public_id      VARCHAR(300);

-- Companies: company photo only
ALTER TABLE companies
    ADD COLUMN photo_url          VARCHAR(500),
    ADD COLUMN photo_public_id    VARCHAR(300);
