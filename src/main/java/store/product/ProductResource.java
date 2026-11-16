package store.product;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.product.api.ProductIn;
import store.product.api.ProductOut;

/**
 * HTTP contract of the product service (same paths as the course's ProductController).
 * Internal service: the gateway authenticates users; this class only maps HTTP to ProductService.
 */
@RestController
@RequestMapping("/products")
public class ProductResource {

    private final ProductService service;

    public ProductResource(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ProductOut> create(@Valid @RequestBody ProductIn in) {
        ProductOut created = service.create(in);
        return ResponseEntity.created(URI.create("/products/" + created.id())).body(created);
    }

    @GetMapping
    public List<ProductOut> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ProductOut findById(@PathVariable String id) {
        return service.findById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health-check")
    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.ok().build();
    }

}
