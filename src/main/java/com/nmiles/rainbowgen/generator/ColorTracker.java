package com.nmiles.rainbowgen.generator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class ColorTracker {
	private int[][][] tracker;
	
	private int MAX_DIMENSION = 256;
	
	public static final int MAX_COLORS = 16777216;
	Random rand;

	ColorTracker(int colors) {
		rand = new Random();
		fillRandomColors(colors);
	}
	
	private void fillRandomColors(int colors){
        tracker = new int[256][256][256];
        for (int i = 0; i < colors; i++){
            int r = rand.nextInt(256);
            int g = rand.nextInt(256);
            int b = rand.nextInt(256);
            if (tracker[r][g][b] == 0){
            	tracker[r][g][b] = new Color(r, g, b, 255).getRGB();
            } else {
                i--;
            }
        }
    }
	
	public List<Integer> getClosestColors(int colorToMatch){
        List<Integer> closestColors = new ArrayList<Integer>();
        int rToCheck;
        int gToCheck;
        int bToCheck;
        int currentR = (colorToMatch >> 16) & 0xFF;
        int currentG = (colorToMatch >> 8) & 0xFF;
        int currentB = colorToMatch & 0xFF;
        int range = 0;
        int redStart, redEnd,
        	greenStart, greenEnd,
        	blueStart, blueEnd;
        do{
            range++;
            rToCheck = currentR - range;
            if (rToCheck >= 0){
            	greenStart = Math.max(0, currentG - range);
            	greenEnd = Math.min(255, currentG + range);
            	blueStart = Math.max(0, currentB - range);
            	blueEnd = Math.min(255, currentB + range);
                for (gToCheck = greenStart; gToCheck <= greenEnd; gToCheck++){
                    for (bToCheck = blueStart; bToCheck <= blueEnd; bToCheck++){
                        if (tracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            rToCheck = currentR + range;
            if (rToCheck < 256){
            	greenStart = Math.max(0, currentG - range);
            	greenEnd = Math.min(255, currentG + range);
            	blueStart = Math.max(0, currentB - range);
            	blueEnd = Math.min(255, currentB + range);
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
            	redEnd = Math.min(255, currentR + range - 1);
            	blueStart = Math.max(0, currentB - range);
            	blueEnd = Math.min(255, currentB + range);
                for (rToCheck = redStart; rToCheck <= redEnd; rToCheck++){
                    for (bToCheck = blueStart; bToCheck <= blueEnd; bToCheck++){
                        if (tracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            gToCheck = currentG + range;
            if(gToCheck < 256){
            	redStart = Math.max(0, currentR - range + 1);
            	redEnd = Math.min(255, currentR + range - 1);
            	blueStart = Math.max(0, currentB - range);
            	blueEnd = Math.min(255, currentB + range);
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
            	redEnd = Math.min(255, currentR + range - 1);
            	greenStart = Math.max(0, currentG - range + 1);
            	greenEnd = Math.min(255, currentG + range - 1);
                for (rToCheck = redStart; rToCheck <= redEnd; rToCheck++){
                    for (gToCheck = greenStart; gToCheck <= greenEnd; gToCheck++){
                        if (tracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            bToCheck = currentB + range;
            if (bToCheck < 256){
            	redStart = Math.max(0, currentR - range + 1);
            	redEnd = Math.min(255, currentR + range - 1);
            	greenStart = Math.max(0, currentG - range + 1);
            	greenEnd = Math.min(255, currentG + range - 1);
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
		int r = (int)((color >> 16) & 0xFF);
		int g = (int)((color >> 8) & 0xFF);
		int b = (int)(color & 0xFF);
		tracker[r][g][b] = 0;
	}
	
	int getRandomUnused(){
		int ret = 1;
		while (ret == 1){
			int r = rand.nextInt(tracker.length);
			int g = rand.nextInt(tracker.length);
			int b = rand.nextInt(tracker.length);
			if (tracker[r][g][b] != 0){
				ret = tracker[r][g][b];
			}
		}
		return ret;
	}
}
