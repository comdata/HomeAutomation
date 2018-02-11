package cm.homeautomation.services.base;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.LogManager;

/**
 * http helper class with credentials
 *
 * @author christoph
 *
 */
public class HTTPHelper {
	public static void performHTTPRequest(String url) {

		final Runnable httpRequestThread = () -> {
			try {
				LogManager.getLogger(HTTPHelper.class).debug("perform http call: " + url);

				final HttpGet getMethod = new HttpGet(url);
				final HttpClientBuilder clientBuilder = HttpClientBuilder.create();

				final String[] userPassword = url.split("@")[0].replace("http://", "").split(":");

				final CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(new AuthScope(getMethod.getURI().getHost(), getMethod.getURI().getPort()),
						new UsernamePasswordCredentials(userPassword[0], userPassword[1]));
				clientBuilder.setDefaultCredentialsProvider(credsProvider);
				final HttpClient httpClient = clientBuilder.build();

				final HttpResponse httpResponse = httpClient.execute(getMethod);

				LogManager.getLogger(HTTPHelper.class).debug("http called done: " + httpResponse.getStatusLine());

			} catch (final IOException e) {
				LogManager.getLogger(HTTPHelper.class).error("calling URL: " + url + " failed", e);
			}
		};
		new Thread(httpRequestThread).start();

	}
}
