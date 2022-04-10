/*
 * Copyright (c) 2012-present Jan Tošovský <jan.tosovsky.cz@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package in.drifted.planisphere.renderer.svg;

import in.drifted.planisphere.model.Point;
import java.util.ArrayList;
import java.util.List;

public final class BezierCircle {

    private static final double KAPPA = 0.5522847498;
    private final List<Point> pointList = new ArrayList<>(12);

    public BezierCircle(double radius) {
        this(new Point(0, 0), radius, 0);
    }

    public BezierCircle(Point center, double radius) {
        this(center, radius, 0);
    }

    public BezierCircle(Point center, double radius, double angle) {
        double rKappa = radius * KAPPA;
        // p0
        pointList.add(new Point(0, -radius));
        // p1
        pointList.add(new Point(rKappa, -radius));
        // p2
        pointList.add(new Point(radius, -rKappa));
        // p3
        pointList.add(new Point(radius, 0));
        // p4
        pointList.add(new Point(radius, rKappa));
        // p5
        pointList.add(new Point(rKappa, radius));
        // p6
        pointList.add(new Point(0, radius));
        // p7
        pointList.add(new Point(-rKappa, radius));
        // p8
        pointList.add(new Point(-radius, rKappa));
        // p9
        pointList.add(new Point(-radius, 0));
        // p10
        pointList.add(new Point(-radius, -rKappa));
        // p11
        pointList.add(new Point(-rKappa, -radius));
        if (angle % 360 != 0) {
            rotate(angle);
        }
        if (center.getX() != 0 || center.getY() != 0) {
            translate(center);
        }
    }

    private void translate(Point center) {
        List<Point> translatedPointList = new ArrayList<>(12);
        for (Point point : pointList) {
            translatedPointList.add(new Point(point.getX() + center.getX(), point.getY() + center.getY()));
        }
        pointList.clear();
        pointList.addAll(translatedPointList);
    }

    private void rotate(double angle) {
        List<Point> rotatedPointList = new ArrayList<>(12);
        double angleInRads = Math.PI / 2 - Math.toRadians(angle);
        for (Point point : pointList) {
            double radius = Math.sqrt(point.getX() * point.getX() + point.getY() * point.getY());
            double angleFinal = angleInRads + Math.atan2(point.getY(), point.getX());
            rotatedPointList.add(new Point(radius * Math.sin(angleFinal), radius * Math.cos(angleFinal)));
        }
        pointList.clear();
        pointList.addAll(rotatedPointList);
    }

    public List<Point> getPointList() {
        return pointList;
    }

}
