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
package in.drifted.planisphere;

import in.drifted.planisphere.util.ResourceUtil;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

public final class Settings {

    public static final String RESOURCE_BASE_PATH = "/in/drifted/planisphere/resources/";
    public static final String RESOURCE_DATA_PATH = RESOURCE_BASE_PATH + "data/";
    public static final String FILE_PATH_STARS = RESOURCE_DATA_PATH + "stars.txt";
    public static final String FILE_PATH_CONSTELLATION_NAMES = RESOURCE_DATA_PATH + "constellationNames.txt";
    public static final String FILE_PATH_CONSTELLATION_LINES = RESOURCE_DATA_PATH + "constellationLines.txt";
    public static final String FILE_PATH_CONSTELLATION_BOUNDARIES = RESOURCE_DATA_PATH + "constellationBoundaries.txt";
    public static final String FILE_PATH_MILKY_WAY_DARK_NORTH = RESOURCE_DATA_PATH + "milkyWayDarkNorth.txt";
    public static final String FILE_PATH_MILKY_WAY_DARK_SOUTH = RESOURCE_DATA_PATH + "milkyWayDarkSouth.txt";
    public static final String FILE_PATH_MILKY_WAY_BRIGHT_NORTH = RESOURCE_DATA_PATH + "milkyWayBrightNorth.txt";
    public static final String FILE_PATH_MILKY_WAY_BRIGHT_SOUTH = RESOURCE_DATA_PATH + "milkyWayBrightSouth.txt";
    public static final String FILE_PATH_TEMPLATES_PROPERTIES = RESOURCE_BASE_PATH + "templates/templates.properties";
    public static final String TEMPLATE_PRINT_DEFAULT = "printDefault";
    public static final String THEME_PRINT_DEFAULT = TEMPLATE_PRINT_DEFAULT + "_dark";
    public static final String TEMPLATE_SCREEN_DEFAULT = "screenDefault";
    public static final String THEME_SCREEN_DEFAULT = TEMPLATE_SCREEN_DEFAULT + "_default";
    public static final String MEDIA_PRINT = "print";
    public static final String MEDIA_SCREEN = "screen";

    public static Collection<String> getAllTemplateNameCollection() throws IOException {

        Collection<String> templateNameCollection = new HashSet<>();

        templateNameCollection.addAll(getTemplateNameCollection(MEDIA_PRINT));
        templateNameCollection.addAll(getTemplateNameCollection(MEDIA_SCREEN));

        return templateNameCollection;
    }

    public static Collection<String> getTemplateNameCollection(String media) throws IOException {

        Collection<String> templateNameCollection = new HashSet<>();

        Properties templatesProperties = new Properties();
        templatesProperties.load(Settings.class.getResourceAsStream(Settings.FILE_PATH_TEMPLATES_PROPERTIES));

        String templateSpec = templatesProperties.getProperty(media).replace("_mode", "");
        if (templateSpec.contains("|")) {
            templateNameCollection = Arrays.asList(templateSpec.split("\\|"));
        } else {
            templateNameCollection.add(templateSpec);
        }

        return templateNameCollection;
    }

    public static Collection<String> getColorSchemeCollection(String templateName) throws IOException {

        Collection<String> colorSchemeCollection = new HashSet<>();

        for (String fileName : ResourceUtil.getResourceCollection(Settings.class, "/in/drifted/planisphere/resources/templates/core")) {
            if (fileName.startsWith(templateName + "_") && fileName.endsWith(".css")) {
                colorSchemeCollection.add(fileName.replace(".css", ""));
            }
        }

        return colorSchemeCollection;
    }

    public static Collection<String> getLocaleValueCollection() throws IOException {

        Collection<String> localeValueCollection = new TreeSet<>();

        for (String fileName : ResourceUtil.getResourceCollection(Settings.class, "/in/drifted/planisphere/resources/localizations")) {
            if (fileName.startsWith("messages_")) {
                localeValueCollection.add(fileName.replace("messages_", "").replace(".properties", "").replace("_", "|"));
            }
        }

        return localeValueCollection;
    }

    public static Map<String, Options> getTemplateOptionsMap(Options options) throws IOException {

        Map<String, Options> templateMap = new LinkedHashMap<>();

        Options invertedDoubleSidedSignOptions = new Options(options.getLatitudeFixed(), options.getLocale(),
                options.getThemeScreen(), options.getThemePrint(), -1, options.hasConstellationLines(),
                options.hasConstellationLabels(), options.getConstellationLabelsMode(), options.hasConstellationBoundaries(),
                options.hasMilkyWay(), options.hasDayLightSavingTimeScale(), options.hasCoordsRADec(), options.hasEcliptic(),
                options.hasAllVisibleStars());

        Properties templatesProperties = new Properties();
        templatesProperties.load(Settings.class.getResourceAsStream(Settings.FILE_PATH_TEMPLATES_PROPERTIES));

        String templateName = options.getThemePrint().split("_")[0];

        String mode = options.isDoubleSided() ? "D" : "S";
        String templateKey = templateName + "_" + mode;

        if (!templatesProperties.containsKey(templateKey)) {
            templateKey = TEMPLATE_PRINT_DEFAULT + "_" + mode;
        }

        int i = 0;
        for (String template : templatesProperties.getProperty(templateKey).split("\\|")) {
            if (i % 2 == 0) {
                templateMap.put(template, options);
            } else {
                templateMap.put(template, invertedDoubleSidedSignOptions);
            }
            i++;
        }

        return templateMap;
    }

    public static void normalizePrintTheme(Options options) throws IOException {

        String themePrint = options.getThemePrint();
        String templateName = themePrint.split("_")[0];

        if (!getColorSchemeCollection(templateName).contains(themePrint)) {
            options.setThemePrint(THEME_PRINT_DEFAULT);
        }
    }
}
