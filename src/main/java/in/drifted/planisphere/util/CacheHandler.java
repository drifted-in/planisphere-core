package in.drifted.planisphere.util;

import in.drifted.planisphere.Settings;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class CacheHandler implements Serializable {

    private HashMap cache;
    private Settings settings = new Settings();

    public CacheHandler() {
        clearCache();
    }

    public ArrayList<Star> getStars() throws Exception {
        String path = settings.getFilePathStars();
        ArrayList stars = (ArrayList) this.cache.get(path);
        if (stars == null) {
            StarsDataLoader dl = new YaleStarsDataLoader(path);
            stars = dl.getStars();
            this.cache.put(path, stars);
        }
        return stars;
    }

    public ArrayList<ConstellationName> getConstellationNames() throws Exception {
        String path = settings.getFilePathConstellationNames();
        ArrayList constellationNames = (ArrayList) this.cache.get(path);
        if (constellationNames == null) {
            ConstellationNamesDataLoader dl = new ConstellationNamesDataLoader(path);
            constellationNames = dl.getConstellationNames();
            this.cache.put(path, constellationNames);
        }
        return constellationNames;
    }

    public ArrayList<Point2D> getConstellationLines() throws Exception {
        String path = settings.getFilePathConstellationLines();
        ArrayList constellationLines = (ArrayList) this.cache.get(path);
        if (constellationLines == null) {
            ConstellationLinesDataLoader dl = new ConstellationLinesDataLoader(path);
            constellationLines = dl.getConstellationLines();
            this.cache.put(path, constellationLines);
        }
        return constellationLines;
    }

    public ArrayList<Point2D> getConstellationBoundaries() throws Exception {
        String path = settings.getFilePathConstellationBoundaries();
        ArrayList constellationBoundaries = (ArrayList) this.cache.get(path);
        if (constellationBoundaries == null) {
            ConstellationBoundariesDataLoader dl = new ConstellationBoundariesDataLoader(path);
            constellationBoundaries = dl.getConstellationBoundaries();
            this.cache.put(path, constellationBoundaries);
        }
        return constellationBoundaries;
    }

    public MilkyWayDataSet getMilkyWayDataSet() throws Exception {
        MilkyWayDataSet milkyWayDataSet = (MilkyWayDataSet) this.cache.get(settings.getFilePathMilkyWayDarkNorth());
        if (milkyWayDataSet == null) {
            ArrayList filePaths = new ArrayList();
            filePaths.add(settings.getFilePathMilkyWayDarkNorth());
            filePaths.add(settings.getFilePathMilkyWayDarkSouth());
            filePaths.add(settings.getFilePathMilkyWayBrightNorth());
            filePaths.add(settings.getFilePathMilkyWayBrightSouth());
            MilkyWayDataLoader dl = new MilkyWayDataLoader(filePaths);
            milkyWayDataSet = dl.getMilkyWayDataSet();
            this.cache.put(settings.getFilePathMilkyWayDarkNorth(), milkyWayDataSet);
        }
        return milkyWayDataSet;
    }

    private synchronized void clearCache() {
        this.cache = new HashMap(20);
    }
}
