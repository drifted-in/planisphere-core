package in.drifted.planisphere.renderer.svg;

import in.drifted.planisphere.util.Coords;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

public final class BezierCircle {

    private ArrayList<Point2D> points = new ArrayList<Point2D>(12);
    private static final double KAPPA = 0.5522847498;

    public BezierCircle(double radius) {
        this(new Point2D.Double(0, 0), radius, 0d);
    }

    public BezierCircle(Point2D center, double radius) {
        this(center, radius, 0d);
    }

    public BezierCircle(Point2D center, double radius, double angle) {
        double rKappa = radius * KAPPA;
        // p0
        points.add(new Point2D.Double(0, -radius));
        // p1
        points.add(new Point2D.Double(rKappa, -radius));
        // p2
        points.add(new Point2D.Double(radius, -rKappa));
        // p3
        points.add(new Point2D.Double(radius, 0));
        // p4
        points.add(new Point2D.Double(radius, rKappa));
        // p5
        points.add(new Point2D.Double(rKappa, radius));
        // p6
        points.add(new Point2D.Double(0, radius));
        // p7
        points.add(new Point2D.Double(-rKappa, radius));
        // p8
        points.add(new Point2D.Double(-radius, rKappa));
        // p9
        points.add(new Point2D.Double(-radius, 0));
        // p10
        points.add(new Point2D.Double(-radius, -rKappa));
        // p11
        points.add(new Point2D.Double(-rKappa, -radius));
        if (angle % 360 != 0) {
            rotate(angle);
        }
        if (center.getX() != 0 || center.getY() != 0) {
            translate(center);
        }
    }

    public void translate(Point2D center) {
        for (Point2D point : points) {
            point.setLocation(point.getX() + center.getX(), point.getY() + center.getY());
        }
    }

    public void rotate(double angle) {
        //double angleInRads = - Math.toRadians(angle) - Math.PI / 2;
        double angleInRads = Math.PI / 2 - Math.toRadians(angle);
        for (Point2D point : points) {
            double radius = Math.sqrt(point.getX() * point.getX() + point.getY() * point.getY());
            double angleFinal = angleInRads + Math.atan2(point.getY(), point.getX());
            point.setLocation(radius * Math.sin(angleFinal), radius * Math.cos(angleFinal));
        }
    }

    public String renderInv() {
        Iterator<Point2D> it = points.iterator();
        StringBuilder result = new StringBuilder();
        String firstPoint = Coords.getCoordsChunk(it.next());
        while (it.hasNext()) {
            result.insert(0, Coords.getCoordsChunk(it.next()));
            result.insert(0, " ");
            result.insert(0, Coords.getCoordsChunk(it.next()));
            result.insert(0, "C");
            if (it.hasNext()) {
                result.insert(0, Coords.getCoordsChunk(it.next()));
                result.insert(0, " ");
            } else {
                result.insert(0, firstPoint);
                result.insert(0, "M");
            }
        }
        result.append(" ");
        result.append(firstPoint);

        return result.toString();
    }

    public String render() {
        Iterator<Point2D> it = points.iterator();
        StringBuilder result = new StringBuilder();
        String firstPoint = Coords.getCoordsChunk(it.next());
        result.append("M");
        result.append(firstPoint);
        while (it.hasNext()) {
            result.append("C");
            result.append(Coords.getCoordsChunk(it.next()));
            result.append(" ");
            result.append(Coords.getCoordsChunk(it.next()));
            result.append(" ");
            if (it.hasNext()) {
                result.append(Coords.getCoordsChunk(it.next()));
            } else {
                result.append(firstPoint);
            }
        }
        return result.toString();
    }
}
