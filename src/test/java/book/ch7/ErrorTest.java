package book.ch7;

import java.time.LocalDate;
import java.time.Month;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;

public class ErrorTest {

	Logger LOGGER = LoggerFactory.getLogger(ErrorTest.class);

	@Test
	public void subscribeErrorHandlerTest() {
		Observable.unsafeCreate(subscriber -> {
			try {
				subscriber.onNext(1.0);
				subscriber.onNext(1 / 0);
			} catch (Exception e) {
				subscriber.onError(e);
			}
		})
				.subscribe(System.out::println, e -> LOGGER.error("error", e));
	}

	@Test
	public void flatMapExceptionHandlingTest() {
		Observable.just(1, 0)
				.flatMap(
						x -> (x == 0) ? Observable.error(new ArithmeticException("Zero :-<")) : Observable.just(10 / x))
				.subscribe(System.out::println, e -> LOGGER.error("error", e));
	}

	@Test
	public void flatMapExceptionHandling2Test() {
		Observable.just(1, 0, 2)
				.map(x -> (10 / x))
				.flatMap(x -> Observable.just(x), e -> Observable.empty(), Observable::empty)
				.subscribe(System.out::println, e -> LOGGER.error("error", e));
	}

	@Test
	public void flatMapExceptionHandling3Test() {
		Observable.just(1, 0, 2)
				.flatMap(x -> Observable.fromCallable(() -> (10 / x))
						.onErrorResumeNext(err -> Observable.just(42)))
				.subscribe(System.out::println, e -> LOGGER.error("error", e));
	}

	@Test
	public void onErrorReturnTest() {
		Observable.just(1, 0)
				.map(x -> (10 / x))
				.onErrorReturn(error -> 42)
				.subscribe(System.out::println, e -> LOGGER.error("error", e));
	}

	@Test
	public void onErrorResumeTest() {
		Observable.just(1, 0, 2)
				.map(x -> (10 / x))
				.onErrorResumeNext(error -> Observable.just(42))
				.subscribe(System.out::println, e -> LOGGER.error("error", e));
	}

	private Observable<LocalDate> nextSolarEclipse(LocalDate after) {
		return Observable
				.just(LocalDate.of(2016, Month.MARCH, 9), LocalDate.of(2016, Month.SEPTEMBER, 1),
						LocalDate.of(2017, Month.FEBRUARY, 26), LocalDate.of(2017, Month.AUGUST, 21),
						LocalDate.of(2018, Month.FEBRUARY, 15), LocalDate.of(2018, Month.JULY, 13),
						LocalDate.of(2018, Month.AUGUST, 11), LocalDate.of(2019, Month.JANUARY, 6),
						LocalDate.of(2019, Month.JULY, 2), LocalDate.of(2019, Month.DECEMBER, 26))
				.skipWhile(date -> !date.isAfter(after))
				.zipWith(Observable.interval(500, 50, TimeUnit.MILLISECONDS), (date, x) -> date);
	}

	@Test
	public void timeoutTest() {
		nextSolarEclipse(LocalDate.of(2016, Month.SEPTEMBER, 1))
				.timeout(() -> Observable.timer(1000, TimeUnit.MILLISECONDS),
						date -> Observable.timer(100, TimeUnit.MILLISECONDS))
				.toBlocking()
				.subscribe(System.out::println, Throwable::printStackTrace);
	}

	@Test
	public void timeintervalTest() {
		nextSolarEclipse(LocalDate.of(2016, Month.SEPTEMBER, 1)).timeInterval()
				.toBlocking()
				.subscribe(System.out::println, Throwable::printStackTrace);
	}

	private Observable<String> risky() {
		return Observable.fromCallable(() -> {
			if (Math.random() < 0.1) {
				Thread.sleep((long) (Math.random() * 2000));
				return "OK";
			} else {
				throw new RuntimeException("Transient");
			}
		});
	}

	@Test
	public void retryTest() {
		risky().timeout(1, TimeUnit.SECONDS)
				.doOnError(th -> LOGGER.warn("ERROR Will retry"))
				.retry(5) // retry max 5 times
				.subscribe(System.out::println, th -> LOGGER.error("retry exhausted", th));
	}

	@Test
	public void retryWithConditionTest() {
		risky().timeout(1, TimeUnit.SECONDS)
				.doOnError(th -> LOGGER.warn("ERROR Will retry" + th.getClass()))
				.retry((attempt, e) -> {
					return attempt <= 3 && !(e instanceof TimeoutException);
				})
				.subscribe(System.out::println, th -> LOGGER.error("retry exhausted", th));
	}

	@Test
	public void retryWithBackoffTest() {
		risky().timeout(1, TimeUnit.SECONDS)
				.doOnError(th -> LOGGER.warn("ERROR Will retry" + th.getClass()))
				.retryWhen(failures -> failures.delay(1, TimeUnit.SECONDS))
				.toBlocking()
				.subscribe(System.out::println, th -> LOGGER.error("retry exhausted", th));
	}

	static final int MAX_ATTEMPTS = 3;

	private Observable<Long> handleRetryAttempt(Throwable err, int attempt) {
		switch (attempt) {
		case 1:
			return Observable.just(42L);
		case MAX_ATTEMPTS:
			return Observable.error(err);
		default:
			long expDelay = (long) Math.pow(2, attempt - 2);
			return Observable.timer(expDelay, TimeUnit.SECONDS);
		}
	}

	@Test
	public void retryWithExponantialBackoffTest() {
		risky().timeout(1, TimeUnit.SECONDS)
				.doOnError(th -> LOGGER.warn("ERROR Will retry" + th.getClass()))
				.retryWhen(failures -> failures.zipWith(Observable.range(1, MAX_ATTEMPTS), this::handleRetryAttempt)
						.flatMap(x -> x))
				.toBlocking()
				.subscribe(System.out::println, th -> LOGGER.error("retry exhausted", th));
	}
}
