package cm.homeautomation.services.hueinterface.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.HueDevice;
import cm.homeautomation.services.hueinterface.HueEmulatorMessage;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class HueInterfaceTest {

	@Test
	void testBasicSend() {
        EntityManager em = EntityManagerService.getManager();

		HueEmulatorMessage hueEmulatorMessage = new HueEmulatorMessage();

        String deviceName = "basic test device";
        String lightId="Test Light 1";
        hueEmulatorMessage.setDeviceName(deviceName);
        hueEmulatorMessage.setLightId(lightId);

		given().when().contentType(ContentType.JSON)
            .body(hueEmulatorMessage).post("/hueInterface/send").then().statusCode(200)
                .body(containsString("true"));
                
        List<HueDevice> resultList = em.createQuery("select h from HueDevice h where h.name=:name", HueDevice.class).setParameter("name", deviceName).getResultList();

        assertNotNull(resultList);
        assertTrue(resultList.size()==1);
        assertTrue(deviceName.equals(resultList.get(0).getName()));
    }
    
    @Test
	void testMissingLightId() {
    
		HueEmulatorMessage hueEmulatorMessage = new HueEmulatorMessage();

        String deviceName = "basic test device";
     
        hueEmulatorMessage.setDeviceName(deviceName);
     

		given().when().contentType(ContentType.JSON)
            .body(hueEmulatorMessage).post("/hueInterface/send").then().statusCode(200)
                .body(containsString("false"));
    }
}
