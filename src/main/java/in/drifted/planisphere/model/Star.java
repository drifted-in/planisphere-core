package in.drifted.planisphere.model;

import java.io.Serializable;

public final class Star implements Serializable {

    private Double RA;
    private Double Dec;
    private Double Mag;

    public Double getDec() {
        return this.Dec;
    }

    public void setDec(Double Dec) {
        this.Dec = Dec;
    }

    public Double getMag() {
        return Mag;
    }

    public void setMag(Double Mag) {
        this.Mag = Mag;
    }

    public Double getRA() {
        return this.RA;
    }

    public void setRA(Double RA) {
        this.RA = RA;
    }
}
