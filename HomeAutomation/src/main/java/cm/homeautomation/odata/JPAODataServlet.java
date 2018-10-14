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
import javax.persistence.EntityManagerFactory;
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
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.processor.core.api.JPAODataGetHandler;

import cm.homeautomation.db.EntityManagerService;

public class JPAODataServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String PUNIT_NAME = "HA";
	private static String[] packages = { "cm.homeautomation.entities" };

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			EntityManagerFactory emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME,
					new HashMap<String, Object>());
			JPAEdmProvider metadataProvider = new JPAEdmProvider(PUNIT_NAME, emf, null, packages);
			OData odata = OData.newInstance();
			ServiceMetadata edm = odata.createServiceMetadata(metadataProvider, new ArrayList<EdmxReference>());
			ODataHttpHandler handler = odata.createHandler(edm);
			handler.process(req, resp);
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

			EntityManagerFactory emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME,
					new HashMap<String, Object>());
			JPAEdmProvider metadataProvider = new JPAEdmProvider(PUNIT_NAME, emf, null, packages);

			OData odata = OData.newInstance();
			ServiceMetadata edm = odata.createServiceMetadata(metadataProvider, new ArrayList<EdmxReference>());
			ODataHttpHandler handler = odata.createHandler(edm);
			ODataRequest request = new ODataRequest();
			request.setMethod(HttpMethod.GET);
			request.setRawBaseUri("http://localhost/HomeAutomation/JPAOData.svc/$metadata");
			request.setRawODataPath("$metadata");
			request.setProtocol("HTTP/1.1");

			ODataResponse process = handler.process(request);
			InputStream content = process.getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(content));
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}

		} catch (RuntimeException e) {

		} catch (ODataException e) {

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
