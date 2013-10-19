package in.drifted.planisphere.util;

import java.io.Serializable;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class LocalizationUtil implements Serializable {

    private static final String LOCALE_BUNDLE = "in.drifted.planisphere.resources.localizations.messages";
    private ResourceBundle resources;

    public LocalizationUtil(Locale locale) {
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
            return String.valueOf(calendar.get(Calendar.YEAR));
        } else {
            return key;
        }
    }

    public String getValue(String key, Double latitude) {
        if (resources.containsKey(key)) {
            return resources.getString(key);
        } else if (key.equals("year")) {
            Calendar calendar = Calendar.getInstance();
            return String.valueOf(calendar.get(Calendar.YEAR));
        } else if (key.equals("latitudeValue")) {
            String strNorth = resources.getString("cardinalPointNorth");
            String strSouth = resources.getString("cardinalPointSouth");
            return Math.abs(latitude.intValue()) + " " + ((latitude < 0) ? strSouth : strNorth);
        } else {
            return key;
        }
    }

    public String translate(String content, Double latitude) {
        String[] chunksRaw = content.split("\\$\\{");
        if (chunksRaw.length <= 1) {
            return content;
        }
        List<String> chunkList = new LinkedList<>();
        for (String chunk : chunksRaw) {
            if (!chunk.contains("}")) {
                if (chunk.length() > 0) {
                    chunkList.add(chunk);
                }
            } else {
                int index = chunk.indexOf("}");
                String key = chunk.substring(0, index);
                chunkList.add(getValue(key, latitude));
                if (index != chunk.length() - 1) {
                    chunkList.add(chunk.substring(index + 1));
                }
            }
        }

        StringBuilder contentUpdated = new StringBuilder();
        for (String chunk : chunkList) {
            contentUpdated.append(chunk);
        }
        return contentUpdated.toString();
    }
}
