package cm.homeautomation.odata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.eclipse.persistence.internal.jpa.jdbc.DataSourceImpl;
import org.flywaydb.core.internal.jdbc.DriverDataSource;
import org.mariadb.jdbc.MariaDbPoolDataSource;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGetHandler;

import cm.homeautomation.db.EntityManagerService;

public class JPAODataServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String PUNIT_NAME = "HA";

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String url = "jdbc:mariadb://localhost:3306/HA?characterEncoding=utf8";
			
			MariaDbPoolDataSource ds = new MariaDbPoolDataSource(url);
			ds.setUser("root");

			JPAODataGetHandler handler = new JPAODataGetHandler(PUNIT_NAME, ds);

			handler.process(req, resp);
		} catch (SQLException e) {

			throw new ServletException(e);
		}

		catch (RuntimeException e) {
			throw new ServletException(e);
		} catch (ODataException e) {
			throw new ServletException(e);
		}
	}

	public static void main(String[] args) throws NamingException {
		try {
			String url = "jdbc:mariadb://localhost:3306/HA?characterEncoding=utf8";
			
			MariaDbPoolDataSource ds = new MariaDbPoolDataSource(url);
			ds.setUser("root");

			JPAODataGetHandler handler = new JPAODataGetHandler(PUNIT_NAME, ds);

			HttpServletRequest request = new HttpServletRequest() {
				private final Map<String, String[]> params = new HashMap<>();

				public Map<String, String[]> getParameterMap() {
					return params;
				}

				public String getParameter(String name) {
					String[] matches = params.get(name);
					if (matches == null || matches.length == 0)
						return null;
					return matches[0];
				}

				@Override
				public Object getAttribute(String name) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Enumeration getAttributeNames() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getCharacterEncoding() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
					// TODO Auto-generated method stub

				}

				@Override
				public int getContentLength() {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public String getContentType() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public ServletInputStream getInputStream() throws IOException {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Enumeration getParameterNames() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String[] getParameterValues(String name) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getProtocol() {
					// TODO Auto-generated method stub
					return "HTTP/1.1";
				}

				@Override
				public String getScheme() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getServerName() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public int getServerPort() {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public BufferedReader getReader() throws IOException {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getRemoteAddr() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getRemoteHost() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public void setAttribute(String name, Object o) {
					// TODO Auto-generated method stub

				}

				@Override
				public void removeAttribute(String name) {
					// TODO Auto-generated method stub

				}

				@Override
				public Locale getLocale() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Enumeration getLocales() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public boolean isSecure() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public RequestDispatcher getRequestDispatcher(String path) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getRealPath(String path) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public int getRemotePort() {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public String getLocalName() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getLocalAddr() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public int getLocalPort() {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public String getAuthType() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Cookie[] getCookies() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public long getDateHeader(String name) {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public String getHeader(String name) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Enumeration getHeaders(String name) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Enumeration getHeaderNames() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public int getIntHeader(String name) {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public String getMethod() {

					return "GET";
				}

				@Override
				public String getPathInfo() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getPathTranslated() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getContextPath() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getQueryString() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getRemoteUser() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public boolean isUserInRole(String role) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public Principal getUserPrincipal() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getRequestedSessionId() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getRequestURI() {
					// TODO Auto-generated method stub
					return "/HomeAutomation/JPAOData.svc/$metadata";
				}

				@Override
				public StringBuffer getRequestURL() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getServletPath() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public HttpSession getSession(boolean create) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public HttpSession getSession() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public boolean isRequestedSessionIdValid() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean isRequestedSessionIdFromCookie() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean isRequestedSessionIdFromURL() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean isRequestedSessionIdFromUrl() {
					// TODO Auto-generated method stub
					return false;
				}

			};

			HttpServletResponse response = new HttpServletResponse() {

				@Override
				public void setLocale(Locale loc) {
					// TODO Auto-generated method stub

				}

				@Override
				public void setContentType(String type) {
					// TODO Auto-generated method stub

				}

				@Override
				public void setContentLength(int len) {
					// TODO Auto-generated method stub

				}

				@Override
				public void setCharacterEncoding(String charset) {
					// TODO Auto-generated method stub

				}

				@Override
				public void setBufferSize(int size) {
					// TODO Auto-generated method stub

				}

				@Override
				public void resetBuffer() {
					// TODO Auto-generated method stub

				}

				@Override
				public void reset() {
					// TODO Auto-generated method stub

				}

				@Override
				public boolean isCommitted() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public PrintWriter getWriter() throws IOException {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public ServletOutputStream getOutputStream() throws IOException {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Locale getLocale() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getContentType() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getCharacterEncoding() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public int getBufferSize() {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public void flushBuffer() throws IOException {
					// TODO Auto-generated method stub

				}

				@Override
				public void setStatus(int sc, String sm) {
					// TODO Auto-generated method stub

				}

				@Override
				public void setStatus(int sc) {
					// TODO Auto-generated method stub

				}

				@Override
				public void setIntHeader(String name, int value) {
					// TODO Auto-generated method stub

				}

				@Override
				public void setHeader(String name, String value) {
					// TODO Auto-generated method stub

				}

				@Override
				public void setDateHeader(String name, long date) {
					// TODO Auto-generated method stub

				}

				@Override
				public void sendRedirect(String location) throws IOException {
					// TODO Auto-generated method stub

				}

				@Override
				public void sendError(int sc, String msg) throws IOException {
					// TODO Auto-generated method stub

				}

				@Override
				public void sendError(int sc) throws IOException {
					// TODO Auto-generated method stub

				}

				@Override
				public String encodeUrl(String url) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String encodeURL(String url) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String encodeRedirectUrl(String url) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String encodeRedirectURL(String url) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public boolean containsHeader(String name) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void addIntHeader(String name, int value) {
					// TODO Auto-generated method stub

				}

				@Override
				public void addHeader(String name, String value) {
					// TODO Auto-generated method stub

				}

				@Override
				public void addDateHeader(String name, long date) {
					// TODO Auto-generated method stub

				}

				@Override
				public void addCookie(Cookie cookie) {
					// TODO Auto-generated method stub

				}
			};
			handler.process(request, response);

		} catch (RuntimeException e) {

		} catch (ODataException e) {

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
