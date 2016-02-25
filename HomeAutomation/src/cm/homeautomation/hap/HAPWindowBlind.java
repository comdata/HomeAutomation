package cm.homeautomation.hap;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.beowulfe.hap.HomekitCharacteristicChangeCallback;
import com.beowulfe.hap.Service;
import com.beowulfe.hap.accessories.WindowCovering;
import com.beowulfe.hap.accessories.properties.WindowCoveringPositionState;

import cm.homeautomation.entities.WindowBlind;
import cm.homeautomation.windowblind.WindowBlindService;

public class HAPWindowBlind implements WindowCovering {

	private int id;
	private String label;
	private WindowBlind windowBlind;
	private HomekitCharacteristicChangeCallback currentPositionCallback;
	private HomekitCharacteristicChangeCallback targetPositionCallback;
	private HomekitCharacteristicChangeCallback positionStateCallback;
	private HomekitCharacteristicChangeCallback obstructionCallback;
	
	public HAPWindowBlind(WindowBlind windowBlind, int id, String label) {
		this.windowBlind = windowBlind;
		this.label = label;
		this.id=id;
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
		// TODO Auto-generated method stub
		return "none";
	}

	@Override
	public String getModel() {
		// TODO Auto-generated method stub
		return "none";
	}

	@Override
	public String getManufacturer() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return CompletableFuture.completedFuture(WindowCoveringPositionState.STOPPED);
	}

	@Override
	public CompletableFuture<Boolean> getObstructionDetected() {
		// TODO Auto-generated method stub
		return CompletableFuture.completedFuture(Boolean.FALSE);
	}

	@Override
	public CompletableFuture<Void> setTargetPosition(int position) throws Exception {
		new WindowBlindService().setDim(new Long(id), Integer.toString(position));
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> setHoldPosition(boolean hold) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void subscribeCurrentPosition(HomekitCharacteristicChangeCallback callback) {
		this.currentPositionCallback = callback;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subscribeTargetPosition(HomekitCharacteristicChangeCallback callback) {
		this.targetPositionCallback = callback;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subscribePositionState(HomekitCharacteristicChangeCallback callback) {
		this.positionStateCallback = callback;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subscribeObstructionDetected(HomekitCharacteristicChangeCallback callback) {
		this.obstructionCallback = callback;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unsubscribeCurrentPosition() {
		// TODO Auto-generated method stub
		this.currentPositionCallback=null;
	}

	@Override
	public void unsubscribeTargetPosition() {
		// TODO Auto-generated method stub
		this.targetPositionCallback=null;
		
	}

	@Override
	public void unsubscribePositionState() {
		// TODO Auto-generated method stub
		this.positionStateCallback=null;
		
	}

	@Override
	public void unsubscribeObstructionDetected() {
		// TODO Auto-generated method stub
		this.obstructionCallback=null;
		
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
