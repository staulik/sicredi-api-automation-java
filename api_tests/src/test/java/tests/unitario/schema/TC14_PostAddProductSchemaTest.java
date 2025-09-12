package tests.unitario.schema;

import core.BaseApi;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

public class TC14_PostAddProductSchemaTest extends BaseApi {

    @Test
    void deveValidarSchemaPostAddProduct() {
        Faker faker = new Faker();
        String titulo = faker.commerce().productName();
        double preco = Double.parseDouble(faker.commerce().price());
        int estoque = faker.number().numberBetween(1, 100);

        String payload = """
            {
              "title": "%s",
              "price": %.2f,
              "stock": %d
            }
            """.formatted(titulo, preco, estoque);

        given()
                .spec(spec())
                .header("Content-Type", "application/json")
                .body(payload)
                .when()
                .post("/products/add")
                .then()
                // dummyjson pode responder 200 ou 201; aceitamos ambos
                .statusCode(anyOf(is(200), is(201)))
                .header("Content-Type", containsString("application/json"))
                .body(matchesJsonSchemaInClasspath("schemas/post-products-add-schema.json"));

        System.out.println("✅ Contrato POST /products/add validado com sucesso!");
    }
}
