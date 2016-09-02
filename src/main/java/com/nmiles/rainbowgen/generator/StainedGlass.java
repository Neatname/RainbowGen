package com.nmiles.rainbowgen.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * This one of the simplest implementations of RandomImage. It works by choosing
 * a number of starting points, and then growing an image by choosing an
 * already-populated pixel and giving it one or more neighbors.
 * Already-populated pixels are kept track of in the edgeList. Whenever a pixel
 * is added to the image, it is also added to the edgeList. The next pixel to
 * give neighbors to is chosen from the edgeList at random. This results in
 * relatively uniform, circular growth centered at each of the starting points
 * because each pixel choice is completely random.
 * 
 * @author Nathan Miles
 *
 */
public class StainedGlass extends RandomImage {
    /** The initial size of the edgeList */
    private static final int INITIAL_LIST_SIZE = 50000;

    /**
     * Used to keep track of all pixels which comprise the "edge" of the image.
     * More formally, each pixel with at least one unpopulated neighbor is in
     * this list. It may (and after a couple of iterations, most certainly does)
     * contain pixels which do have all of their neighbors populated.
     */
    private List<Pixel> edgeList;

    /**
     * Initializes the image, adds a number of initial points equal to
     * startingPoints or 1, whichever is greater, then adds them to the
     * edgeList.
     * 
     * @param width
     * @param height
     * @param startingPoints
     */
    public StainedGlass(int width, int height, int startingPoints) {
        super(width, height);
        edgeList = new ArrayList<>(INITIAL_LIST_SIZE);

        int newX = rand.nextInt(width);
        int newY = rand.nextInt(height);
        int newColor = colorTracker.getRandomUnused();
        updateImage(newX, newY, newColor);
        edgeList.add(new Pixel(newX, newY));
        int counter = 1;
        while (counter < startingPoints) {
            newX = rand.nextInt(width);
            newY = rand.nextInt(height);
            if (image.getRGB(newX, newY) != -33554432) {
                continue;
            }
            newColor = colorTracker.getRandomUnused();
            updateImage(newX, newY, newColor);
            edgeList.add(new Pixel(newX, newY));
            counter++;
        }
    }

    /**
     * Tries to populate the next group of pixels in the image. It picks a
     * random pixel from the edgeList, then gets a List of all unpopulated
     * neighbors. Then it gets a List of possible colors to add, and it steps
     * through and randomly populates pixels from the first List with colors
     * form the second List without repeats. Any pixels added to the image are
     * also added to the edgeList.
     */
    @Override
    public void nextPixel() {
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
            edgeList.add(newPixel);
        }
        // if all of the initial pixel's neighbors are now populated, remove it
        // form the list
        if (neighbors.size() == 0) {
            edgeList.remove(indexToGet);
        }
    }
    
    /**
     * Returns true if the image is finished, false if not.
     * 
     * @return true if the image is finished, false if not.
     */
    @Override
    public boolean isFinished() {
        return edgeList.size() == 0;
    }

}
