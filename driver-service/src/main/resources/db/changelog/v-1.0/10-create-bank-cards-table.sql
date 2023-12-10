CREATE TABLE bank_cards
(
    id          BIGSERIAL PRIMARY KEY,
    driver_id   BIGINT  NOT NULL,
    card_number VARCHAR(255) NOT NULL UNIQUE CHECK (card_number ~ '^[0-9]{16}$'),
    balance     NUMERIC NOT NULL,
    is_default  BOOLEAN NOT NULL
);
