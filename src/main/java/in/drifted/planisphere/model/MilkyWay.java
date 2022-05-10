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
package in.drifted.planisphere.model;

import java.util.List;

public final class MilkyWay {

    private List<Point> darkNorth;
    private List<Point> darkSouth;
    private List<Point> brightNorth;
    private List<Point> brightSouth;

    public List<Point> getDarkNorth() {
        return darkNorth;
    }

    public void setDarkNorth(List<Point> darkNorth) {
        this.darkNorth = darkNorth;
    }

    public List<Point> getDarkSouth() {
        return darkSouth;
    }

    public void setDarkSouth(List<Point> darkSouth) {
        this.darkSouth = darkSouth;
    }

    public List<Point> getBrightNorth() {
        return brightNorth;
    }

    public void setBrightNorth(List<Point> brightNorth) {
        this.brightNorth = brightNorth;
    }

    public List<Point> getBrightSouth() {
        return brightSouth;
    }

    public void setBrightSouth(List<Point> brightSouth) {
        this.brightSouth = brightSouth;
    }
}
