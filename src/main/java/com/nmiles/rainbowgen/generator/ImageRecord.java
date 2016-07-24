package com.nmiles.rainbowgen.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * A record of image generation for the RainbowGen program. It contains all
 * necessary information about a given image to recreate it, namely the x and y
 * coordinates, color, and order the pixels were populated in. The record is
 * stored in chunks. The chunks are represented as Strings. Each pixel within
 * each chunk is represented by hex values in the form XXXYYYCCCCCC where the Xs
 * are the hex equivalent of the x coordinate, the Ys of the y, and the Cs are
 * the hex value of the color. The order of the pixels is the order in which
 * they were added to the ImageRecord with lower-index chunks holding earlier
 * pixels.
 * 
 * @author Nathan Miles
 *
 */
public class ImageRecord {
	/**
	 * A mask used to strip the alpha channel from a color as defined by the
	 * Color class in the java.awt package, which has alpha in the top 4 bits.
	 */
	private static final int ALPHA_MASK = 0x00FFFFFF;
	/** The number of pixels in each record chunk. */
	private static final int CHUNK_SIZE = 2000;
	/** The number of bytes in each chunk. */
	private static final int BYTES_PER_CHUNK = 12 * CHUNK_SIZE;
	/** Used to build each chunk to avoid a bunch of String concatenation. */
	private StringBuilder sb;
	/** The list of all chunks that have been generated thus far. */
	private List<String> chunks;

	/**
	 * Constructs a new, initially empty ImageRecord.
	 * 
	 * @param numPixels
	 *            The number of pixels that this ImageRecord will eventually
	 *            contain. This number may technically be any positive number,
	 *            but passing the real value is recommended for efficiency. If
	 *            incorrect, the backing ArrayList may have to be resized, or
	 *            too much memory may be used.
	 */
	public ImageRecord(int numPixels) {
		chunks = new ArrayList<>(numPixels / CHUNK_SIZE + 1);
		sb = new StringBuilder(BYTES_PER_CHUNK);
	}

	/**
	 * Adds a pixel to the record.
	 * 
	 * @param x
	 *            The x coordinate of the pixel to add.
	 * @param y
	 *            The y coordinate of the pixel to add.
	 * @param color
	 *            The color of the pixel to add.
	 */
	public void addPixel(int x, int y, int color) {
		sb.append(String.format("%03X%03X", x, y));
		sb.append(String.format("%06X", color & ALPHA_MASK));
		if (sb.length() == BYTES_PER_CHUNK) {
			chunks.add(sb.toString());
			sb = new StringBuilder(BYTES_PER_CHUNK);
		}
	}

	/**
	 * Finalizes the ImageRecord. This method MUST be called on the completion
	 * of image generation. If it is not, the final chunk may not be saved.
	 */
	public void makeFinal() {
		if (sb.length() != 0) {
			chunks.add(sb.toString());
			sb = null;
		}
	}

	/**
	 * Gets the number of chunks in this ImageRecord.
	 * 
	 * @return The number of chunks
	 */
	public int getNumChunks() {
		return chunks.size();
	}

	/**
	 * Gets the chunk at the given index in the backing List.
	 * 
	 * @param i
	 *            The index of the chunk to get.
	 * @return The chunk
	 */
	public String getChunk(int i) {
		return chunks.get(i);
	}
}
