package com.nmiles.rainbowgen.generator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class ColorTracker {
	private int[][][] tracker;
	
	private int MAX_DIMENSION = 256;
	
	public static final int MAX_COLORS = 16777216;

	ColorTracker(int colors) {
		fillRandomColors(colors);
	}
	
	private void fillRandomColors(int colors){
        tracker = new int[256][256][256];
        Random rand = new Random();
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
        int bToCheck;
        int gToCheck;
        int currentR = (colorToMatch >> 16) & 0xFF;
        int currentG = (colorToMatch >> 8) & 0xFF;
        int currentB = colorToMatch & 0xFF;
        //currentR /= colorScalar;
        //currentG /= colorScalar;
        //currentB /= colorScalar;
        int range = 0;
        do{
            range++;
            rToCheck = currentR - range;
            if (rToCheck >= 0){
                for (int g = -range; g <= range; g++){
                    for (int b = -range; b <= range; b++){
                        gToCheck = g + currentG;
                        bToCheck = b + currentB;
                        if (gToCheck >= 0 && bToCheck >= 0 && gToCheck < 256 && bToCheck < 256 &&
                            tracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            rToCheck = currentR + range;
            if (rToCheck < 256){
                for (int g = -range; g <= range; g++){
                    for (int b = -range; b <= range; b++){
                        gToCheck = g + currentG;
                        bToCheck = b + currentB;
                        if (gToCheck >= 0 && bToCheck >= 0 && gToCheck < 256 && bToCheck < 256 &&
                        		tracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            gToCheck = currentG - range;
            if(gToCheck >= 0){
                for (int r = -range + 1; r <= range - 1; r++){
                    for (int b = -range; b <= range; b++){
                        rToCheck = r + currentR;
                        bToCheck = b + currentB;
                        if (rToCheck >= 0 && bToCheck >= 0 && rToCheck < 256 && bToCheck < 256 &&
                        		tracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            gToCheck = currentG + range;
            if(gToCheck < 256){
                for (int r = -range + 1; r <= range - 1; r++){
                    for (int b = -range; b <= range; b++){
                        rToCheck = r + currentR;
                        bToCheck = b + currentB;
                        if (rToCheck >= 0 && bToCheck >= 0 && rToCheck < 256 && bToCheck < 256 &&
                        		tracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            bToCheck = currentB - range;
            if (bToCheck >= 0){
                for (int r = -range + 1; r <= range - 1; r++){
                    for (int g = -range + 1; g <= range - 1; g++){
                        rToCheck = r + currentR;
                        gToCheck = g + currentG;
                        if (rToCheck >= 0 && gToCheck >= 0 && rToCheck < 256 && gToCheck < 256 &&
                        		tracker[rToCheck][gToCheck][bToCheck] != 0){
                            closestColors.add(tracker[rToCheck][gToCheck][bToCheck]);
                        }
                    }
                }
            }
            bToCheck = currentB + range;
            if (bToCheck < 256){
                for (int r = -range + 1; r <= range - 1; r++){
                    for (int g = -range + 1; g <= range - 1; g++){
                        rToCheck = r + currentR;
                        gToCheck = g + currentG;
                        if (rToCheck >= 0 && gToCheck >= 0 && rToCheck < 256 && gToCheck < 256 &&
                        		tracker[rToCheck][gToCheck][bToCheck] != 0){
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
}
