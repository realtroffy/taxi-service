ALTER TABLE bank_cards
    ADD CONSTRAINT fk_bank_card_driver_id FOREIGN KEY (driver_id) REFERENCES drivers (id) ON DELETE CASCADE;