package tests.unitario.schema;

import core.BaseApi;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.containsString;

public class TC11_GetProductByIdSchemaTest extends BaseApi {

    @Test
    void deveValidarSchemaProductById() {
        int id = 1; // estável para contrato

        given()
                .spec(spec())
                .pathParam("id", id)
                .when()
                .get("/products/{id}")
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body(matchesJsonSchemaInClasspath("schemas/get-product-by-id-schema.json"));

        System.out.println("\n✅ Contrato validado com sucesso para /products/" + id);
    }
}
