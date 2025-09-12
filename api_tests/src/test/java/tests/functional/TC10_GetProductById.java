package tests.functional;

import core.BaseApi;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TC10_GetProductById extends BaseApi {

    @Test
    void deveBuscarProdutoPorId() {
        int id = 1; // estável para teste funcional; no E2E usaremos o ID criado

        Response resp = given()
                .spec(spec())
                .pathParam("id", id)
                .when()
                .get("/products/{id}")
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("id", equalTo(id))
                .body("title", not(isEmptyOrNullString()))
                // price vem como Float/Double -> matcher precisa ser do mesmo tipo
                .body("price", greaterThan(0f))
                .body("stock", greaterThanOrEqualTo(0))
                .extract().response();

        // Sanidade extra: campos opcionais
        assertThat(resp.jsonPath().getString("brand"), notNullValue());
        assertThat(resp.jsonPath().getString("category"), notNullValue());

        System.out.println("\n=== /products/{id} ===");
        System.out.println(resp.asPrettyString());
    }
}
