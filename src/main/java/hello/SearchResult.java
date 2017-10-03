package hello;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
	private List<Geoname> geonames = new ArrayList<>();

	public List<Geoname> getGeonames() {
		return geonames;
	}

	public void setGeonames(List<Geoname> geonames) {
		this.geonames = geonames;
	}
}
