package hello;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Subscriber;

public class SecondTryTest {

	private static ExecutorService pool;

	Logger LOGGER = LoggerFactory.getLogger(SecondTryTest.class);

	@BeforeClass
	public static void setUpClass() {
		pool = Executors.newFixedThreadPool(10);
	}

	@AfterClass
	public static void tearDownClass() {
		pool.shutdown();
	}

	@Test
	public void foo1Test() {

		Observable<String> data = getData();
		data.map((s) -> {
			return s + " daniel";
		}).subscribe(s -> System.out.println(s));
	}

	@Test
	public void foo2Test() {
		Observable<String> from = Observable.from(Arrays.asList("a", "b", "c", "ab", "cb", "ba"));
		from.map(s -> "DEBUG: " + s).filter(s -> s.contains("a")).subscribe(System.out::println);

	}

	@Test
	public void foo3Test() throws InterruptedException {
		getDataSlow().toBlocking().subscribe(number -> {
			LOGGER.debug("random generate number: " + number);
		}, ex -> {
			LOGGER.warn("ex was thrown: ", ex);
		});
	}

	@Test
	public void observableMergeTest() throws InterruptedException {
		Observable<Integer> dataSlow = getDataSlow();
		Observable<Integer> dataSlow2 = getDataSlow();

		Observable.merge(dataSlow, dataSlow2).toBlocking().subscribe(element -> {
			LOGGER.debug("merged observable numbers: " + element);
		});
	}

	@Test
	public void observableZipTest() throws InterruptedException {
		Observable<Integer> dataSlow = getDataSlow();
		Observable<String> dataSlow2 = getDataSlow2();
		Observable.zip(dataSlow, dataSlow2, (d1, d2) -> Arrays.asList(d1, d2)).toBlocking().subscribe(element -> {
			LOGGER.debug("merged observable numbers: " + element);
		});
	}

	@Test
	public void observablesInLoopTest() throws InterruptedException {
		long startTime = System.currentTimeMillis();
		List<Observable<Integer>> os = new ArrayList<Observable<Integer>>();
		for (int i = 0; i < 10; i++) {
			os.add(getDataSlow());
		}
		Observable.merge(os).toBlocking().subscribe(elements -> {
			LOGGER.debug("OUTPUT from merged observable numbers: " + elements);
		});
		LOGGER.debug("duration: " + (System.currentTimeMillis() - startTime) + "msec");
	}

	private Observable<String> getData() {
		return Observable.unsafeCreate(new Observable.OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> observer) {
				observer.onNext("hello");
				observer.onCompleted();
			}
		});
	}

	private Observable<Integer> getDataSlow() {
		return Observable.unsafeCreate(aSubscriber -> {
			try {
				pool.submit(() -> {
					try {
						Thread.sleep(500);
						Random rand = new Random();
						aSubscriber.onNext(rand.nextInt(1000));
						aSubscriber.onCompleted();
					} catch (InterruptedException ex) {
						LOGGER.warn("interupted exception: ", ex);
						aSubscriber.onError(ex);
					}
				});
			} catch (Exception ex) {
				LOGGER.warn("exception in getDataSlow(): ", ex);
				aSubscriber.onError(ex);
			}
		});
	}

	private Observable<String> getDataSlow2() {
		return Observable.unsafeCreate(aSubscriber -> {
			try {
				pool.submit(() -> {
					try {
						Thread.sleep(1000);
						aSubscriber.onNext(UUID.randomUUID().toString());
						aSubscriber.onCompleted();
					} catch (InterruptedException ex) {
						LOGGER.warn("interupted exception: ", ex);
						aSubscriber.onError(ex);
					}
				});
			} catch (Exception ex) {
				LOGGER.warn("exception in getDataSlow(): ", ex);
				aSubscriber.onError(ex);
			}
		});
	}

}
