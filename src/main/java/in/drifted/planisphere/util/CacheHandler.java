package in.drifted.planisphere.util;

import in.drifted.planisphere.model.ConstellationName;
import in.drifted.planisphere.model.MilkyWayDataSet;
import in.drifted.planisphere.model.Star;
import in.drifted.planisphere.Settings;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CacheHandler implements Serializable {

    private HashMap cache;
    private Settings settings = new Settings();

    public CacheHandler() {
        clearCache();
    }

    public List<Star> getStarList() throws IOException {
        String path = settings.getFilePathStars();
        List starList = (List) this.cache.get(path);
        if (starList == null) {
            StarsDataLoader dl = new YaleStarsDataLoader(path);
            starList = dl.getStarList();
            this.cache.put(path, starList);
        }
        return starList;
    }

    public List<ConstellationName> getConstellationNameList() throws IOException {
        String path = settings.getFilePathConstellationNames();
        List constellationNameList = (List) this.cache.get(path);
        if (constellationNameList == null) {
            ConstellationNamesDataLoader dl = new ConstellationNamesDataLoader(path);
            constellationNameList = dl.getConstellationNameList();
            this.cache.put(path, constellationNameList);
        }
        return constellationNameList;
    }

    public List<Point2D> getConstellationLineList() throws IOException {
        String path = settings.getFilePathConstellationLines();
        List constellationLineList = (List) this.cache.get(path);
        if (constellationLineList == null) {
            ConstellationLinesDataLoader dl = new ConstellationLinesDataLoader(path);
            constellationLineList = dl.getConstellationLineList();
            this.cache.put(path, constellationLineList);
        }
        return constellationLineList;
    }

    public List<Point2D> getConstellationBoundaryList() throws IOException {
        String path = settings.getFilePathConstellationBoundaries();
        List constellationBoundaryList = (List) this.cache.get(path);
        if (constellationBoundaryList == null) {
            ConstellationBoundariesDataLoader dl = new ConstellationBoundariesDataLoader(path);
            constellationBoundaryList = dl.getConstellationBoundaryList();
            this.cache.put(path, constellationBoundaryList);
        }
        return constellationBoundaryList;
    }

    public MilkyWayDataSet getMilkyWayDataSet() throws IOException {
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
        cache = new HashMap(20);
    }
}
