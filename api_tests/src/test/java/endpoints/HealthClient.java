package endpoints;

import core.BaseApi;
import io.restassured.response.Response;

public class HealthClient extends BaseApi {
    public Response getStatus() {
        return spec().get("/test");
    }
}
