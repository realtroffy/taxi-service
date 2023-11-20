ALTER TABLE cars
    ADD CONSTRAINT fk_bank_card_driver_id FOREIGN KEY (driver_id) REFERENCES drivers (id) ON DELETE CASCADE;