package cm.homeautomation.services.led;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;

import cm.homeautomation.services.base.BaseService;

@Path("led/")
public class LEDService extends BaseService {

	@GET
	@Path("set/{roomId}/{r}/{g}/{b}")
	public void setLed(@PathParam("r") int r, @PathParam("g") int g, @PathParam("b") int b) {

		try {
			String url = "http://192.168.1.38:80/?mode=SET&red=" + r + "&green=" + g + "&blue=" + b;
			HttpGet getMethod = new HttpGet(url);
			HttpClientBuilder clientBuilder = HttpClientBuilder.create();

			HttpClient httpClient = clientBuilder.build();

			httpClient.execute(getMethod);
		} catch (IOException e) {
//			LogManager.getLogger(this.getClass()).error("IO Exception while calling LED service.", e);
		}
	}
}
