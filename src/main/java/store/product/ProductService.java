package store.product;

import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import store.product.api.ProductIn;
import store.product.api.ProductOut;

/**
 * Business rules for products.
 *
 * <ul>
 *   <li>Prices are USD and must be greater than zero (400 otherwise): order-service treats a
 *       negative price as "service unavailable", so one is never stored.</li>
 *   <li>Prices are kept with two decimals, so the value returned on creation is the same one
 *       read back later (NUMERIC(19,2)).</li>
 *   <li>An unknown id is 404, for reads and deletes (ids are case-insensitive). order-service turns
 *       the 404 of {@code GET /products/{id}} into "400 Product does not exist".</li>
 * </ul>
 */
@Service
public class ProductService {

    /** Name ignoring case (same order on H2 and Postgres), then creation time and id as tie-breakers. */
    private static final Sort BY_NAME = Sort.by(Sort.Order.asc("name").ignoreCase(), Sort.Order.asc("createdAt"), Sort.Order.asc("id"));

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProductOut create(ProductIn in) {
        if (in.price().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "price must be greater than zero");
        }
        // @Digits(fraction = 2) on ProductIn already rejects more than two decimals.
        var price = in.price().setScale(2, RoundingMode.UNNECESSARY);
        var model = new ProductModel(UUID.randomUUID().toString(), in.name().trim(), price, in.unit().trim(),
            OffsetDateTime.now(ZoneOffset.UTC));
        return repository.save(model).toOut();
    }

    /** Every product, ordered by name; with {@code name}, only those whose name contains it (case-insensitive). */
    @Transactional(readOnly = true)
    public List<ProductOut> findAll(String name) {
        var models = name == null || name.isBlank()
            ? repository.findAll(BY_NAME)
            : repository.findByNameContainingIgnoreCase(name.trim(), BY_NAME);
        return models.stream().map(ProductModel::toOut).toList();
    }

    @Transactional(readOnly = true)
    public ProductOut findById(String id) {
        return repository.findById(key(id)).map(ProductModel::toOut).orElseThrow(() -> notFound(id));
    }

    /** Orders keep their own price snapshot, so deleting a product does not change past orders. */
    @Transactional
    public void delete(String id) {
        repository.delete(repository.findById(key(id)).orElseThrow(() -> notFound(id)));
    }

    /** Ids are generated in lowercase; order-service also accepts uppercase UUIDs, so normalize before looking up. */
    private static String key(String id) {
        return id.toLowerCase(Locale.ROOT);
    }

    private static ResponseStatusException notFound(String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "product " + id + " not found");
    }

}
