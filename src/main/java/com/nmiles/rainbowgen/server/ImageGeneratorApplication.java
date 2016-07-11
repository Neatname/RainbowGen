package com.nmiles.rainbowgen.server;

import java.util.Scanner;

import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;

import com.nmiles.rainbowgen.generator.FastIterator;
import com.nmiles.rainbowgen.generator.ImageRecord;
import com.nmiles.rainbowgen.generator.RandomImage;

public class ImageGeneratorApplication extends WebSocketApplication {
	
	private static final int MAX_DIMENSIONS = 4096;
	
	private static final int MAX_PERCENT = 4096;
	
	private static final int MILLIS_IN_A_SECOND = 1000;
	
	private static final int PING_INTERVAL = 15;
	
	private static final byte[] PING_DATA = {};

	@Override
	public void onMessage(WebSocket websocket, String data){
		if (data == null || data.length() < 4){
			throw new IllegalArgumentException();
		}
		
		// read and check input
		int width = 0, height = 0, individualPercent = 0;
		try {
			Scanner s = new Scanner(data);
			if (!s.next().equals("new")){
				s.close();
				throw new IllegalArgumentException();
			}
			width = s.nextInt();
			height = s.nextInt();
			individualPercent = s.nextInt();
			s.close();
			if (width <= 0|| height <= 0 || individualPercent <= 0 ||
				width > MAX_DIMENSIONS || height > MAX_DIMENSIONS || individualPercent > MAX_PERCENT){
				throw new IllegalArgumentException();
			}
		} catch (Exception e){
			websocket.close(1, "Error: invalid request");
			return;
		}
		
		// build image
		RandomImage image = null;
		try {
			websocket.sendPing(PING_DATA);
			long lastPing = System.currentTimeMillis();
			image = new FastIterator(width, height, individualPercent);
			while (!image.isFinished()){
				if (System.currentTimeMillis() - lastPing > PING_INTERVAL * MILLIS_IN_A_SECOND){
					websocket.sendPing(PING_DATA);
					lastPing = System.currentTimeMillis();
				}
				image.nextPixel();
			}
			websocket.send("generated");
		} catch (Exception e){
			websocket.close(2, "Something went wrong while generating your image :(");
			return;
		}
		
		//send image in chunks
		ImageRecord record = image.getRecord();
		record.makeFinal();
		int chunks = record.getNumChunks();
		for (int i = 0; i < chunks; i++){
			websocket.send(record.getChunk(i));
		}
		
		websocket.send("done");
		websocket.close();
	}
	
}
