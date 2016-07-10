package com.nmiles.rainbowgen.server;

import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.http.server.HttpServer;

public class RainbowgenServer {

	public static void main(String[] args) {
		int port = Integer.parseInt(System.getenv("PORT"));
		HttpServer server = HttpServer.createSimpleServer("static/", "0.0.0.0", port);
		
		try {
			server.start();
			while (true){
				TimeUnit.SECONDS.sleep(10);
			}
			//System.out.println("Press any key to stop the server...");
			//System.in.read();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

}
