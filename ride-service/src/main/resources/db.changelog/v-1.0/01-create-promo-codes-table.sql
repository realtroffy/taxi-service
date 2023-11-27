CREATE TABLE promo_codes
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) UNIQUE NOT NULL,
    discount    NUMERIC NOT NULL,
    start_date  TIMESTAMP NOT NULL,
    finish_time TIMESTAMP NOT NULL
);