package store.product.api;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Body of {@code POST /products}. Same JSON shape as the course's {@code ProductIn}:
 * {@code {"name": "Tomato", "price": 10.12, "unit": "kg"}}.
 *
 * <p>Only structural limits live here (column sizes, NUMERIC(19,2)). Business rules such as
 * "price must be positive" belong in {@code ProductService}.
 */
public record ProductIn(
    @NotBlank @Size(max = 256) String name,
    @NotNull @Digits(integer = 17, fraction = 2) BigDecimal price,
    @NotBlank @Size(max = 32) String unit
) {}
