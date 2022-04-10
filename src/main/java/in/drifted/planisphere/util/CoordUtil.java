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
package in.drifted.planisphere.util;

import in.drifted.planisphere.model.Point;

public final class CoordUtil {

    public static final Point convert(double RA, double Dec, double latitude, double scale) {

        Point result = null;
        double RAInRads = RA * Math.PI / 12.0;

        if (latitude > 0) {
            if (Dec < latitude - 90) {
                return null;
            }
            double radius = scale * 0.89 * (90 - Dec) / (180 - latitude);
            result = new Point(Math.cos(RAInRads) * radius, Math.sin(RAInRads) * radius);
        } else {
            if (Dec > latitude + 90) {
                return null;
            }
            double radius = scale * 0.89 * (90 + Dec) / (180 + latitude);
            result = new Point(-Math.cos(RAInRads) * radius, Math.sin(RAInRads) * radius);
        }

        return result;
    }

    public static final Point convertWithoutCheck(double RA, double Dec, double latitude, double scale) {

        Point result = null;
        double RAInRads = RA * Math.PI / 12.0;

        if (latitude > 0) {
            double radius = scale * 0.89 * (90 - Dec) / (180 - latitude);
            result = new Point(Math.cos(RAInRads) * radius, Math.sin(RAInRads) * radius);
        } else {
            double radius = scale * 0.89 * (90 + Dec) / (180 + latitude);
            result = new Point(-Math.cos(RAInRads) * radius, Math.sin(RAInRads) * radius);
        }

        return result;
    }

}
