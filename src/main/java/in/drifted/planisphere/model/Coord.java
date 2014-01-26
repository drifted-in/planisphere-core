package in.drifted.planisphere.model;

public final class Coord {

    private Double x;
    private Double y;

    public Coord() {
    }

    public Coord(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public void setLocation(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }
}
