package in.drifted.planisphere.util;

import java.awt.Color;
import java.io.Serializable;

public class Star implements Serializable {

    private Double RA;
    private Double Dec;
    private Float Mag;
    private Color Color;

    public Color getColor() {
        return this.Color;
    }

    public void setColor(Color Color) {
        this.Color = Color;
    }

    public Double getDec() {
        return this.Dec;
    }

    public void setDec(Double Dec) {
        this.Dec = Dec;
    }

    public Float getMag() {
        return this.Mag;
    }

    public void setMag(Float Mag) {
        this.Mag = Mag;
    }

    public Double getRA() {
        return this.RA;
    }

    public void setRA(Double RA) {
        this.RA = RA;
    }
}