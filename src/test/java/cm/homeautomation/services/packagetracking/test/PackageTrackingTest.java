package cm.homeautomation.services.packagetracking.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.entities.Package;
import cm.homeautomation.entities.PackageHistory;
import cm.homeautomation.entities.PackageHistoryPK;
import cm.homeautomation.entities.PackagePK;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class PackageTrackingTest {
	
	@Inject
	EntityManager em;
	
	@Inject
	ConfigurationService configurationService;
	
    @Test
    void testPackagesGetAllOpen() {
        given().when().get("/packages/getAllOpen").then().statusCode(200).body(is("[]"));
    }

    @Test
    void testPackagesGetAllOpenWithEntity() {
        

        

        Package pack = new Package();
        pack.setPackageName("Test Package");
        pack.setCarrierName("dp");
        pack.setId(new PackagePK("test", "dp"));
        List<PackageHistory> packageHistory = new ArrayList<>();
        pack.setPackageHistory(packageHistory);
        em.persist(pack);
        

        given().when().get("/packages/getAllOpen").then().statusCode(200)
                .body(containsString("\"" + pack.getPackageName() + "\""));

        

        em.createQuery("delete from Package p where p.packageName=:name").setParameter("name", pack.getPackageName())
                .executeUpdate();

        
    }

    @Test
    void testPackagesGetAllOpenWithEntityAndHistory() {
        

        

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
        

        given()
          .when().get("/packages/getAllOpen")
          .then()
             .statusCode(200)
             .body(containsString("\""+pack.getPackageName()+"\""));

        

        em.createQuery("delete from PackageHistory p").executeUpdate();
        em.createQuery("delete from Package p where p.packageName=:name").setParameter("name", pack.getPackageName()).executeUpdate();

        
    }
}