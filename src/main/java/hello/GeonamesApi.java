package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import retrofit2.http.GET;

import retrofit2.http.Query;
import rx.Observable;
import rx.schedulers.Schedulers;

public interface GeonamesApi {

	Logger LOGGER = LoggerFactory.getLogger(GeonamesApi.class);

	default Observable<Integer> populationOf(String query) {
		return search(query).concatMapIterable(SearchResult::getGeonames).map(Geoname::getPopulation)
				.filter(p -> p != null).singleOrDefault(0)
				.doOnError(th -> LOGGER.warn("falling back to 0 for {}", query, th)).onErrorReturn(th -> 0)
				.subscribeOn(Schedulers.io());

	}

	default Observable<SearchResult> search(String query) {
		return search(query, 1, "LONG", "gerlacdt");
	}

	@GET("/searchJSON")
	public Observable<SearchResult> search(@Query("q") String query, @Query("maxRows") int maxRows,
			@Query("style") String style, @Query("username") String username);
}
