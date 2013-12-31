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
import java.util.ArrayList;
import java.util.List;

public final class CacheHandler {

    private static volatile CacheHandler instance = null;

    private List<Star> starList;
    private List<ConstellationName> constellationNameList;
    private List<Point2D> constellationLineList;
    private List<Point2D> constellationBoundaryList;
    private MilkyWay milkyWay;

    public static CacheHandler getInstance() {
        if (instance == null) {
            synchronized (CacheHandler.class) {
                if (instance == null) {
                    instance = new CacheHandler();
                }
            }
        }
        return instance;
    }

    private CacheHandler() {

        try {
            starList = StarListLoader.getStarList(Settings.FILE_PATH_STARS);
            constellationNameList = ConstellationNameListLoader.getConstellationNameList(Settings.FILE_PATH_CONSTELLATION_NAMES);
            constellationLineList = ConstellationLineListLoader.getConstellationLineList(Settings.FILE_PATH_CONSTELLATION_LINES);
            constellationBoundaryList = ConstellationBoundaryListLoader.getConstellationBoundaryList(Settings.FILE_PATH_CONSTELLATION_BOUNDARIES);

            List filePaths = new ArrayList();
            filePaths.add(Settings.FILE_PATH_MILKY_WAY_DARK_NORTH);
            filePaths.add(Settings.FILE_PATH_MILKY_WAY_DARK_SOUTH);
            filePaths.add(Settings.FILE_PATH_MILKY_WAY_BRIGHT_NORTH);
            filePaths.add(Settings.FILE_PATH_MILKY_WAY_BRIGHT_SOUTH);
            milkyWay = MilkyWayLoader.getMilkyWay(filePaths);

        } catch (IOException e) {
        }
    }

    public List<Star> getStarList() {
        return starList;
    }

    public List<ConstellationName> getConstellationNameList() {
        return constellationNameList;
    }

    public List<Point2D> getConstellationLineList() {
        return constellationLineList;
    }

    public List<Point2D> getConstellationBoundaryList() {
        return constellationBoundaryList;
    }

    public MilkyWay getMilkyWay() {
        return milkyWay;
    }

}
