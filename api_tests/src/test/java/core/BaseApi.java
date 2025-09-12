package core;

import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;

public class BaseApi {
    protected static final String BASE_URL = "https://dummyjson.com";

    protected RequestSpecification spec() {
        return given()
                .relaxedHTTPSValidation()       // ignora validação SSL (apenas para teste local)
                .baseUri(BASE_URL)
                .header("Accept", "application/json")
                .log().all();
    }
}
