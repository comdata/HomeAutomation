package cm.homeautomation.services.manualtask.test;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ManualTaskServiceTest {

	//@Test
	void testGetAllOpenBasicTest() {

		given().when().get("/manualtask/getAllOpen").then().statusCode(200);
	}

}
