package in.drifted.planisphere.renderer.svg;

import in.drifted.planisphere.Options;
import in.drifted.planisphere.model.CardinalPoint;
import in.drifted.planisphere.model.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;

public class CardinalPointLabelTest {

    @Test
    public void ZeroP1() throws Exception {
        test(0.0, 1, "E|NE|N|NW|W|null|null|null");
    }

    @Test
    public void ZeroM1() throws Exception {
        test(0.0, -1, "W|SW|S|SE|E|null|null|null");
    }

    @Test
    public void N30P1() throws Exception {
        test(30.0, 1, "E|NE|N|NW|W|null|null|null");
    }

    @Test
    public void N30M1() throws Exception {
        test(30.0, -1, "W|SW|S|SE|E|null|null|null");
    }

    @Test
    public void S30P1() throws Exception {
        test(-30.0, 1, "W|SW|S|SE|E|null|null|null");
    }

    @Test
    public void S30M1() throws Exception {
        test(-30.0, -1, "E|NE|N|NW|W|null|null|null");
    }

    @Test
    public void N50() throws Exception {
        test(50.0, 1, "W|SW|S|SE|E|NE|N|NW");
    }

    @Test
    public void S50() throws Exception {
        test(-50.0, 1, "E|NE|N|NW|W|SW|S|SE");
    }

    private void test(double latitude, int side, String expected) throws Exception {

        Options options = new Options(latitude, Locale.ENGLISH, null, null, side, false, false, 0, false,
                false, false, false, false, false);

        double scale = 100;

        List<Point> mapAreaPointList = SvgRenderer.getMapAreaPointList(options, scale);
        List<CardinalPoint> cardinalPointList = SvgRenderer.getCardinalPointList(options, scale, mapAreaPointList);

        String result = getDelimitedLabelList(cardinalPointList);
        Assert.assertEquals(expected, result);
    }

    private static String getDelimitedLabelList(List<CardinalPoint> cardinalPointList) {

        List<String> labelList = new ArrayList<>();
        for (CardinalPoint cardinalPoint : cardinalPointList) {
            labelList.add((cardinalPoint != null) ? cardinalPoint.getLabel() : "null");
        }

        return String.join("|", labelList);
    }
}
