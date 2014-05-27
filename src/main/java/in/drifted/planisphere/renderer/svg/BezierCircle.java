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

import in.drifted.planisphere.model.Coord;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public final class BezierCircle {

    private static final Double KAPPA = 0.5522847498;
    private final List<Coord> pointList = new LinkedList<>();

    public BezierCircle(Double radius) {
        this(new Coord(0.0, 0.0), radius, 0.0);
    }

    public BezierCircle(Coord center, Double radius) {
        this(center, radius, 0.0);
    }

    public BezierCircle(Coord center, Double radius, Double angle) {
        Double rKappa = radius * KAPPA;
        // p0
        pointList.add(new Coord(0.0, -radius));
        // p1
        pointList.add(new Coord(rKappa, -radius));
        // p2
        pointList.add(new Coord(radius, -rKappa));
        // p3
        pointList.add(new Coord(radius, 0.0));
        // p4
        pointList.add(new Coord(radius, rKappa));
        // p5
        pointList.add(new Coord(rKappa, radius));
        // p6
        pointList.add(new Coord(0.0, radius));
        // p7
        pointList.add(new Coord(-rKappa, radius));
        // p8
        pointList.add(new Coord(-radius, rKappa));
        // p9
        pointList.add(new Coord(-radius, 0.0));
        // p10
        pointList.add(new Coord(-radius, -rKappa));
        // p11
        pointList.add(new Coord(-rKappa, -radius));
        if (angle % 360 != 0) {
            rotate(angle);
        }
        if (center.getX() != 0 || center.getY() != 0) {
            translate(center);
        }
    }

    public void translate(Coord center) {
        for (Coord point : pointList) {
            point.setLocation(point.getX() + center.getX(), point.getY() + center.getY());
        }
    }

    public void rotate(Double angle) {
        Double angleInRads = Math.PI / 2 - Math.toRadians(angle);
        for (Coord point : pointList) {
            Double radius = Math.sqrt(point.getX() * point.getX() + point.getY() * point.getY());
            Double angleFinal = angleInRads + Math.atan2(point.getY(), point.getX());
            point.setLocation(radius * Math.sin(angleFinal), radius * Math.cos(angleFinal));
        }
    }

    public String getPathDataInv() {

        StringBuilder pathData = new StringBuilder();

        Iterator<Coord> it = pointList.iterator();
        String firstPoint = PathUtil.getCoordsChunk(it.next());
        while (it.hasNext()) {
            pathData.insert(0, PathUtil.getCoordsChunk(it.next()));
            pathData.insert(0, " ");
            pathData.insert(0, PathUtil.getCoordsChunk(it.next()));
            pathData.insert(0, "C");
            if (it.hasNext()) {
                pathData.insert(0, PathUtil.getCoordsChunk(it.next()));
                pathData.insert(0, " ");
            } else {
                pathData.insert(0, firstPoint);
                pathData.insert(0, "M");
            }
        }
        pathData.append(" ");
        pathData.append(firstPoint);

        return pathData.toString();
    }

    public String getPathData() {

        StringBuilder pathData = new StringBuilder();

        Iterator<Coord> it = pointList.iterator();
        String firstPoint = PathUtil.getCoordsChunk(it.next());
        pathData.append("M");
        pathData.append(firstPoint);
        while (it.hasNext()) {
            pathData.append("C");
            pathData.append(PathUtil.getCoordsChunk(it.next()));
            pathData.append(" ");
            pathData.append(PathUtil.getCoordsChunk(it.next()));
            pathData.append(" ");
            if (it.hasNext()) {
                pathData.append(PathUtil.getCoordsChunk(it.next()));
            } else {
                pathData.append(firstPoint);
            }
        }
        return pathData.toString();
    }
}
