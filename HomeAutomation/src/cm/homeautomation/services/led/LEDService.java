package cm.homeautomation.services.led;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import cm.homeautomation.services.base.BaseService;

@Path("led")
public class LEDService extends BaseService {

	@GET
	@Path("set/{r}/{g}/{b}")
	public void setLed(@PathParam("r") int r, @PathParam("g") int g, @PathParam("b") int b) {

		try {
			String url = "http://192.168.1.38:80/?mode=SET&red=" + r + "&green=" + g + "&blue=" + b;
			HttpGet getMethod = new HttpGet(url);
			HttpClientBuilder clientBuilder = HttpClientBuilder.create();

			// String[] userPassword = url.split("@")[0].replace("http://",
			// "").split(":");
			/*
			 * CredentialsProvider credsProvider = new
			 * BasicCredentialsProvider(); credsProvider.setCredentials(new
			 * AuthScope(getMethod.getURI().getHost(),
			 * getMethod.getURI().getPort()), new
			 * UsernamePasswordCredentials(userPassword[0], userPassword[1]));
			 * clientBuilder.setDefaultCredentialsProvider(credsProvider);
			 */
			HttpClient httpClient = clientBuilder.build();

			httpClient.execute(getMethod);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
