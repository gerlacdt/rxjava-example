package hello;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class Clients {

	private MeetupApi meetupApi;
	private GeonamesApi geonamesApi;

	public Clients() {
		// meetup api
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		Retrofit retrofitMeetupApi = new Retrofit.Builder().baseUrl("https://api.meetup.com")
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.addConverterFactory(JacksonConverterFactory.create(objectMapper))
				.build();
		setMeetupApi(retrofitMeetupApi.create(MeetupApi.class));

		// geonames api
		Retrofit geonamesRetrofit = new Retrofit.Builder().baseUrl("http://api.geonames.org")
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.addConverterFactory(JacksonConverterFactory.create(objectMapper))
				.build();
		setGeonamesApi(geonamesRetrofit.create(GeonamesApi.class));
	}

	public MeetupApi getMeetupApi() {
		return meetupApi;
	}

	public void setMeetupApi(MeetupApi meetupApi) {
		this.meetupApi = meetupApi;
	}

	public GeonamesApi getGeonamesApi() {
		return geonamesApi;
	}

	public void setGeonamesApi(GeonamesApi geonamesApi) {
		this.geonamesApi = geonamesApi;
	}

}
