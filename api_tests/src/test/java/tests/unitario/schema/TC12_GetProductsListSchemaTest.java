package tests.unitario.schema;

import core.BaseApi;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.containsString;

public class TC12_GetProductsListSchemaTest extends BaseApi {

    @Test
    void deveValidarSchemaProductsList() {
        given()
                .spec(spec())
                .when()
                .get("/products")
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body(matchesJsonSchemaInClasspath("schemas/get-products-list-schema.json"));

        System.out.println("✅ Contrato GET /products (lista) validado com sucesso!");
    }
}
