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
		ImageRecord record = null;
		int chunksSent = 0;
		try {
			websocket.sendPing(PING_DATA);
			//System.out.println("Building " + width + " " + height + " " + individualPercent);
			image = new FastIterator(width, height, individualPercent);
			record = image.getRecord();
			while (!image.isFinished()){
				if (record.getNumChunks() > chunksSent){
					websocket.send("{\"type\": \"chunk\", \"chunk\": \"" + record.getChunk(chunksSent++) + "\"}");
				}
				image.nextPixel();
			}
		} catch (Exception e){
			System.out.print(e.getMessage());
			websocket.close(2, "Something went wrong while generating your image :(");
			return;
		}

		record.makeFinal();
		if (chunksSent < record.getNumChunks()){
			websocket.send("{\"type\": \"chunk\", \"chunk\": \"" + record.getChunk(chunksSent++) + "\"}");
		}
		websocket.send("{\"type\": \"done\"}");
	}
}
