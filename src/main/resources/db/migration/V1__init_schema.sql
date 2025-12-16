CREATE TABLE subscription_plans
(
    id             SERIAL PRIMARY KEY,
    name           VARCHAR(50)    NOT NULL UNIQUE,
    price_monthly  DECIMAL(10, 2) NOT NULL,
    price_yearly   DECIMAL(10, 2) NOT NULL,
    max_properties INT            NOT NULL,
    features       JSONB,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone         VARCHAR(20),
    profile_type  VARCHAR(50)  NOT NULL, -- INVESTOR, AGENCY, etc.
    is_verified   BOOLEAN                  DEFAULT FALSE,
    avatar_url    VARCHAR(255),
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles
(
    user_id BIGINT      NOT NULL,
    role    VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE user_subscriptions
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    plan_id    INT         NOT NULL,
    start_date DATE        NOT NULL,
    end_date   DATE,
    status     VARCHAR(50) NOT NULL, -- ACTIVE, EXPIRED, TRIAL
    auto_renew BOOLEAN                  DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscription_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_subscription_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans (id)
);

CREATE TABLE event_publication
(
    id               UUID                        NOT NULL,
    completion_date  TIMESTAMP(6) WITH TIME ZONE,
    event_type       VARCHAR(512)                NOT NULL,
    listener_id      VARCHAR(512)                NOT NULL,
    publication_date TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    serialized_event VARCHAR(4000)               NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_users_email ON users (email);
