package cm.homeautomation.services.base;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Room;
import cm.homeautomation.entities.Sensor;

public class JerseyApplication extends Application {
	
	public JerseyApplication() {
		super();
	}
	
	private void setupTestData() {
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

	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> classes = new HashSet<Class<?>>();
		// register resources and features
		classes.add(MultiPartFeature.class);
		classes.add(JacksonFeature.class);
		return classes;
	}
}
