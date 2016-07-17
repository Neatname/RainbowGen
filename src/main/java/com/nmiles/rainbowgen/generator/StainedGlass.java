package com.nmiles.rainbowgen.generator;

import java.util.ArrayList;
import java.util.List;

public class StainedGlass extends RandomImage {
	
	private static final int INITIAL_LIST_SIZE = 50000;

	public StainedGlass(int width, int height, int startingPoints){
		super(width, height);
		edgeList = new ArrayList<>(INITIAL_LIST_SIZE);
		
		int newX = rand.nextInt(width);
		int newY = rand.nextInt(height);
		int newColor = colorTracker.getRandomUnused();
		updateImage(newX, newY, newColor);
		edgeList.add(new DirectionalPixel(newX, newY, 1, 1));
		int counter = 1;
		while (counter < startingPoints){
			newX = rand.nextInt(width);
			newY = rand.nextInt(height);
			if (image.getRGB(newX, newY) != -33554432){
				continue;
			}
			newColor = colorTracker.getRandomUnused();
			updateImage(newX, newY, newColor);
			edgeList.add(new DirectionalPixel(newX, newY, 1, 1));
			counter++;
		}
	}
	
	/*@Override
	public void nextPixel() {
		int indexToGet = rand.nextInt(edgeList.size());
		DirectionalPixel toAddTo = edgeList.get(indexToGet);
		List<DirectionalPixel> neighbors = toAddTo.getPossibilities(image);
		if (neighbors.size() == 0){
			edgeList.remove(indexToGet);
			return;
		}
		List<Integer> closestColors = colorTracker.getClosestColors(image.getRGB(toAddTo.getX(), toAddTo.getY()));
		DirectionalPixel newPixel = neighbors.get(rand.nextInt(neighbors.size()));
		int newColor = closestColors.get(rand.nextInt(closestColors.size()));
		updateImage(newPixel.getX(), newPixel.getY(), newColor);
		edgeList.add(newPixel);
	}*/
	
	@Override
	public void nextPixel() {
		int indexToGet = rand.nextInt(edgeList.size());
		DirectionalPixel toAddTo = edgeList.get(indexToGet);
		List<DirectionalPixel> neighbors = toAddTo.getPossibilities(image);
		if (neighbors.size() == 0){
			edgeList.remove(indexToGet);
			return;
		}
		List<Integer> closestColors = colorTracker.getClosestColors(image.getRGB(toAddTo.getX(), toAddTo.getY()));
		while (neighbors.size() > 0 && closestColors.size() > 0){
			DirectionalPixel newPixel = neighbors.remove(rand.nextInt(neighbors.size()));
			int newColor = closestColors.remove(rand.nextInt(closestColors.size()));
			updateImage(newPixel.getX(), newPixel.getY(), newColor);
			edgeList.add(newPixel);
		}
	}

}
