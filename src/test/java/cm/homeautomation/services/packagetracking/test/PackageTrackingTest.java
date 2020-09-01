package cm.homeautomation.services.packagetracking.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Package;
import cm.homeautomation.entities.PackageHistory;
import cm.homeautomation.entities.PackagePK;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class PackageTrackingTest {
    @Test
    public void testPackagesGetAllOpen() {
        given().when().get("/packages/getAllOpen").then().statusCode(200).body(is("[]"));
    }

    @Test
    public void testPackagesGetAllOpenWithEntity() {
        EntityManager em = EntityManagerService.getManager();

        em.getTransaction().begin();

        Package pack = new Package();
        pack.setPackageName("Test Package");
        pack.setCarrierName("dp");
        pack.setId(new PackagePK("test", "dp"));
        List<PackageHistory> packageHistory=new ArrayList<>();
        pack.setPackageHistory(packageHistory);
        em.persist(pack);
        em.getTransaction().commit();

        given()
          .when().get("/packages/getAllOpen")
          .then()
             .statusCode(200)
             .body(containsString("\""+pack.getPackageName()+"\""));

        em.getTransaction().begin();

        em.createQuery("delete from Package p where p.packageName=:name").setParameter("name", pack.getPackageName()).executeUpdate();

        em.getTransaction().commit();
    }
}