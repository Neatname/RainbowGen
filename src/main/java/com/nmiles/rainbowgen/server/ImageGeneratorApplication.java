package com.nmiles.rainbowgen.server;

import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;

public class ImageGeneratorApplication extends WebSocketApplication {

	@Override
	public void onMessage(WebSocket websocket, String data){
		websocket.send(data);
	}
	
}
