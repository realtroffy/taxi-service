CREATE UNIQUE INDEX idx_unique_is_default ON bank_cards(passenger_id, is_default) WHERE is_default = true;
