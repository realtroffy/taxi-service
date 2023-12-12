ALTER TABLE bank_cards
    ADD CONSTRAINT check_card_number_length CHECK (card_number ~ '^[0-9]{16}$');
