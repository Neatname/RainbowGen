package com.nmiles.rainbowgen.generator;

import java.util.*;

import java.awt.image.BufferedImage;

/**
 * This class is basically just a wrapper for X and Y values with a method for determining
 * which adjacent pixels are unpopulated in an image.
 * 
 * @author Nathan Miles
 *
 */
public class Pixel {
	/** The value that all blank pixels are set to in the BufferedImage */
	private static final int EMPTY_VALUE = -33554432;
	
    /** The x coordinate of this pixel */
    private short x;
    
    /** The x coordinate of this pixel */
    private short y;
    
    /**
     * Constructs a new Pixel with the given X and Y coordinates
     * @param x The x coordinate of the new Pixel
     * @param y The y coordinate of the new Pixel
     */
    public Pixel (int x, int y){
        this.x = (short)x;
        this.y = (short)y;
    }

    /**
     * Gets the x value of this Pixel
     * @return The x value
     */
    public int getX(){
        return x;
    }
    
    /**
     * Gets the y value of this Pixel
     * @return The y value
     */
    public int getY(){
        return y;
    }
    
    /**
     * Gets a List of all Pixels adjacent to this one in the BufferedImage that are
     * not yet populated.
     * @param image The image to search for adjacent pixels in
     * @return The List of all unpopulated, adjacent pixels
     */
    public List<Pixel> getPossibilities(BufferedImage image){
        List<Pixel> nextPossibilities = new ArrayList<Pixel>();
        int xToCheck;
        int yToCheck;
        for (int xDiff = -1; xDiff <= 1; xDiff++){
            for (int yDiff = -1; yDiff <= 1; yDiff++){
                xToCheck = x + xDiff;
                yToCheck = y + yDiff;
                try{
                    if (xToCheck >= 0 && yToCheck >= 0 && xToCheck < image.getWidth() && yToCheck < image.getHeight() && image.getRGB(xToCheck, yToCheck) == EMPTY_VALUE){
                        nextPossibilities.add(new Pixel(xToCheck, yToCheck));
                    }
                } catch (ArrayIndexOutOfBoundsException e){
                    // if we're at the edge of the image, just do nothing
                }
            }
        }
        return nextPossibilities;
    }
}