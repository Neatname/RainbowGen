package com.nmiles.rainbowgen.generator;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

public abstract class RandomImage {
    /** this is the number of colors representable in 32 bits. */
    public static final int MAX_COLORS = 16777216;
    
    public static final int RGB_VALUES = 256;
    
    public static final int MAX_COLOR_VALUE = 255;
    
    private static final int ALPHA_MASK = 0x00FFFFFF;
    
    protected int width;
    
    protected int height;
    
    protected ColorTracker colorTracker;
    
    protected BufferedImage image;
    
    private int totalPixels;
    
    protected int pixelsFilled;
    
    //protected double colorScalar;
    
    protected List<DirectionalPixel> edgeList;
    
    protected XORShiftRandom rand;
    
    private ImageRecord record;
    
    
    public RandomImage(int width, int height){
        if (width * height > MAX_COLORS){
            throw new IllegalArgumentException();
        }
        this.width = width;
        this.height = height;
        rand = new XORShiftRandom();
        
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(new Color (0, 0, 0, 254));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        
        colorTracker = new ColorTracker(width * height);
        record = new ImageRecord(width * height);
        pixelsFilled = 0;
        totalPixels = width * height;
    }
    
    public BufferedImage getImage(){
        return image;
    }
    
    

    
    protected void updateImage(int x, int y, int color){
        image.setRGB(x, y, color);
        record.addPixel(x, y, color);
        colorTracker.markUsed(color);
    }
    
    
    
    public boolean isFinished(){
        return edgeList.size() == 0;
    }
    
    public abstract void nextPixel();
    
    public int getQueueSize(){
        return edgeList.size();
    }
    
    public ImageRecord getRecord(){
    	return record;
    }
    
    public int percentDone(){
    	return pixelsFilled * 100 / totalPixels;
    }
}
