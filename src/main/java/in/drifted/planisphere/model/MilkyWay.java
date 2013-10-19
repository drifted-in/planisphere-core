package in.drifted.planisphere.model;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.List;

public final class MilkyWay implements Serializable {

    private List<Point2D> darkNorth;
    private List<Point2D> darkSouth;
    private List<Point2D> brightNorth;
    private List<Point2D> brightSouth;

    public List<Point2D> getDarkNorth() {
        return darkNorth;
    }

    public void setDarkNorth(List<Point2D> darkNorth) {
        this.darkNorth = darkNorth;
    }

    public List<Point2D> getDarkSouth() {
        return darkSouth;
    }

    public void setDarkSouth(List<Point2D> darkSouth) {
        this.darkSouth = darkSouth;
    }

    public List<Point2D> getBrightNorth() {
        return brightNorth;
    }

    public void setBrightNorth(List<Point2D> brightNorth) {
        this.brightNorth = brightNorth;
    }

    public List<Point2D> getBrightSouth() {
        return brightSouth;
    }

    public void setBrightSouth(List<Point2D> brightSouth) {
        this.brightSouth = brightSouth;
    }
}
