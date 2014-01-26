package in.drifted.planisphere.renderer.svg;

import in.drifted.planisphere.model.Coord;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PathUtil {

    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("###.##", new DecimalFormatSymbols(Locale.ENGLISH));

    public static String getPathData(List<Coord> coordList, Boolean append) {

        StringBuilder pathData = new StringBuilder();

        Boolean isFirst = true;
        for (Coord coord : coordList) {
            if (isFirst) {
                if (append) {
                    pathData.append("L");
                } else {
                    pathData.append("M");
                }
                pathData.append(format(coord.getX()));
                pathData.append(" ");
                pathData.append(format(coord.getY()));
                isFirst = false;
            }
            pathData.append("L");
            pathData.append(format(coord.getX()));
            pathData.append(" ");
            pathData.append(format(coord.getY()));
        }
        pathData.append("z");
        
        return pathData.toString();
    }
    
    public static String getCirclePathData(Double radius) {

        BezierCircle circle = new BezierCircle(radius);
        return circle.getPathData();
    }

    public static String getCirclePathData(Coord center, Double radius) {

        BezierCircle circle = new BezierCircle(center, radius);
        return circle.getPathData();
    }

    public static String getCirclePathData(Coord center, Double radius, Double angle) {

        BezierCircle circle = new BezierCircle(center, radius, angle);
        return circle.getPathData();
    }
    
    public static String getCirclePathDataInv(Coord center, Double radius) {

        BezierCircle circle = new BezierCircle(center, radius);
        return circle.getPathDataInv();
    }

    public static String getLineHorizontalPathData(Coord center, Double length) {

        StringBuilder path = new StringBuilder();
        path.append("M");
        path.append(format(center.getX() - length / 2.0));
        path.append(" ");
        path.append(format(center.getY()));
        path.append("h");
        path.append(length);

        return path.toString();
    }
    
    public static Coord getIntersection(Double ax, Double ay, Double bx, Double by, Double cx, Double cy) {

        // Get the perpendicular bisector of (x1, y1) and (x2, y2)
        Double x1, y1, dx1, dy1;
        x1 = (bx + ax) / 2.0;
        y1 = (by + ay) / 2.0;
        dy1 = bx - ax;
        dx1 = -(by - ay);

        // Get the perpendicular bisector of (x2, y2) and (x3, y3)
        Double x2, y2, dx2, dy2;
        x2 = (cx + bx) / 2.0;
        y2 = (cy + by) / 2.0;
        dy2 = cx - bx;
        dx2 = -(cy - by);

        // See where the lines intersect
        Double ox, oy;
        ox = (y1 * dx1 * dx2 + x2 * dx1 * dy2 - x1 * dy1 * dx2 - y2 * dx1 * dx2)
                / (dx1 * dy2 - dy1 * dx2);
        oy = (ox - x1) * dy1 / dx1 + y1;

        return new Coord(ox, oy);
    }

    public static final String getCoordsChunk(Coord coord) {
        return format(coord.getX()) + " " + format(coord.getY());
    }

    public static final String format(Double number) {
        return NUMBER_FORMAT.format(number);
    }

}
