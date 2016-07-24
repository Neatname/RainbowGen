package com.nmiles.rainbowgen.server;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.nmiles.rainbowgen.generator.FastIterator;
import com.nmiles.rainbowgen.generator.ImageRecord;
import com.nmiles.rainbowgen.generator.RandomImage;
import com.nmiles.rainbowgen.generator.StainedGlass;

/**
 * The WebSocket endpoint for the server. This class manages everything about
 * the WebSocket's lifetime.
 * 
 * @author Nathan Miles
 *
 */
public class ImageGeneratorApplication extends WebSocketApplication {
	/** The max dimension that any image may have in the x or y direction. */
	private static final int MAX_DIMENSIONS = 4096;
	/**
	 * The highest allowable percentage. Percentages are represented from 0-1000
	 * to allow for more precision.
	 */
	private static final int MAX_PERCENT = 999;
	/** A blank byte array for sending pings. */
	private static final byte[] PING_DATA = {};

	/**
	 * Called when a connected WebSocket sends a message.
	 */
	@Override
	public void onMessage(WebSocket websocket, String data) {
		ImageThread t = new ImageThread(websocket, data);
		t.start();
	}

	/**
	 * Called when a WebSocket disconnects or is disconnected from the server.
	 */
	@Override
	public void onClose(WebSocket socket, DataFrame frame) {
		ChunkScheduler.getInstance().remove(socket);
	}

	/**
	 * A custom implementation of java.lang.Thread that generates an image from
	 * parameters given during construction.
	 * 
	 * @author Nathan Miles
	 *
	 */
	private class ImageThread extends Thread {
		/** The WebSocket that requested the image */
		private WebSocket websocket;
		/** The data that was originally sent with the image request */
		private String data;

		/**
		 * Constructs a new ImageThread for the given websocket with the
		 * parameters given in data.
		 * 
		 * @param websocket
		 *            The WebSocket that requested the image
		 * @param data
		 *            The data that was originally sent with the image request
		 */
		private ImageThread(WebSocket websocket, String data) {
			this.websocket = websocket;
			this.data = data;
		}

		/**
		 * Invoked when the ImageThread is started.
		 */
		public void run() {
			// read and check input
			String type = "";
			JSONObject obj = null;
			int width = 0, height = 0;
			System.out.println("Creating image: " + data);
			// parse the JSON contained in data
			try {
				JSONParser parser = new JSONParser();
				obj = (JSONObject) parser.parse(data);
				type = (String) obj.get("type");
				width = ((Long) obj.get("width")).intValue();
				height = ((Long) obj.get("height")).intValue();
				if (width <= 0 || height <= 0 || width > MAX_DIMENSIONS
						|| height > MAX_DIMENSIONS) {
					throw new IllegalArgumentException();
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				websocket.send("Parse error");
				return;
			}
			ChunkScheduler scheduler = ChunkScheduler.getInstance();
			ConcurrentLinkedQueue<String> queue = scheduler.register(websocket);
			// build image
			RandomImage image = null;
			ImageRecord record = null;
			int chunksSent = 0;
			try {
				/*
				 * Send a ping just in case the client was getting jumpy about
				 * timing out
				 */
				websocket.sendPing(PING_DATA);
				/*
				 * If you add a new image type, you need to add another option
				 * here.
				 */
				switch (type) {
					case "fastIterator":
						int individualPercent = ((Long) obj.get("individualPercent")).intValue();
						if (individualPercent < 0
								|| individualPercent > MAX_PERCENT) {
							throw new IllegalArgumentException("Invalid percent");
						}
						image = new FastIterator(width, height, individualPercent);
						break;
					case "stainedGlass":
						int startingPoints = ((Long) obj.get("startingPoints")).intValue();
						image = new StainedGlass(width, height, startingPoints);
						break;
				}

				record = image.getRecord();
				while (!image.isFinished()) {
					if (record.getNumChunks() > chunksSent) {
						if (!websocket.isConnected()) {
							return;
						}
						queue.add("{\"type\": \"chunk\", \"chunk\": \""
								+ record.getChunk(chunksSent++) + "\"}");
					}
					image.nextPixel();
				}
			} catch (Exception e) {
				System.out.print(e.getMessage());
				e.printStackTrace();
				queue.add("{\"type\": \"error\", \"message\": \"Something went wrong while generating your image.\"}");
				return;
			}

			record.makeFinal();
			if (chunksSent < record.getNumChunks()) {
				queue.add("{\"type\": \"chunk\", \"chunk\": \""
						+ record.getChunk(chunksSent++) + "\"}");
			}
			queue.add("{\"type\": \"done\"}");
		}
	}
}
