package in.drifted.planisphere.model;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.List;

public class MilkyWayDataSet implements Serializable {

    private List<Point2D> darkNorth;
    private List<Point2D> darkSouth;
    private List<Point2D> brightNorth;
    private List<Point2D> brightSouth;

    public List<Point2D> getDarkNorth() {
        return this.darkNorth;
    }

    public void setDarkNorth(List<Point2D> darkNorth) {
        this.darkNorth = darkNorth;
    }

    public List<Point2D> getDarkSouth() {
        return this.darkSouth;
    }

    public void setDarkSouth(List<Point2D> darkSouth) {
        this.darkSouth = darkSouth;
    }

    public List<Point2D> getBrightNorth() {
        return this.brightNorth;
    }

    public void setBrightNorth(List<Point2D> lightNorth) {
        this.brightNorth = lightNorth;
    }

    public List<Point2D> getBrightSouth() {
        return this.brightSouth;
    }

    public void setBrightSouth(List<Point2D> lightSouth) {
        this.brightSouth = lightSouth;
    }
}
