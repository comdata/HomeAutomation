package cm.homeautomation.services.overview;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OverviewTiles {

	private List<OverviewTile> overviewTiles;

	public List<OverviewTile> getOverviewTiles() {
		if (overviewTiles == null) {
			overviewTiles = new ArrayList<>();
		}

		return overviewTiles;
	}

	public OverviewTile getTileForRoom(String roomId) {
		for (final OverviewTile overviewTile : overviewTiles) {
			if (overviewTile.getRoomId().equals(roomId)) {
				return overviewTile;
			}
		}
		return null;
	}

	public void setOverviewTiles(List<OverviewTile> overviewTiles) {
		this.overviewTiles = overviewTiles;
	}

}
