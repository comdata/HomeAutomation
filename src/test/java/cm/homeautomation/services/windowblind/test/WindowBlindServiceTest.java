package cm.homeautomation.services.windowblind.test;

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
public class WindowBlindServiceTest {
    //@Test
    public void testWindowBlindGetAll() {
        given()
          .when().get("/windowBlinds/getAll")
          .then()
             .statusCode(200)
             .body(is("{\"windowBlinds\":[]}"));
    }

    //@Test
    public void testWindowBlindGetAllWithEntity() {
        EntityManager em = EntityManagerService.getManager();

        em.getTransaction().begin();

        Room room = new Room();

        room.setRoomName("WindowBlind Test Room");
        em.persist(room);

        WindowBlind windowBlind = new WindowBlind();

        windowBlind.setName("Test");
        windowBlind.setRoom(room);

        em.persist(windowBlind);
        em.getTransaction().commit();


        given()
          .when().get("/windowBlinds/getAll")
          .then()
             .statusCode(200)
             .body(containsString("{\"windowBlinds\":[{\"id\":"));

        em.getTransaction().begin();

        em.createQuery("delete from WindowBlind w where w.name=:name").setParameter("name", windowBlind.getName()).executeUpdate();
        em.createQuery("delete from Room r where r.roomName=:name").setParameter("name", room.getRoomName()).executeUpdate();

        em.getTransaction().commit();

    }
}