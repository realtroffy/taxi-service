CREATE TABLE bank_cards
(
    id          BIGSERIAL PRIMARY KEY,
    driver_id   BIGINT  NOT NULL,
    card_number BIGINT  NOT NULL UNIQUE,
    balance     NUMERIC NOT NULL,
    is_default  BOOLEAN NOT NULL
);
