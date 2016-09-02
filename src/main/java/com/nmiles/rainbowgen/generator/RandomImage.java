package com.nmiles.rainbowgen.generator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * This class is the meat of this whole thing. All image algorithm classes must
 * extend this class. Any runner for this class operates like this: it loops
 * while isFinished() returns false, and at each iteration, it calls
 * nextPixel(). nextPixel() is responsible for populating the image. All image
 * building beyond initial setup (which should be done in the child class's
 * constructor) should be done in the nextPixel() method. For examples of
 * implementations of this class, see the FastIterator and StainedGlass class
 * documentation.
 * 
 * @author Nathan Miles
 *
 */
public abstract class RandomImage {
    /** The number of colors representable in 32 bits. */
    public static final int MAX_COLORS = 16777216;

    /**
     * The maximum dimension allowable for an image. This limit exists because
     * of the ImageRecord class. That class represents the x and y coordinates
     * of pixels as three hex values, and 0xFFF = 4096, so this is the max.
     */
    public static final int MAX_DIMENSION = 4096;

    /** The number of R, G, and B values representable in a 32-bit int. */
    public static final int RGB_VALUES = 256;

    /** The maximum value of any given R, G, or B channel. */
    public static final int MAX_COLOR_VALUE = 255;

    /** The width of the image. */
    protected int width;

    /** The height of the image. */
    protected int height;

    /**
     * This keeps track of which colors have so far been used in this image. For
     * usage details, see the ColorTracker documentation.
     */
    protected ColorTracker colorTracker;

    /**
     * This is essentially a bitmap that stores all of the current image data.
     * It can be rendered directly to a JFrame in non-web interfaces. In the web
     * interface, it basically only serves to hold the color data for individual
     * pixels. Color data for a pixel can be retrieved by calling
     * image.getRGB(toAddTo.getX(), toAddTo.getY()) from any subclass.
     */
    protected BufferedImage image;

    /** An extremely fast random number generator. */
    protected XORShiftRandom rand;

    /** Stores the image data for transfer to a client application. */
    private ImageRecord record;

    /**
     * Performs basic setup for the image. The only thing this does apart from
     * instantiating fields is paint the whole BufferedImage with the color
     * #FE000000, which is used to indicate that a pixel hasn't been populated
     * yet. All pixels populated after this point MUST have alpha values of 255
     * rather than the initial 254. If you use the ColorTracker to get colors
     * from, which you should, this won't be an issue, because that class
     * handles that.
     * 
     * @param width
     *            The width of the image to be generated.
     * @param height
     *            The height of the image to be generated.
     */
    public RandomImage(int width, int height) {
        if (width > MAX_DIMENSION || height > MAX_DIMENSION) {
            throw new IllegalArgumentException("Image's dimensions must be <= 4096x4096.");
        }
        this.width = width;
        this.height = height;
        rand = new XORShiftRandom();

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(new Color(0, 0, 0, 254));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

        colorTracker = new ColorTracker(width * height);
        record = new ImageRecord(width * height);
    }

    /**
     * This method adds a pixel to the BufferedImage, adds it to the
     * ImageRecord, and then marks the color as used in the ColorTracker. This
     * method absolutely MUST be called each time a subclass wants to add a
     * pixel to the image. For usage examples, see the FastIterator and
     * StainedGlass classes.
     * 
     * @param x
     *            The x coordinate of the pixel to add.
     * @param y
     *            The y coordinate of the pixel to add.
     * @param color
     *            The color of the pixel to add.
     */
    protected void updateImage(int x, int y, int color) {
        image.setRGB(x, y, color);
        record.addPixel(x, y, color);
        colorTracker.markUsed(color);
    }

    /**
     * Returns true if the image is finished, false if not.
     * 
     * @return true if the image is finished, false if not.
     */
    public abstract boolean isFinished();

    /**
     * In the most general sense, this method should move the image closer to
     * completion. Calling this method enough times MUST eventually result in a
     * complete image, and isFinished() returning true. What exactly this method
     * does is entirely dependent on the implementation, so this description is
     * purposely vague. For example implementations of this method, see the
     * FastIterator and StainedGlass classes.
     */
    public abstract void nextPixel();

    /**
     * Returns the ImageRecord associated with this image.
     * 
     * @return The ImageRecord
     */
    public ImageRecord getRecord() {
        return record;
    }
}
