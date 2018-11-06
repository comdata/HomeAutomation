package cm.homeautomation.services.cameras;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.LogManager;

import cm.homeautomation.configuration.ConfigurationService;
import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Camera;
import cm.homeautomation.entities.CameraImageHistory;
import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import cm.homeautomation.services.base.BaseService;

@Path("camera")
public class CameraService extends BaseService {

	@Path("getAll")
	@GET
	public List<Camera> getAll() {
		EntityManager em = EntityManagerService.getManager();

		@SuppressWarnings("unchecked")
		List<Camera> resultList = (List<Camera>) em.createQuery("select c from Camera c where c.enabled=true")
				.getResultList();
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
		EntityManager em = EntityManagerService.getNewManager();

		List<Camera> resultList = em.createQuery("select c from Camera c where c.id=:id", Camera.class)
				.setParameter("id", id).getResultList();

		em.close();

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
		EntityManager em = EntityManagerService.getNewManager();
		@SuppressWarnings("unchecked")
		List<Camera> resultList = em.createQuery("select c from Camera c where c.id=:id")
				.setParameter("id", Long.parseLong(args[0])).getResultList();
		if (resultList != null) {

			for (Camera camera : resultList) {
				singleCameraUpdate(args, em, camera);
			}
		}
		cleanOldImages();
	}

	private static void singleCameraUpdate(String[] args, EntityManager em, Camera camera) {
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

			EventBusService.getEventBus().post(event);
		} catch (Exception e) {
			em.getTransaction().rollback();
			loadNoImage(args, em, camera);
		} finally {
			em.close();
		}
	}

	private static void cleanOldImages() {
		EntityManager em = EntityManagerService.getNewManager();

		em.getTransaction().begin();

		em.createQuery("delete from CameraImageHistory c where c.dateTaken<=:deleteDate")
				.setParameter("deleteDate", new Date((new Date()).getTime() - (3 * 86400 * 1000))).executeUpdate();

		em.getTransaction().commit();
		em.close();

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

}
