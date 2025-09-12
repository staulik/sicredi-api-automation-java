package tests.functional;

import core.BaseApi;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TC02_GetUsers extends BaseApi {

    @Test
    public void deveRetornarUsuarios() {
        Response resp = spec()
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .response();

        // >>> imprime o corpo bonito no console
        System.out.println("\n=== BODY /users ===");
        System.out.println(resp.asPrettyString());

        // Validações mínimas
        assertNotNull(resp.jsonPath().getList("users"), "Lista 'users' não deve ser nula");
        assertTrue(resp.jsonPath().getList("users").size() > 0, "Lista 'users' deve ter itens");

        // Regras de negócio: username e password existem
        String username = resp.jsonPath().getString("users[0].username");
        String password = resp.jsonPath().getString("users[0].password");
        assertNotNull(username, "Campo 'username' deve existir no primeiro usuário");
        assertNotNull(password, "Campo 'password' deve existir no primeiro usuário");

        System.out.printf("Primeiro usuário -> username: %s | password: %s%n", username, password);
    }
}
