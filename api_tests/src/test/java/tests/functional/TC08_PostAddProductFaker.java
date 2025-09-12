package tests.functional;

import core.BaseApi;
import com.github.javafaker.Faker;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TC08_PostAddProductFaker extends BaseApi {

    @Test
    void deveCriarProdutoComDadosFaker() {
        Faker faker = new Faker(new Locale("pt-BR"));

        String titulo = faker.commerce().productName();
        double preco = faker.number().randomDouble(2, 10, 1000);
        int estoque = faker.number().numberBetween(1, 100);

        String payload = String.format("""
            {
              "title": "%s",
              "price": %.2f,
              "stock": %d
            }
            """, titulo, preco, estoque);

        Response resp = given()
                .spec(spec())
                .header("Content-Type", "application/json")
                .body(payload)
                .when()
                .post("/products/add")
                .then()
                // Aceita 200 OU 201 para criação (API pode variar)
                .statusCode(anyOf(is(200), is(201)))
                .extract().response();

        // Validações
        assertThat(resp.jsonPath().getString("title"), equalTo(titulo));
        assertThat(resp.jsonPath().getInt("stock"), equalTo(estoque));
        assertThat(resp.jsonPath().getDouble("price"), closeTo(preco, 0.01));

        System.out.println("\n=== /products/add (faker) ===");
        System.out.println(resp.asPrettyString());
    }
}
