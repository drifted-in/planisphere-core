package in.drifted.planisphere.util;

import java.awt.geom.Point2D;
import java.io.Serializable;

public class ConstellationName implements Serializable {

    private String id;
    private String abbreviation;
    private String latin;
    private Point2D coord;

    public ConstellationName(String abbreviation, String latin, Point2D coord) {
        setAbbreviation(abbreviation);
        setLatin(latin);
        setId(generateId(latin));
        setCoord(coord);
    }

    private String generateId(String name) {
        String[] fragments = name.split(" ");
        StringBuilder strId = new StringBuilder();
        for (int i = 0; i < fragments.length; i++) {
            String candidate = fragments[i].toLowerCase();
            if (i > 0) {
                candidate = candidate.substring(0, 1).toUpperCase() + candidate.substring(1);
            }
            strId.append(candidate);
        }
        return strId.toString();
    }

    public String getId() {
        return id;
    }

    private void setId(String id) {
        this.id = id;
    }

    public String getAbbreviation() {
        return this.abbreviation;
    }

    private void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public Point2D getCoord() {
        return this.coord;
    }

    private void setCoord(Point2D coord) {
        this.coord = coord;
    }

    public String getLatin() {
        return this.latin;
    }

    private void setLatin(String latin) {
        this.latin = latin;
    }
}
