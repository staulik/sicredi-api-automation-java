package tests.functional;

import core.BaseApi;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TC04_UserAuthFieldsTest extends BaseApi {

    @Test
    void deveValidarUsernamePassword() {
        Response resp = spec()                  // aplica relaxedHTTPSValidation + baseUri + Accept JSON
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<String> usernames = resp.jsonPath().getList("users.username");
        List<String> passwords = resp.jsonPath().getList("users.password");

        assertNotNull(usernames, "Lista de usernames não deve ser nula");
        assertFalse(usernames.isEmpty(), "Lista de usernames está vazia!");
        assertNotNull(passwords, "Lista de passwords não deve ser nula");
        assertFalse(passwords.isEmpty(), "Lista de passwords está vazia!");

        System.out.println("\n=== USERNAMES ===");
        System.out.println(usernames);
        System.out.println("=== PASSWORDS ===");
        System.out.println(passwords);
    }
}

