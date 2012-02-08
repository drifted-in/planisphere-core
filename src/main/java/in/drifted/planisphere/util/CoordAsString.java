package in.drifted.planisphere.util;

import java.io.Serializable;

public class CoordAsString implements Serializable {

    private String x = "";
    private String y = "";

    public CoordAsString() {
    }

    public CoordAsString(String x, String y) {
        this.x = x;
        this.y = y;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

}
