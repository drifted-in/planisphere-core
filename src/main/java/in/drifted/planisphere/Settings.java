package in.drifted.planisphere;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    public static final String THEME_PRINT_DEFAULT = TEMPLATE_PRINT_DEFAULT + "_default";

    public static Collection<String> getTemplateNameCollection() throws IOException {

        Properties templatesProperties = new Properties();

        templatesProperties.load(Settings.class.getResourceAsStream(Settings.FILE_PATH_TEMPLATES_PROPERTIES));

        return Arrays.asList(templatesProperties.getProperty("print").replace("_mode", "").split("\\|"));
    }

    public static Collection<String> getColorSchemeCollection(String templateName) throws IOException {

        Collection<String> colorSchemeCollection = new HashSet<>();

        try (
                InputStream in = Settings.class.getResourceAsStream("/in/drifted/planisphere/resources/templates/core");
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(templateName + "_") && line.endsWith(".css")) {
                    colorSchemeCollection.add(line.replace(".css", ""));
                }
            }
        }

        return colorSchemeCollection;
    }

    public static Collection<String> getLocaleValueCollection() throws IOException {

        Collection<String> localeValueCollection = new TreeSet<>();

        try (
                InputStream in = Settings.class.getResourceAsStream("/in/drifted/planisphere/resources/localizations");
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("messages_")) {
                    localeValueCollection.add(line.replace("messages_", "").replace(".properties", "").replace("_", "|"));
                }
            }
        }

        return localeValueCollection;
    }

    public static Map<String, Options> getTemplateOptionsMap(Options options) throws IOException {

        Map<String, Options> templateMap = new LinkedHashMap<>();

        Options invertedDoubleSidedSignOptions = new Options(options);
        invertedDoubleSidedSignOptions.setDoubleSidedSign(-1);

        Properties templatesProperties = new Properties();
        templatesProperties.load(Settings.class.getResourceAsStream(Settings.FILE_PATH_TEMPLATES_PROPERTIES));

        String templateName = options.getPrintTheme().split("_")[0];

        String mode = options.getDoubleSided() ? "D" : "S";
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

        String printTheme = options.getPrintTheme();
        String templateName = printTheme.split("_")[0];

        if (!getColorSchemeCollection(templateName).contains(printTheme)) {
            options.setPrintTheme(THEME_PRINT_DEFAULT);
        }
    }
}
