package com.nmiles.rainbowgen.generator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Keeps track of colors which have been used for the RainbowGen program. Read
 * individual method documentation for how to use.
 * 
 * @author Nathan Miles
 *
 */
class ColorTracker {
    /** The actual array that keeps track of the color usage. */
    private int[][][] tracker;

    /**
     * The highest dimension that a particular ColorTracker can have on each
     * side.
     */
    private int MAX_DIMENSION = 256;

    /** The dimension of this ColorTracker */
    private int dimension;

    /**
     * The index of the last cell in each array of the tracker. This exists to
     * avoid inlining the calculation of dimension - 1 every time it's needed.
     */
    private int lastCell;

    /**
     * The scalar that each index must be multiplied by to calculate the color
     * stored at its coordinates. Conversely, this is the number that each RGB
     * value must be divided by to get its index in the array.
     */
    private int scalar;

    /** The maximum number of colors that any given ColorTracker may hold. */
    public static final int MAX_COLORS = 16777216;

    /** A Random instance for various uses. */
    private Random rand;

    /**
     * Constructs a new ColorTracker with the given number of colors in it. The
     * backing array will be scaled to have the smallest possible dimensions to
     * avoid searching overhead.
     * 
     * @param colors
     *            The number of colors this ColorTracker should hold. This
     *            number should be equal to the product of the width and height
     *            of the image being built.
     */
    ColorTracker(int colors) {
        rand = new Random();
        fill(colors);
        lastCell = dimension - 1;
    }

    /**
     * Fills the backing array with the appropriate colors (represented as
     * ints). After it finishes that, it removes cells randomly until the number
     * of non-zero cells is equal to the number of colors.
     * 
     * @param colors
     *            The number of colors to fill.
     */
    private void fill(int colors) {
        // Under the current implementation, the scalar must be a multiple of 2,
        // so choose
        // the smallest multiple of 2 that accommodates the needed number of
        // colors
        dimension = 1;
        while (dimension * dimension * dimension < colors) {
            dimension *= 2;
        }
        // initialize the backing array
        scalar = MAX_DIMENSION / dimension;
        tracker = new int[dimension][dimension][dimension];
        int rToSet, gToSet, bToSet;
        // fill the backing array
        for (int r = 0; r < dimension; r++) {
            rToSet = r * scalar;
            for (int g = 0; g < dimension; g++) {
                gToSet = g * scalar;
                for (int b = 0; b < dimension; b++) {
                    bToSet = b * scalar;
                    tracker[r][g][b] = (new Color(rToSet, gToSet, bToSet)).getRGB();
                }
            }
        }
        // remove colors randomly until we have the correct number of colors in
        // the array
        int toRemove = (dimension * dimension * dimension) - colors;
        for (; toRemove > 0; toRemove--) {
            int r = rand.nextInt(dimension);
            int g = rand.nextInt(dimension);
            int b = rand.nextInt(dimension);
            if (tracker[r][g][b] == 0) {
                toRemove++;
                continue;
            } else {
                tracker[r][g][b] = 0;
            }
        }
    }

