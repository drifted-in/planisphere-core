package in.drifted.planisphere;

import java.io.Serializable;

public class Settings implements Serializable {

    public static final String RESOURCE_BASE_PATH = "/in/drifted/planisphere/resources/";
    public static final String[] PARAM_ELEMENTS = {"dialHoursMarkerMajor", "dialHoursMarkerMinor"};
    private Double latitude;
    private Double scale = 768d;
    private String filePathStars;
    private String filePathConstellationNames;
    private String filePathConstellationLines;
    private String filePathConstellationBoundaries;
    private String filePathMilkyWayDarkNorth;
    private String filePathMilkyWayDarkSouth;
    private String filePathMilkyWayBrightNorth;
    private String filePathMilkyWayBrightSouth;    

    public Settings() {
        String resourceDataPath = RESOURCE_BASE_PATH + "data/";
        filePathStars = resourceDataPath + "stars.txt";
        filePathConstellationNames = resourceDataPath + "constellationNames.txt";
        filePathConstellationLines = resourceDataPath + "constellationLines.txt";
        filePathConstellationBoundaries = resourceDataPath + "constellationBoundaries.txt";
        filePathMilkyWayDarkNorth = resourceDataPath + "milkyWayDarkNorth.txt";
        filePathMilkyWayDarkSouth = resourceDataPath + "milkyWayDarkSouth.txt";
        filePathMilkyWayBrightNorth = resourceDataPath + "milkyWayBrightNorth.txt";
        filePathMilkyWayBrightSouth = resourceDataPath + "milkyWayBrightSouth.txt";
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getScale() {
        return scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    public String getFilePathStars() {
        return filePathStars;
    }

    public void setFilePathStars(String filePathStars) {
        this.filePathStars = filePathStars;
    }

    public String getFilePathConstellationNames() {
        return filePathConstellationNames;
    }

    public void setFilePathConstellationNames(String filePathConstellationNames) {
        this.filePathConstellationNames = filePathConstellationNames;
    }

    public String getFilePathConstellationLines() {
        return filePathConstellationLines;
    }

    public void setFilePathConstellationLines(String filePathConstellationLines) {
        this.filePathConstellationLines = filePathConstellationLines;
    }

    public String getFilePathConstellationBoundaries() {
        return filePathConstellationBoundaries;
    }

    public void setFilePathConstellationBoundaries(String filePathConstellationBoundaries) {
        this.filePathConstellationBoundaries = filePathConstellationBoundaries;
    }

    public String getFilePathMilkyWayDarkNorth() {
        return filePathMilkyWayDarkNorth;
    }

    public void setFilePathMilkyWayDarkNorth(String filePathMilkyWayDarkNorth) {
        this.filePathMilkyWayDarkNorth = filePathMilkyWayDarkNorth;
    }

    public String getFilePathMilkyWayDarkSouth() {
        return filePathMilkyWayDarkSouth;
    }

    public void setFilePathMilkyWayDarkSouth(String filePathMilkyWayDarkSouth) {
        this.filePathMilkyWayDarkSouth = filePathMilkyWayDarkSouth;
    }

    public String getFilePathMilkyWayBrightNorth() {
        return filePathMilkyWayBrightNorth;
    }

    public void setFilePathMilkyWayBrightNorth(String filePathMilkyWayBrightNorth) {
        this.filePathMilkyWayBrightNorth = filePathMilkyWayBrightNorth;
    }

    public String getFilePathMilkyWayBrightSouth() {
        return filePathMilkyWayBrightSouth;
    }

    public void setFilePathMilkyWayBrightSouth(String filePathMilkyWayBrightSouth) {
        this.filePathMilkyWayBrightSouth = filePathMilkyWayBrightSouth;
    }
    
}
