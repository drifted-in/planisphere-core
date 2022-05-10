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

import in.drifted.planisphere.model.Star;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class StarListLoader {

    public static List<Star> getStarList(String filePath) throws IOException {

        List<Star> starList = new ArrayList<>();

        try (
                InputStream inputStream = StarListLoader.class.getResourceAsStream(filePath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "ASCII"))) {

            String strLine;
            while ((strLine = reader.readLine()) != null) {
                if (!strLine.isEmpty() && !strLine.startsWith("#")) {
                    String[] values = strLine.split(",");
                    starList.add(new Star(Double.parseDouble(values[0]) / 1000.0, Double.parseDouble(values[1]) / 100.0, Double.parseDouble(values[2]) / 100.0));
                }
            }
        }

        return starList;
    }
}
