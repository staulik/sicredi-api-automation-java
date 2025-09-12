package tests.functional;

import core.BaseApi;
import core.Session;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TC06_PostAddProduct extends BaseApi {

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
            token = login.jsonPath().getString("accessToken");
        }
        assertNotNull(token, "Não foi possível obter token de autenticação.");
        Session.setToken(token);
        return token;
    }

    @Test
    void deveCriarProduto() {
        String token = obterToken();

        String novoProduto = """
            {
              "title": "Notebook QA",
              "price": 2000,
              "description": "Notebook para testes automatizados",
              "category": "electronics"
            }
        """;

        Response resp = spec()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(novoProduto)
                .when()
                .post("/products/add")
                .then()
                .extract().response();

        int status = resp.statusCode();
        assertTrue(status == 200 || status == 201, "Esperado 200/201. Recebido: " + status);

        // Valida campos básicos
        assertEquals("Notebook QA", resp.jsonPath().getString("title"), "Título incorreto");
        assertEquals(2000, resp.jsonPath().getInt("price"), "Preço incorreto");
        assertNotNull(resp.jsonPath().getInt("id"), "ID não deve ser nulo");

        System.out.println("\n=== Produto criado com sucesso ===");
        System.out.println(resp.asPrettyString());
    }
}
