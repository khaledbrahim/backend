CREATE TABLE visitor_requests
(
    id            BIGSERIAL PRIMARY KEY,
    property_id   BIGINT      NOT NULL REFERENCES properties (id),
    user_id       BIGINT REFERENCES users (id),
    visitor_name  VARCHAR(255),
    visitor_email VARCHAR(255),
    visitor_phone VARCHAR(50),
    message       TEXT,
    type          VARCHAR(50) NOT NULL,
    status        VARCHAR(50) NOT NULL,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP
);

CREATE INDEX idx_visitor_requests_property ON visitor_requests (property_id);
CREATE INDEX idx_visitor_requests_user ON visitor_requests (user_id);
