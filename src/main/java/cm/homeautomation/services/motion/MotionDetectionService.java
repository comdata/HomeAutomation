package cm.homeautomation.services.motion;

import org.greenrobot.eventbus.Subscribe;

import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.services.base.AutoCreateInstance;
import cm.homeautomation.services.base.BaseService;

@AutoCreateInstance
public class MotionDetectionService extends BaseService {

	public MotionDetectionService() {
		EventBusService.getEventBus().register(this);

	}

	@Subscribe
	public void registerMotionEvent(final MotionEvent motionEvent) {

		final boolean state = motionEvent.isState();

		// TODO refactor this. add suppression and flip detection logic
		// add
		System.out.println(motionEvent.getMessageString());

	}

}
