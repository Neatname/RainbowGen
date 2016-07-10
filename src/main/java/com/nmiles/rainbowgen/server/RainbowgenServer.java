package com.nmiles.rainbowgen.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

public class RainbowgenServer {

	public static void main(String[] args) {
		HttpServer server = HttpServer.createSimpleServer();
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
		try {
			server.start();
			System.out.println("Press any key to stop the server...");
			System.in.read();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

}
