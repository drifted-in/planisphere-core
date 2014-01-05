package in.drifted.planisphere.model;

import java.awt.geom.Point2D;
import java.io.Serializable;

public final class CardinalPoint implements Serializable {

    private Double startOffset;
    private Double radius;
    private Point2D center;
    private Point2D tickStart;
    private Point2D tickEnd;
    private String label;

    public Point2D getCenter() {
        return this.center;
    }

    public void setCenter(Point2D center) {
        this.center = center;
    }

    public Double getRadius() {
        return this.radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    public Double getStartOffset() {
        return this.startOffset;
    }

    public void setStartOffset(Double startOffset) {
        this.startOffset = startOffset;
    }

    public Point2D getTickEnd() {
        return this.tickEnd;
    }

    public void setTickEnd(Point2D tickEnd) {
        this.tickEnd = tickEnd;
    }

    public Point2D getTickStart() {
        return this.tickStart;
    }

    public void setTickStart(Point2D tickStart) {
        this.tickStart = tickStart;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
