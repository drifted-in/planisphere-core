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
package in.drifted.planisphere.l10n;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class LocalizationUtil {

    private static final String LOCALE_BUNDLE = "in.drifted.planisphere.resources.localizations.messages";
    private ResourceBundle resources;
    private ResourceBundle resourcesFallback;

    public LocalizationUtil(Locale locale) {

        try {
            resources = ResourceBundle.getBundle(LOCALE_BUNDLE, locale);
            resourcesFallback = ResourceBundle.getBundle(LOCALE_BUNDLE, Locale.ENGLISH);

        } catch (MissingResourceException e) {
            resources = ResourceBundle.getBundle(LOCALE_BUNDLE, Locale.ENGLISH);
            resourcesFallback = resources;
        }
    }

    public Collection<String> getKeyCollection() {
        return resources.keySet();
    }

    public String getValue(String key) {
        switch (key) {
            case "year":
                return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
            default:
                if (resources.containsKey(key)) {
                    return resources.getString(key);
                } else if (resourcesFallback.containsKey(key)) {
                    return resourcesFallback.getString(key);
                } else {
                    return key;
                }
        }
    }

    public String getValue(String key, Double latitude) {
        switch (key) {
            case "latitudeValue":
                String strNorth = getValue("cardinalPointNorth");
                String strSouth = getValue("cardinalPointSouth");
                return Math.abs(latitude.intValue()) + " " + ((latitude < 0) ? strSouth : strNorth);
            default:
                return getValue(key);
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
