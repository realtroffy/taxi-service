INSERT INTO drivers
values (22, 'john.doe@google.com', '12345', 'John', 'Doe', 5.0, true),
       (66, 'artur@google.com', '12345', 'Artur', 'Asiptsou', 3.0, false),
       (100, 'ivan@google.com', '12345', 'Ivan', 'Ivanov', 2.0, false);
SELECT SETVAL('drivers_id_seq', (SELECT MAX(id) from drivers));

INSERT INTO bank_cards
values (1, 22, '1234567891011131', 100, true),
       (2, 66, '1234567891011132', 1000, false),
       (3, 100, '1234567891011133', 50, true);
SELECT SETVAL('bank_cards_id_seq', (SELECT MAX(id) from bank_cards));

INSERT INTO cars
values (22, 'lada vesta', 'green', '1248-AC7'),
       (66, 'mazda cx5', 'black', '1122-AC7'),
       (100, 'opel astra', 'white', '3267-AC7');