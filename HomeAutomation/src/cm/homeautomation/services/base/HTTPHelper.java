package cm.homeautomation.services.base;

import java.io.IOException;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

public class HTTPHelper {
	public static void performHTTPRequest(String url) {
		try {
			System.out.println("perform http call: "+url);
			
			HttpGet getMethod = new HttpGet(url);
			HttpClientBuilder clientBuilder = HttpClientBuilder.create();

			String[] userPassword = url.split("@")[0].replace("http://", "").split(":");

			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(new AuthScope(getMethod.getURI().getHost(), getMethod.getURI().getPort()),
					new UsernamePasswordCredentials(userPassword[0], userPassword[1]));
			clientBuilder.setDefaultCredentialsProvider(credsProvider);
			HttpClient httpClient = clientBuilder.build();

			httpClient.execute(getMethod);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
