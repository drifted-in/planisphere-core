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

public final class ConstellationName {

    private final String id;
    private final String abbreviation;
    private final String latinName;
    private final Point point;

    public ConstellationName(String abbreviation, String latinName, Point point) {
        this.abbreviation = abbreviation;
        this.latinName = latinName;
        this.id = generateId(latinName);
        this.point = point;
    }

    private String generateId(String name) {
        String[] fragments = name.split(" ");
        StringBuilder strId = new StringBuilder();
        for (int i = 0; i < fragments.length; i++) {
            String candidate = fragments[i].toLowerCase();
            if (i > 0) {
                candidate = candidate.substring(0, 1).toUpperCase() + candidate.substring(1);
            }
            strId.append(candidate);
        }
        return strId.toString();
    }

    public String getId() {
        return id;
    }

    public String getAbbreviation() {
        return this.abbreviation;
    }

    public Point getPoint() {
        return this.point;
    }

    public String getLatinName() {
        return this.latinName;
    }
}
