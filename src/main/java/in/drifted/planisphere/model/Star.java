package in.drifted.planisphere.model;

import java.io.Serializable;

public final class Star implements Serializable {

    private final Double RA;
    private final Double Dec;
    private final Double Mag;

    public Star(Double RA, Double Dec, Double Mag) {
        this.RA = RA;
        this.Dec = Dec;
        this.Mag = Mag;
    }

    public Double getRA() {
        return RA;
    }

    public Double getDec() {
        return Dec;
    }

    public Double getMag() {
        return Mag;
    }
}
