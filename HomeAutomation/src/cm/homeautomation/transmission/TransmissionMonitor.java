package cm.homeautomation.transmission;

import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;

import cm.homeautomation.eventbus.EventBusService;
import cm.homeautomation.eventbus.EventObject;
import nl.stil4m.transmission.api.TransmissionRpcClient;
import nl.stil4m.transmission.api.domain.RemoveTorrentInfo;
import nl.stil4m.transmission.api.domain.TorrentInfo;
import nl.stil4m.transmission.api.domain.TorrentInfoCollection;
import nl.stil4m.transmission.rpc.RpcClient;
import nl.stil4m.transmission.rpc.RpcCommand;
import nl.stil4m.transmission.rpc.RpcConfiguration;
import nl.stil4m.transmission.rpc.RpcException;

public class TransmissionMonitor {

	public static void checkTorrents(String[] args) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
			RpcConfiguration rpcConfiguration = new RpcConfiguration();
			rpcConfiguration.setHost(URI.create("http://"+args[0]+":"+args[1]+"@"+args[2]+":"+args[3]+"/transmission/rpc"));
			
			RpcClient client = new RpcClient(rpcConfiguration, objectMapper);

			
			TransmissionRpcClient rpcClient = new TransmissionRpcClient(client);
			
			
			TorrentInfoCollection result = rpcClient.getAllTorrentsInfo();

			Long downloadSpeed = rpcClient.getSessionStats().getDownloadSpeed();
			Long uploadSpeed = rpcClient.getSessionStats().getUploadSpeed();
			System.out.println("Download speed: " + downloadSpeed);
			System.out.println("Upload speed: " + uploadSpeed);
			int numberOfTorrents = result.getTorrents().size();
			int numberOfDoneTorrents = 0;
			List<TorrentInfo> torrents = result.getTorrents();
			for (TorrentInfo torrentInfo : torrents) {
				Boolean finished = torrentInfo.getFinished();

				double percentDone = torrentInfo.getPercentDone().doubleValue();
				System.out.println("Percent done: " + percentDone);
				if (percentDone == 1) {
					numberOfDoneTorrents++;
				}
			}
			System.out.println("Done torrents: " + numberOfDoneTorrents);
			System.out.println("Running torrents: " + numberOfTorrents);

			TransmissionStatusData torrentData = new TransmissionStatusData();
			torrentData.setUploadSpeed(uploadSpeed);
			torrentData.setDownloadSpeed(downloadSpeed);
			torrentData.setTorrents(numberOfTorrents);
			torrentData.setDoneTorrents(numberOfDoneTorrents);
			EventObject eventObject = new EventObject(torrentData);
			EventBusService.getEventBus().post(eventObject);


		} catch (RpcException e) {
		

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
