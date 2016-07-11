package com.nmiles.rainbowgen.generator;


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
        
        int startR = rand.nextInt(colorTracker.length);
        int startG = rand.nextInt(colorTracker.length);
        int startB = rand.nextInt(colorTracker.length);
        int colorToAdd = colorTracker[startR][startG][startB];
        
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
            DirectionalPixel pixelToAdd = nextPossibilities.remove(nextPossibilities.size() - 1);
            int colorToAdd = colorPossibilities.get(rand.nextInt(colorPossibilities.size()));
            colorTracker[(int)(((colorToAdd >> 16) & 0xFF) / colorScalar)][(int)(((colorToAdd >> 8) & 0xFF) / colorScalar)][(int)((colorToAdd & 0xFF) / colorScalar)] = 0;
            edgeIterator.add(pixelToAdd);
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
