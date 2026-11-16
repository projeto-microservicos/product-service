package store.product;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import store.product.api.ProductIn;
import store.product.api.ProductOut;

/**
 * Business rules for products. The HTTP layer (ProductResource) and persistence
 * (ProductModel, ProductRepository, Flyway) are ready; every method below is yours.
 *
 * <p>Until implemented, each method answers 501 Not Implemented. The disabled tests in
 * ProductContractTest describe the expected behavior: enable them one by one.
 */
@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProductOut create(ProductIn in) {
        // TODO(product): implement product creation.
        //  1. Trim name and unit before saving.
        //  2. Enforce your price rule (e.g. price > 0) -> 400 BAD_REQUEST.
        //     order-service treats a negative price as "service unavailable" (503), so never store one.
        //  3. Save new ProductModel(UUID.randomUUID().toString(), ..., OffsetDateTime.now(ZoneOffset.UTC))
        //     and return model.toOut().
        throw todo("create product");
    }

    @Transactional(readOnly = true)
    public List<ProductOut> findAll() {
        // TODO(product): return every product as ProductOut.
        //  Optional (course): filter by name ("like") through a query parameter.
        throw todo("list products");
    }

    @Transactional(readOnly = true)
    public ProductOut findById(String id) {
        // TODO(product): repository.findById(id) -> 404 NOT_FOUND when absent.
        //  order-service relies on this 404: it turns it into "400 Product does not exist".
        throw todo("find product by id");
    }

    @Transactional
    public void delete(String id) {
        // TODO(product): delete the product; decide between 404 or idempotent 204 when absent.
        //  Orders keep their own price snapshot, so deleting a product does not change past orders.
        throw todo("delete product");
    }

    private static ResponseStatusException todo(String what) {
        return new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "TODO: " + what);
    }

}
