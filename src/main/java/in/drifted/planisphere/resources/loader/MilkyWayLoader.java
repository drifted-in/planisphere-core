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
package in.drifted.planisphere.resources.loader;

import in.drifted.planisphere.model.Point;
import in.drifted.planisphere.model.MilkyWay;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public final class MilkyWayLoader {

    public static MilkyWay getMilkyWay(List<String> filePathList) throws IOException {

        MilkyWay milkyWay = new MilkyWay();

        int i = 0;
        Double ngp = Math.toRadians(27.4);

        for (String filePath : filePathList) {

            List<Point> dataSet = new LinkedList<>();

            try (
                    InputStream inputStream = MilkyWayLoader.class.getResourceAsStream(filePath);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "ASCII"))) {

                String strLine;
                while ((strLine = reader.readLine()) != null) {
                    if (!strLine.isEmpty() && !strLine.startsWith("#")) {
                        String[] values = strLine.split(" ");
                        double l = Math.toRadians(360.0 - 33.0 - (180.0 + Double.parseDouble(values[0]) / 100.0));
                        double b = Math.toRadians(Double.parseDouble(values[1]) / 1000.0);
                        double Dec = Math.toDegrees(Math.asin(Math.cos(b) * Math.cos(ngp) * Math.sin(l) + Math.sin(b) * Math.sin(ngp)));
                        double RA = 90.0 + (282.25 + Math.toDegrees(Math.atan2(Math.cos(b) * Math.cos(l), Math.sin(b) * Math.cos(ngp) - Math.cos(b) * Math.sin(ngp) * Math.sin(l)))) / 15.0;
                        Point coord = new Point(RA, Dec);
                        dataSet.add(coord);
                    }
                }
            }

            switch (i) {
                case 0:
                    milkyWay.setDarkNorth(dataSet);
                    break;
                case 1:
                    milkyWay.setDarkSouth(dataSet);
                    break;
                case 2:
                    milkyWay.setBrightNorth(dataSet);
                    break;
                case 3:
                    milkyWay.setBrightSouth(dataSet);
                    break;
                default:
            }
            i++;
        }

        return milkyWay;
    }
}
