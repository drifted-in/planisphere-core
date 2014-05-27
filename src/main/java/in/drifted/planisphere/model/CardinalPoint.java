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

public final class CardinalPoint {

    private Double startOffset;
    private Double radius;
    private Coord center;
    private Coord tickStart;
    private Coord tickEnd;
    private String label;

    public Double getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(Double startOffset) {
        this.startOffset = startOffset;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    public Coord getCenter() {
        return center;
    }

    public void setCenter(Coord center) {
        this.center = center;
    }

    public Coord getTickStart() {
        return tickStart;
    }

    public void setTickStart(Coord tickStart) {
        this.tickStart = tickStart;
    }

    public Coord getTickEnd() {
        return tickEnd;
    }

    public void setTickEnd(Coord tickEnd) {
        this.tickEnd = tickEnd;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
