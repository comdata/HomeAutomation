package cm.homeautomation.hap;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.Service;
import com.beowulfe.hap.accessories.VerticalTiltingWindowCovering;
import com.beowulfe.hap.accessories.WindowCovering;
import com.beowulfe.hap.accessories.properties.WindowCoveringPositionState;

import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.windowblind.WindowBlindService;

public class HAPWindowBlind implements VerticalTiltingWindowCovering {

	private int id;
	private String label;
	private WindowBlind windowBlind;
	private HomekitCharacteristicChangeCallback currentPositionCallback;
	private HomekitCharacteristicChangeCallback targetPositionCallback;
	private HomekitCharacteristicChangeCallback positionStateCallback;
	private HomekitCharacteristicChangeCallback obstructionCallback;
	private HomekitCharacteristicChangeCallback currentTiltAngleCallback;
	private HomekitCharacteristicChangeCallback targetTiltAngleCallback;

	public HAPWindowBlind(WindowBlind windowBlind, int id, String label) {
		this.windowBlind = windowBlind;
		this.label = label;
		this.id = id;
	}
	
	public HAPWindowBlind() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void identify() {
		System.out.println("Identifying Window Covering");
	}

	@Override
	public String getSerialNumber() {
		return "none";
	}

	@Override
	public String getModel() {
		return "none";
	}

	@Override
	public String getManufacturer() {
		return "none";
	}

	@Override
	public CompletableFuture<Integer> getCurrentPosition() {
		return CompletableFuture.completedFuture(getPosition());
	}

	private Integer getPosition() {
		return new Integer(new Float(windowBlind.getCurrentValue()).intValue());
	}

	@Override
	public CompletableFuture<Integer> getTargetPosition() {
		return CompletableFuture.completedFuture(getPosition());
	}

	@Override
	public CompletableFuture<WindowCoveringPositionState> getPositionState() {
		return CompletableFuture.completedFuture(WindowCoveringPositionState.STOPPED);
	}

	@Override
	public CompletableFuture<Boolean> getObstructionDetected() {
		return CompletableFuture.completedFuture(Boolean.FALSE);
	}

	@Override
	public CompletableFuture<Void> setTargetPosition(int position) throws Exception {
		new WindowBlindService().setDim(new Long(id), Integer.toString(position));
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> setHoldPosition(boolean hold) throws Exception {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void subscribeCurrentPosition(HomekitCharacteristicChangeCallback callback) {
		System.out.println("Current position callback");
		this.currentPositionCallback = callback;
	}

	@Override
	public void subscribeTargetPosition(HomekitCharacteristicChangeCallback callback) {
		System.out.println("Target position callback");
		this.targetPositionCallback = callback;
	}

	@Override
	public void subscribePositionState(HomekitCharacteristicChangeCallback callback) {
		System.out.println("position state callback");
		this.positionStateCallback = callback;
	}

	@Override
	public void subscribeObstructionDetected(HomekitCharacteristicChangeCallback callback) {
		System.out.println("Obstruction position callback");
		this.obstructionCallback = callback;
	}

	@Override
	public void unsubscribeCurrentPosition() {
		this.currentPositionCallback = null;
	}

	@Override
	public void unsubscribeTargetPosition() {
		this.targetPositionCallback = null;
	}

	@Override
	public void unsubscribePositionState() {
		this.positionStateCallback = null;
	}

	@Override
	public void unsubscribeObstructionDetected() {
		this.obstructionCallback = null;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setLabel(String label) {
		this.label = label;
	}


	@Override
	public CompletableFuture<Integer> getCurrentVerticalTiltAngle() {
		
		return CompletableFuture.completedFuture(new Integer(0));
	}

	@Override
	public CompletableFuture<Integer> getTargetVerticalTiltAngle() {
		
		return CompletableFuture.completedFuture(new Integer(0));
	}

	@Override
	public CompletableFuture<Void> setTargetVerticalTiltAngle(int angle) throws Exception {
		
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void subscribeCurrentVerticalTiltAngle(HomekitCharacteristicChangeCallback callback) {
		this.currentTiltAngleCallback = callback;
	}

	@Override
	public void subscribeTargetVerticalTiltAngle(HomekitCharacteristicChangeCallback callback) {
		this.targetTiltAngleCallback = callback;
	}

	@Override
	public void unsubscribeCurrentVerticalTiltAngle() {
		this.currentTiltAngleCallback=null;
		
	}

	@Override
	public void unsubscribeTargetVerticalTiltAngle() {
		this.targetTiltAngleCallback = null;
		
	}
}
