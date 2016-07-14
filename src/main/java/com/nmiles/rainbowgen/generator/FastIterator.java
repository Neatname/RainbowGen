package com.nmiles.rainbowgen.generator;


import java.awt.Color;
import java.util.List;
import java.util.ListIterator;

public class FastIterator extends RandomImage {

    private int individualPercent;
    
    private ListIterator<DirectionalPixel> edgeIterator;
    
    public FastIterator(int width, int height, int individualPercent){
        
        super(width, height);
        this.individualPercent = individualPercent;
        DirectionalPixel pixelToAdd = new DirectionalPixel(rand, width, height);
        edgeList.add(pixelToAdd);
        
        int startR = rand.nextInt(256);
        int startG = rand.nextInt(256);
        int startB = rand.nextInt(256);
        int colorToAdd = new Color(startR, startG, startB).getRGB();
        
        updateImage(pixelToAdd.getX(), pixelToAdd.getY(), colorToAdd);
        colorTracker[startR][startG][startB] = 0;
        
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
        
        List<Integer> colorPossibilities = getClosestColors(colorToMatch);
        
        while (nextPossibilities.size() != 0 && colorPossibilities.size() != 0){
            DirectionalPixel pixelToAdd = nextPossibilities.remove(rand.nextInt(nextPossibilities.size()));
            int colorToAdd = colorPossibilities.remove(rand.nextInt(colorPossibilities.size()));
            colorTracker[(int)((colorToAdd >> 16) & 0xFF)][(int)((colorToAdd >> 8) & 0xFF)][(int)(colorToAdd & 0xFF)] = 0;
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