    /**
     * Gets a List containing the closest color(s) to the given color that have
     * not yet been used in the image.
     * 
     * @param colorToMatch
     *            The color to get the closest values to.
     * @return The list of closest colors.
     */
    public List<Integer> getClosestColors(int colorToMatch) {
        // The list that will eventually be returned
        List<Integer> closestColors = new ArrayList<Integer>();
        int rToCheck;
        int gToCheck;
        int bToCheck;
        // convert the int representation of the color to a Color object
        // to avoid disgusting bitwise math
        Color c = new Color(colorToMatch);
        int currentR = c.getRed() / scalar;
        int currentG = c.getGreen() / scalar;
        int currentB = c.getBlue() / scalar;
        int range = 0;
        int redStart, redEnd, greenStart, greenEnd, blueStart, blueEnd;

        /*
         * This loop is what actually searches the backing array for unused
         * colors. This is the processor hog of the whole program, so
         * performance is valued over code clarity. The backing array is a 3D
         * "cube" with r, g, and b on the different axes. This loop searches in
         * what can be thought of as an expanding shell originating at
         * colorToMatch. At each iteration, it searches each cell in the array
         * that is on the surface of the cube centered at colorToMatch with
         * "radius" equal to range.
         */
        while (closestColors.size() == 0) {
            range++;

            // This chunk searches the face of the search cube with
            // the r coordinate currentR - range
            rToCheck = currentR - range;
            // no need to search if outside cube
            if (rToCheck >= 0) {
                // calculate search bounds so we don't waste time searching
                // outside of the cube
                greenStart = Math.max(0, currentG - range);
                greenEnd = Math.min(lastCell, currentG + range);
                blueStart = Math.max(0, currentB - range);
                blueEnd = Math.min(lastCell, currentB + range);
                // search the whole calculated area (a rectangular face)
                for (gToCheck = greenStart; gToCheck <= greenEnd; gToCheck++) {
                    for (bToCheck = blueStart; bToCheck <= blueEnd; bToCheck++) {
                        // if this cell is not 0, add it to the return list
                        if (tracker[rToCheck][gToCheck][bToCheck] != 0) {
                            closestColors
                                    .add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }

            // This chunk searches the face of the search cube with
            // the r coordinate currentR + range
            rToCheck = currentR + range;
            if (rToCheck < dimension) {
                greenStart = Math.max(0, currentG - range);
                greenEnd = Math.min(lastCell, currentG + range);
                blueStart = Math.max(0, currentB - range);
                blueEnd = Math.min(lastCell, currentB + range);
                for (gToCheck = greenStart; gToCheck <= greenEnd; gToCheck++) {
                    for (bToCheck = blueStart; bToCheck <= blueEnd; bToCheck++) {
                        if (tracker[rToCheck][gToCheck][bToCheck] != 0) {
                            closestColors
                                    .add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }

            // This chunk searches the face of the search cube with
            // the g coordinate currentG - range
            gToCheck = currentG - range;
            if (gToCheck >= 0) {
                redStart = Math.max(0, currentR - range + 1);
                redEnd = Math.min(lastCell, currentR + range - 1);
                blueStart = Math.max(0, currentB - range);
                blueEnd = Math.min(lastCell, currentB + range);
                for (rToCheck = redStart; rToCheck <= redEnd; rToCheck++) {
                    for (bToCheck = blueStart; bToCheck <= blueEnd; bToCheck++) {
                        if (tracker[rToCheck][gToCheck][bToCheck] != 0) {
                            closestColors
                                    .add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }

            // This chunk searches the face of the search cube with
            // the g coordinate currentG + range
            gToCheck = currentG + range;
            if (gToCheck < dimension) {
                redStart = Math.max(0, currentR - range + 1);
                redEnd = Math.min(lastCell, currentR + range - 1);
                blueStart = Math.max(0, currentB - range);
                blueEnd = Math.min(lastCell, currentB + range);
                for (rToCheck = redStart; rToCheck <= redEnd; rToCheck++) {
                    for (bToCheck = blueStart; bToCheck <= blueEnd; bToCheck++) {
                        if (tracker[rToCheck][gToCheck][bToCheck] != 0) {
                            closestColors
                                    .add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }

            // This chunk searches the face of the search cube with
            // the b coordinate currentB - range
            bToCheck = currentB - range;
            if (bToCheck >= 0) {
                redStart = Math.max(0, currentR - range + 1);
                redEnd = Math.min(lastCell, currentR + range - 1);
                greenStart = Math.max(0, currentG - range + 1);
                greenEnd = Math.min(lastCell, currentG + range - 1);
                for (rToCheck = redStart; rToCheck <= redEnd; rToCheck++) {
                    for (gToCheck = greenStart; gToCheck <= greenEnd; gToCheck++) {
                        if (tracker[rToCheck][gToCheck][bToCheck] != 0) {
                            closestColors
                                    .add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }

            // This chunk searches the face of the search cube with
            // the b coordinate currentB + range
            bToCheck = currentB + range;
            if (bToCheck < dimension) {
                redStart = Math.max(0, currentR - range + 1);
                redEnd = Math.min(lastCell, currentR + range - 1);
                greenStart = Math.max(0, currentG - range + 1);
                greenEnd = Math.min(lastCell, currentG + range - 1);
                for (rToCheck = redStart; rToCheck <= redEnd; rToCheck++) {
                    for (gToCheck = greenStart; gToCheck <= greenEnd; gToCheck++) {
                        if (tracker[rToCheck][gToCheck][bToCheck] != 0) {
                            closestColors
                                    .add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            } // end if
        } // end while
        return closestColors;
    }

    /**
     * Marks a color as being used. For proper images, this method MUST be
     * invoked after adding any pixel to the image. Additional precautions may
     * also need to be taken by the calling method to ensure no duplicate
     * pixels. For example, if multiple pixels are populated from a single call
     * of getClosestColors(), the calling method is responsible for ensuring
     * that no color from that list is used twice.
     * 
     * @param color
     *            The color to mark
     */
    void markUsed(int color) {
        Color c = new Color(color);
        int r = c.getRed() / scalar;
        int g = c.getGreen() / scalar;
        int b = c.getBlue() / scalar;
        tracker[r][g][b] = 0;
    }

    /**
     * Gets a random unused color
     * 
     * @return The color
     */
    int getRandomUnused() {
        int ret = 1; // all used colors are negative, so this value will never occur
        while (ret == 1) {
            int r = rand.nextInt(dimension);
            int g = rand.nextInt(dimension);
            int b = rand.nextInt(dimension);
            if (tracker[r][g][b] != 0) {
                ret = tracker[r][g][b];
            }
        }
        return ret;
    }
}
