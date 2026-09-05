CREATE TABLE certificate (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user (id),
    name VARCHAR(200) NOT NULL,
    issuing_org VARCHAR(200) NOT NULL,
    issue_date DATE,
    verification_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_certificate_user_id
    ON certificate (user_id);