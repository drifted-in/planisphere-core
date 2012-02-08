package in.drifted.planisphere.util;

import java.util.HashMap;
import java.util.Map;
import java.awt.Color;
import in.drifted.planisphere.Settings;
import java.io.Serializable;

public class Styles implements Serializable {

    /*
    cutting_outline_main = "0 0 0 0.4"
    cutting_background = "0 0 0 1"
    area_outline = "0 0 0 0.4"
    area_background = "0 0 0 0"

    area_sign = "0 0 0 0"
    area_label = "0 0 0 0"
    hour_sign = "0 0 0 0"
    hour_halfsign = "0 0 0 0"
    hour_caption = "0 0 0 1"
    hour_summertime = "0 0 0 0"
    zoodiac_label = "0 0 0 0.9"
    zoodiac_caption = "0 0 0 0"
    zoodiac_backgroud = "0 0 0 0.8"
    zoodiac_contents = "0 0 0 0"
    map_outline = "0 0 0 0.6"
    map_background = "1 0 0 0.7"
    month_main_scale = "0 0 0 0"
    month_large_scale = "0 0 0 0"
    month_small_scale = "0 0 0 0"
    month_names = "0 0 0 0"
    star = "0 0 1 0"
    con_lines = "0 0 1 0.5"
    con_bounds = "0 0 1 0.7"
    con_abbrev = "0 0 0 0"
    con_overview = "0 0 0 0"
    con_divlines = "0 0 0 0"
    con_names = "0 0 0 0"
    copyright = "0 0 0 0"
    milky_dark = "1 0 0 0.65"
    milky_bright = "1 0 0 0.6"
    zoodiac = "0 0 1 0.7"
    RaDec_lines = "0 0 1 0.7"
    RaDec_desc = "0 0 1 0.2"
     */
    public static final String MILKY_WAY_DARK = "milkyWayDark";
    public static final String MILKY_WAY_BRIGHT = "milkyWayBright";
    public static final String PATH_FOR_TEXT = "pathForText";
    public static final String CARDINAL_POINT_LABELS = "cardinalPointLabels";
    private static Map<String, Color> schemeDefault = createSchemeDefault();

    private static Map<String, Color> createSchemeDefault() {
        Map<String, Color> result = new HashMap<String, Color>();
        result.put("lines", new Color(1f, 0.5f, 0.5f, 1f));
        result.put("circles", new Color(1f, 1f, 0f, 1f));
        return result;
    }
    private static Map<String, Color> schemeBW = createSchemeBW();

    private static Map<String, Color> createSchemeBW() {
        Map<String, Color> result = new HashMap<String, Color>();
        result.put("lines", new Color(1f, 0.5f, 0.5f, 1f));
        result.put("circles", new Color(0f, 0f, 0f, 1f));
        return result;
    }

    public static Color getColor(String key) {
        Map style = null;
        /*
        switch (Settings.colorScheme) {
            case 1:
                style = schemeBW;
                break;
            default:
                style = schemeDefault;
        }
         */
        return (Color) style.get(key);
    }

    public static String getColorStr(String key) {
        return formatColor(getColor(key));
    }

    public static String getAlphaStr(String key) {
        return new Integer(getColor(key).getAlpha()).toString();
    }
    private static Map<String, Map<String, String>> styles = createStyles();

    private static Map<String, Map<String, String>> createStyles() {
        Map<String, String> attributes = null;
        Map<String, Map<String, String>> items = new HashMap<String, Map<String, String>>();

        // milky way bright
        attributes = new HashMap<String, String>();
        attributes.put("stroke", "red");
        attributes.put("fill", getColorStr("circles"));
        items.put(MILKY_WAY_BRIGHT, attributes);

        // milky way dark
        attributes = new HashMap<String, String>();
        attributes.put("stroke", "yellow");
        attributes.put("fill", getColorStr("lines"));
        items.put(MILKY_WAY_DARK, attributes);

        // path for text
        attributes = new HashMap<String, String>();
        attributes.put("stroke", "none");
        attributes.put("fill", "none");
        items.put(PATH_FOR_TEXT, attributes);

        // cardinal point labels
        attributes = new HashMap<String, String>();
        attributes.put("fill", "orange");
        attributes.put("font-size", Coords.format(0.04 * Settings.scale));
        items.put(CARDINAL_POINT_LABELS, attributes);

        return items;
    }

    public static Map<String, String> getAttributes(String style) {
        return (Map<String, String>) styles.get(style);
    }

    private static String formatColor(Color color) {
        return "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
    }
}
