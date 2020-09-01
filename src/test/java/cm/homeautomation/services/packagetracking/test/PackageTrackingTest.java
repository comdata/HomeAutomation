package cm.homeautomation.services.packagetracking.test;

import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.WindowBlind;
import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;

import javax.persistence.EntityManager;

@QuarkusTest
public class PackageTrackingTest {
    @Test
    public void testPackagesGetAllOpen() {
        given()
          .when().get("/packages/getAllOpen")
          .then()
             .statusCode(200)
             .body(is("[]"));
    }
}