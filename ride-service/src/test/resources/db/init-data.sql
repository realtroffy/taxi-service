INSERT INTO promo_codes
values (1, 'SUPER50', 0.5, '2022-01-01 10:00', '2024-01-01 10:00'),
       (2, 'SUPER30', 0.3, '2022-01-01 10:00', '2024-01-01 10:00');
SELECT SETVAL('promo_codes_id_seq', (SELECT MAX(id) from promo_codes));

INSERT INTO rides
values (1, 'Minsk', 'London', 1, 1, null, null, '2023-01-01 10:00', '2023-01-01 10:01', '2023-01-01 10:01', null,
        '1', 1, 18.79,
        'ACTIVE'),
       (2, 'Minsk', 'London', 2, null, null, null, '2023-01-01 10:00', null, null, null,
        '2', 1, 18.79,
        'PENDING'),
       (3, 'Minsk', 'London', 3, 3, 3, 3, '2023-01-01 10:00', '2023-01-01 10:01', '2023-01-01 10:01',
        '2023-01-01 10:02',
        '2', 1, 18.79,
        'FINISHED'),
       (4, 'Minsk', 'London', 3, 3, 1, 5, '2023-01-01 10:00', '2023-01-01 10:01', '2023-01-01 10:01',
        '2023-01-01 10:02',
        '2', 1, 18.79,
        'FINISHED');
SELECT SETVAL('rides_id_seq', (SELECT MAX(id) from rides));
