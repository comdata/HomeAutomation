package cm.homeautomation.odata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;

import com.sap.olingo.jpa.metadata.api.JPAEdmProvider;

import cm.homeautomation.db.EntityManagerService;

public class JPAODataServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String PUNIT_NAME = "JPAOData";

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			EntityManagerFactory emf =EntityManagerService.getManager().getEntityManagerFactory();
			JPAEdmProvider metadataProvider = new JPAEdmProvider(PUNIT_NAME, emf, null, null);

			OData odata = OData.newInstance();
			ServiceMetadata edm = odata.createServiceMetadata(metadataProvider, new ArrayList<EdmxReference>());
			ODataHttpHandler handler = odata.createHandler(edm);

			handler.process(req, resp);
		} catch (RuntimeException e) {
			throw new ServletException(e);
		} catch (ODataException e) {
		throw new ServletException(e);
		}
	}
	
	public static void main(String[] args) {
		try {
			EntityManagerFactory emf =EntityManagerService.getManager().getEntityManagerFactory();
			JPAEdmProvider metadataProvider = new JPAEdmProvider(PUNIT_NAME, emf, null, null);

			OData odata = OData.newInstance();
			ServiceMetadata edm = odata.createServiceMetadata(metadataProvider, new ArrayList<EdmxReference>());
			ODataHttpHandler handler = odata.createHandler(edm);
			ODataRequest request=new ODataRequest();
			request.setMethod(HttpMethod.GET);
			request.setRawBaseUri("http://localhost/HomeAutomation/JPAOData.svc/");
			request.setRawODataPath("/");
			request.setProtocol("HTTP/1.1");
			
			ODataResponse process = handler.process(request);
			InputStream content = process.getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(content));
			String line=null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (RuntimeException e) {

		} catch (ODataException e) {
		
		} catch (IOException e) {

		}
	}
}
