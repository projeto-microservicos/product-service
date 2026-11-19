package store.product;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductModel, String> {

    /** Backs {@code GET /products?name=}: SQL LIKE '%name%' ignoring case (Spring Data escapes % and _). */
    List<ProductModel> findByNameContainingIgnoreCase(String name, Sort sort);

}
