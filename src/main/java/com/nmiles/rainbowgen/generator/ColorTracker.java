package com.nmiles.rainbowgen.generator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class ColorTracker {
	private int[][][] tracker;
	
	private int MAX_DIMENSION = 256;
	
	private int dimension;
	
	private int lastCell;
	
	private int scalar;
	
	public static final int MAX_COLORS = 16777216;
	Random rand;

	ColorTracker(int colors) {
		rand = new Random();
		fill(colors);
		lastCell = dimension - 1;
	}
	
	private void fill(int colors){
		dimension = 1;
		while (dimension * dimension * dimension < colors){
			dimension *= 2;
		}
		scalar = MAX_DIMENSION / dimension;
        tracker = new int[dimension][dimension][dimension];
        int rToSet, gToSet, bToSet;
        for (int r = 0; r < dimension; r++){
        	rToSet = r * scalar;
        	for (int g = 0; g < dimension; g++){
        		gToSet = g * scalar;
        		for (int b = 0; b < dimension; b++){
        			bToSet = b * scalar;
        			tracker[r][g][b] = (new Color(rToSet, gToSet, bToSet)).getRGB();
        		}
        	}
        }
        int toRemove = (dimension * dimension * dimension) - colors;
        for (; toRemove > 0; toRemove--){
        	int r = rand.nextInt(dimension);
        	int g = rand.nextInt(dimension);
        	int b = rand.nextInt(dimension);
        	if (tracker[r][g][b] == 0){
        		toRemove++;
        		continue;
        	} else {
        		tracker[r][g][b] = 0;
        	}
        }
    }
	
	public List<Integer> getClosestColors(int colorToMatch){
        List<Integer> closestColors = new ArrayList<Integer>();
        int rToCheck;
        int gToCheck;
        int bToCheck;
        Color c = new Color(colorToMatch);
        int currentR = c.getRed() / scalar;
        int currentG = c.getGreen() / scalar;
        int currentB = c.getBlue() / scalar;
        int range = 0;
        int redStart, redEnd,
        	greenStart, greenEnd,
        	blueStart, blueEnd;
        do{
            range++;
            rToCheck = currentR - range;
            if (rToCheck >= 0){
            	greenStart = Math.max(0, currentG - range);
            	greenEnd = Math.min(lastCell, currentG + range);
            	blueStart = Math.max(0, currentB - range);
            	blueEnd = Math.min(lastCell, currentB + range);
                for (gToCheck = greenStart; gToCheck <= greenEnd; gToCheck++){
                    for (bToCheck = blueStart; bToCheck <= blueEnd; bToCheck++){
                        if (tracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            rToCheck = currentR + range;
            if (rToCheck < dimension){
            	greenStart = Math.max(0, currentG - range);
            	greenEnd = Math.min(lastCell, currentG + range);
            	blueStart = Math.max(0, currentB - range);
            	blueEnd = Math.min(lastCell, currentB + range);
                for (gToCheck = greenStart; gToCheck <= greenEnd; gToCheck++){
                    for (bToCheck = blueStart; bToCheck <= blueEnd; bToCheck++){
                        if (tracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            gToCheck = currentG - range;
            if(gToCheck >= 0){
            	redStart = Math.max(0, currentR - range + 1);
            	redEnd = Math.min(lastCell, currentR + range - 1);
            	blueStart = Math.max(0, currentB - range);
            	blueEnd = Math.min(lastCell, currentB + range);
                for (rToCheck = redStart; rToCheck <= redEnd; rToCheck++){
                    for (bToCheck = blueStart; bToCheck <= blueEnd; bToCheck++){
                        if (tracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            gToCheck = currentG + range;
            if(gToCheck < dimension){
            	redStart = Math.max(0, currentR - range + 1);
            	redEnd = Math.min(lastCell, currentR + range - 1);
            	blueStart = Math.max(0, currentB - range);
            	blueEnd = Math.min(lastCell, currentB + range);
                for (rToCheck = redStart; rToCheck <= redEnd; rToCheck++){
                    for (bToCheck = blueStart; bToCheck <= blueEnd; bToCheck++){
                        if (tracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            bToCheck = currentB - range;
            if (bToCheck >= 0){
            	redStart = Math.max(0, currentR - range + 1);
            	redEnd = Math.min(lastCell, currentR + range - 1);
            	greenStart = Math.max(0, currentG - range + 1);
            	greenEnd = Math.min(lastCell, currentG + range - 1);
                for (rToCheck = redStart; rToCheck <= redEnd; rToCheck++){
                    for (gToCheck = greenStart; gToCheck <= greenEnd; gToCheck++){
                        if (tracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            bToCheck = currentB + range;
            if (bToCheck < dimension){
            	redStart = Math.max(0, currentR - range + 1);
            	redEnd = Math.min(lastCell, currentR + range - 1);
            	greenStart = Math.max(0, currentG - range + 1);
            	greenEnd = Math.min(lastCell, currentG + range - 1);
                for (rToCheck = redStart; rToCheck <= redEnd; rToCheck++){
                    for (gToCheck = greenStart; gToCheck <= greenEnd; gToCheck++){
                        if (tracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
        } while (closestColors.size() == 0);
        return closestColors;
    }
	
	
	
	void markUsed(int color){
		Color c = new Color(color);
		int r = c.getRed() / scalar;
		int g = c.getGreen() / scalar;
		int b = c.getBlue() / scalar;
		tracker[r][g][b] = 0;
	}
	
	int getRandomUnused(){
		int ret = 1;
		while (ret == 1){
			int r = rand.nextInt(dimension);
			int g = rand.nextInt(dimension);
			int b = rand.nextInt(dimension);
			if (tracker[r][g][b] != 0){
				ret = tracker[r][g][b];
			}
		}
		return ret;
	}
}
