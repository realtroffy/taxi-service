ALTER TABLE rides
    ADD CONSTRAINT fk_promo_code_ride
        FOREIGN KEY (promo_code_id) REFERENCES promo_codes (id);