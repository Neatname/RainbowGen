package com.nmiles.rainbowgen.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.StaticHttpHandler;

public class RainbowgenServer {

	public static void main(String[] args) {
		int port = Integer.parseInt(System.getenv("PORT"));
		HttpServer server = HttpServer.createSimpleServer("static/", "0.0.0.0", port);
		server.getServerConfiguration().addHttpHandler(new HttpHandler() {
			public void service(Request request, Response response) throws Exception {
				final SimpleDateFormat format = new SimpleDateFormat(
						"EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
				final String date = format.format(new Date(System
						.currentTimeMillis()));
				response.setContentType("text/plain");
				response.setContentLength(date.length());
				response.getWriter().write(date);
			}
		}, "/time");
		
		//server.getServerConfiguration().addHttpHandler(new StaticHttpHandler("static/"), "/");
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
