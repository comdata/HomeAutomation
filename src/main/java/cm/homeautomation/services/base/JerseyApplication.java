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

	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> classes = new HashSet<>();
		// register resources and features
		classes.add(MultiPartFeature.class);
		classes.add(JacksonFeature.class);
		return classes;
	}
}
