package cm.homeautomation.services.initialdata;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Sensor;
import cm.homeautomation.services.base.BaseService;

@Path("initialdata")
public class InitialData extends BaseService{

	@GET
	@Path("load")
	public void createData() {
		EntityManager em = EntityManagerService.getManager();

		em.getTransaction().begin();
		em.createQuery("delete from SensorData" ).executeUpdate();
		em.createQuery("delete from Sensor" ).executeUpdate();
		em.createQuery("delete from Room" ).executeUpdate();
		
		
		em.getTransaction().commit();
		em.getTransaction().begin();
		
		Room room = new Room();
		
		room.setRoomName("Wohnzimmer");
		
		Sensor tempSensor=new Sensor();
		tempSensor.setSensorName("Temperature");
		tempSensor.setSensorPin("18");
		tempSensor.setSensorTechnicalType("11");
		tempSensor.setSensorType("TEMPERATURE");
		tempSensor.setSensorPosition("LOCAL");
		tempSensor.setRoom(room);
		
		room.getSensors().add(tempSensor);
		
		Sensor humSensor=new Sensor();
		humSensor.setSensorName("Humidity");
		humSensor.setSensorPin("18");
		humSensor.setSensorTechnicalType("11");
		humSensor.setSensorType("HUMIDITY");
		humSensor.setSensorPosition("LOCAL");
		humSensor.setRoom(room);
		
		room.getSensors().add(humSensor);
		
		em.persist(room);
		em.getTransaction().commit();
	}
	
}
