package book.ch3;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import rx.Observable;

public class Chapter3Test {

	private Observable<String> speak(String quote, long millisPerChar) {
		String[] tokens = quote.replaceAll("[:,]", "").split(" ");
		Observable<String> words = Observable.from(tokens);
		Observable<Long> absoluteDelay = words.map(String::length).map(len -> len * millisPerChar)
				.scan((total, current) -> total + current);

		return words.zipWith(absoluteDelay.startWith(0L), Pair::of)
				.flatMap(pair -> Observable.just(pair.getLeft()).delay(pair.getRight(), TimeUnit.MILLISECONDS));

	}

	@Test
	public void speakMergeTest() {
		Observable<String> alice = speak("1 To be, or not to be: that is the question", 3);
		Observable<String> bob = speak("2 Though this be madness, yet there is method in't", 2);
		Observable<String> jane = speak(
				"3 there is more things in Heaven and Earth, Horatio, than are dreamt of in your philosophy", 1);

		Observable.merge(alice.map(w -> "Alice: " + w), bob.map(w -> "Bob: " + w), jane.map(w -> "Jane: " + w))
				.toBlocking().subscribe(System.out::println);

	}

	@Test
	public void speakConcatTest() {
		Observable<String> alice = speak("1 To be, or not to be: that is the question", 3);
		Observable<String> bob = speak("2 Though this be madness, yet there is method in't", 2);
		Observable<String> jane = speak(
				"3 there is more things in Heaven and Earth, Horatio, than are dreamt of in your philosophy", 1);

		Observable.concat(alice.map(w -> "Alice: " + w), bob.map(w -> "Bob: " + w), jane.map(w -> "Jane: " + w))
				.toBlocking().subscribe(System.out::println);
	}

	private <T> Observable.Transformer<T, T> odd() {
		Observable<Boolean> trueFalse = Observable.just(true, false).repeat();
		return upstream -> upstream.zipWith(trueFalse, Pair::of).filter(Pair::getRight).map(Pair::getLeft);
	}

	@Test
	public void composeTest() {
		Observable<Character> alphabet = Observable.range(0, 'Z' - 'A' + 1).map(c -> (char) ('A' + c));

		alphabet.compose(odd()).forEach(System.out::println);
		System.out.println("-----------------");
		alphabet.compose(odd()).subscribe(System.out::println);
	}
}
