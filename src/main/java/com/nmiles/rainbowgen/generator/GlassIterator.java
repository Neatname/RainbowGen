package com.nmiles.rainbowgen.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * This algorithm is just a combination of StainedGlass and FastIterator. It
 * works by having a certain chance each iteration to switch between the
 * algorithms.
 * @author Nathan Miles
 *
 */
public class GlassIterator extends RandomImage {
    /**
     * In Iterator mode, the percent chance that the iterator will stop at a
     * given pixel in the edgeList. Percentages are on a scale of 1 - 1000.
     */
    private int individualPercent;
    /** In Iterator mode, the chance at each iteration to switch to
     * Glass mode. Expressed as 1 in [switchToGlass]. */
    private int switchToGlass;
    /** In Glass mode, the chance at each iteration to switch to Iterator
     * mode. Expressed as 1 in [switchToIterator]. */
    private int switchToIterator;
    /** A list of all pixels on the edge of the image, just as in StainedGlass. */
    private List<Pixel> edgeList;
    /** An indicator of whether or not the algorithm is currently in Glass mode. */
    private boolean inGlassMode = true;
    /** Keeps track of the simulated iterator's position when in Iterator mode. */
    private int iteratorPos;
    
    /**
     * Constructs a new GlassIterator
     * 
     * @param width
     *              The width of the image
     * @param height
     *              The height of the image
     * @param switchToGlass
     *              In Iterator mode, the chance at each iteration to switch to
     *              Glass mode. Expressed as 1 in [switchToGlass].
     * @param switchToIterator
     *              In Glass mode, the chance at each iteration to switch to
     *              Iterator mode. Expressed as 1 in [switchToIterator].
     * @param individualPercent
     */
    public GlassIterator(int width, int height, int switchToGlass, int switchToIterator, int individualPercent){
        super(width, height);
        // bounds checking on new params
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

    /**
     * Populates the next pixel or pixels for this iteration. It first
     * determines which mode we're in, then executes in the appropriate mode.
     */
    @Override
    public void nextPixel() {
        if (inGlassMode){
            doGlass();
        } else {
            doIterator();
        }
    }
    
    /**
     * Executes this iteration in Glass mode. This method is almost identical
     * to StainedGlass's nextPixel() method. The only changes are: now when
     * Pixels are added to the edgeList, they are now added immediately after
     * the pixel that was chosen from the edgeList rather than at the end; and
     * after executing this iteration, the choice of whether or not to switch
     * to Iterator mode is made.
     */
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
            
            /* Add it to the edgeList right after the pixel that was chosen
             * earlier. This is done so that close-together pixels are close
             * together when we switch to Iterator mode.
             */
            edgeList.add(indexToGet + 1, newPixel);
        }
        // if all of the initial pixel's neighbors are now populated, remove it
        // form the list
        if (neighbors.size() == 0) {
            edgeList.remove(indexToGet);
        }
        
        // determine whether to switch to Iterator mode
        if (rand.nextInt(switchToIterator) == 0){
            inGlassMode = false;
            // set a random iterator position
            iteratorPos = rand.nextInt(edgeList.size());
        }
    }
    
    /**
     * Execute this iteration in Iterator mode. The fundamental logic is
     * identical to FastIterator's nextPixel() method except for the check for
     * whether or not to switch to Glass mode. But while the fundamental logic
     * is the same, there is a large implementational difference in that we are
     * no longer using a ListIterator. We are instead simulating a ListIterator
     * by maintaining an index that we increment or decrement as we step
     * through the list.
     */
    private void doIterator(){
        Pixel toAddTo = chooseByIterator();
        List<Pixel> nextPossibilities = toAddTo.getPossibilities(image);
        if (nextPossibilities.size() == 0) {
            edgeList.remove(iteratorPos);
            return;
        }

        int colorToMatch = image.getRGB(toAddTo.getX(), toAddTo.getY());

        List<Integer> colorPossibilities = colorTracker.getClosestColors(colorToMatch);

        // keep populating pixels until the neighbor list or color list runs out
        while (nextPossibilities.size() != 0 && colorPossibilities.size() != 0) {
            Pixel pixelToAdd = nextPossibilities.remove(rand.nextInt(nextPossibilities.size()));
            int colorToAdd = colorPossibilities.remove(rand.nextInt(colorPossibilities.size()));
            // add the pixel to the edgeList in the current place
            edgeList.add(iteratorPos, pixelToAdd);
            updateImage(pixelToAdd.getX(), pixelToAdd.getY(), colorToAdd);
        }
        
        // determine whether or not to switch modes
        if (rand.nextInt(switchToGlass) == 0){
            inGlassMode = true;
        }
    }

    /**
     * Does exactly what FastIterator's choosePixelFromList() method does, just
     * with the simulated ListIterator paradigm
     * @return The chosen Pixel
     */
    private Pixel chooseByIterator(){
        // randomly choose right or left
        boolean rightInd = rand.nextInt(2) == 0;
        
        // save the last index in the list so we don't have to keep calculating
        int lastIndex = edgeList.size() - 1;
        
        // loop until we return
        for(;;){
            // if we're going right
            if (rightInd){
                iteratorPos++;
                // if we aren't past the end
                if (iteratorPos <= lastIndex){
                    // if we're stopping here
                    if (rand.nextInt(1000) < individualPercent){
                        // make sure we aren't in the negatives before sending
                        if (iteratorPos >= 0){
                            return edgeList.get(iteratorPos);
                        }
                    }
                // this is the last element, turn around
                } else {
                    rightInd = false;
                }
            // we're going left
            } else {
                iteratorPos--;
                // if we aren't past the beginning
                if (iteratorPos >= 0){
                    // if we're stopping here
                    if (rand.nextInt(1000) < individualPercent){
                        // make sure we aren't past the end of the list
                        if (iteratorPos <= lastIndex){
                            return edgeList.get(iteratorPos);
                        }
                    }
                // we're at the end, turn around
                } else {
                    rightInd = true;
                }
            }
        }
    }
}
