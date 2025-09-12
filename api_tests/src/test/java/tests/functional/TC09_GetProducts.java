package tests.functional;

import core.BaseApi;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TC09_GetProducts extends BaseApi {

    @Test
    void deveListarProdutos() {
        Response resp = given()
                .spec(spec())
                .when()
                .get("/products")
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("products", notNullValue())
                .body("products.size()", greaterThan(0))
                .body("total", greaterThan(0))
                .body("limit", greaterThan(0))
                .extract().response();

        // Valida campos do primeiro produto (sanidade)
        assertThat(resp.jsonPath().getInt("products[0].id"), greaterThan(0));
        assertThat(resp.jsonPath().getString("products[0].title"), not(isEmptyOrNullString()));
        assertThat(resp.jsonPath().getFloat("products[0].price"), greaterThan(0.0f));
        assertThat(resp.jsonPath().getInt("products[0].stock"), greaterThanOrEqualTo(0));

        System.out.println("\n=== /products ===");
        System.out.println(resp.asPrettyString());
    }
}
