package cm.homeautomation.planes;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import cm.homeautomation.sensors.base.TechnicalSensor;

public class PlaneSensor implements TechnicalSensor {

	private String url = "http://192.168.1.33:8080/data.json";
	private CloseableHttpClient httpClient;
	private HttpGet httpGet;
	private String type;
	private static final String HTTP_STATUS_OK = "HTTP/1.1 200";

	public PlaneSensor() {
		httpClient = HttpClients.createDefault();
		httpGet = new HttpGet(url);
	}

	public PlaneSensor(String technicalType, String url) {
		this.url=url;
		this.type=technicalType;
		httpClient = HttpClients.createDefault();
		httpGet = new HttpGet(url);
	}

	@Override
	public String getValue() {

		try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {

			if (!HTTP_STATUS_OK.equals(httpResponse.getStatusLine().toString().subSequence(0, HTTP_STATUS_OK.length()))) {
				System.out.println("HTTP error while fetching flight data from dump1090: [" + httpResponse.getStatusLine() + "]");
				return "0";
			}
			
			 String json = EntityUtils.toString(httpResponse.getEntity());
			

			ObjectMapper mapper = new ObjectMapper();
			try {
				List<PlaneData> planeList = mapper.readValue(json,
						TypeFactory.defaultInstance().constructCollectionType(List.class, PlaneData.class));

				for (PlaneData planeData : planeList) {
					System.out.println("Seen Plane:"+ planeData.getFlight());
				}

				return Integer.toString(planeList.size());

			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return "0";
	}

	@Override
	public String getType() {
		return type;
	}

	public static void main(String[] args) {
		new PlaneSensor().getValue();
	}

}
