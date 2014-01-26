package in.drifted.planisphere.model;

import java.util.List;

public final class MilkyWay {

    private List<Coord> darkNorth;
    private List<Coord> darkSouth;
    private List<Coord> brightNorth;
    private List<Coord> brightSouth;

    public List<Coord> getDarkNorth() {
        return darkNorth;
    }

    public void setDarkNorth(List<Coord> darkNorth) {
        this.darkNorth = darkNorth;
    }

    public List<Coord> getDarkSouth() {
        return darkSouth;
    }

    public void setDarkSouth(List<Coord> darkSouth) {
        this.darkSouth = darkSouth;
    }

    public List<Coord> getBrightNorth() {
        return brightNorth;
    }

    public void setBrightNorth(List<Coord> brightNorth) {
        this.brightNorth = brightNorth;
    }

    public List<Coord> getBrightSouth() {
        return brightSouth;
    }

    public void setBrightSouth(List<Coord> brightSouth) {
        this.brightSouth = brightSouth;
    }
}
