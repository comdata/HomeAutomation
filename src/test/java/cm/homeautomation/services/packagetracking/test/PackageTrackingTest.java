package cm.homeautomation.services.packagetracking.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Package;
import cm.homeautomation.entities.PackageHistory;
import cm.homeautomation.entities.PackageHistoryPK;
import cm.homeautomation.entities.PackagePK;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class PackageTrackingTest {
    @Test
    void testPackagesGetAllOpen() {
        given().when().get("/packages/getAllOpen").then().statusCode(200).body(is("[]"));
    }

    @Test
    void testPackagesGetAllOpenWithEntity() {
        EntityManager em = EntityManagerService.getManager();

        em.getTransaction().begin();

        Package pack = new Package();
        pack.setPackageName("Test Package");
        pack.setCarrierName("dp");
        pack.setId(new PackagePK("test", "dp"));
        List<PackageHistory> packageHistory = new ArrayList<>();
        pack.setPackageHistory(packageHistory);
        em.persist(pack);
        em.getTransaction().commit();

        given().when().get("/packages/getAllOpen").then().statusCode(200)
                .body(containsString("\"" + pack.getPackageName() + "\""));

        em.getTransaction().begin();

        em.createQuery("delete from Package p where p.packageName=:name").setParameter("name", pack.getPackageName())
                .executeUpdate();

        em.getTransaction().commit();
    }

    @Test
    void testPackagesGetAllOpenWithEntityAndHistory() {
        EntityManager em = EntityManagerService.getManager();

        em.getTransaction().begin();

        Package pack = new Package();
        pack.setPackageName("Test Package");
        pack.setCarrierName("dp");
        PackagePK packId = new PackagePK("test", "dp");
        pack.setId(packId);
        List<PackageHistory> packageHistory = new ArrayList<>();

        PackageHistory history = new PackageHistory();
        PackageHistoryPK newHistoryId=new PackageHistoryPK(packId.getTrackingNumber(), packId.getCarrier(), new Date(), "Test");
        history.setTrackedPackage(pack);

        history.setId(newHistoryId);

        packageHistory.add(history);

        pack.setPackageHistory(packageHistory);
        em.persist(history);
        em.persist(pack);
        em.getTransaction().commit();

        given()
          .when().get("/packages/getAllOpen")
          .then()
             .statusCode(200)
             .body(containsString("\""+pack.getPackageName()+"\""));

        em.getTransaction().begin();

        em.createQuery("delete from PackageHistory p").executeUpdate();
        em.createQuery("delete from Package p where p.packageName=:name").setParameter("name", pack.getPackageName()).executeUpdate();

        em.getTransaction().commit();
    }
}