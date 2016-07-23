package com.nmiles.rainbowgen.server;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.nmiles.rainbowgen.generator.FastIterator;
import com.nmiles.rainbowgen.generator.ImageRecord;
import com.nmiles.rainbowgen.generator.RandomImage;
import com.nmiles.rainbowgen.generator.StainedGlass;

public class ImageGeneratorApplication extends WebSocketApplication {
	
	private static final int MAX_DIMENSIONS = 4096;
	
	private static final int MAX_PERCENT = 999;
	
	private static final byte[] PING_DATA = {};

	@Override
	public void onMessage(WebSocket websocket, String data){
		ImageThread t = new ImageThread(websocket, data);
		t.start();
	}
	
	@Override
	public void onClose(WebSocket socket, DataFrame frame){
		ChunkScheduler.getInstance().remove(socket);
	}
	
	private class ImageThread extends Thread {
		WebSocket websocket;
		String data;
		private ImageThread(WebSocket websocket, String data){
			this.websocket = websocket;
			this.data = data;
		}
		
		public void run() {
			// read and check input
			String type = "";
			JSONObject obj = null;
			int width = 0, height = 0;
			System.out.println("Creating image: " + data);
			try {
				JSONParser parser = new JSONParser();
				obj = (JSONObject) parser.parse(data);
				type = (String) obj.get("type");
				width = ((Long) obj.get("width")).intValue();
				height = ((Long) obj.get("height")).intValue();
				if (width <= 0|| height <= 0 ||
					width > MAX_DIMENSIONS || height > MAX_DIMENSIONS){
					throw new IllegalArgumentException();
				}
			} catch (Exception e){
				System.out.println(e.getMessage());
				websocket.send("Parse error");
				return;
			}
			ChunkScheduler scheduler = ChunkScheduler.getInstance();
			ConcurrentLinkedQueue <String> queue = scheduler.register(websocket);
			// build image
			RandomImage image = null;
			ImageRecord record = null;
			int chunksSent = 0;
			try {
				websocket.sendPing(PING_DATA);
				switch (type){
				case "fastIterator":
					int individualPercent = ((Long) obj.get("individualPercent")).intValue();
					if (individualPercent < 0 || individualPercent > MAX_PERCENT){
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
				while (!image.isFinished()){
					if (record.getNumChunks() > chunksSent){
						if (!websocket.isConnected()){
							return;
						}
						queue.add("{\"type\": \"chunk\", \"chunk\": \"" + record.getChunk(chunksSent++) + "\"}");
					}
					image.nextPixel();
				}
			} catch (Exception e){
				System.out.print(e.getMessage());
				e.printStackTrace();
				queue.add("{\"type\": \"error\", \"message\": \"Something went wrong while generating your image.\"}");
				return;
			}

			record.makeFinal();
			if (chunksSent < record.getNumChunks()){
				queue.add("{\"type\": \"chunk\", \"chunk\": \"" + record.getChunk(chunksSent++) + "\"}");
			}
			queue.add("{\"type\": \"done\"}");
		}
	}
}
