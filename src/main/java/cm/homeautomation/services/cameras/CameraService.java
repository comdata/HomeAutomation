package cm.homeautomation.services.cameras;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.LogManager;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Camera;
import cm.homeautomation.entities.CameraImageHistory;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.BaseService;
import io.vertx.core.eventbus.EventBus;

@Path("camera/")
public class CameraService extends BaseService {

	@Inject
	EventBus bus;
	private static CameraService instance;

	public CameraService() {
		instance = this;
	}

	@Path("getAll")
	@GET
	public List<Camera> getAll() {
		EntityManager em = EntityManagerService.getManager();

		@SuppressWarnings("unchecked")
		List<Camera> resultList = em.createQuery("select c from Camera c where c.enabled=true").getResultList();
		return resultList;
	}

	private static BufferedImage resize(final URL url, final Dimension size) throws IOException {
		final BufferedImage image = ImageIO.read(url);
		final BufferedImage resized = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g = resized.createGraphics();
		g.drawImage(image, 0, 0, size.width, size.height, null);
		g.dispose();
		return resized;
	}

	@Path("getSnapshot/{id}")
	@Produces("image/jpeg")
	@GET
	public Response getSnapshot(@PathParam("id") Long id) {
		EntityManager em = EntityManagerService.getManager();

		List<Camera> resultList = em.createQuery("select c from Camera c where c.id=:id", Camera.class)
				.setParameter("id", id).getResultList();

		if (resultList != null && !resultList.isEmpty()) {
			Camera camera = resultList.get(0);
			final byte[] imageData = camera.getImageSnapshot();
			StreamingOutput stream = new StreamingOutput() {

				@Override
				public void write(OutputStream output) throws IOException {
					try {
						output.write(imageData);
					} catch (Exception e) {
						LogManager.getLogger(this.getClass()).error("Write camera output stream failed.", e);
					}
				}
			};

			return Response.ok(stream).build();
		}

		return Response.serverError().build();
	}

	public static void prepareCameraImage(String[] args) {
		EntityManager em = EntityManagerService.getManager();
		@SuppressWarnings("unchecked")
		List<Camera> resultList = em.createQuery("select c from Camera c where c.id=:id")
				.setParameter("id", Long.parseLong(args[0])).getResultList();
		if (resultList != null) {

			for (Camera camera : resultList) {
				if (camera.isEnabled()) {
					singleCameraUpdate(args, em, camera);
				}
			}
		}
		cleanOldImages();
	}

	private static void singleCameraUpdate(String[] args, EntityManager em, Camera camera) {
		instance.singleCameraUpdateInternal(args, em, camera);
	}

	private void singleCameraUpdateInternal(String[] args, EntityManager em, Camera camera) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
			em.getTransaction().begin();

			final BufferedImage image = resize(new URL(camera.getIcon()),
					new Dimension(Integer.parseInt(args[1]), Integer.parseInt(args[2])));
			ImageIO.write(image, "jpg", bos);
			byte[] cameraSnapshot = bos.toByteArray();
			camera.setImageSnapshot(cameraSnapshot);
			em.merge(camera);

			String historyEnabledString = ConfigurationService.getConfigurationProperty("camera", "historyEnabled");

			if (historyEnabledString == null || "".equals(historyEnabledString)) {
				historyEnabledString = "false";
			}

			boolean historyEnabled = Boolean.parseBoolean(historyEnabledString);

			if (historyEnabled) {
				// persist history of camera images
				CameraImageHistory cameraImageHistory = new CameraImageHistory();
				cameraImageHistory.setCamera(camera);
				cameraImageHistory.setDateTaken(new Date());
				cameraImageHistory.setImageSnapshot(cameraSnapshot);
				em.persist(cameraImageHistory);
			}

			em.getTransaction().commit();

			CameraImageUpdateEvent cameraEvent = new CameraImageUpdateEvent();
			cameraEvent.setCamera(camera);
			EventObject event = new EventObject(cameraEvent);

			bus.publish("EventObject", event);
		} catch (Exception e) {
			em.getTransaction().rollback();
			loadNoImage(args, em, camera);
		} finally {
		}
	}

	private static void cleanOldImages() {
		EntityManager em = EntityManagerService.getManager();

		em.getTransaction().begin();

		em.createQuery("delete from CameraImageHistory c where c.dateTaken<=:deleteDate")
				.setParameter("deleteDate", new Date((new Date()).getTime() - (3 * 86400 * 1000))).executeUpdate();

		em.getTransaction().commit();
	}

	private static void loadNoImage(String[] args, EntityManager em, Camera camera) {
		try {
			em.getTransaction().begin();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			final BufferedImage image = resize(new File("resource/noimage.png").toURI().toURL(),
					new Dimension(Integer.parseInt(args[1]), Integer.parseInt(args[2])));
			ImageIO.write(image, "jpg", bos);
			byte[] cameraSnapshot = bos.toByteArray();

			camera.setImageSnapshot(cameraSnapshot);
			em.merge(camera);
			em.getTransaction().commit();
		} catch (IOException | RuntimeException e) {
			LogManager.getLogger(CameraService.class).error("loading the 'no image' image failed.", e);
		}
	}

	@GET
	@Path("stream/{id}")
	public Response getStream(@PathParam("id") Long id) {
		StreamingOutput streamingOutput = new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {

				EntityManager em = EntityManagerService.getManager();

				Camera camera = em.find(Camera.class, id);

				if (camera != null && camera.getInternalStream() != null && !camera.getInternalStream().isEmpty()) {
					String uri = camera.getInternalStream();

					HttpClient proxy = HttpClientBuilder.create().build();

					HttpGet proxyMethod = new HttpGet(uri);

					HttpResponse httpResponse = proxy.execute(proxyMethod);

					write(httpResponse.getEntity().getContent(), output);

				}

			}

			private void write(final InputStream inputStream, final OutputStream outputStream) throws IOException {
				int b;
				while ((b = inputStream.read()) != -1) {
					outputStream.write(b);
				}

				outputStream.flush();
			}

		};
		return Response.ok(streamingOutput, MediaType.valueOf("video/x-motion-jpeg"))
				// .header("content-disposition", "attachment; filename = myfile.pdf")
				.build();
	}

}
