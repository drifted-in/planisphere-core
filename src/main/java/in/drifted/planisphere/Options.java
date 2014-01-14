package in.drifted.planisphere;

import java.io.Serializable;
import java.util.Locale;

public final class Options implements Serializable {

    private String localeValue = "en";
    private Double latitude = 30.0;
    private String screenImage;
    private String printImage;
    private Boolean constellationLines = true;
    private Boolean constellationLabels = true;
    private Integer constellationLabelsOptions = 0;
    private Boolean constellationBoundaries = false;
    private Boolean milkyWay = false;
    private Boolean dayLightSavingTimeScale = true;
    private Boolean coordsRADec = true;
    private Boolean ecliptic = true;
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
        this.screenImage = options.getScreenImage();
        this.printImage = options.getPrintImage();
        this.constellationLines = options.getConstellationLines();
        this.constellationLabels = options.getConstellationLabels();
        this.constellationLabelsOptions = options.getConstellationLabelsOptions();
        this.constellationBoundaries = options.getConstellationBoundaries();
        this.milkyWay = options.getMilkyWay();
        this.dayLightSavingTimeScale = options.getDayLightSavingTimeScale();
        this.coordsRADec = options.getCoordsRADec();
        this.ecliptic = options.getEcliptic();
        this.doubleSidedSign = options.getDoubleSidedSign();
    }

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

    public String getScreenImage() {
        return screenImage;
    }

    public void setScreenImage(String screenImage) {
        this.screenImage = screenImage;
    }

    public String getPrintImage() {
        return printImage;
    }

    public void setPrintImage(String printImage) {
        this.printImage = printImage;
    }

    public Boolean getConstellationLines() {
        return constellationLines;
    }

    public void setConstellationLines(Boolean constellationLines) {
        this.constellationLines = constellationLines;
    }

    public Boolean getConstellationLabels() {
        return constellationLabels;
    }

    public void setConstellationLabels(Boolean constellationLabels) {
        this.constellationLabels = constellationLabels;
    }

    public Integer getConstellationLabelsOptions() {
        return constellationLabelsOptions;
    }

    public void setConstellationLabelsOptions(Integer constellationLabelsOptions) {
        this.constellationLabelsOptions = constellationLabelsOptions;
    }

    public Boolean getConstellationBoundaries() {
        return constellationBoundaries;
    }

    public void setConstellationBoundaries(Boolean constellationBoundaries) {
        this.constellationBoundaries = constellationBoundaries;
    }

    public Boolean getMilkyWay() {
        return milkyWay;
    }

    public void setMilkyWay(Boolean milkyWay) {
        this.milkyWay = milkyWay;
    }

    public Boolean getDayLightSavingTimeScale() {
        return dayLightSavingTimeScale;
    }

    public void setDayLightSavingTimeScale(Boolean dayLightSavingTimeScale) {
        this.dayLightSavingTimeScale = dayLightSavingTimeScale;
    }

    public Boolean getCoordsRADec() {
        return coordsRADec;
    }

    public void setCoordsRADec(Boolean coordsRADec) {
        this.coordsRADec = coordsRADec;
    }

    public Boolean getEcliptic() {
        return ecliptic;
    }

    public void setEcliptic(Boolean ecliptic) {
        this.ecliptic = ecliptic;
    }

    public Boolean getDoubleSided() {
        return Math.abs(latitude) < 35.0;
    }

    public Integer getDoubleSidedSign() {
        return doubleSidedSign;
    }

    public void setDoubleSidedSign(Integer doubleSidedSign) {
        this.doubleSidedSign = doubleSidedSign;
    }
}
