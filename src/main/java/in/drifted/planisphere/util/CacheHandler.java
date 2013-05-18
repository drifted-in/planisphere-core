package in.drifted.planisphere.util;

import in.drifted.planisphere.resources.loader.ConstellationBoundaryListLoader;
import in.drifted.planisphere.resources.loader.ConstellationLineListLoader;
import in.drifted.planisphere.resources.loader.ConstellationNameListLoader;
import in.drifted.planisphere.resources.loader.MilkyWayLoader;
import in.drifted.planisphere.resources.loader.StarListLoader;
import in.drifted.planisphere.model.ConstellationName;
import in.drifted.planisphere.model.MilkyWay;
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
        List starList = (List) cache.get(path);
        if (starList == null) {
            starList = StarListLoader.getStarList(path);
            this.cache.put(path, starList);
        }
        return starList;
    }

    public List<ConstellationName> getConstellationNameList() throws IOException {
        String path = settings.getFilePathConstellationNames();
        List constellationNameList = (List) cache.get(path);
        if (constellationNameList == null) {
            constellationNameList = ConstellationNameListLoader.getConstellationNameList(path);
            cache.put(path, constellationNameList);
        }
        return constellationNameList;
    }

    public List<Point2D> getConstellationLineList() throws IOException {
        String path = settings.getFilePathConstellationLines();
        List constellationLineList = (List) cache.get(path);
        if (constellationLineList == null) {
            constellationLineList = ConstellationLineListLoader.getConstellationLineList(path);
            cache.put(path, constellationLineList);
        }
        return constellationLineList;
    }

    public List<Point2D> getConstellationBoundaryList() throws IOException {
        String path = settings.getFilePathConstellationBoundaries();
        List constellationBoundaryList = (List) cache.get(path);
        if (constellationBoundaryList == null) {
            constellationBoundaryList = ConstellationBoundaryListLoader.getConstellationBoundaryList(path);
            cache.put(path, constellationBoundaryList);
        }
        return constellationBoundaryList;
    }

    public MilkyWay getMilkyWay() throws IOException {
        MilkyWay milkyWay = (MilkyWay) cache.get(settings.getFilePathMilkyWayDarkNorth());
        if (milkyWay == null) {
            List filePaths = new ArrayList();
            filePaths.add(settings.getFilePathMilkyWayDarkNorth());
            filePaths.add(settings.getFilePathMilkyWayDarkSouth());
            filePaths.add(settings.getFilePathMilkyWayBrightNorth());
            filePaths.add(settings.getFilePathMilkyWayBrightSouth());
            milkyWay = MilkyWayLoader.getMilkyWay(filePaths);
            cache.put(settings.getFilePathMilkyWayDarkNorth(), milkyWay);
        }
        return milkyWay;
    }

    private synchronized void clearCache() {
        cache = new HashMap(20);
    }
}
