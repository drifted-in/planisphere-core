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

    private final double startOffset;
    private final double radius;
    private final Point center;
    private final Point tickStart;
    private final Point tickEnd;
    private final String label;

    public CardinalPoint(double startOffset, double radius, Point center, Point tickStart, Point tickEnd, String label) {
        this.startOffset = startOffset;
        this.radius = radius;
        this.center = center;
        this.tickStart = tickStart;
        this.tickEnd = tickEnd;
        this.label = label;
    }

    public double getStartOffset() {
        return startOffset;
    }

    public double getRadius() {
        return radius;
    }

    public Point getCenter() {
        return center;
    }

    public Point getTickStart() {
        return tickStart;
    }

    public Point getTickEnd() {
        return tickEnd;
    }

    public String getLabel() {
        return label;
    }

}
