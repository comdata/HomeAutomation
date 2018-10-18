package cm.homeautomation.services.cameras;

import cm.homeautomation.entities.Camera;

public class CameraImageUpdateEvent {
	private Camera camera;

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}
}
