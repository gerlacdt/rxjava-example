package book.ch6;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Test;

import rx.Observable;

public class FlowControlTest {

	@Test
	public void periodicTest() {
		long startTime = System.currentTimeMillis();
		Observable.interval(10, TimeUnit.MILLISECONDS)
				.timestamp()
				.sample(1, TimeUnit.SECONDS)
				.map(ts -> ts.getTimestampMillis() - startTime + "ms: " + ts.getValue())
				.take(5)
				.toBlocking()
				.subscribe(System.out::println);
	}

	@Test
	public void bufferTest() {
		Observable.range(1, 7)
				.buffer(3)
				.toBlocking()
				.subscribe(list -> System.out.println(list));
	}

	@Test
	public void bufferWindowTest() {
		Observable.range(1, 7)
				.buffer(3, 1)
				.toBlocking()
				.subscribe(System.out::println);
	}

	private Double averageOfList(List<Double> list) {
		return list.stream()
				.collect(Collectors.averagingDouble(x -> x));
	}

	@Test
	public void averageTest() {
		Random random = new Random();
		Observable.defer(() -> Observable.just(random.nextGaussian()))
				.repeat(1000)
				.buffer(100, 1)
				.map(this::averageOfList)
				.toBlocking()
				.subscribe(System.out::println);
	}

	@Test
	public void bufferTimePeriodTest() {
		Observable<String> names = Observable.just("Mary", "Patricia", "Linda", "Barbara", "Elisabeth", "Jennifer",
				"Maria", "Susan", "Margaret", "Dorothey");

		Observable<Long> absoluteDelays = Observable.just(0.1, 0.6, 0.9, 1.1, 3.3, 3.4, 3.5, 3.6, 4.4, 4.8)
				.map(d -> (long) (d * 1_000));

		Observable<String> delayedNames = Observable.zip(names, absoluteDelays, (n, d) -> Observable.just(n)
				.delay(d, TimeUnit.MILLISECONDS))
				.flatMap(o -> o);

		delayedNames.buffer(1, TimeUnit.SECONDS)
				.toBlocking()
				.subscribe(System.out::println);
	}

}
