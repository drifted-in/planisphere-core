package in.drifted.planisphere;

import java.io.Serializable;
import java.util.Locale;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public final class Options implements Serializable {

    private String localeValue = "en";
    private Double latitude = 50.0;
    private String screenImage;
    private String printImage;
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
        this.screenImage = options.getScreenImage();
        this.printImage = options.getPrintImage();
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

    public String getScreenImage() {
        return screenImage;
    }

    @XmlAttribute
    public void setScreenImage(String screenImage) {
        this.screenImage = screenImage;
    }

    public String getPrintImage() {
        return printImage;
    }

    @XmlAttribute
    public void setPrintImage(String printImage) {
        this.printImage = printImage;
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