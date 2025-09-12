package tests.unitario.schema;

import core.BaseApi;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

public class TC13_PostAuthLoginSchemaTest extends BaseApi {

    @Test
    void deveValidarSchemaPostAuthLogin() {
        String payload = """
            {
              "username": "emilys",
              "password": "emilyspass"
            }
            """;

        given()
                .spec(spec())
                .header("Content-Type", "application/json")
                .body(payload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body(matchesJsonSchemaInClasspath("schemas/post-auth-login-schema.json"));

        System.out.println("✅ Contrato POST /auth/login validado com sucesso!");
    }
}
