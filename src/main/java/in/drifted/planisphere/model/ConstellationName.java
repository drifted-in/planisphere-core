package in.drifted.planisphere.model;

public final class ConstellationName {

    private final String id;
    private final String abbreviation;
    private final String latin;
    private final Coord coord;

    public ConstellationName(String abbreviation, String latin, Coord coord) {
        this.abbreviation = abbreviation;
        this.latin = latin;
        this.id = generateId(latin);
        this.coord = coord;
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

    public String getAbbreviation() {
        return this.abbreviation;
    }

    public Coord getCoord() {
        return this.coord;
    }

    public String getLatin() {
        return this.latin;
    }
}
