package com.nmiles.rainbowgen.generator;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class ImageRecordTest {
	
	/*@Test
	public void testImageRecord(){
		ImageRecord ir = new ImageRecord(1);
		int r = 255;
		int g = 100;
		int b = 1;
		int color = (r << 16) + (g << 8) + b;
		ir.addPixel(5, 50, color);
		assertNull(ir.getChunk(0));
		ir.makeFinal();
		byte[] expected = {0, 80, 50, (byte) 255, 100, 1};
		byte[] actual = ir.getChunk(0);
		assertTrue(Arrays.equals(expected, actual));
	}*/
}
