package com.nmiles.rainbowgen.generator;

import java.util.*;

import java.awt.image.BufferedImage;
public class DirectionalPixel {
    final private double[][] DIRECTIONS = new double[][] {{3.0, 2.0, 1.0},
                                                          {4.0, 0.0, 0.0},
                                                          {5.0, 6.0, 7.0}};

    private short x;
    private short y;
    private float direction;
    private float shapeFactor;

    public DirectionalPixel (XORShiftRandom rand, int width, int height){
        x = (short)rand.nextInt(width);
        y = (short)rand.nextInt(height);
        shapeFactor = (float)50.0;
        direction = simplify((float)rand.nextInt(100));
    }
    
    public DirectionalPixel (XORShiftRandom rand, int width, int height, float shapeFactor){
        x = (short)rand.nextInt(width);
        y = (short)rand.nextInt(height);
        this.shapeFactor = shapeFactor;
        direction = simplify((float)rand.nextInt(100));
    }
    
    public DirectionalPixel (DirectionalPixel parent, int[] location){
        shapeFactor = parent.getShapeFactor();
        direction = parent.newAverageDirection(location, shapeFactor);
        x = (short)location[0];
        y = (short)location[1];
    }
    
    public DirectionalPixel (DirectionalPixel parent, int x, int y){
        shapeFactor = parent.getShapeFactor();
        direction = parent.newAverageDirection(new int[] {x, y}, shapeFactor);
        this.x = (short)x;
        this.y = (short)y;
    }

    public DirectionalPixel (int x, int y, float direction, float shapeFactor){
        this.x = (short)x;
        this.y = (short)y;
        this.direction = direction;
        this.shapeFactor = shapeFactor;
    }

    public int getX(){
        return x;
    }
    
    public int getY(){
        return y;
    }
    
    public float getDirection(){
        return direction;
    }
    
    public float getShapeFactor(){
        return shapeFactor;
    }
    
    private float simplify (float direction){
        while (direction > 4.0 || direction < -4.0){    
            if (direction > 4.0){
                direction -= 8.0;
            } else {
                direction += 8.0;
            }
        }
        return direction;
    }
    
    private float getRelativeDirection (int[] next){
        return (float)DIRECTIONS[this.x - next[0] + 1][this.y - next[1] + 1];
    }
    
    private float newAverageDirection (int[] next, float shapeFactor){
        float newDirection = this.getRelativeDirection(next);
        while (Math.abs(this.direction - newDirection) > 4.0){
            if (this.direction - newDirection > 4.0){
                newDirection += 8.0;
            } else {
                newDirection -= 8.0;
            }
        }
        newDirection = (this.direction * shapeFactor + newDirection) / (shapeFactor + 1);
        return simplify(newDirection);
    }
    
    public List<DirectionalPixel> getSortedPossibilities (BufferedImage image, float curl){
        List<DirectionalPixel> nextPossibilities = new ArrayList<DirectionalPixel>();
        ArrayList<Float> differences = new ArrayList<Float>(8);
        int xToCheck;
        int yToCheck;
        for (int xDiff = -1; xDiff <= 1; xDiff++){
            for (int yDiff = -1; yDiff <= 1; yDiff++){
                xToCheck = x + xDiff;
                yToCheck = y + yDiff;
                try{
                    if (xToCheck >= 0 && yToCheck >= 0 && xToCheck < image.getWidth() && yToCheck < image.getHeight() && image.getRGB(xToCheck, yToCheck) == -33554432){
                        nextPossibilities.add(new DirectionalPixel(this, xToCheck, yToCheck));
                        differences.add(getDifference(this.direction - curl, this.getRelativeDirection(new int[] {nextPossibilities.get(nextPossibilities.size() - 1).getX(), nextPossibilities.get(nextPossibilities.size() - 1).getY()})));
                    }
                } catch (ArrayIndexOutOfBoundsException e){}
            }
        }
        boolean sorted = false;
        while (!sorted){
            sorted = true;
            for (int i = 1; i < differences.size(); i++){
                if (differences.get(i) < differences.get(i - 1)){
                    sorted = false;
                    differences.add(i - 1, differences.get(i));
                    differences.remove(i + 1);
                    nextPossibilities.add(i - 1, nextPossibilities.get(i));
                    nextPossibilities.remove(i + 1);
                }
            }
        }
        return nextPossibilities;
    }
    
    private float getDifference(float first, float second){
        while (Math.abs(first - second) > 4){
            if (first - second > 4){
                second += 8;
            } else {
                second -= 8;
            }
        }
        return Math.abs(first - second);
    }
    
    public List<DirectionalPixel> getPossibilities(BufferedImage image){
        List<DirectionalPixel> nextPossibilities = new ArrayList<DirectionalPixel>();
        int xToCheck;
        int yToCheck;
        for (int xDiff = -1; xDiff <= 1; xDiff++){
            for (int yDiff = -1; yDiff <= 1; yDiff++){
                xToCheck = x + xDiff;
                yToCheck = y + yDiff;
                try{
                    if (xToCheck >= 0 && yToCheck >= 0 && xToCheck < image.getWidth() && yToCheck < image.getHeight() && image.getRGB(xToCheck, yToCheck) == -33554432){
                        nextPossibilities.add(new DirectionalPixel(this, xToCheck, yToCheck));
                    }
                } catch (ArrayIndexOutOfBoundsException e){}
            }
        }
        return nextPossibilities;
    }

    public List<DirectionalPixel> getMorePossibilities(BufferedImage image) {
        List<DirectionalPixel> nextPossibilities = new ArrayList<DirectionalPixel>();
        int xToCheck;
        int yToCheck;
        for (int xDiff = -2; xDiff <= 2; xDiff++){
            for (int yDiff = -2; yDiff <= 2; yDiff++){
                xToCheck = x + xDiff;
                yToCheck = y + yDiff;
                try{
                    if (xToCheck >= 0 && yToCheck >= 0 && xToCheck < image.getWidth() && yToCheck < image.getHeight() && image.getRGB(xToCheck, yToCheck) == -33554432){
                        nextPossibilities.add(new DirectionalPixel(this, xToCheck, yToCheck));
                    }
                } catch (ArrayIndexOutOfBoundsException e){}
            }
        }
        return nextPossibilities;
    }
}