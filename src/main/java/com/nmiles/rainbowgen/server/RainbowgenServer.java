package com.nmiles.rainbowgen.server;

import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;

public class RainbowgenServer {

	public static void main(String[] args) {
		int port = Integer.parseInt(System.getenv("PORT"));
		HttpServer server = HttpServer.createSimpleServer("static/", "0.0.0.0", port);
		server.getListener("grizzly").registerAddOn(new WebSocketAddOn());
		
		final WebSocketApplication imageMaker = new ImageGeneratorApplication();
		WebSocketEngine.getEngine().register("", "/imagesocket", imageMaker);
		
		try {
			server.start();
			while (true){
				TimeUnit.SECONDS.sleep(10);
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}

}
