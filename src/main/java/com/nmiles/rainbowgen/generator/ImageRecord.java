package com.nmiles.rainbowgen.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageRecord {
	
	private static final int RED_MASK = 0x00FF0000;
	
	private static final int GREEN_MASK = 0x0000FF00;
	
	private static final int BLUE_MASK = 0x000000FF;
	
	private static final int BOTTOM_X_MASK = 0x0000000F;
	
	private static final int BOTTOM_Y_MASK = 0x000000FF;
	
	private static final int CHUNK_SIZE = 5000;
	
	private static final int BYTES_PER_CHUNK = 6 * CHUNK_SIZE;
	
	private byte[] currentChunk;
	
	private List<byte[]> chunks;
	
	private boolean finished = false;
	
	private int chunkPointer;
	
	public ImageRecord(int numPixels){
		currentChunk = new byte[BYTES_PER_CHUNK];
		chunks = new ArrayList<>(numPixels / CHUNK_SIZE + 1);
		chunkPointer = 0;
	}
	
	//OLD
	/*public void addPixel(int x, int y, int color){
		sb.append(String.format("%03X%03X", x, y));
		sb.append(String.format("%06X", color & ALPHA_MASK));
		if (sb.length() == BYTES_PER_CHUNK){
			chunks.add(sb.toString());
			sb = new StringBuilder(BYTES_PER_CHUNK);
		}
	}*/
	
	public void addPixel(int x, int y, int color){
		// chunkPointer points to the first byte of this pixel's location
		// bits 11-4 of go here
		currentChunk[chunkPointer] = (byte) (x >> 4);
		chunkPointer++;
		// chunkPointer points to second position
		// bits 3-0 of x and 11-8 of y go here
		currentChunk[chunkPointer] = (byte) ((x & BOTTOM_X_MASK) << 4);
		currentChunk[chunkPointer] += (byte) (y >> 8);
		chunkPointer++;
		// chunkPointer points to third position
		// bits 7-0 of y go here
		currentChunk[chunkPointer] = (byte) ((y & BOTTOM_Y_MASK));
		chunkPointer++;
		// chunkPointer points to the fourth position
		// r goes here
		currentChunk[chunkPointer] = (byte) ((color & RED_MASK) >> 16);
		chunkPointer++;
		// chunkPointer points to the fifth position
		// g goes here
		currentChunk[chunkPointer] = (byte) ((color & GREEN_MASK) >> 8);
		chunkPointer++;
		// chunkPointer points to the sixth position
		// b goes here
		currentChunk[chunkPointer] = (byte) (color & BLUE_MASK);
		chunkPointer++;
		// chunkPointer points to next position
		if (chunkPointer >= currentChunk.length){
			chunks.add(currentChunk);
			currentChunk = new byte[BYTES_PER_CHUNK];
			chunkPointer = 0;
		}
	}
	
	//OLD
	/*public void makeFinal(){
		if (sb.length() != 0){
			chunks.add(sb.toString());
			sb = null;
			finished = true;
		}
	}*/
	
	public void makeFinal(){
		if (chunkPointer != 0){
			chunks.add(Arrays.copyOf(currentChunk, chunkPointer));
			currentChunk = null;
			finished = true;
		}
	}
	
	public int getNumChunks(){
		return chunks.size();
	}
	
	public byte[] getChunk(int i){
		if (finished){
			return chunks.get(i);
		} else {
			return null;
		}
	}
}
