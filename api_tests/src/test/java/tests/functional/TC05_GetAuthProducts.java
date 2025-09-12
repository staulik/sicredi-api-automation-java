package tests.functional;

import core.BaseApi;
import core.Session;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TC05_GetAuthProducts extends BaseApi {

    // Garante token mesmo se o teste rodar isolado (sem executar o AuthLoginTest antes)
    private String obterToken() {
        String tk = Session.getToken();
        if (tk != null && !tk.isBlank()) return tk;

        String body = """
            { "username": "emilys", "password": "emilyspass" }
        """;

        Response login = spec()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/auth/login")
                .then()
                .extract().response();

        String token = login.jsonPath().getString("token");
        if (token == null || token.isBlank()) {
            token = login.jsonPath().getString("accessToken"); // fallback
        }
        assertNotNull(token, "Não foi possível obter token de autenticação.");
        Session.setToken(token);
        return token;
    }

    @Test
    void deveListarProdutosAutenticado() {
        String token = obterToken();

        Response resp = spec()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/auth/products")
                .then()
                .extract().response();

        int status = resp.statusCode();
        assertTrue(status == 200 || status == 201, "Status deve ser 200/201. Recebido: " + status);
        assertNotNull(resp.jsonPath().getList("products"), "Lista 'products' não deve ser nula");
        assertFalse(resp.jsonPath().getList("products").isEmpty(), "Lista 'products' não deve ser vazia");

        System.out.println("\n=== /auth/products (ok) ===");
        System.out.println(resp.asPrettyString());
    }

    @Test
    void deveNegarAcessoComTokenInvalido() {
        Response resp = spec()
                .header("Authorization", "Bearer invalido")
                .when()
                .get("/auth/products")
                .then()
                .extract().response();

        int status = resp.statusCode();
        // a API pode responder 401 ou 403 conforme o provider
        assertTrue(status == 401 || status == 403, "Esperado 401/403. Recebido: " + status);

        System.out.println("\n=== /auth/products (token inválido) ===");
        System.out.println(resp.asPrettyString());
    }
}
