package rxhttp;

import io.reactivex.netty.protocol.http.server.HttpServer;
import rx.Observable;

public class RxHttpServer {

	public static void main(String[] args) {
		HttpServer.newServer(8080).start((req, resp) -> {
			Observable<String> response = Observable.just("Hello World");
			return resp.writeString(response);
		}).awaitShutdown();
	}
}
