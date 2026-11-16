-- Flyway runs this inside the "products" schema (spring.flyway.schemas).
-- Prices are stored in USD; order-service converts to other currencies through exchange-service.
CREATE TABLE products (
    id         VARCHAR(36)              PRIMARY KEY,
    name       VARCHAR(256)             NOT NULL,
    price      NUMERIC(19, 2)           NOT NULL,
    unit       VARCHAR(32)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
