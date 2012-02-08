package in.drifted.planisphere;

import java.io.Serializable;

public class Settings implements Serializable {

    public static Double latitude;
    public static Double scale = 768d;

    /*
    public static Double width = 1024.0;
    public static Double height = 768.0;
    public static Double x = -1024d;
    public static Double y = -768d;  
    public static Integer scaleRatio = 1;
    public static Double shiftX = 0d;
    public static Double shiftY = 0d;
     */
            
    public static final String resourceBasePath =  "/in/drifted/planisphere/resources/";
    private static final String resourceDataPath = resourceBasePath + "data/";
    public static String filePathStars = resourceDataPath + "stars.txt";
    public static String filePathConstellationNames = resourceDataPath + "constellationNames.txt";
    public static String filePathConstellationLines = resourceDataPath + "constellationLines.txt";
    public static String filePathConstellationBoundaries = resourceDataPath + "constellationBoundaries.txt";
    public static String filePathMilkyWayDarkNorth = resourceDataPath + "milkyWayDarkNorth.txt";
    public static String filePathMilkyWayDarkSouth = resourceDataPath + "milkyWayDarkSouth.txt";
    public static String filePathMilkyWayBrightNorth = resourceDataPath + "milkyWayBrightNorth.txt";
    public static String filePathMilkyWayBrightSouth = resourceDataPath + "milkyWayBrightSouth.txt";
    public static String[] paramElements = {"dialHoursMarkerMajor", "dialHoursMarkerMinor"};
}
