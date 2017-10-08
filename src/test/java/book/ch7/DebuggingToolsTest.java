package book.ch7;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import rx.Notification;
import rx.Observable;
import rx.exceptions.CompositeException;
import rx.observables.BlockingObservable;
import rx.observers.TestSubscriber;

public class DebuggingToolsTest {

	Logger LOGGER = LoggerFactory.getLogger(DebuggingToolsTest.class);

	@Test
	public void checkOrderAndResultTest() {
		List<String> list = Observable.range(1, 3)
				.concatMap(x -> Observable.just(x, -x))
				.map(Object::toString)
				.toList()
				.toBlocking()
				.single();

		assertEquals(list, Arrays.asList("1", "-1", "2", "-2", "3", "-3"));
	}

	@Test
	public void checkErrorPropagationTest() {
		File file = new File("404.txt");
		BlockingObservable<String> fileContents = Observable.fromCallable(() -> Files.toString(file, UTF_8))
				.toBlocking();

		try {
			fileContents.single();
			fail("should not reach this point");
		} catch (RuntimeException expected) {
			expected.printStackTrace();
			assertEquals(expected.getCause()
					.getClass(), FileNotFoundException.class);
		}
	}

	@Test
	public void materializeTest() {
		Observable<Notification<Integer>> notifications = Observable.just(3, 0, 2, 0, 1, 0)
				.concatMapDelayError(x -> Observable.fromCallable(() -> 100 / x))
				.materialize();

		List<Notification.Kind> kinds = notifications.map(Notification::getKind)
				.toList()
				.toBlocking()
				.single();

		assertEquals(kinds, Arrays.asList(Notification.Kind.OnNext, Notification.Kind.OnNext, Notification.Kind.OnNext,
				Notification.Kind.OnError));
	}

	@Test
	public void testSubscriberTest() {
		Observable<Integer> obs = Observable.just(3, 0, 2, 0, 1, 0)
				.concatMapDelayError(x -> Observable.fromCallable(() -> 100 / x));

		TestSubscriber<Integer> ts = new TestSubscriber<>();
		obs.subscribe(ts);

		ts.assertValues(33, 50, 100);
		// ts.assertError(ArithmeticException.class);  // fails
		ts.assertError(CompositeException.class);

	}
}
