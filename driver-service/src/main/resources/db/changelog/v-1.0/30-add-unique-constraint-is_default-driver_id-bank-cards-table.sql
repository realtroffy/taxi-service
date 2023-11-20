CREATE UNIQUE INDEX unique_driver_id_is_default ON bank_cards (driver_id, is_default) WHERE is_default = true;
