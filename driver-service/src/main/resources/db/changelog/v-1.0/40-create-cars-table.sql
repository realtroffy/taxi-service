CREATE TABLE cars
(
    driver_id BIGINT       NOT NULL,
    model     VARCHAR(255) NOT NULL,
    colour    VARCHAR(255) NOT NULL,
    number    VARCHAR(255) NOT NULL UNIQUE

);
