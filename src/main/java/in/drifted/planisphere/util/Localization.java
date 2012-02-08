package in.drifted.planisphere.util;

import in.drifted.planisphere.Settings;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Localization implements Serializable {

    private final String LOCALE_BUNDLE = "in.drifted.planisphere.resources.localizations.messages";
    ResourceBundle resources;

    public Localization(Locale locale) {
        try {
            resources = ResourceBundle.getBundle(LOCALE_BUNDLE, locale);
        } catch (MissingResourceException e) {
            resources = ResourceBundle.getBundle(LOCALE_BUNDLE, Locale.ENGLISH);
        }
    }

    public String getValue(String key) {
        if (resources.containsKey(key)) {
            return resources.getString(key);
        } else if (key.equals("year")) {
            Calendar calendar = Calendar.getInstance();
            return new Integer(calendar.get(Calendar.YEAR)).toString();
        } else if (key.equals("latitudeValue")) {
            String strNorth = resources.getString("cardinalPointNorth");
            String strSouth = resources.getString("cardinalPointSouth");
            return Math.abs(Settings.latitude.intValue()) + " " + ((Settings.latitude < 0) ? strSouth : strNorth);
        } else {
            return key;
        }
    }

    public String translate(String content) {
        String[] chunksRaw = content.split("\\$\\{");
        if (chunksRaw.length <= 1) {
            return content;
        }
        ArrayList<String> chunks = new ArrayList<String>();
        for (int c = 0; c < chunksRaw.length; c++) {
            if (!chunksRaw[c].contains("}")) {
                if (chunksRaw[c].length() > 0) {
                    chunks.add(chunksRaw[c]);
                }
            } else {
                int index = chunksRaw[c].indexOf("}");
                String key = chunksRaw[c].substring(0, index);
                chunks.add(getValue(key));
                if (index != chunksRaw[c].length() - 1) {
                    chunks.add(chunksRaw[c].substring(index + 1));
                }
            }
        }
        StringBuilder contentUpdated = new StringBuilder();
        for (String item : chunks) {
            contentUpdated.append(item);
        }
        return contentUpdated.toString();
    }
}
