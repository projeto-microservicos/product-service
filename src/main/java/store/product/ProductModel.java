package store.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import store.product.api.ProductOut;

/** JPA mapping of {@code products.products} (see db/migration/V1__products.sql). */
@Entity
@Table(name = "products")
public class ProductModel {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 32)
    private String unit;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected ProductModel() {}

    public ProductModel(String id, String name, BigDecimal price, String unit, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.unit = unit;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public String getUnit() { return unit; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public ProductOut toOut() {
        return new ProductOut(id, name, price, unit);
    }

}
