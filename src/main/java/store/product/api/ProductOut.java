package store.product.api;

import java.math.BigDecimal;

/**
 * Public view of a product. order-service reads {@code id} and {@code price} (USD)
 * from {@code GET /products/{id}} when it creates an order.
 */
public record ProductOut(
    String id,
    String name,
    BigDecimal price,
    String unit
) {}
