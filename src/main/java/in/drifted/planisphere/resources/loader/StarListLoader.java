package in.drifted.planisphere.resources.loader;

import in.drifted.planisphere.model.Star;
import java.util.List;

public abstract interface StarListLoader {

    public abstract List<Star> getStarList();
}