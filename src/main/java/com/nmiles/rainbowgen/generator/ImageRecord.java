package com.nmiles.rainbowgen.generator;

import java.util.ArrayList;
import java.util.List;

public class ImageRecord {
	
	private static final int ALPHA_MASK = 0x00FFFFFF;
	
	private static final int CHUNK_SIZE = 5000;
	
	private static final int BYTES_PER_CHUNK = 12 * CHUNK_SIZE;
	
	private StringBuilder sb;
	
	private List<String> chunks;
	
	private boolean finished = false;
	
	public ImageRecord(int numPixels){
		sb = new StringBuilder(BYTES_PER_CHUNK);
		chunks = new ArrayList<>(numPixels / CHUNK_SIZE + 1);
	}
	
	public void addPixel(int x, int y, int color){
		sb.append(String.format("%03X%03X", x, y));
		sb.append(String.format("%06X", color & ALPHA_MASK));
		if (sb.length() == BYTES_PER_CHUNK){
			chunks.add(sb.toString());
			sb = new StringBuilder(BYTES_PER_CHUNK);
		}
	}
	
	public void makeFinal(){
		if (sb.length() != 0){
			chunks.add(sb.toString());
			finished = true;
		}
	}
	
	public int getNumChunks(){
		return chunks.size();
	}
	
	public String getChunk(int i){
		if (finished){
			return chunks.get(i);
		} else {
			return null;
		}
	}
}
