package cm.homeautomation.db;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;

import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;

@Startup
public class InfluxDBService {

	@ConfigProperty(name = "influx.server.url")
	String serverURL;

	@ConfigProperty(name = "influx.token")
	String token;

	@ConfigProperty(name = "influx.org")
	String org;

	@ConfigProperty(name = "influx.bucket")
	String bucket;

	@ApplicationScoped
	InfluxDBClient produceService() {
		return InfluxDBClientFactory.create(serverURL, token.toCharArray(), org, bucket);
	}
	

}
