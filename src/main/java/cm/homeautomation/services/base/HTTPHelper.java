package cm.homeautomation.services.base;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.context.ManagedExecutor;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

/**
 * http helper class with credentials
 *
 * @author christoph
 *
 */
@ApplicationScoped
public class HTTPHelper {

	@Inject
	ManagedExecutor executor;

	public void performHTTPRequest(String url) {

		final Runnable httpRequestThread = () -> {
			try {

				HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
				HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(url));
				String rawResponse = request.execute().parseAsString();
				System.out.println(rawResponse);
//				//LogManager.getLogger(HTTPHelper.class).debug("http called done: " + httpResponse.getStatusLine());

			} catch (final IOException e) {
//				//LogManager.getLogger(HTTPHelper.class).error("calling URL: " + url + " failed", e);
			}
		};
		executor.runAsync(httpRequestThread);

	}
}
