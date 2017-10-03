package book.ch8;

import org.junit.Test;

import hello.Cities;
import hello.City;
import hello.Clients;
import hello.GeonamesApi;
import hello.MeetupApi;
import hello.SearchResult;
import rx.Observable;

public class RetrofitTest {

	@Test
	public void meetupApiTest() {
		Clients clients = new Clients();
		MeetupApi meetupApi = clients.getMeetupApi();

		// stuttgart, germany
		double lat = 48.7758;
		double lon = 9.1829;

		Observable<Cities> cities = meetupApi.listCities(lat, lon);
		Observable<City> cityObs = cities.concatMapIterable(Cities::getResults);
		Observable<String> map = cityObs.filter(city -> city.distanceTo(lat, lon) < 50)
				.map(City::getCity);
		map.toBlocking()
				.subscribe(System.out::println);
	}

	@Test
	public void geonamesApiTest() {
		Clients clients = new Clients();
		GeonamesApi api = clients.getGeonamesApi();

		String query = "stuttgart";
		Observable<SearchResult> geonames = api.search(query);

		geonames.concatMapIterable(SearchResult::getGeonames)
				.map(geoname -> geoname.getName())
				.toBlocking()
				.subscribe(System.out::println);
	}

	@Test
	public void populationTest() {
		Clients clients = new Clients();
		MeetupApi meetupApi = clients.getMeetupApi();
		GeonamesApi geonamesApi = clients.getGeonamesApi();

		// stuttgart, germany
		double lat = 48.7758;
		double lon = 9.1829;

		Observable<Long> totalPopulation = meetupApi.listCities(lat, lon)
				.concatMapIterable(Cities::getResults)
				.filter(city -> city.distanceTo(lat, lon) < 50)
				.map(City::getCity)
				.flatMap(geonamesApi::populationOf)
				.reduce(0L, (x, y) -> x + y);

		totalPopulation.toBlocking()
				.subscribe(System.out::println);
	}

}
