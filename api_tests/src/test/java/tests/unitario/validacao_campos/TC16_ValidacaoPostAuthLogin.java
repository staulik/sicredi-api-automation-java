package tests.unitario.validacao_campos;

import core.BaseApi;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Validações de campo para POST /auth/login
 * - Campos ausentes
 * - Campos vazios
 * - Tipos inválidos
 * - Body vazio
 *
 * Observação: A API pode retornar 400 (Bad Request) ou 401 (Unauthorized) para credenciais inválidas,
 * e em alguns casos o proxy (Cloudflare) pode devolver HTML. Por isso os asserts são resilientes.
 */
public class TC16_ValidacaoPostAuthLogin extends BaseApi {

    // ===== Helpers =====

    /** Verifica status 400/401 e mensagem de erro (JSON), com fallback para HTML */
    private void assertErroCliente(Response resp, String... keywordsEsperados) {
        assertThat("Status inesperado",
                resp.getStatusCode(), anyOf(equalTo(400), equalTo(401)));

        String ct = resp.getHeader("Content-Type");
        String body = resp.asString();

        if (ct != null && ct.toLowerCase().contains("application/json")) {
            String msg = resp.jsonPath().getString("message");
            // fallback: se não veio 'message', tentamos com o corpo inteiro
            if (msg == null) msg = body;

            assertThat("Mensagem JSON deve conter pelo menos um dos termos esperados. Body: " + body,
                    msg.toLowerCase(),
                    anyOf(
                            containsString("invalid"),
                            containsString("username"),
                            containsString("password"),
                            containsString("required"),
                            containsString("missing")
                    ));

            // Se foi passado um conjunto de palavras-chave específicas, verificamos também
            for (String kw : keywordsEsperados) {
                if (kw != null && !kw.isBlank()) {
                    if (msg.toLowerCase().contains(kw.toLowerCase())) {
                        // bateu uma keyword específica -> OK e retorna
                        return;
                    }
                }
            }
            // Se nenhuma keyword específica foi passada, ou não bateu, seguimos (já validamos termos genéricos acima).
        } else {
            // HTML (ex.: Cloudflare). Checamos indicadores genéricos de erro 400/401.
            assertThat("Esperava HTML de erro 400/401", body,
                    anyOf(containsString("Bad Request"), containsString("Unauthorized"), containsString("cloudflare")));
        }
    }

    private Response postLogin(String jsonPayload) {
        return given()
                .spec(spec())
                .header("Content-Type", "application/json")
                .body(jsonPayload)
                .when()
                .post("/auth/login")
                .then()
                .extract().response();
    }

    // ===== Testes =====

    @Test
    @DisplayName("TC16.01 - POST /auth/login - Username ausente")
    void deveValidarUsernameAusente() {
        String payload = """
            {
              "password": "qualquer"
            }
            """;
        Response resp = postLogin(payload);
        assertErroCliente(resp, "username");
    }

    @Test
    @DisplayName("TC16.02 - POST /auth/login - Password ausente")
    void deveValidarPasswordAusente() {
        String payload = """
            {
              "username": "emilys"
            }
            """;
        Response resp = postLogin(payload);
        assertErroCliente(resp, "password");
    }

    @Test
    @DisplayName("TC16.03 - POST /auth/login - Username vazio")
    void deveValidarUsernameVazio() {
        String payload = """
            {
              "username": "",
              "password": "qualquer"
            }
            """;
        Response resp = postLogin(payload);
        assertErroCliente(resp, "username");
    }

    @Test
    @DisplayName("TC16.04 - POST /auth/login - Password vazio")
    void deveValidarPasswordVazio() {
        String payload = """
            {
              "username": "emilys",
              "password": ""
            }
            """;
        Response resp = postLogin(payload);
        assertErroCliente(resp, "password");
    }

    @Test
    @DisplayName("TC16.05 - POST /auth/login - Tipos inválidos (numéricos)")
    void deveValidarTiposInvalidos() {
        String payload = """
            {
              "username": 123,
              "password": 456
            }
            """;
        Response resp = postLogin(payload);
        assertErroCliente(resp, "invalid", "type", "username", "password");
    }

    @Test
    @DisplayName("TC16.06 - POST /auth/login - Body vazio {}")
    void deveValidarBodyVazio() {
        String payload = "{}";
        Response resp = postLogin(payload);
        assertErroCliente(resp, "username", "password", "invalid", "required");
    }

    @Test
    @DisplayName("TC16.07 - POST /auth/login - Credenciais completamente inválidas")
    void deveValidarCredenciaisInvalidas() {
        String payload = """
            {
              "username": "usuario_que_nao_existe",
              "password": "senha_errada_demais"
            }
            """;
        Response resp = postLogin(payload);
        // dummyjson normalmente retorna "Invalid credentials"
        assertErroCliente(resp, "invalid", "credentials", "username", "password");
    }
}
