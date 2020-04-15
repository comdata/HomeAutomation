package cm.homeautomation.odata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.mariadb.jdbc.MariaDbDataSource;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;
import com.sap.olingo.jpa.metadata.api.JPAEntityManagerFactory;
import com.sap.olingo.jpa.processor.core.api.JPAODataBatchProcessor;
import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDHandler;
import com.sap.olingo.jpa.processor.core.api.JPAODataGetHandler;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestProcessor;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;

public class JPAODataServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String PUNIT_NAME = "HA";
	private static String[] packages = null; // { "cm.homeautomation.entities" };
	private ODataHttpHandler metaDataHandler;
	private JPAODataGetHandler getHandler;
	private JPAODataCRUDHandler crudHandler;

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			LogManager.getLogger(this.getClass()).info("JPA OData Bridge");

			EntityManagerFactory emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME,
					new HashMap<String, Object>());
			JPAEdmProvider metadataProvider = new JPAEdmProvider(PUNIT_NAME, emf, null, packages);
			OData odata = OData.newInstance();
			ServiceMetadata edm = odata.createServiceMetadata(metadataProvider, new ArrayList<EdmxReference>());
			metaDataHandler = odata.createHandler(edm);
			
			MariaDbDataSource ds = new MariaDbDataSource("jdbc:mariadb://ha-mariadb:3306/HA?characterEncoding=utf8");
			ds.setUser("root");
			
			getHandler = new JPAODataGetHandler(PUNIT_NAME, ds);
			crudHandler = new JPAODataCRUDHandler(PUNIT_NAME, ds);
			
		} catch (ODataException | SQLException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			resp.setHeader("Access-Control-Allow-Origin", "*");
			if (req.getRequestURI().endsWith("$metadata")) {
				metaDataHandler.process(req, resp);
			} else {
				if ("GET".equals(req.getMethod())) {
					getHandler.process(req, resp);
				} else {
					crudHandler.process(req, resp);
				}
			}

		}

		catch (RuntimeException | ODataException e) {
			throw new ServletException(e);
		}
	}

	public static void main(String[] args) throws NamingException {
		try {

			EntityManagerFactory emf = JPAEntityManagerFactory.getEntityManagerFactory(PUNIT_NAME,
					new HashMap<String, Object>());
			JPAEdmProvider metadataProvider = new JPAEdmProvider(PUNIT_NAME, emf, null, packages);

			OData odata = OData.newInstance();
			ServiceMetadata edm = odata.createServiceMetadata(metadataProvider, new ArrayList<EdmxReference>());

			
			
			ODataHttpHandler handler = odata.createHandler(edm);
			
			JPAODataGetHandler handler2 = new JPAODataGetHandler(PUNIT_NAME);
			
			ODataRequest request = new ODataRequest();
			request.setMethod(HttpMethod.GET);
			request.setRawBaseUri("http://localhost/HomeAutomation/JPAOData.svc/Rooms");
			request.setRawODataPath("Rooms");
			request.setProtocol("HTTP/1.1");
			
			JPAODataSessionContextAccess context = (JPAODataSessionContextAccess)handler2.getJPAODataContext();
			
		    EntityManager em=emf.createEntityManager();
			handler.register(new JPAODataRequestProcessor(context, em));
		    handler.register(new JPAODataBatchProcessor(context, em));

			ODataResponse process = handler.process(request);
			InputStream content = process.getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(content));
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}

		} catch (RuntimeException e) {

		} catch (ODataException e) {

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
