package cm.homeautomation.services.windowblind.test;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class WindowBlindServiceTest {
    @Test
    public void testWindowBlindGetAll() {
        given()
          .when().get("/windowBlinds/getAll")
          .then()
             .statusCode(200)
             .body(is("{}"));
    }
}