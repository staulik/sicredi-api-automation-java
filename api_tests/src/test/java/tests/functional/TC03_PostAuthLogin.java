package tests.functional;

import core.BaseApi;
import core.Session;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TC03_PostAuthLogin extends BaseApi {

    @Test
    void deveAutenticarUsuarioERetornarToken() {
        String body = """
            {
                "username": "emilys",
                "password": "emilyspass"
            }
            """;

        Response resp = spec()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/auth/login")
                .then()
                .extract()
                .response();

        int status = resp.statusCode();
        System.out.println("\n=== RESPOSTA /auth/login (status " + status + ") ===");
        System.out.println(resp.asPrettyString());

        // aceita 200 (observado) ou 201 (documentado)
        assertTrue(status == 200 || status == 201, "Status deve ser 200 ou 201. Recebido: " + status);

        // tenta pegar token em diferentes chaves
        String token = resp.jsonPath().getString("token");
        if (token == null || token.isBlank()) {
            token = resp.jsonPath().getString("accessToken"); // fallback, se a API usar outro nome
        }

        String username = resp.jsonPath().getString("username");
        if (username == null) {
            // algumas variantes retornam dentro de um objeto "user"
            username = resp.jsonPath().getString("user.username");
        }

        assertEquals("emilys", username, "Username retornado não confere!");
        assertNotNull(token, "Token não deve ser nulo! Body: " + resp.asPrettyString());

        // guarda para próximos testes autenticados
        Session.setToken(token);

        System.out.println("\n=== LOGIN OK ===");
        System.out.println("Username: " + username);
        System.out.println("Token: " + token);
    }
}
