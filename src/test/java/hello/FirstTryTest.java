package hello;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class FirstTryTest {

	private final ExecutorService pool = Executors.newCachedThreadPool();

	@Test
	public void helloTest() {
		List<String> names = new ArrayList<String>();

		names.add("Ben");
		names.add("George");

		Observable.from(names).subscribe(s -> System.out.println("hello " + s + "!"));
	}

	@Test
	public void observableBlocking() throws InterruptedException {
		Observable<String> customObservableBlocking = customObservableBlocking();

		customObservableBlocking.subscribe(it -> {
			System.out.println("hello from blocking observable " + it);
		});
	}

	@Test
	public void observableNonBlocking() {
		Observable<String> o = customObservableNonBlocking();
		Observable<String> o2 = Observable.just("a", "b", "c");
		Observable.merge(o, o2).toBlocking().subscribe((it) -> {
			System.out.println(it);
		});
	}

	private Observable<String> customObservableBlocking() {
		Observable<String> o = Observable.unsafeCreate(aSubscriber -> {

			for (int i = 0; i < 10; i++) {
				if (aSubscriber.isUnsubscribed() == false) {
					aSubscriber.onNext("value_" + i);
				}
			}

			if (aSubscriber.isUnsubscribed() == false) {
				aSubscriber.onCompleted();
			}
		});

		return o;
	}

	private Observable<String> customObservableNonBlocking() {
		Observable<String> o = Observable.unsafeCreate(aSubscriber -> {

			pool.execute(() -> {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (int i = 0; i < 10; i++) {
					if (aSubscriber.isUnsubscribed() == false) {
						aSubscriber.onNext("non_value_" + i);
					}
				}

				if (aSubscriber.isUnsubscribed() == false) {
					aSubscriber.onCompleted();
				}
			});
		});

		return o;
	}

	@Test
	public void mapObservable() {
		Observable<Integer> numbers = Observable.range(0, 10);
		numbers.map(item -> item * 3).filter(item -> item % 2 == 0).subscribe(item -> System.out.println(item));
	}

	@Test
	public void flatMapTest() {
		Observable<Integer> numbers = Observable.range(0, 10);

		Func1<Integer, Observable<Integer>> multiples = n -> Observable.from(Arrays.asList(n * 2, n * 3));

		numbers.flatMap(multiples).subscribe((i) -> System.out.println(i));
	}

	@Test
	public void collectTest() {
		long startTime = System.currentTimeMillis();
		Observable.range(1, 10).subscribeOn(Schedulers.io()).subscribe(i -> System.out.println("i = " + randInt()));

		System.out.println("duration: " + (System.currentTimeMillis() - startTime) + "msec");

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private Integer randInt() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Random random = new Random();
		return random.nextInt(1000);
	}
}
