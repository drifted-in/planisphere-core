/*
 * Copyright (c) 2022-present Jan Tošovský <jan.tosovsky.cz@gmail.com>
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
package in.drifted.planisphere.util;

import in.drifted.planisphere.model.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

public class PolygonClipper {

    private static final int SCALE = 1000;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(SCALE));
    private final Polygon clipPolygon;

    public PolygonClipper(List<Point> clipAreaPointList) {
        this.clipPolygon = geometryFactory.createPolygon(getContourCoordinates(clipAreaPointList));
    }

    public List<List<Point>> getClippedContourList(List<Point> areaPointList) {

        List<List<Point>> contourList = new ArrayList<>();

        Coordinate[] contourCoordinates = getContourCoordinates(areaPointList);
        Polygon polygon = geometryFactory.createPolygon(contourCoordinates);
        Geometry geometry = polygon.buffer(0).intersection(clipPolygon);

        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            contourList.add(Arrays.stream(geometry.getGeometryN(i).getCoordinates())
                    .map(coordinate -> new Point(coordinate.getX(), coordinate.getY()))
                    .toList());
        }

        return contourList;
    }

    private Coordinate[] getContourCoordinates(List<Point> contourPointList) {
        return contourPointList.stream()
                .map(point -> new Coordinate(rounded(point.getX()), rounded(point.getY())))
                .toArray(Coordinate[]::new);
    }

    public static double rounded(double number) {
        return 1.0 * Math.round(number * SCALE) / SCALE;
    }

}
