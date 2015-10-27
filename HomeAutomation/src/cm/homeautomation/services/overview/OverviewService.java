package cm.homeautomation.services.overview;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import cm.homeautomation.services.base.BaseService;

@Path("overview")
public class OverviewService extends BaseService{

	@Path("get")
	@GET
	public OverviewTiles getOverviewTiles() {
		OverviewTiles overviewTiles = new OverviewTiles();
		
		OverviewTile wohnzimmer = new OverviewTile();
		wohnzimmer.setNumber("23");
		wohnzimmer.setNumberUnit("C");
		wohnzimmer.setTitle("Wohnzimmer");
		wohnzimmer.setInfo("Temperatur");
		wohnzimmer.setInfoState("success");
		
		overviewTiles.getOverviewTiles().add(wohnzimmer);
		
		return overviewTiles;
	}
}
