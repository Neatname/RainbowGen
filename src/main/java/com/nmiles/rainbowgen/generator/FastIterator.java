package com.nmiles.rainbowgen.generator;


import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class FastIterator extends RandomImage {

    private int individualPercent;
    
    private ListIterator<DirectionalPixel> edgeIterator;
    
    public FastIterator(int width, int height, int individualPercent){
        
        super(width, height);
        this.individualPercent = individualPercent;
        edgeList = new LinkedList<DirectionalPixel>();
        DirectionalPixel pixelToAdd = new DirectionalPixel(rand, width, height);
        edgeList.add(pixelToAdd);
        
        int colorToAdd = colorTracker.getRandomUnused();
        
        updateImage(pixelToAdd.getX(), pixelToAdd.getY(), colorToAdd);
        
        edgeIterator = edgeList.listIterator();
    }
    @Override
    public void nextPixel() {
        
        DirectionalPixel toAddTo = getNextPixel(); 
        List<DirectionalPixel> nextPossibilities = toAddTo.getPossibilities(image);
        if (nextPossibilities.size() == 0){
            edgeIterator.remove();
            return;
        }
        
        int colorToMatch = image.getRGB(toAddTo.getX(), toAddTo.getY());
        
        List<Integer> colorPossibilities = colorTracker.getClosestColors(colorToMatch);
        
        while (nextPossibilities.size() != 0 && colorPossibilities.size() != 0){
            DirectionalPixel pixelToAdd = nextPossibilities.remove(rand.nextInt(nextPossibilities.size()));
            int colorToAdd = colorPossibilities.remove(rand.nextInt(colorPossibilities.size()));
            edgeIterator.add(pixelToAdd);
            pixelsFilled++;
            if (rand.nextInt(2) == 0){
                edgeIterator.previous();
            }
            updateImage(pixelToAdd.getX(), pixelToAdd.getY(), colorToAdd);
        }
    }
    
    
    private DirectionalPixel getNextPixel(){
        
        boolean rightInd;
        if (rand.nextInt(2) == 0){
            rightInd = true;
        } else {
            rightInd = false;
        }
        
        for (;;){
            if (rightInd){
                if (edgeIterator.hasNext()){
                    if (rand.nextInt(1000) < individualPercent){
                        return edgeIterator.next();
                    } else {
                        edgeIterator.next();
                    }
                } else {
                    rightInd = false;
                }
            } else {
                if (edgeIterator.hasPrevious()){
                    if (rand.nextInt(1000) < individualPercent){
                        return edgeIterator.previous();
                    } else {
                        edgeIterator.previous();
                    }
                } else {
                    rightInd = true;
                }
            }
        }
    }
}
