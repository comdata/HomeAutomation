package cm.homeautomation.services.hueinterface.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.jupiter.api.Test;

import cm.homeautomation.services.hueinterface.HueEmulatorMessage;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class HueInterfaceTest {

	@Test
	void testBasicSend() {

		HueEmulatorMessage hueEmulatorMessage = new HueEmulatorMessage();

		hueEmulatorMessage.setDeviceName("basic test device");

		given().when().post("/hueInterface/send", hueEmulatorMessage).then().statusCode(200)
				.body(containsString("true"));
	}

}
