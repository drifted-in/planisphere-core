package in.drifted.planisphere.model;

public final class CardinalPoint {

    private Double startOffset;
    private Double radius;
    private Coord center;
    private Coord tickStart;
    private Coord tickEnd;
    private String label;

    public Double getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(Double startOffset) {
        this.startOffset = startOffset;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    public Coord getCenter() {
        return center;
    }

    public void setCenter(Coord center) {
        this.center = center;
    }

    public Coord getTickStart() {
        return tickStart;
    }

    public void setTickStart(Coord tickStart) {
        this.tickStart = tickStart;
    }

    public Coord getTickEnd() {
        return tickEnd;
    }

    public void setTickEnd(Coord tickEnd) {
        this.tickEnd = tickEnd;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
