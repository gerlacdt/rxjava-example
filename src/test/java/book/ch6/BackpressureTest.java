package book.ch6;

import org.junit.Test;

import hello.Dish;
import rx.Observable;
import rx.observables.SyncOnSubscribe;
import rx.schedulers.Schedulers;

public class BackpressureTest {

	private void sleepMillis(int n) {
		try {
			Thread.sleep(n);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void simpleTest() {
		Observable<Dish> dishes = Observable.range(1, 1_000_000)
				.map(Dish::new);

		dishes.subscribe(x -> {
			System.out.println("Washing: " + x);
			sleepMillis(50);
		});
	}

	@Test
	public void simpleOtherThreadTest() {
		Observable<Dish> dishes = Observable.range(1, 1_000_000)
				.map(Dish::new);

		dishes.observeOn(Schedulers.io())
				.toBlocking()
				.subscribe(x -> {
					System.out.println("Washing: " + x);
					sleepMillis(50);
				}, Throwable::printStackTrace);
	}

	private Observable<Integer> myRange(int from, int count) {
		return Observable.unsafeCreate(subscriber -> {
			int i = from;
			while (i < from + count) {
				if (!subscriber.isUnsubscribed()) {
					subscriber.onNext(i++);
				} else {
					return;
				}
			}
			subscriber.onCompleted();
		});
	}

	@Test
	public void brokenBackpressureTest() {
		Observable<Dish> dishes = myRange(1, 1_000_000).map(Dish::new);

		dishes.observeOn(Schedulers.io())
				.toBlocking()
				.subscribe(x -> {
					System.out.println("Washing: " + x);
					sleepMillis(50);
				}, Throwable::printStackTrace);
	}
	
	@Test
	public void safeBackpressureTest() {
		Observable.OnSubscribe<Integer> onSubscribe = SyncOnSubscribe.createStateful(() -> 0, (cur, observer) -> {
			observer.onNext(cur);
			return cur + 1;
		});
		Observable<Integer> naturals = Observable.unsafeCreate(onSubscribe);
		Observable<Dish> dishes = naturals.map(Dish::new);

		dishes.observeOn(Schedulers.io())
				.toBlocking()
				.subscribe(x -> {
					System.out.println("Washing: " + x);
					//sleepMillis(50);
				}, Throwable::printStackTrace);
	}
}
