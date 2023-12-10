CREATE TABLE rides
(
    id                     BIGSERIAL PRIMARY KEY,
    start_location         VARCHAR(255)   NOT NULL,
    end_location           VARCHAR(255)   NOT NULL,
    passenger_id           BIGINT         NOT NULL,
    driver_id              BIGINT,
    driver_rating          INTEGER,
    passenger_rating       INTEGER,
    booking_time           TIMESTAMP      NOT NULL,
    approved_time          TIMESTAMP,
    start_time             TIMESTAMP,
    finish_time            TIMESTAMP,
    passenger_bank_card_id VARCHAR(255),
    promo_code_id          BIGINT,
    cost                   NUMERIC(10, 2) NOT NULL
);