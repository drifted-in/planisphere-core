package in.drifted.planisphere.renderer.svg;

import in.drifted.planisphere.util.CoordUtil;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class BezierCircle {

    private List<Point2D> pointList = new LinkedList<>();
    private static final Double KAPPA = 0.5522847498;

    public BezierCircle(Double radius) {
        this(new Point2D.Double(0, 0), radius, 0d);
    }

    public BezierCircle(Point2D center, Double radius) {
        this(center, radius, 0d);
    }

    public BezierCircle(Point2D center, Double radius, Double angle) {
        Double rKappa = radius * KAPPA;
        // p0
        pointList.add(new Point2D.Double(0, -radius));
        // p1
        pointList.add(new Point2D.Double(rKappa, -radius));
        // p2
        pointList.add(new Point2D.Double(radius, -rKappa));
        // p3
        pointList.add(new Point2D.Double(radius, 0));
        // p4
        pointList.add(new Point2D.Double(radius, rKappa));
        // p5
        pointList.add(new Point2D.Double(rKappa, radius));
        // p6
        pointList.add(new Point2D.Double(0, radius));
        // p7
        pointList.add(new Point2D.Double(-rKappa, radius));
        // p8
        pointList.add(new Point2D.Double(-radius, rKappa));
        // p9
        pointList.add(new Point2D.Double(-radius, 0));
        // p10
        pointList.add(new Point2D.Double(-radius, -rKappa));
        // p11
        pointList.add(new Point2D.Double(-rKappa, -radius));
        if (angle % 360 != 0) {
            rotate(angle);
        }
        if (center.getX() != 0 || center.getY() != 0) {
            translate(center);
        }
    }

    public void translate(Point2D center) {
        for (Point2D point : pointList) {
            point.setLocation(point.getX() + center.getX(), point.getY() + center.getY());
        }
    }

    public void rotate(Double angle) {
        Double angleInRads = Math.PI / 2 - Math.toRadians(angle);
        for (Point2D point : pointList) {
            Double radius = Math.sqrt(point.getX() * point.getX() + point.getY() * point.getY());
            Double angleFinal = angleInRads + Math.atan2(point.getY(), point.getX());
            point.setLocation(radius * Math.sin(angleFinal), radius * Math.cos(angleFinal));
        }
    }

    public String renderInv() {
        Iterator<Point2D> it = pointList.iterator();
        StringBuilder result = new StringBuilder();
        String firstPoint = CoordUtil.getCoordsChunk(it.next());
        while (it.hasNext()) {
            result.insert(0, CoordUtil.getCoordsChunk(it.next()));
            result.insert(0, " ");
            result.insert(0, CoordUtil.getCoordsChunk(it.next()));
            result.insert(0, "C");
            if (it.hasNext()) {
                result.insert(0, CoordUtil.getCoordsChunk(it.next()));
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
        Iterator<Point2D> it = pointList.iterator();
        StringBuilder result = new StringBuilder();
        String firstPoint = CoordUtil.getCoordsChunk(it.next());
        result.append("M");
        result.append(firstPoint);
        while (it.hasNext()) {
            result.append("C");
            result.append(CoordUtil.getCoordsChunk(it.next()));
            result.append(" ");
            result.append(CoordUtil.getCoordsChunk(it.next()));
            result.append(" ");
            if (it.hasNext()) {
                result.append(CoordUtil.getCoordsChunk(it.next()));
            } else {
                result.append(firstPoint);
            }
        }
        return result.toString();
    }
}
