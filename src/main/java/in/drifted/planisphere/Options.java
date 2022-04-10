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

import in.drifted.planisphere.l10n.LocalizationUtil;
import java.util.Locale;

public class Options {

    private final double latitudeFixed;
    private final int latitudeFixedSign;
    private final double latitude;
    private final int latitudeSign;
    private final boolean doubleSided;
    private final int doubleSidedSign;

    private final Locale locale;
    private final LocalizationUtil localizationUtil;

    private final String themeScreen;
    private final String themePrint;

    private final boolean constellationLines;
    private final boolean constellationLabels;
    private final int constellationLabelsMode;
    private final boolean constellationBoundaries;
    private final boolean milkyWay;
    private final boolean dayLightSavingTimeScale;
    private final boolean coordsRADec;
    private final boolean ecliptic;
    private final boolean allVisibleStars;

    public Options(double latitude) {
        this(latitude, Locale.ENGLISH, Settings.THEME_PRINT_DEFAULT);
    }

    public Options(double latitude, Locale locale, String themePrint) {
        this(latitude, locale, Settings.THEME_SCREEN_DEFAULT, themePrint, 1, true, true, 0, false, false, true, true, true, false);
    }

    public Options(double latitude, Locale locale, String themeScreen, String themePrint, int side, boolean constellationLines,
            boolean constellationLabels, int constellationLabelsMode, boolean constellationBoundaries, boolean milkyWay,
            boolean dayLightSavingTimeScale, boolean coordsRADec, boolean ecliptic, boolean allVisibleStars) {

        this.latitudeFixed = latitude;
        this.doubleSided = Math.abs(latitudeFixed) < 35.0;
        this.latitudeFixedSign = (latitudeFixed >= 0) ? 1 : -1;  // Math.signum() returns 0 for zero latitude
        this.doubleSidedSign = side * latitudeFixedSign;
        this.latitude = doubleSided ? doubleSidedSign * 65.0 : latitudeFixed;
        this.latitudeSign = (this.latitude >= 0) ? 1 : -1;

        this.locale = locale;
        this.localizationUtil = new LocalizationUtil(locale);

        this.themeScreen = themeScreen;
        this.themePrint = themePrint;

        this.constellationLines = constellationLines;
        this.constellationLabels = constellationLabels;
        this.constellationLabelsMode = constellationLabelsMode;
        this.constellationBoundaries = constellationBoundaries;
        this.milkyWay = milkyWay;
        this.dayLightSavingTimeScale = dayLightSavingTimeScale;
        this.coordsRADec = coordsRADec;
        this.ecliptic = ecliptic;
        this.allVisibleStars = allVisibleStars;
    }

    public double getLatitudeFixed() {
        return latitudeFixed;
    }

    public int getLatitudeFixedSign() {
        return latitudeFixedSign;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getLatitudeSign() {
        return latitudeSign;
    }

    public boolean isDoubleSided() {
        return doubleSided;
    }

    public int getDoubleSidedSign() {
        return doubleSidedSign;
    }

    public Locale getLocale() {
        return locale;
    }

    public LocalizationUtil getLocalizationUtil() {
        return localizationUtil;
    }

    public String getThemeScreen() {
        return themeScreen;
    }

    public String getThemePrint() {
        return themePrint;
    }

    public boolean hasConstellationLines() {
        return constellationLines;
    }

    public boolean hasConstellationLabels() {
        return constellationLabels;
    }

    public int getConstellationLabelsMode() {
        return constellationLabelsMode;
    }

    public boolean hasConstellationBoundaries() {
        return constellationBoundaries;
    }

    public boolean hasMilkyWay() {
        return milkyWay;
    }

    public boolean hasDayLightSavingTimeScale() {
        return dayLightSavingTimeScale;
    }

    public boolean hasCoordsRADec() {
        return coordsRADec;
    }

    public boolean hasEcliptic() {
        return ecliptic;
    }

    public boolean hasAllVisibleStars() {
        return allVisibleStars;
    }

}
