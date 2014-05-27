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

public final class Star {

    private final Double RA;
    private final Double Dec;
    private final Double Mag;

    public Star(Double RA, Double Dec, Double Mag) {
        this.RA = RA;
        this.Dec = Dec;
        this.Mag = Mag;
    }

    public Double getRA() {
        return RA;
    }

    public Double getDec() {
        return Dec;
    }

    public Double getMag() {
        return Mag;
    }
}
