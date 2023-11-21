CREATE TABLE drivers
(
    id           BIGSERIAL PRIMARY KEY,
    email        VARCHAR(255)     NOT NULL UNIQUE,
    password     VARCHAR(255)     NOT NULL,
    first_name   VARCHAR(255)     NOT NULL,
    last_name    VARCHAR(255)     NOT NULL,
    rating       DOUBLE PRECISION NOT NULL DEFAULT 5.0,
    is_available BOOLEAN          NOT NULL DEFAULT false
);