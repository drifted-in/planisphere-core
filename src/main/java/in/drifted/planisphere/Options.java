package in.drifted.planisphere;

import java.io.Serializable;
import java.util.Locale;

public class Options implements Serializable {

    private String localeValue = "en";
    private Locale currentLocale;
    private Double latitude = 55d;
    private String screenImage;
    private String printImage;
    private boolean constellationLines;
    private boolean constellationLabels;
    private Integer constellationLabelsOptions;
    private boolean constellationBoundaries;
    private boolean milkyWay;
    private boolean dayLightSavingTimeScale;
    private boolean coordsRADec;
    private boolean ecliptic;

    public String getLocaleValue() {
        return localeValue;
    }

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

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public boolean isConstellationBoundaries() {
        return constellationBoundaries;
    }

    public void setConstellationBoundaries(boolean constellationBoundaries) {
        this.constellationBoundaries = constellationBoundaries;
    }

    public boolean isConstellationLines() {
        return constellationLines;
    }

    public void setConstellationLines(boolean constellationLines) {
        this.constellationLines = constellationLines;
    }

    public boolean isConstellationLabels() {
        return constellationLabels;
    }

    public void setConstellationLabels(boolean constellationLabels) {
        this.constellationLabels = constellationLabels;
    }

    public Integer getConstellationLabelsOptions() {
        return constellationLabelsOptions;
    }

    public void setConstellationLabelsOptions(Integer constellationLabelsOptions) {
        this.constellationLabelsOptions = constellationLabelsOptions;
    }

    public boolean isCoordsRADec() {
        return coordsRADec;
    }

    public void setCoordsRADec(boolean coordsRADec) {
        this.coordsRADec = coordsRADec;
    }

    public boolean isDayLightSavingTimeScale() {
        return dayLightSavingTimeScale;
    }

    public void setDayLightSavingTimeScale(boolean dayLightSavingTimeScale) {
        this.dayLightSavingTimeScale = dayLightSavingTimeScale;
    }

    public boolean isEcliptic() {
        return ecliptic;
    }

    public void setEcliptic(boolean ecliptic) {
        this.ecliptic = ecliptic;
    }

    public boolean isMilkyWay() {
        return milkyWay;
    }

    public void setMilkyWay(boolean milkyWay) {
        this.milkyWay = milkyWay;
    }

    public String getPrintImage() {
        return printImage;
    }

    public void setPrintImage(String printImage) {
        this.printImage = printImage;
    }

    public String getScreenImage() {
        return screenImage;
    }

    public void setScreenImage(String screenImage) {
        this.screenImage = screenImage;
    }

}