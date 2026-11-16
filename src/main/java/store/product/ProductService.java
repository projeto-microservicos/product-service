package store.product;

import java.util.List;
import java.util.UUID;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import store.product.api.ProductIn;
import store.product.api.ProductOut;

/**
 * Product persistence with positive USD prices.
 */
@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProductOut create(ProductIn in) {
        if (in.price().signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must be positive");
        }
        return repository.save(new ProductModel(UUID.randomUUID().toString(), in.name().trim(),
            in.price(), in.unit().trim(), OffsetDateTime.now(ZoneOffset.UTC))).toOut();
    }

    @Transactional(readOnly = true)
    public List<ProductOut> findAll() {
        return repository.findAll().stream().map(ProductModel::toOut).toList();
    }

    @Transactional(readOnly = true)
    public ProductOut findById(String id) {
        return find(id).toOut();
    }

    @Transactional
    public void delete(String id) {
        repository.delete(find(id));
    }

    private ProductModel find(String id) {
        return repository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

}
