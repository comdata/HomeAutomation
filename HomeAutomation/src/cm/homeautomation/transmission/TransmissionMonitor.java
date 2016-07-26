package cm.homeautomation.transmission;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;

import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import nl.stil4m.transmission.api.TransmissionRpcClient;
import nl.stil4m.transmission.api.domain.RemoveTorrentInfo;
import nl.stil4m.transmission.api.domain.TorrentInfoCollection;
import nl.stil4m.transmission.rpc.RpcClient;
import nl.stil4m.transmission.rpc.RpcCommand;
import nl.stil4m.transmission.rpc.RpcConfiguration;
import nl.stil4m.transmission.rpc.RpcException;

public class TransmissionMonitor extends Thread {

	private TransmissionRpcClient rpcClient;
	private boolean run = false;

	public TransmissionMonitor() {
	
		run = true;
	}

	@Override
	public void run() {
		super.run();

		while (run) {
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
				RpcConfiguration rpcConfiguration = new RpcConfiguration();
				rpcConfiguration.setHost(URI.create("http://192.168.1.36:9091/transmission/rpc"));

				RpcClient client = new RpcClient(rpcConfiguration, objectMapper);
				
				rpcClient = new TransmissionRpcClient(client);
				
				TorrentInfoCollection result = rpcClient.getAllTorrentsInfo();

				Long downloadSpeed = rpcClient.getSessionStats().getDownloadSpeed();
				Long uploadSpeed = rpcClient.getSessionStats().getUploadSpeed();
				System.out.println("Download speed: " + downloadSpeed);
				System.out.println("Upload speed: " + uploadSpeed);
				int numberOfTorrents = result.getTorrents().size();
				System.out.println("Running torrents: " + numberOfTorrents);
				
				
				
				TransmissionStatusData torrentData=new TransmissionStatusData();
				torrentData.setUploadSpeed(uploadSpeed);
				torrentData.setDownloadSpeed(downloadSpeed);
				torrentData.setTorrents(numberOfTorrents);
				EventObject eventObject=new EventObject(torrentData);
				EventBusService.getEventBus().post(eventObject);
				
				Thread.sleep(60 * 1000);
			} catch (RpcException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void stopMonitor() {
		run = false;
		this.interrupt();
	}
	
	public static void main(String[] args) {
		TransmissionMonitor transmissionMonitor = new TransmissionMonitor();
		
		transmissionMonitor.start();
	}
}
