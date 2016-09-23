package com.nmiles.rainbowgen.generator;

import java.util.ArrayList;
import java.util.List;

public class GlassIterator extends RandomImage {
    
    private int individualPercent;
    
    private int switchToGlass;
    
    private int switchToIterator;
    
    private List<Pixel> edgeList;
    
    private boolean inGlassMode = true;
    
    private int iteratorPos;
    
    public GlassIterator(int width, int height, int switchToGlass, int switchToIterator, int individualPercent){
        super(width, height);
        if (individualPercent <= 0 || individualPercent > 1000 ||
            switchToGlass <= 0 || switchToIterator <= 0){
            throw new IllegalArgumentException();
        }
        this.individualPercent = individualPercent;
        this.switchToGlass = switchToGlass;
        this.switchToIterator = switchToIterator;
        edgeList = new ArrayList<>();
        
        Pixel pixelToAdd = new Pixel(rand.nextInt(width), rand.nextInt(height));
        edgeList.add(pixelToAdd);

        int colorToAdd = colorTracker.getRandomUnused();

        updateImage(pixelToAdd.getX(), pixelToAdd.getY(), colorToAdd);
    }

    @Override
    public boolean isFinished() {
        return edgeList.isEmpty();
    }

    @Override
    public void nextPixel() {
        // do the appropriate mode
        if (inGlassMode){
            doGlass();
        } else {
            doIterator();
        }
    }
    
    
    private void doGlass(){
        // pick a random index in the list
        int indexToGet = rand.nextInt(edgeList.size());
        // get it
        Pixel toAddTo = edgeList.get(indexToGet);
        // get its neighbors
        List<Pixel> neighbors = toAddTo.getPossibilities(image);
        // if no neighbors, remove it and return
        if (neighbors.size() == 0) {
            edgeList.remove(indexToGet);
            return;
        }
        // get the list of possible colors
        List<Integer> closestColors = colorTracker.getClosestColors(image.getRGB(toAddTo.getX(), toAddTo.getY()));
        // as long as both lists still have members, populate pixels randomly
        while (neighbors.size() > 0 && closestColors.size() > 0) {
            // get a random pixel
            Pixel newPixel = neighbors.remove(rand.nextInt(neighbors.size()));
            // get a random color
            int newColor = closestColors.remove(rand.nextInt(closestColors.size()));
            // add it to the image
            updateImage(newPixel.getX(), newPixel.getY(), newColor);
            // add it to the edgeList
            edgeList.add(indexToGet + 1, newPixel);
        }
        // if all of the initial pixel's neighbors are now populated, remove it
        // form the list
        if (neighbors.size() == 0) {
            edgeList.remove(indexToGet);
        }
        
        if (rand.nextInt(switchToIterator) == 0){
            inGlassMode = false;
            // set a random iterator position
            iteratorPos = rand.nextInt(edgeList.size());
        }


        // determine if we need to switch modes
        if (inGlassMode){
            if (rand.nextInt(switchToIterator) == 0){
                inGlassMode = false;
                // set a random iterator position
                iteratorPos = rand.nextInt(edgeList.size());
            }
        } else {
            if (rand.nextInt(switchToGlass) == 0){
                inGlassMode = true;
            }
        }
    }
    
    
    private void doIterator(){
        Pixel toAddTo = chooseByIterator();
        List<Pixel> nextPossibilities = toAddTo.getPossibilities(image);
        if (nextPossibilities.size() == 0) {
            edgeList.remove(iteratorPos);
            return;
        }

        int colorToMatch = image.getRGB(toAddTo.getX(), toAddTo.getY());

        List<Integer> colorPossibilities = colorTracker.getClosestColors(colorToMatch);

        while (nextPossibilities.size() != 0 && colorPossibilities.size() != 0) {
            Pixel pixelToAdd = nextPossibilities.remove(rand.nextInt(nextPossibilities.size()));
            int colorToAdd = colorPossibilities.remove(rand.nextInt(colorPossibilities.size()));
            // add the pixel to the edgeList in the current place
            edgeList.add(iteratorPos, pixelToAdd);
            updateImage(pixelToAdd.getX(), pixelToAdd.getY(), colorToAdd);
        }
        
        if (rand.nextInt(switchToGlass) == 0){
            inGlassMode = true;
        }
    }

    
    private Pixel chooseByIterator(){
        // randomly choose right or left
        boolean rightInd = rand.nextInt(2) == 0;
        
        int lastIndex = edgeList.size() - 1;
        
        for(;;){
            if (rightInd){
                iteratorPos++;
                if (iteratorPos <= lastIndex){
                    if (rand.nextInt(1000) < individualPercent){
                        if (iteratorPos >= 0){
                            return edgeList.get(iteratorPos);
                        }
                    }
                } else {
                    rightInd = false;
                }
            } else {
                iteratorPos--;
                if (iteratorPos >= 0){
                    if (rand.nextInt(1000) < individualPercent){
                        if (iteratorPos <= lastIndex){
                            return edgeList.get(iteratorPos);
                        }
                    }
                } else {
                    rightInd = true;
                }
            }
        }
    }
}
