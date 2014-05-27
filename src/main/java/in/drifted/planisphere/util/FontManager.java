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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public final class FontManager {

    private static final String FONT_BASE_PATH = "/in/drifted/planisphere/resources/fonts/";
    private final ResourceBundle resources;
    private final String country;

    public FontManager(Locale locale) {
        resources = ResourceBundle.getBundle("in.drifted.planisphere.resources.fonts.mapping");
        if (locale.getLanguage().equals("ar")) {
            country = "ar";
        } else {
            country = locale.getCountry();
        }
    }

    public String translate(String content) throws IOException {
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
                String fontKey = key.substring(0, key.indexOf("."));
                Collection<String> fontFileNameCollection = getFontFileNameCollection(fontKey);
                Boolean isSingle = fontFileNameCollection.size() == 1;
                String indexMarker = "";

                if (key.contains(".font-family")) {
                    int i = 0;
                    Iterator<String> it = fontFileNameCollection.iterator();
                    while (it.hasNext()) {
                        if (!isSingle) {
                            indexMarker = "-" + (++i);
                        }
                        String fontName = it.next().substring(0, key.indexOf(".")) + indexMarker;
                        chunkList.add("\"" + fontName + "\"");
                        if (it.hasNext()) {
                            chunkList.add(", ");
                        }
                    }
                } else {
                    int i = 0;
                    CacheHandler cacheHandler = CacheHandler.getInstance();
                    for (String fontFileName : fontFileNameCollection) {
                        if (!isSingle) {
                            indexMarker = "-" + (++i);
                        }
                        String fontName = fontFileName.substring(0, key.indexOf(".")) + indexMarker;
                        chunkList.add("@font-face {font-family: \"" + fontName + "\"; src: url(" + cacheHandler.getFontData(FONT_BASE_PATH + fontFileName) + ");}\n");
                    }
                }
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

    private Collection<String> getFontFileNameCollection(String key) {

        return Arrays.asList(getValue(key).split("\\|"));
    }

    private String getValue(String key) {
        String keyLocal = key + "." + country.toLowerCase(Locale.ENGLISH);
        String keyDefault = key + ".default";
        if (resources.containsKey(keyLocal)) {
            return resources.getString(keyLocal);
        } else if (resources.containsKey(keyDefault)) {
            return resources.getString(keyDefault);
        } else {
            return key;
        }
    }

}
