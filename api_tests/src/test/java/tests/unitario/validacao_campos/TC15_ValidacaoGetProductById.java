package tests.unitario.validacao_campos;

import core.BaseApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TC15_ValidacaoGetProductById extends BaseApi {

    @Test
    @DisplayName("TC15.01 - GET /products/{id} - ID inexistente deve retornar 404")
    void deveRetornar404ParaIdInexistente() {
        String id = "999999";

        given()
                .spec(spec())
                .pathParam("id", id)
                .when()
                .get("/products/{id}")
                .then()
                .statusCode(404)
                .header("Content-Type", containsString("application/json"))
                .body("message", equalTo("Product with id '" + id + "' not found"));
    }

    @Test
    @DisplayName("TC15.02 - GET /products/{id} - ID alfanumérico deve retornar 404")
    void deveRetornar404ParaIdAlfanumerico() {
        String id = "aaaaa";

        given()
                .spec(spec())
                .pathParam("id", id)
                .when()
                .get("/products/{id}")
                .then()
                .statusCode(404)
                .header("Content-Type", containsString("application/json"))
                .body("message", equalTo("Product with id '" + id + "' not found"));
    }

    @Test
    @DisplayName("TC15.03 - GET /products/{id} - ID zero deve retornar 404")
    void deveRetornar404ParaIdZero() {
        String id = "0";

        given()
                .spec(spec())
                .pathParam("id", id)
                .when()
                .get("/products/{id}")
                .then()
                .statusCode(404)
                .header("Content-Type", containsString("application/json"))
                .body("message", equalTo("Product with id '" + id + "' not found"));
    }

    @Test
    @DisplayName("TC15.04 - GET /products/{id} - ID negativo deve retornar 404")
    void deveRetornar404ParaIdNegativo() {
        String id = "-1";

        given()
                .spec(spec())
                .pathParam("id", id)
                .when()
                .get("/products/{id}")
                .then()
                .statusCode(404)
                .header("Content-Type", containsString("application/json"))
                .body("message", equalTo("Product with id '" + id + "' not found"));
    }

    @Test
    @DisplayName("TC15.05 - GET /products/{id} - Caracteres especiais no ID")
    void deveTratarCaracteresEspeciais() {
        // Valor bruto (sem encode) — para compor a mensagem esperada
        String rawId = "@@$$%%%";
        // Valor encodado de forma segura (evita URISyntaxException)
        String idEncoded = URLEncoder.encode(rawId, StandardCharsets.UTF_8);

        var resp =
                given()
                        .spec(spec())
                        // NÃO desabilitar o encoding aqui. Passamos já encodado.
                        .pathParam("id", idEncoded)
                        .when()
                        .get("/products/{id}")
                        .then()
                        .statusCode(anyOf(equalTo(404), equalTo(400)))
                        .extract().response();

        if (resp.getStatusCode() == 404) {
            // Backend respondeu JSON 404
            assertThat(resp.getHeader("Content-Type"), containsString("application/json"));
            // A API pode devolver a mensagem com o valor encodado; cobrimos as duas possibilidades:
            String msg = resp.jsonPath().getString("message");
            assertThat(msg, anyOf(
                    equalTo("Product with id '" + rawId + "' not found"),
                    equalTo("Product with id '" + idEncoded + "' not found"),
                    containsString("not found")
            ));
        } else {
            // CDN (Cloudflare) barrou e retornou HTML 400
            assertThat(resp.getHeader("Content-Type"),
                    anyOf(containsString("text/html"), containsString("text/plain")));
            assertThat(resp.asString(),
                    anyOf(containsString("400 Bad Request"), containsString("cloudflare")));
        }
    }
}

