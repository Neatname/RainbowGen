package com.nmiles.rainbowgen.generator;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * You should read about how StainedGlass works before trying to read through
 * this class's description.
 * 
 * This algorithm produces possibly the widest range of results of any existing
 * algorithm. It works by maintaining a edgeList exactly as StainedGlass does,
 * and the neighbor population works in exactly the same way as StainedGlass,
 * but the way that pixels are chosen from the list is extremely different.
 * Rather than choose pixels randomly from the list, it maintains an Iterator
 * over that list and skips back and forth in the list, picking pixels as it
 * goes and giving them neighbors. For more detail about how pixels are picked
 * from the list, see choosePixelFromList().
 * 
 * @author Nathan Miles
 *
 */
public class FastIterator extends RandomImage {

    /**
     * The percent chance that the iterator will stop at a given pixel in the
     * edgeList. Percentages are on a scale of 1 - 1000.
     */
    private int individualPercent;

    /** A list of all pixels on the edge of the image, just as in StainedGlass. */
    private List<Pixel> edgeList;

    /**
     * An iterator over the edgeList. This is what is used to actually traverse
     * the list and retrieve items from it.
     */
    private ListIterator<Pixel> edgeIterator;

    /**
     * Constructs a new FastIterator with the given parameters.
     * 
     * @param width
     *            The width of the new image
     * @param height
     *            The height of the new image
     * @param individualPercent
     *            The percent change that the iterator will stop at a given
     *            pixel in the edgeList. Percentages are on a scale of 1-1000.
     */
    public FastIterator(int width, int height, int individualPercent) {
        super(width, height);
        if (individualPercent < 1 || individualPercent > 1000) {
            throw new IllegalArgumentException(
                    "individualPercent must be between 1 and 1000");
        }
        this.individualPercent = individualPercent;
        edgeList = new LinkedList<Pixel>();
        Pixel pixelToAdd = new Pixel(rand.nextInt(width), rand.nextInt(height));
        edgeList.add(pixelToAdd);

        int colorToAdd = colorTracker.getRandomUnused();

        updateImage(pixelToAdd.getX(), pixelToAdd.getY(), colorToAdd);

        edgeIterator = edgeList.listIterator();
    }

    /**
     * Chooses a pixel from the edgeList and populates its neighbors exactly as
     * StainedGlass does. For details on pixel choosing, see
     * choosePixelFromList().
     */
    @Override
    public void nextPixel() {

        Pixel toAddTo = choosePixelFromList();
        List<Pixel> nextPossibilities = toAddTo.getPossibilities(image);
        if (nextPossibilities.size() == 0) {
            edgeIterator.remove();
            return;
        }

        int colorToMatch = image.getRGB(toAddTo.getX(), toAddTo.getY());

        List<Integer> colorPossibilities = colorTracker.getClosestColors(colorToMatch);

        while (nextPossibilities.size() != 0 && colorPossibilities.size() != 0) {
            Pixel pixelToAdd = nextPossibilities.remove(rand.nextInt(nextPossibilities.size()));
            int colorToAdd = colorPossibilities.remove(rand.nextInt(colorPossibilities.size()));
            // add the pixel to the edgeList in the current place
            edgeIterator.add(pixelToAdd);
            // 50% chance of ending up on either side of the just-added pixel
            if (rand.nextInt(2) == 0) {
                edgeIterator.previous();
            }
            updateImage(pixelToAdd.getX(), pixelToAdd.getY(), colorToAdd);
        }
    }

    /**
     * Chooses a pixel from the edgeList. The edgeIterator starts moving either
     * left or right through the edgeList, and at each step, has a certain
     * percent chance to stop and return the current item. This percent chance
     * is on a scale of 1-1000 and is stored in the individualPercent field.
     * 
     * @return
     */
    private Pixel choosePixelFromList() {
        // randomly choose right or left
        boolean rightInd = rand.nextInt(2) == 0;

        // loop until we return
        for (;;) {
            // if we're going right
            if (rightInd) {
                // if this isn't the last element in the list
                if (edgeIterator.hasNext()) {
                    // if we're stopping here
                    if (rand.nextInt(1000) < individualPercent) {
                        // return the next element
                        return edgeIterator.next();
                    } else {
                        // else just go to the next element and repeat
                        edgeIterator.next();
                    }
                } else {
                    // we're at the end of the list so we have to turn around
                    // and go back
                    rightInd = false;
                }
                // we're going left
            } else {
                // if this isn't the first element in the list
                if (edgeIterator.hasPrevious()) {
                    // if we're stopping here
                    if (rand.nextInt(1000) < individualPercent) {
                        // return the next element
                        return edgeIterator.previous();
                    } else {
                        // else just go to the next element and repeat
                        edgeIterator.previous();
                    }
                } else {
                    // we're at the beginning of the list so we have to turn
                    // around and go back
                    rightInd = true;
                }
            }
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
