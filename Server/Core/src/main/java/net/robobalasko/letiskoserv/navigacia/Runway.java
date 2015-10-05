package net.robobalasko.letiskoserv.navigacia;

import java.io.Serializable;

/**
 *
 * @author rbalasko
 */
public final class Runway implements Serializable {
    
    private int runwayNumber;
    
    private int runwayLength;
    
    private double runwayLat;
    
    private double runwayLon;
    
    private int pixelCoordStartX;
    
    private int pixelCoordStartY;
    
    private int pixelCoordEndX;
    
    private int pixelCoordEndY;
    
    public Runway(int runwayNumber, int runwayLength,
            double runwayLat, double runwayLon) throws InvalidRunwayDataException {
	setRunwayNumber(runwayNumber);
	setRunwayLength(runwayLength);
        this.runwayLat = runwayLat;
        this.runwayLon = runwayLon;
    }
    
    public Runway() {
    }
    
    private boolean checkRunwayNumber(int runwayNumber) {
	return runwayNumber > 0 && runwayNumber <= 36;
    }
    
    private boolean checkRunwayLength(int runwayLength) {
	return runwayLength >= 500 && runwayLength <= 6000;
    }

    public int getRunwayNumber() {
	return runwayNumber;
    }

    public void setRunwayNumber(int runwayNumber) throws InvalidRunwayDataException {
	if (!checkRunwayNumber(runwayNumber)) {
	    throw new InvalidRunwayDataException();
	}
	
	this.runwayNumber = runwayNumber;
    }

    public int getRunwayLength() {
	return runwayLength;
    }

    public void setRunwayLength(int runwayLength) throws InvalidRunwayDataException {
	if (!checkRunwayLength(runwayLength)) {
	    throw new InvalidRunwayDataException();
	}
	
	this.runwayLength = runwayLength;
    }

    public int getPixelCoordStartX() {
        return pixelCoordStartX;
    }

    public void setPixelCoordStartX(int pixelCoordStartX) {
        this.pixelCoordStartX = pixelCoordStartX;
    }

    public int getPixelCoordStartY() {
        return pixelCoordStartY;
    }

    public void setPixelCoordStartY(int pixelCoordStartY) {
        this.pixelCoordStartY = pixelCoordStartY;
    }

    public int getPixelCoordEndX() {
        return pixelCoordEndX;
    }

    public void setPixelCoordEndX(int pixelCoordEndX) {
        this.pixelCoordEndX = pixelCoordEndX;
    }

    public int getPixelCoordEndY() {
        return pixelCoordEndY;
    }

    public void setPixelCoordEndY(int pixelCoordEndY) {
        this.pixelCoordEndY = pixelCoordEndY;
    }

    public double getRunwayLat() {
        return runwayLat;
    }

    public double getRunwayLon() {
        return runwayLon;
    }
    
}
