package cm.homeautomation.services.overview;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OverviewTiles {

	private List<OverviewTile> overviewTiles;

	public List<OverviewTile> getOverviewTiles() {
		if (overviewTiles == null) {
			overviewTiles=new ArrayList<OverviewTile>();
		}
		
		return overviewTiles;
	}

	public void setOverviewTiles(List<OverviewTile> overviewTiles) {
		this.overviewTiles = overviewTiles;
	}
	
	
	
}
