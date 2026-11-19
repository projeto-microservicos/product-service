package store.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import store.product.api.ProductIn;
import store.product.api.ProductOut;

/** Executable specification of ProductService (create, list/search, find, delete). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "management.server.port=0",
    "spring.datasource.url=jdbc:h2:mem:products-contract;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password="})
class ProductContractTest {

    @Value("${local.server.port}") int port;

    RestClient api() { return RestClient.create("http://localhost:" + port); }

    ProductOut create(String name, String price) {
        return api().post().uri("/products").body(new ProductIn(name, new BigDecimal(price), "kg"))
            .retrieve().body(ProductOut.class);
    }

    @Test
    void createReturns201WithLocationAndTheSavedProduct() {
        var response = api().post().uri("/products")
            .body(new ProductIn("  Tomato ", new BigDecimal("10.12"), "kg"))
            .retrieve().toEntity(ProductOut.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        var created = response.getBody();
        assertThat(UUID.fromString(created.id())).isNotNull();
        assertThat(response.getHeaders().getLocation()).hasToString("/products/" + created.id());
        assertThat(created.name()).isEqualTo("Tomato");
        assertThat(created.price()).isEqualByComparingTo("10.12");
        assertThat(created.unit()).isEqualTo("kg");
    }

    @Test
    void nonPositivePriceIsRejected() {
        assertThatThrownBy(() -> create("Free lunch", "0"))
            .isInstanceOf(HttpClientErrorException.BadRequest.class);
        assertThatThrownBy(() -> create("Refund", "-1.00"))
            .isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    void findAllListsCreatedProducts() {
        var tomato = create("Tomato", "10.12");
        var cheese = create("Cheese", "0.62");

        var all = api().get().uri("/products").retrieve()
            .body(new ParameterizedTypeReference<java.util.List<ProductOut>>() {});
        assertThat(all).contains(tomato, cheese);
    }

    @Test
    void findAndDeleteById() {
        var created = create("Tomato", "10.12");
        assertThat(api().get().uri("/products/{id}", created.id()).retrieve().body(ProductOut.class))
            .isEqualTo(created);

        var deleted = api().delete().uri("/products/{id}", created.id()).retrieve().toBodilessEntity();
        assertThat(deleted.getStatusCode().value()).isEqualTo(204);
        assertThatThrownBy(() -> api().get().uri("/products/{id}", created.id()).retrieve().toBodilessEntity())
            .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void unknownIdIs404SoOrderServiceCanAnswer400() {
        assertThatThrownBy(() -> api().get().uri("/products/{id}", UUID.randomUUID()).retrieve().toBodilessEntity())
            .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void deletingAnUnknownIdIs404() {
        assertThatThrownBy(() -> api().delete().uri("/products/{id}", UUID.randomUUID()).retrieve().toBodilessEntity())
            .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    @Test
    void priceKeepsTwoDecimalsFromCreationToReads() {
        var created = create("Onion", "3.5");
        assertThat(created.price()).isEqualTo(new BigDecimal("3.50"));
        assertThat(api().get().uri("/products/{id}", created.id()).retrieve().body(ProductOut.class)).isEqualTo(created);
    }

    @Test
    void blankNameListsEverything() {
        var created = create("Garlic " + UUID.randomUUID(), "2.00");
        var all = api().get().uri(u -> u.path("/products").queryParam("name", " ").build()).retrieve()
            .body(new ParameterizedTypeReference<java.util.List<ProductOut>>() {});
        assertThat(all).contains(created);
    }

    @Test
    void idLookupIgnoresCaseLikeOrderServiceDoes() {
        var created = create("Pepper", "1.20");
        assertThat(api().get().uri("/products/{id}", created.id().toUpperCase()).retrieve().body(ProductOut.class))
            .isEqualTo(created);
    }

    @Test
    void searchByPartOfTheNameIgnoresCase() {
        String tag = UUID.randomUUID().toString().substring(0, 8);
        var cherry = create("Cherry tomato " + tag, "4.00");
        var roma = create("ROMA TOMATO " + tag, "3.00");
        create("Cheese " + tag, "1.00");

        var found = api().get().uri(u -> u.path("/products").queryParam("name", "tomato " + tag).build()).retrieve()
            .body(new ParameterizedTypeReference<java.util.List<ProductOut>>() {});
        assertThat(found).containsExactly(cherry, roma);
    }

}
