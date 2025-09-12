package tests.functional;

import endpoints.HealthClient;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TC01_GetTest {
    private final HealthClient health = new HealthClient();

    @Test
    void deveRetornarOk() {
        var resp = health.getStatus();
        assertEquals(200, resp.statusCode(), "Status HTTP deve ser 200");

        JsonPath json = resp.jsonPath();
        assertEquals("ok", json.getString("status"));
        assertEquals("GET", json.getString("method"));

        System.out.println("Resposta: " + resp.asString());
    }
}
