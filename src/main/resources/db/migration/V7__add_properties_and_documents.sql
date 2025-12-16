CREATE TABLE properties
(
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    name              VARCHAR(255) NOT NULL,
    type              VARCHAR(50)  NOT NULL,
    status            VARCHAR(50)  NOT NULL,
    address           VARCHAR(255),
    city              VARCHAR(100),
    country           VARCHAR(100),
    zip_code          VARCHAR(20),
    area              DOUBLE PRECISION,
    price             NUMERIC(19, 2),
    acquisition_date  DATE,
    construction_year INTEGER,
    number_of_rooms   INTEGER,
    description       TEXT,
    main_photo_url    VARCHAR(500),
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at        TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_property_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE property_documents
(
    id           BIGSERIAL PRIMARY KEY,
    property_id  BIGINT       NOT NULL,
    filename     VARCHAR(255) NOT NULL,
    storage_key  VARCHAR(500) NOT NULL,
    content_type VARCHAR(100),
    size         BIGINT,
    category     VARCHAR(50),
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_document_property FOREIGN KEY (property_id) REFERENCES properties (id) ON DELETE CASCADE
);

CREATE INDEX idx_property_user ON properties (user_id);
CREATE INDEX idx_document_property ON property_documents (property_id);
