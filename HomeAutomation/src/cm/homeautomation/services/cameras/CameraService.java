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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

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

		List<Camera> resultList = (List<Camera>) em.createQuery("select c from Camera c").getResultList();
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

		List<Camera> resultList = (List<Camera>) em.createQuery("select c from Camera c where c.id=:id")
				.setParameter("id", id).getResultList();

		em.close();
		
		for (Camera camera : resultList) {
			final byte[] imageData = camera.getImageSnapshot();
			StreamingOutput stream = new StreamingOutput() {

				@Override
				public void write(OutputStream output) throws IOException, WebApplicationException {
					try {
						output.write(imageData);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			};

			return Response.ok(stream).build();
		}
		return Response.serverError().build();
	}

	public static void prepareCameraImage(String[] args) {
		EntityManager em = EntityManagerService.getNewManager();
		List<Camera> resultList = em.createQuery("select c from Camera c where c.id=:id")
				.setParameter("id", Long.parseLong(args[0])).getResultList();
		if (resultList != null) {

			for (Camera camera : resultList) {
				try {
					em.getTransaction().begin();
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					final BufferedImage image = resize(new URL(camera.getIcon()),
							new Dimension(Integer.parseInt(args[1]), Integer.parseInt(args[2])));
					ImageIO.write(image, "jpg", bos);
					byte[] cameraSnapshot = bos.toByteArray();
					camera.setImageSnapshot(cameraSnapshot);
					em.merge(camera);
					
					// persist history of camera images
					CameraImageHistory cameraImageHistory = new CameraImageHistory();
					cameraImageHistory.setCamera(camera);
					cameraImageHistory.setDateTaken(new Date());
					cameraImageHistory.setImageSnapshot(cameraSnapshot);
					em.persist(cameraSnapshot);
					
					
					em.getTransaction().commit();

					CameraImageUpdateEvent cameraEvent = new CameraImageUpdateEvent();
					cameraEvent.setCamera(camera);
					
					EventObject event = new EventObject(cameraEvent);

					EventBusService.getEventBus().post(event);
				} catch (IOException e) {
					em.getTransaction().rollback();
					loadNoImage(args, em, camera);
				} catch (RuntimeException e) {
					em.getTransaction().rollback();
					loadNoImage(args, em, camera);
				}
			}

		}
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
		} catch (IOException e) {
			e.printStackTrace();
		}catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

}
