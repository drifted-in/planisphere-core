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

import java.io.Serializable;
import java.util.Locale;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public final class Options implements Serializable {

    private String localeValue = "en";
    private Double latitude = 50.0;
    private String screenTheme;
    private String printTheme;
    private Boolean constellationLines = true;
    private Boolean constellationLabels = true;
    private Integer constellationLabelsMode = 0;
    private Boolean constellationBoundaries = false;
    private Boolean milkyWay = false;
    private Boolean dayLightSavingTimeScale = true;
    private Boolean coordsRADec = true;
    private Boolean ecliptic = true;
    private Boolean allVisibleStars = false;
    // helper param
    private Integer doubleSidedSign = 1;
    // calculated values, getters only
    /*
     private Locale currentLocale;
     private Boolean doubleSided;
     */

    public Options() {
    }

    public Options(Options options) {
        this.localeValue = options.getLocaleValue();
        this.latitude = options.getLatitude();
        this.screenTheme = options.getScreenTheme();
        this.printTheme = options.getPrintTheme();
        this.constellationLines = options.getConstellationLines();
        this.constellationLabels = options.getConstellationLabels();
        this.constellationLabelsMode = options.getConstellationLabelsMode();
        this.constellationBoundaries = options.getConstellationBoundaries();
        this.milkyWay = options.getMilkyWay();
        this.dayLightSavingTimeScale = options.getDayLightSavingTimeScale();
        this.coordsRADec = options.getCoordsRADec();
        this.ecliptic = options.getEcliptic();
        this.allVisibleStars = options.getAllVisibleStars();
        this.doubleSidedSign = options.getDoubleSidedSign();
    }

    public String getLocaleValue() {
        return localeValue;
    }

    @XmlAttribute
    public void setLocaleValue(String localeValue) {
        this.localeValue = localeValue;
    }

    public Locale getCurrentLocale() {
        String[] localeFragments = localeValue.split("\\|");
        Locale locale;
        if (localeFragments.length > 1) {
            locale = new Locale(localeFragments[0], localeFragments[1]);
        } else {
            locale = new Locale(localeFragments[0]);
        }
        return locale;
    }

    public Double getLatitude() {
        return latitude;
    }

    @XmlAttribute
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getScreenTheme() {
        return screenTheme;
    }

    @XmlAttribute
    public void setScreenTheme(String screenTheme) {
        this.screenTheme = screenTheme;
    }

    public String getPrintTheme() {
        return printTheme;
    }

    @XmlAttribute
    public void setPrintTheme(String printTheme) {
        this.printTheme = printTheme;
    }

    public Boolean getConstellationLines() {
        return constellationLines;
    }

    @XmlAttribute
    public void setConstellationLines(Boolean constellationLines) {
        this.constellationLines = constellationLines;
    }

    public Boolean getConstellationLabels() {
        return constellationLabels;
    }

    @XmlAttribute
    public void setConstellationLabels(Boolean constellationLabels) {
        this.constellationLabels = constellationLabels;
    }

    public Integer getConstellationLabelsMode() {
        return constellationLabelsMode;
    }

    @XmlAttribute
    public void setConstellationLabelsMode(Integer constellationLabelsMode) {
        this.constellationLabelsMode = constellationLabelsMode;
    }

    public Boolean getConstellationBoundaries() {
        return constellationBoundaries;
    }

    @XmlAttribute
    public void setConstellationBoundaries(Boolean constellationBoundaries) {
        this.constellationBoundaries = constellationBoundaries;
    }

    public Boolean getMilkyWay() {
        return milkyWay;
    }

    @XmlAttribute
    public void setMilkyWay(Boolean milkyWay) {
        this.milkyWay = milkyWay;
    }

    public Boolean getDayLightSavingTimeScale() {
        return dayLightSavingTimeScale;
    }

    @XmlAttribute
    public void setDayLightSavingTimeScale(Boolean dayLightSavingTimeScale) {
        this.dayLightSavingTimeScale = dayLightSavingTimeScale;
    }

    public Boolean getCoordsRADec() {
        return coordsRADec;
    }

    @XmlAttribute
    public void setCoordsRADec(Boolean coordsRADec) {
        this.coordsRADec = coordsRADec;
    }

    public Boolean getEcliptic() {
        return ecliptic;
    }

    @XmlAttribute
    public void setEcliptic(Boolean ecliptic) {
        this.ecliptic = ecliptic;
    }

    public Boolean getAllVisibleStars() {
        return allVisibleStars;
    }

    @XmlAttribute
    public void setAllVisibleStars(Boolean allVisibleStars) {
        this.allVisibleStars = allVisibleStars;
    }

    public Boolean getDoubleSided() {
        return Math.abs(latitude) < 35.0;
    }

    public Integer getDoubleSidedSign() {
        return doubleSidedSign;
    }

    @XmlAttribute
    public void setDoubleSidedSign(Integer doubleSidedSign) {
        this.doubleSidedSign = doubleSidedSign;
    }
}
