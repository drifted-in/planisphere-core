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

import in.drifted.planisphere.resources.loader.ConstellationBoundaryListLoader;
import in.drifted.planisphere.resources.loader.ConstellationLineListLoader;
import in.drifted.planisphere.resources.loader.ConstellationNameListLoader;
import in.drifted.planisphere.resources.loader.MilkyWayLoader;
import in.drifted.planisphere.resources.loader.StarListLoader;
import in.drifted.planisphere.model.ConstellationName;
import in.drifted.planisphere.model.Point;
import in.drifted.planisphere.model.MilkyWay;
import in.drifted.planisphere.model.Star;
import in.drifted.planisphere.Settings;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CacheHandler {

    private static volatile CacheHandler instance = null;

    private List<Star> starList;
    private List<ConstellationName> constellationNameList;
    private List<Point> constellationLineList;
    private List<Point> constellationBoundaryList;
    private MilkyWay milkyWay;
    private final Map<String, String> colorSchemeMap = new HashMap<>();
    private final Map<String, String> fontDataMap = new HashMap<>();

    public static CacheHandler getInstance() {
        if (instance == null) {
            synchronized (CacheHandler.class) {
                if (instance == null) {
                    instance = new CacheHandler();
                }
            }
        }
        return instance;
    }

    private CacheHandler() {

        try {
            starList = StarListLoader.getStarList(Settings.FILE_PATH_STARS);
            constellationNameList = ConstellationNameListLoader.getConstellationNameList(Settings.FILE_PATH_CONSTELLATION_NAMES);
            constellationLineList = ConstellationLineListLoader.getConstellationLineList(Settings.FILE_PATH_CONSTELLATION_LINES);
            constellationBoundaryList = ConstellationBoundaryListLoader.getConstellationBoundaryList(Settings.FILE_PATH_CONSTELLATION_BOUNDARIES);

            List filePaths = new ArrayList();
            filePaths.add(Settings.FILE_PATH_MILKY_WAY_DARK_NORTH);
            filePaths.add(Settings.FILE_PATH_MILKY_WAY_DARK_SOUTH);
            filePaths.add(Settings.FILE_PATH_MILKY_WAY_BRIGHT_NORTH);
            filePaths.add(Settings.FILE_PATH_MILKY_WAY_BRIGHT_SOUTH);
            milkyWay = MilkyWayLoader.getMilkyWay(filePaths);

        } catch (IOException e) {
        }
    }

    public void reset() {
        colorSchemeMap.clear();
        fontDataMap.clear();
    }

    public String getColorSchemeData(String templateName, String colorScheme) throws IOException {

        if (!colorSchemeMap.containsKey(colorScheme)) {

            String colorSchemePath = getColorSchemePath(colorScheme);
            URL url = CacheHandler.class.getResource(colorSchemePath);
            if (url == null) {
                // every template has a default color scheme
                String defaultColorScheme = templateName.split("\\.|_")[0] + "_default";
                return getColorSchemeData(templateName, defaultColorScheme);
            }

            try (
                    InputStream inputStream = CacheHandler.class.getResourceAsStream(colorSchemePath);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                StringBuilder builder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                    builder.append("\n");
                }
                colorSchemeMap.put(colorScheme, builder.toString());
            }
        }

        return colorSchemeMap.get(colorScheme);
    }

    private String getColorSchemePath(String colorScheme) {
        return Settings.RESOURCE_BASE_PATH + "templates/core/" + colorScheme + ".css";
    }

    public String getFontData(String fontDataPath) throws IOException {

        if (!fontDataMap.containsKey(fontDataPath)) {

            try (
                    InputStream fontDataStream = CacheHandler.class.getResourceAsStream(fontDataPath);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                StringBuilder fontData = new StringBuilder("data:font/ttf;base64,");

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = fontDataStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                fontData.append(Base64.getEncoder().encodeToString(outputStream.toByteArray()));

                fontDataMap.put(fontDataPath, fontData.toString());
            }
        }

        return fontDataMap.get(fontDataPath);
    }

    public List<Star> getStarList() {
        return starList;
    }

    public List<ConstellationName> getConstellationNameList() {
        return constellationNameList;
    }

    public List<Point> getConstellationLineList() {
        return constellationLineList;
    }

    public List<Point> getConstellationBoundaryList() {
        return constellationBoundaryList;
    }

    public MilkyWay getMilkyWay() {
        return milkyWay;
    }

}
