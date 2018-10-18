package cm.homeautomation.services.legotrain;

public class TrainControlMessage {
	private String train;
	private int speed;
	private int channel;

	public String getTrain() {
		return train;
	}

	public void setTrain(String train) {
		this.train = train;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

}
