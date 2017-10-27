package problems;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import rx.Observable;

public class StreamsTest {

	@Test
	public void syncTest() {
		Observable<String> o1 = Observable.fromCallable(() -> {
			System.out.println("side effect called once cause of caching");
			return "a";
		})
				.delay(500, TimeUnit.MILLISECONDS)
				.cache()
				.repeat();
		Observable<String> o2 = Observable.just("1", "2", "3")
				.delay(300, TimeUnit.MILLISECONDS);
		Observable<String> o3 = o1.zipWith(o2, (a, b) -> {
			return a + b;
		});
		o3.toBlocking()
				.subscribe(System.out::println);
	}
}
