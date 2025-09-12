package tests.e2e;

import com.github.javafaker.Faker;
import core.BaseApi;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E - Fluxo: health -> login -> auth/products -> add product -> (tenta) get product by id")
public class EndToEndFlowTest extends BaseApi {

    @Test
    @DisplayName("Fluxo completo com fallback para GET /products/{id} (DummyJSON não persiste criação)")
    public void deveExecutarFluxoE2ECompleto() {
        // 1) GET /test (health)
        given()
                .spec(spec())
                .when()
                .get("/test")
                .then()
                .statusCode(200)
                .body("status", equalTo("ok"))
                .body("method", equalTo("GET"));

        // 2) POST /auth/login (token)
        String loginPayload = """
            {
              "username": "emilys",
              "password": "emilyspass"
            }
            """;

        Response loginResp = given()
                .spec(spec())
                .header("Content-Type", "application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("username", equalTo("emilys"))
                .body("accessToken", notNullValue())
                .extract().response();

        String accessToken = loginResp.jsonPath().getString("accessToken");
        assertNotNull(accessToken, "Token não deve ser nulo!");

        // 3) GET /auth/products (com token)
        given()
                .spec(spec())
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .get("/auth/products")
                .then()
                .statusCode(200)
                .body("products", notNullValue());

        // 4) POST /products/add (cria produto com Faker)
        Faker faker = new Faker();
        String title  = faker.commerce().productName();
        double price  = Double.parseDouble(faker.commerce().price());
        int stock     = faker.number().numberBetween(1, 100);

        String productPayload = String.format("""
            {
              "title": "%s",
              "price": %.2f,
              "stock": %d
            }
            """, title, price, stock);

        Response addResp = given()
                .spec(spec())
                .header("Content-Type", "application/json")
                .body(productPayload)
                .when()
                .post("/products/add")
                .then()
                // DummyJSON pode devolver 200 ou 201
                .statusCode(anyOf(is(200), is(201)))
                .body("title", equalTo(title))
                .body("stock", equalTo(stock))
                .extract().response();

        int productId = addResp.jsonPath().getInt("id");
        assertTrue(productId > 0, "Id do produto deve ser > 0");

        // Validação extra do price na resposta do POST (evita problemas de double)
        double returnedPrice = addResp.jsonPath().getDouble("price");
        assertTrue(Math.abs(returnedPrice - price) < 0.01,
                "Preço retornado difere do enviado (tolerância 0.01)");

        // 5) GET /products/{id} (tentativa — API não persiste item criado)
        Response getById = given()
                .spec(spec())
                .pathParam("id", productId)
                .when()
                .get("/products/{id}");

        if (getById.statusCode() == 200) {
            // Caso raro (se a API passar a persistir), validamos
            assertEquals(productId, getById.jsonPath().getInt("id"));
            assertEquals(title, getById.jsonPath().getString("title"));
        } else {
            // Comportamento atual/esperado do DummyJSON
            assertEquals(404, getById.statusCode(), "Esperado 404 pois o recurso não é persistido");
            System.out.printf("INFO: DummyJSON não persiste o recurso criado. id=%d retornou 404.%n", productId);
        }
    }
}
