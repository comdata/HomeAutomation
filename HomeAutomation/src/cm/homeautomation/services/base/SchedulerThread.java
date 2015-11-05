package cm.homeautomation.services.base;

import java.io.File;

import it.sauronsoftware.cron4j.Scheduler;

/**
 * provide refresh methods for the scheduler
 * 
 * @author mertins
 *
 */
public class SchedulerThread extends Thread {
	private Scheduler scheduler;
	private boolean run=true;
	
	@Override
	public void run() {
		super.run();
		
		setScheduler(new Scheduler());
		File scheduleFile = new File("schedule.cron");
		getScheduler().scheduleFile(scheduleFile);
		getScheduler().start();
		
		while (run) {
			
			getScheduler().descheduleFile(scheduleFile);
			getScheduler().scheduleFile(scheduleFile);
			try {
				Thread.sleep(60*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void stopThread() {
		run=false;
		getScheduler().stop();
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}
}
