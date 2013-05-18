package in.drifted.planisphere.util;

import in.drifted.planisphere.model.Star;
import java.util.List;

public abstract interface StarsDataLoader {

    public abstract List<Star> getStarList();
}