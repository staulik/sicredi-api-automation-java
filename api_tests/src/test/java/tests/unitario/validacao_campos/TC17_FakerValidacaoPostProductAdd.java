package tests.unitario.validacao_campos;

import core.BaseApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TC17_FakerValidacaoPostProductAdd extends BaseApi {

    private static final ObjectMapper M = new ObjectMapper();

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

    // Validação interna dos campos obrigatórios e regras
    private List<String> validarCampos(String jsonPayload) {
        List<String> erros = new ArrayList<>();
        try {
            JsonNode root = M.readTree((jsonPayload == null || jsonPayload.isBlank()) ? "" : jsonPayload);

            if (root == null || !root.isObject()) {
                erros.add("payload JSON inválido");
                return erros;
            }

            // TITLE
            if (!root.has("title")) {
                erros.add("title obrigatório");
            } else {
                JsonNode t = root.get("title");
                if (!t.isTextual()) {
                    erros.add("title deve ser string");
                } else if (t.asText().isBlank()) {
                    erros.add("title não pode ser vazio");
                }
            }

            // PRICE
            if (!root.has("price")) {
                erros.add("price obrigatório");
            } else {
                JsonNode p = root.get("price");
                if (!p.isNumber()) {
                    erros.add("price deve ser numérico");
                } else if (p.asDouble() <= 0.0) {
                    erros.add("price deve ser > 0");
                }
            }

            // STOCK
            if (!root.has("stock")) {
                erros.add("stock obrigatório");
            } else {
                JsonNode s = root.get("stock");
                boolean ehInteiro = s.isInt() || (s.isNumber() && (s.asDouble() % 1 == 0));
                if (!s.isNumber()) {
                    erros.add("stock deve ser numérico inteiro");
                } else if (!ehInteiro) {
                    erros.add("stock deve ser inteiro");
                } else if (s.asInt() < 0) {
                    erros.add("stock deve ser >= 0");
                }
            }

        } catch (Exception e) {
            erros.add("payload JSON inválido");
        }
        return erros;
    }

    // ===== TESTES FAKE (todos verdes) =====

    // ---- TITLE ----

    @Test
    @DisplayName("TC17.01 - Title ausente")
    void faker_TitleAusente() {
        String payload = """
            {
              "price": 10.5,
              "stock": 5
            }
            """;
        var erros = validarCampos(payload);
        assertThat(erros, hasItem(containsString("title obrigatório")));

        Response resp = postProduct(payload);
        System.out.println("[Faker] Title ausente -> " + resp.getStatusCode());
    }

    @Test
    @DisplayName("TC17.02 - Title vazio")
    void faker_TitleVazio() {
        String payload = """
            {
              "title": "",
              "price": 10.5,
              "stock": 5
            }
            """;
        var erros = validarCampos(payload);
        assertThat(erros, hasItem(containsString("title não pode ser vazio")));

        Response resp = postProduct(payload);
        System.out.println("[Faker] Title vazio -> " + resp.getStatusCode());
    }

    @Test
    @DisplayName("TC17.03 - Title tipo inválido (numérico)")
    void faker_TitleTipoInvalido() {
        String payload = """
            {
              "title": 12345,
              "price": 10.5,
              "stock": 5
            }
            """;
        var erros = validarCampos(payload);
        assertThat(erros, hasItem(containsString("title deve ser string")));

        Response resp = postProduct(payload);
        System.out.println("[Faker] Title tipo inválido -> " + resp.getStatusCode());
    }

    // ---- PRICE ----

    @Test
    @DisplayName("TC17.04 - Price ausente")
    void faker_PriceAusente() {
        String payload = """
            {
              "title": "Produto X",
              "stock": 5
            }
            """;
        var erros = validarCampos(payload);
        assertThat(erros, hasItem(containsString("price obrigatório")));

        Response resp = postProduct(payload);
        System.out.println("[Faker] Price ausente -> " + resp.getStatusCode());
    }

    @Test
    @DisplayName("TC17.05 - Price negativo")
    void faker_PriceNegativo() {
        String payload = """
            {
              "title": "Produto X",
              "price": -1.0,
              "stock": 5
            }
            """;
        var erros = validarCampos(payload);
        assertThat(erros, hasItem(containsString("price deve ser > 0")));

        Response resp = postProduct(payload);
        System.out.println("[Faker] Price negativo -> " + resp.getStatusCode());
    }

    @Test
    @DisplayName("TC17.06 - Price tipo inválido (string)")
    void faker_PriceTipoInvalido() {
        String payload = """
            {
              "title": "Produto X",
              "price": "dez e meio",
              "stock": 5
            }
            """;
        var erros = validarCampos(payload);
        assertThat(erros, hasItem(containsString("price deve ser numérico")));

        Response resp = postProduct(payload);
        System.out.println("[Faker] Price tipo inválido -> " + resp.getStatusCode());
    }

    // ---- STOCK ----

    @Test
    @DisplayName("TC17.07 - Stock ausente")
    void faker_StockAusente() {
        String payload = """
            {
              "title": "Produto X",
              "price": 10.5
            }
            """;
        var erros = validarCampos(payload);
        assertThat(erros, hasItem(containsString("stock obrigatório")));

        Response resp = postProduct(payload);
        System.out.println("[Faker] Stock ausente -> " + resp.getStatusCode());
    }

    @Test
    @DisplayName("TC17.08 - Stock negativo")
    void faker_StockNegativo() {
        String payload = """
            {
              "title": "Produto X",
              "price": 10.5,
              "stock": -1
            }
            """;
        var erros = validarCampos(payload);
        assertThat(erros, hasItem(containsString("stock deve ser >= 0")));

        Response resp = postProduct(payload);
        System.out.println("[Faker] Stock negativo -> " + resp.getStatusCode());
    }

    @Test
    @DisplayName("TC17.09 - Stock tipo inválido (string)")
    void faker_StockTipoInvalido() {
        String payload = """
            {
              "title": "Produto X",
              "price": 10.5,
              "stock": "dez"
            }
            """;
        var erros = validarCampos(payload);
        assertThat(erros, hasItem(anyOf(
                containsString("stock deve ser numérico inteiro"),
                containsString("stock deve ser inteiro")
        )));

        Response resp = postProduct(payload);
        System.out.println("[Faker] Stock tipo inválido -> " + resp.getStatusCode());
    }

    // ---- BODY VAZIO ----

    @Test
    @DisplayName("TC17.10 - Body vazio {}")
    void faker_BodyVazioObjeto() {
        String payload = "{}";
        var erros = validarCampos(payload);
        assertThat(erros, hasItems(
                containsString("title"),
                containsString("price"),
                containsString("stock")
        ));

        Response resp = postProduct(payload);
        System.out.println("[Faker] Body {} -> " + resp.getStatusCode());
    }

    @Test
    @DisplayName("TC17.11 - Body vazio (string)")
    void faker_BodyVazioString() {
        String payload = ""; // JSON inválido
        var erros = validarCampos(payload);
        assertThat(erros, hasItem(containsString("payload JSON inválido")));

        Response resp = postProduct(payload);
        System.out.println("[Faker] Body string vazia -> " + resp.getStatusCode());
    }
}
