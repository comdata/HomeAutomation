package cm.homeautomation.transmission;

import java.net.URI;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import cm.homeautomation.eventbus.EventObject;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.eventbus.EventBus;
import nl.stil4m.transmission.api.TransmissionRpcClient;
import nl.stil4m.transmission.api.domain.RemoveTorrentInfo;
import nl.stil4m.transmission.api.domain.TorrentInfo;
import nl.stil4m.transmission.api.domain.TorrentInfoCollection;
import nl.stil4m.transmission.api.domain.ids.NumberListIds;
import nl.stil4m.transmission.rpc.RpcClient;
import nl.stil4m.transmission.rpc.RpcConfiguration;
import nl.stil4m.transmission.rpc.RpcException;

@ApplicationScoped
public class TransmissionMonitor {

	@Inject
	EventBus bus;
	private static TransmissionMonitor instance;

	void startup(@Observes StartupEvent event) {
		instance = this;
	}

	public static void checkTorrents(String[] args) {
		instance.checkTorrentsInternal(args);
	}

	public void checkTorrentsInternal(String[] args) {
		try {
			ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			;
			objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

			RpcConfiguration rpcConfiguration = new RpcConfiguration();
			rpcConfiguration.setHost(URI
					.create("http://" + args[0] + ":" + args[1] + "@" + args[2] + ":" + args[3] + "/transmission/rpc"));

			RpcClient client = new RpcClient(rpcConfiguration, objectMapper);

			TransmissionRpcClient rpcClient = new TransmissionRpcClient(client);

			TorrentInfoCollection result = rpcClient.getAllTorrentsInfo();

			Long downloadSpeed = rpcClient.getSessionStats().getDownloadSpeed();
			Long uploadSpeed = rpcClient.getSessionStats().getUploadSpeed();
			//LogManager.getLogger(TransmissionMonitor.class).info("Download speed: " + downloadSpeed);
			//LogManager.getLogger(TransmissionMonitor.class).info("Upload speed: " + uploadSpeed);
			int numberOfTorrents = result.getTorrents().size();
			int numberOfDoneTorrents = 0;
			List<TorrentInfo> torrents = result.getTorrents();
			for (TorrentInfo torrentInfo : torrents) {
				double percentDone = torrentInfo.getPercentDone().doubleValue();
				//LogManager.getLogger(TransmissionMonitor.class).info("Percent done: " + percentDone);
				if (percentDone == 1) {
					numberOfDoneTorrents++;
					torrentInfo.getId();
					String name = torrentInfo.getName();

					// remove done torrents, keeping local data
					rpcClient.removeTorrent(new RemoveTorrentInfo(new NumberListIds(torrentInfo.getId()), false));
					bus.publish("EventObject", new EventObject(new TransmissionDownloadFinishedEvent(name)));

				}
			}
			//LogManager.getLogger(TransmissionMonitor.class).info("Done torrents: " + numberOfDoneTorrents);
			//LogManager.getLogger(TransmissionMonitor.class).info("Running torrents: " + numberOfTorrents);

			TransmissionStatusData torrentData = new TransmissionStatusData();
			torrentData.setUploadSpeed(uploadSpeed);
			torrentData.setDownloadSpeed(downloadSpeed);
			torrentData.setTorrents(numberOfTorrents);
			torrentData.setDoneTorrents(numberOfDoneTorrents);
			EventObject eventObject = new EventObject(torrentData);
			bus.publish("EventObject", eventObject);
		} catch (RpcException | Exception e) {
			//LogManager.getLogger(TransmissionMonitor.class).error(e);
		}
	}
}
