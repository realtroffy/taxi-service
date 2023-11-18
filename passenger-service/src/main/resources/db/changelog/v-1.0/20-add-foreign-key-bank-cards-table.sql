ALTER TABLE bank_cards
    ADD CONSTRAINT fk_passenger_id FOREIGN KEY (passenger_id) REFERENCES passengers (id) ON DELETE CASCADE;