package tests.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.BaseApi;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

public class TC07_PostAddProductExternal extends BaseApi {

    private static final ObjectMapper M = new ObjectMapper();

    /** Tenta carregar um recurso do classpath por vários caminhos. */
    private String readFirstAvailable(String... resourcePaths) throws Exception {
        for (String path : resourcePaths) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                if (is != null) {
                    return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        throw new IllegalStateException(
                "Nenhum recurso encontrado. Tente um destes caminhos no classpath:\n - data/produtos.json\n - data/produto.json\n" +
                        "Coloque o arquivo em: src/test/resources/data/ e marque 'src/test/resources' como Test Resources Root no IntelliJ."
        );
    }

    /** Aceita JSON array ou objeto único e devolve lista de maps. */
    private List<Map<String, Object>> toListOfMaps(String json) throws Exception {
        JsonNode root = M.readTree(json);
        List<Map<String, Object>> out = new ArrayList<>();
        if (root.isArray()) {
            for (JsonNode n : root) out.add(M.convertValue(n, LinkedHashMap.class));
        } else if (root.isObject()) {
            out.add(M.convertValue(root, LinkedHashMap.class));
        } else {
            throw new IllegalArgumentException("JSON de massa inválido: esperado objeto ou array.");
        }
        return out;
    }

    @Test
    public void deveCriarProdutosComMassaExterna() throws Exception {
        // Tenta ambos: data/produtos.json (lista) ou data/produto.json (único)
        String json = readFirstAvailable("data/produtos.json", "data/produto.json");
        List<Map<String, Object>> produtos = toListOfMaps(json);

        for (Map<String, Object> produto : produtos) {
            var resp = given()
                    .spec(spec()) // BaseApi: relaxedHTTPSValidation + baseUri
                    .header("Content-Type", "application/json")
                    .body(produto)
                    .when()
                    .post("/products/add")
                    .then()
                    .statusCode(anyOf(equalTo(200), equalTo(201)))
                    .extract().response();

            assertThat(resp.jsonPath().getString("title"),
                    equalTo(String.valueOf(produto.get("title"))));
        }
    }
}
