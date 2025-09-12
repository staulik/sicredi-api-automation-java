package tests.unitario.validacao_campos;

import core.BaseApi;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

/**
 * ERRO ESPERADO — POST /products/add
 * Objetivo: cada cenário envia payload inválido e ESPERA erro (400/422).
 * Se o endpoint aceitar (200/201), o teste FALHA explicitamente.
 *
 * Obs.: Em ambiente DummyJSON, vários cenários vão FALHAR (retorna 201),
 * servindo como evidência de ausência de validação no mock.
 */
public class TC18_ValidacaoPostProductAdd_ErroEsperado extends BaseApi {

    private Response postProduct(String jsonPayload) {
        return given()
                .spec(spec())
                .header("Content-Type", "application/json")
                .body(jsonPayload)
                .when()
                .post("/products/add")
                .then()
                .extract().response();
    }

    /** Espera 400/422. Se vier 200/201, falha com mensagem clara. Faz checks brandos no corpo. */
    private void assertErroEsperado(Response resp, String... keywords) {
        int sc = resp.getStatusCode();
        String body = resp.asString();
        String ct = resp.getHeader("Content-Type");

        // Aceitou payload inválido → falha explícita
        if (sc == 200 || sc == 201) {
            throw new AssertionError(
                    "⚠ Erro esperado NÃO ocorreu: endpoint aceitou payload inválido e retornou "
                            + sc + ". Body: " + body
            );
        }

        // Status de erro esperado para validação
        assertThat("Status inesperado (esperava 400/422)", sc, anyOf(equalTo(400), equalTo(422)));

        // Heurística de mensagem (JSON) ou HTML de proxy
        if (ct != null && ct.toLowerCase().contains("application/json")) {
            String msg = null;
            try { msg = resp.jsonPath().getString("message"); } catch (Exception ignored) {}
            if (msg == null) msg = body;

            // termos genéricos de validação
            assertThat("Mensagem deveria indicar erro de validação. Body: " + body,
                    msg.toLowerCase(),
                    anyOf(
                            containsString("invalid"),
                            containsString("required"),
                            containsString("missing"),
                            containsString("title"),
                            containsString("price"),
                            containsString("stock")
                    )
            );

            // palavras-chave específicas, se fornecidas
            for (String kw : keywords) {
                if (kw != null && !kw.isBlank()) {
                    assertThat("Esperava conter keyword específica: " + kw + " | Body: " + body,
                            msg.toLowerCase(), containsString(kw.toLowerCase()));
                }
            }
        } else {
            // HTML (Cloudflare / gateway)
            assertThat("Esperava HTML de erro do proxy/gateway",
                    body, anyOf(containsString("Bad Request"), containsString("cloudflare")));
        }
    }

    // ===== Cenários =====

    // ---- TITLE ----

    @Test
    @DisplayName("TC17.01 - POST /products/add - Title ausente (Erro Esperado)")
    void titleAusente() {
        String payload = """
            {
              "price": 10.5,
              "stock": 5
            }
            """;
        Response resp = postProduct(payload);
        assertErroEsperado(resp, "title");
    }

    @Test
    @DisplayName("TC17.02 - POST /products/add - Title vazio (Erro Esperado)")
    void titleVazio() {
        String payload = """
            {
              "title": "",
              "price": 10.5,
              "stock": 5
            }
            """;
        Response resp = postProduct(payload);
        assertErroEsperado(resp, "title");
    }

    @Test
    @DisplayName("TC17.03 - POST /products/add - Title tipo inválido (numérico) (Erro Esperado)")
    void titleTipoInvalido() {
        String payload = """
            {
              "title": 12345,
              "price": 10.5,
              "stock": 5
            }
            """;
        Response resp = postProduct(payload);
        assertErroEsperado(resp, "title", "type", "invalid");
    }

    // ---- PRICE ----

    @Test
    @DisplayName("TC17.04 - POST /products/add - Price ausente (Erro Esperado)")
    void priceAusente() {
        String payload = """
            {
              "title": "Produto X",
              "stock": 5
            }
            """;
        Response resp = postProduct(payload);
        assertErroEsperado(resp, "price");
    }

    @Test
    @DisplayName("TC17.05 - POST /products/add - Price negativo (Erro Esperado)")
    void priceNegativo() {
        String payload = """
            {
              "title": "Produto X",
              "price": -1.0,
              "stock": 5
            }
            """;
        Response resp = postProduct(payload);
        assertErroEsperado(resp, "price", "invalid");
    }

    @Test
    @DisplayName("TC17.06 - POST /products/add - Price tipo inválido (string) (Erro Esperado)")
    void priceTipoInvalido() {
        String payload = """
            {
              "title": "Produto X",
              "price": "dez e meio",
              "stock": 5
            }
            """;
        Response resp = postProduct(payload);
        assertErroEsperado(resp, "price", "type", "invalid");
    }

    // ---- STOCK ----

    @Test
    @DisplayName("TC17.07 - POST /products/add - Stock ausente (Erro Esperado)")
    void stockAusente() {
        String payload = """
            {
              "title": "Produto X",
              "price": 10.5
            }
            """;
        Response resp = postProduct(payload);
        assertErroEsperado(resp, "stock");
    }

    @Test
    @DisplayName("TC17.08 - POST /products/add - Stock negativo (Erro Esperado)")
    void stockNegativo() {
        String payload = """
            {
              "title": "Produto X",
              "price": 10.5,
              "stock": -1
            }
            """;
        Response resp = postProduct(payload);
        assertErroEsperado(resp, "stock", "invalid");
    }

    @Test
    @DisplayName("TC17.09 - POST /products/add - Stock tipo inválido (string) (Erro Esperado)")
    void stockTipoInvalido() {
        String payload = """
            {
              "title": "Produto X",
              "price": 10.5,
              "stock": "dez"
            }
            """;
        Response resp = postProduct(payload);
        assertErroEsperado(resp, "stock", "type", "invalid");
    }

    // ---- BODY VAZIO ----

    @Test
    @DisplayName("TC17.10 - POST /products/add - Body vazio {} (Erro Esperado)")
    void bodyVazioObjeto() {
        String payload = "{}";
        Response resp = postProduct(payload);
        assertErroEsperado(resp, "title", "price", "stock", "required");
    }

    @Test
    @DisplayName("TC17.11 - POST /products/add - Body vazio (string) JSON inválido (Erro Esperado)")
    void bodyVazioString() {
        String payload = ""; // JSON inválido
        Response resp = given()
                .spec(spec())
                .header("Content-Type", "application/json")
                .body(payload)
                .when()
                .post("/products/add")
                .then()
                .extract().response();

        // Aqui esperamos 400 de parse JSON (gateway/proxy); se aceitar → falha
        assertErroEsperado(resp, "invalid", "json", "body");
    }
}
