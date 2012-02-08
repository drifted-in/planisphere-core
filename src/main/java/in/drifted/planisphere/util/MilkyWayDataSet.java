package in.drifted.planisphere.util;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;

public class MilkyWayDataSet implements Serializable {

    private ArrayList<Point2D> darkNorth;
    private ArrayList<Point2D> darkSouth;
    private ArrayList<Point2D> brightNorth;
    private ArrayList<Point2D> brightSouth;

    public ArrayList<Point2D> getDarkNorth() {
        return this.darkNorth;
    }

    public void setDarkNorth(ArrayList<Point2D> darkNorth) {
        this.darkNorth = darkNorth;
    }

    public ArrayList<Point2D> getDarkSouth() {
        return this.darkSouth;
    }

    public void setDarkSouth(ArrayList<Point2D> darkSouth) {
        this.darkSouth = darkSouth;
    }

    public ArrayList<Point2D> getBrightNorth() {
        return this.brightNorth;
    }

    public void setBrightNorth(ArrayList<Point2D> lightNorth) {
        this.brightNorth = lightNorth;
    }

    public ArrayList<Point2D> getBrightSouth() {
        return this.brightSouth;
    }

    public void setBrightSouth(ArrayList<Point2D> lightSouth) {
        this.brightSouth = lightSouth;
    }
}
