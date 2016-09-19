package cm.homeautomation.services.cameras;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.ws.rs.Path;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.Camera;
import cm.homeautomation.services.base.BaseService;

@Path("cameras")
public class CameraService extends BaseService {

	@Path("getAll")
	public List<Camera> getAll() {
		EntityManager em = EntityManagerService.getManager();

		List<Camera> resultList = (List<Camera>) em.createQuery("select c from Camera c").getResultList();
		return resultList;
	}

	private static BufferedImage resize(final URL url, final Dimension size) throws IOException {
		final BufferedImage image = ImageIO.read(url);
		final BufferedImage resized = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = resized.createGraphics();
		g.drawImage(image, 0, 0, size.width, size.height, null);
		g.dispose();
		return resized;
	}

	public static void prepareCameraImage(String[] args) {
		EntityManager em = EntityManagerService.getManager();
		List<Camera> resultList = em.createQuery("select c from Camera c where id=:id")
				.setParameter("id", Long.parseLong(args[0])).getResultList();
		if (resultList != null) {
			try {
				for (Camera camera : resultList) {
					em.getTransaction().begin();
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					final BufferedImage image = resize(new URL(camera.getIcon()),
							new Dimension(Integer.parseInt(args[1]), Integer.parseInt(args[2])));
					ImageIO.write(image, "jpg", bos);
					byte[] cameraSnapshot = bos.toByteArray();
					camera.setImageSnapshot(cameraSnapshot);
					em.merge(camera);
					em.getTransaction().commit();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
