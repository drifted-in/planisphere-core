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

import java.text.DateFormatSymbols;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

public class SubsetUtil {

    public static String getFontForgeSelectionScript(Locale locale) {

        /*
         
         Method Used for subsetting this huge font for non-latin languages:
         https://github.com/android/platform_frameworks_base/blob/master/data/fonts/DroidSansFallback.ttf
        
         1. Generate script and copy it from e.g. debug window into the clipboard        
         2. Open FontForge | File | Open... and choose DroidSansFallback.ttf
         3. Choose File | Execute script...
         4. Insert the script to the dialog area and press Ok
         5. Choose Edit | Selection | Invert Selection
         6. Choose Encoding | Detach & Remove glyphs... (removing glyphs takes quite long)
         7. Choose Element | Font Info... | OS/2 | Misc. | Embeddable and select 'Installable Font'
         8. Choose File | Generate Fonts...
         9. Store the final file in the following path: \in\drifted\planisphere\core\src\main\resources\in\drifted\planisphere\resources\fonts\droid-sans-[locale].ttf
        
         */
        StringBuilder sb = new StringBuilder("Select(32, 127)\n");

        for (Integer codePoint : getCodePointCollection(locale)) {
            sb.append("SelectMoreSingletons(");
            sb.append(codePoint);
            sb.append(")\n");
        }

        return sb.toString();
    }

    private static Collection<Integer> getCodePointCollection(Locale locale) {

        Collection<Integer> codePointCollection = new HashSet<>();

        LocalizationUtil localizationUtil = new LocalizationUtil(locale);
        for (String key : localizationUtil.getKeyCollection()) {
            codePointCollection.addAll(getCodePointCollection(localizationUtil.getValue(key)));
        }

        DateFormatSymbols symbols = new DateFormatSymbols(locale);
        for (String month : symbols.getMonths()) {
            codePointCollection.addAll(getCodePointCollection(month));
        }

        return codePointCollection;
    }

    private static Collection<Integer> getCodePointCollection(String text) {

        Collection<Integer> codePointCollection = new HashSet<>();

        for (int i = 0; i < text.length(); i++) {
            Integer codePoint = text.codePointAt(i);
            if (codePoint > 128) {
                codePointCollection.add(codePoint);
            }
        }

        return codePointCollection;
    }

}
