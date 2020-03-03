package cm.homeautomation.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.LogManager;

public final class HttpProxyServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2746460197982900999L;
	private URL url;
	private HttpClient proxy;

	@Override
	public void init(final ServletConfig config) throws ServletException {

		super.init(config);

		try {
			url = new URL(config.getInitParameter("url"));
		} catch (MalformedURLException me) {
			throw new ServletException("Proxy URL is invalid", me);
		}
		proxy = HttpClientBuilder.create().build();

	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		@SuppressWarnings("unchecked")
		Map<String, String[]> requestParameters = ((Map<String, String[]>) request.getParameterMap());

		StringBuilder query = new StringBuilder();
		for (Entry<String, String[]> entry : requestParameters.entrySet()) {

			String name = entry.getKey();

			for (String value : entry.getValue()) {
				try {
					if (query.length() == 0) {
						query.append("?");
					} else {
						query.append("&");
					}

					name = URLEncoder.encode(name, "UTF-8");
					value = URLEncoder.encode(value, "UTF-8");

					query.append(String.format("&%s=%s", name, value));
				} catch (UnsupportedEncodingException e) {
					LogManager.getLogger(this.getClass()).error("unsupported encoding " + name + " - " + value, e);
				}
			}
		}

		String uri = String.format("%s%s", url.toString(), query.toString());
		HttpGet proxyMethod = new HttpGet(uri);

		try {
			HttpResponse httpResponse = proxy.execute(proxyMethod);

			Header[] responseHeaders = proxyMethod.getAllHeaders();
			for (Header header : responseHeaders) {
				response.setHeader(header.getName(), header.getValue());
			}

			write(httpResponse.getEntity().getContent(), response.getOutputStream());
		} catch (IOException e) {
			LogManager.getLogger(this.getClass()).error("IO Exception while writing response", e);
		} catch (UnsupportedOperationException e) {
			LogManager.getLogger(this.getClass()).error("UnsupportedOperationException while writing response", e);
		}
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		@SuppressWarnings("unchecked")
		Map<String, String[]> requestParameters = request.getParameterMap();

		String uri = url.toString();
		HttpPost proxyMethod = new HttpPost(uri);
		for (Entry<String, String[]> entry : requestParameters.entrySet()) {
			for (String value : entry.getValue()) {

				proxyMethod.getParams().setParameter(entry.getKey(), value);
			}
		}

		try {
			HttpResponse httpResponse = proxy.execute(proxyMethod);
			write(httpResponse.getEntity().getContent(), response.getOutputStream());
		} catch (IOException e) {
			LogManager.getLogger(this.getClass()).error("IO Exception while writing response", e);
		} catch (UnsupportedOperationException e) {
			LogManager.getLogger(this.getClass()).error("UnsupportedOperationException while writing response", e);
		}
		proxyMethod.releaseConnection();
	}

	private void write(final InputStream inputStream, final OutputStream outputStream) throws IOException {
		int b;
		while ((b = inputStream.read()) != -1) {
			outputStream.write(b);
		}

		outputStream.flush();
	}

	@Override
	public String getServletInfo() {
		return "Http Proxy Servlet";
	}
}