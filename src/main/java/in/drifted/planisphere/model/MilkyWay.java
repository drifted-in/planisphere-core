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

    private List<Coord> darkNorth;
    private List<Coord> darkSouth;
    private List<Coord> brightNorth;
    private List<Coord> brightSouth;

    public List<Coord> getDarkNorth() {
        return darkNorth;
    }

    public void setDarkNorth(List<Coord> darkNorth) {
        this.darkNorth = darkNorth;
    }

    public List<Coord> getDarkSouth() {
        return darkSouth;
    }

    public void setDarkSouth(List<Coord> darkSouth) {
        this.darkSouth = darkSouth;
    }

    public List<Coord> getBrightNorth() {
        return brightNorth;
    }

    public void setBrightNorth(List<Coord> brightNorth) {
        this.brightNorth = brightNorth;
    }

    public List<Coord> getBrightSouth() {
        return brightSouth;
    }

    public void setBrightSouth(List<Coord> brightSouth) {
        this.brightSouth = brightSouth;
    }
}
