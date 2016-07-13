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
    
    protected int[][][] colorTracker;
    
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
        
        setupColorTracker();
        edgeList = new LinkedList<DirectionalPixel>();
        record = new ImageRecord(width * height);
        pixelsFilled = 0;
        totalPixels = width * height;
    }
    
    public BufferedImage getImage(){
        return image;
    }
    
    // SCALING
    /*private void setupColorTracker(){
        if (width * height <= MAX_COLORS){
            fillColorTracker();
        } else {
            throw new IllegalArgumentException();
        }
    }*/
    
    private void setupColorTracker(){
        if (width * height == MAX_COLORS){
            fillColorTracker();
        } else if (width * height < MAX_COLORS){
            fillRandomColors();
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    // SCALING
    /*private void fillColorTracker(){
    	int pixels = width * height;
    	int arraySize = 1;
    	while (arraySize * arraySize * arraySize < pixels){
    		arraySize++;
    	}
    	colorScalar = (double) RGB_VALUES / (double) arraySize;
        colorTracker = new int[arraySize][arraySize][arraySize];
        for (int r = 0; r < arraySize; r++){
            for (int g = 0; g < arraySize; g++){
                for (int b = 0; b < arraySize; b++){
                    colorTracker[r][g][b] = new Color((int)(r * colorScalar), (int)(g * colorScalar), (int)(b * colorScalar)).getRGB();
                    //System.out.println(colorTracker[r][g][b]);
                }
            }
        }
        
    }*/
    
    private void fillColorTracker(){
        colorTracker = new int[256][256][256];
        for (int r = 0; r <= 255; r++){
            for (int g = 0; g<= 255; g++){
                for (int b = 0; b <= 255; b++){
                    colorTracker[r][g][b] = new Color(r, g, b, 255).getRGB();
                }
            }
        }
    }
    
    private void fillRandomColors(){
        colorTracker = new int[256][256][256];
        int target = width * height;
        for (int i = 0; i < target; i++){
            int r = rand.nextInt(256);
            int g = rand.nextInt(256);
            int b = rand.nextInt(256);
            if (colorTracker[r][g][b] == 0){
                colorTracker[r][g][b] = new Color(r, g, b, 255).getRGB();
            } else {
                i--;
            }
        }
    }
    
    /**
    public void saveImage(String path){
        File out = new File(path);
        try {
            ImageIO.write(image, "PNG", out);
        } catch (IOException e){
            System.out.println("Image save failed, exiting...");
            System.exit(0);
        }
    }*/
    
    protected void updateImage(int x, int y, int color){
        image.setRGB(x, y, color);
        record.addPixel(x, y, color);
    }
    
    public List<Integer> getClosestColors(int colorToMatch){
        List<Integer> closestColors = new ArrayList<Integer>();
        int rToCheck;
        int bToCheck;
        int gToCheck;
        int currentR = (colorToMatch >> 16) & 0xFF;
        int currentG = (colorToMatch >> 8) & 0xFF;
        int currentB = colorToMatch & 0xFF;
        //currentR /= colorScalar;
        //currentG /= colorScalar;
        //currentB /= colorScalar;
        int range = 0;
        do{
            range++;
            rToCheck = currentR - range;
            if (rToCheck >= 0){
                for (int g = -range; g <= range; g++){
                    for (int b = -range; b <= range; b++){
                        gToCheck = g + currentG;
                        bToCheck = b + currentB;
                        if (gToCheck >= 0 && bToCheck >= 0 && gToCheck < RGB_VALUES && bToCheck < RGB_VALUES &&
                            colorTracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(colorTracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            rToCheck = currentR + range;
            if (rToCheck < RGB_VALUES){
                for (int g = -range; g <= range; g++){
                    for (int b = -range; b <= range; b++){
                        gToCheck = g + currentG;
                        bToCheck = b + currentB;
                        if (gToCheck >= 0 && bToCheck >= 0 && gToCheck < RGB_VALUES && bToCheck < RGB_VALUES &&
                            colorTracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(colorTracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            gToCheck = currentG - range;
            if(gToCheck >= 0){
                for (int r = -range + 1; r <= range - 1; r++){
                    for (int b = -range; b <= range; b++){
                        rToCheck = r + currentR;
                        bToCheck = b + currentB;
                        if (rToCheck >= 0 && bToCheck >= 0 && rToCheck < RGB_VALUES && bToCheck < RGB_VALUES &&
                            colorTracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(colorTracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            gToCheck = currentG + range;
            if(gToCheck < colorTracker.length){
                for (int r = -range + 1; r <= range - 1; r++){
                    for (int b = -range; b <= range; b++){
                        rToCheck = r + currentR;
                        bToCheck = b + currentB;
                        if (rToCheck >= 0 && bToCheck >= 0 && rToCheck < RGB_VALUES && bToCheck < RGB_VALUES &&
                            colorTracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(colorTracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            bToCheck = currentB - range;
            if (bToCheck >= 0){
                for (int r = -range + 1; r <= range - 1; r++){
                    for (int g = -range + 1; g <= range - 1; g++){
                        rToCheck = r + currentR;
                        gToCheck = g + currentG;
                        if (rToCheck >= 0 && gToCheck >= 0 && rToCheck < RGB_VALUES && gToCheck < RGB_VALUES &&
                            colorTracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(colorTracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            bToCheck = currentB + range;
            if (bToCheck < RGB_VALUES){
                for (int r = -range + 1; r <= range - 1; r++){
                    for (int g = -range + 1; g <= range - 1; g++){
                        rToCheck = r + currentR;
                        gToCheck = g + currentG;
                        if (rToCheck >= 0 && gToCheck >= 0 && rToCheck < RGB_VALUES && gToCheck < RGB_VALUES &&
                            colorTracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(colorTracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
        } while (closestColors.size() == 0);
        return closestColors;
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
