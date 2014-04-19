package in.drifted.planisphere.test;

import in.drifted.planisphere.Options;
import in.drifted.planisphere.renderer.svg.SvgRenderer;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.junit.Assert;
import org.junit.Test;

public class CardinalPointLabelTest {

    private final SvgRenderer svg = new SvgRenderer();
    private final Options options = new Options();

    @Test
    public void N30P1() throws Exception {
        test(30.0, 1, "E|NE|N|NW|W|null|null|null|");
    }

    @Test
    public void N30M1() throws Exception {
        test(30.0, -1, "W|SW|S|SE|E|null|null|null|");
    }

    @Test
    public void S30P1() throws Exception {
        test(-30.0, 1, "W|SW|S|SE|E|null|null|null|");
    }

    @Test
    public void S30M1() throws Exception {
        test(-30.0, -1, "E|NE|N|NW|W|null|null|null|");
    }

    @Test
    public void N50() throws Exception {
        test(50.0, 1, "W|SW|S|SE|E|NE|N|NW|");
    }

    @Test
    public void S50() throws Exception {
        test(-50.0, 1, "E|NE|N|NW|W|SW|S|SE|");
    }

    private void test(Double latitude, Integer doubleSidedSign, String expected) throws Exception {

        options.setLatitude(latitude);
        options.setDoubleSidedSign(doubleSidedSign);

        try (OutputStream outputStream = new FileOutputStream("D:/planisphere.svg")) {
            svg.createFromTemplate("screenBlue.svg", outputStream, options);
            String result = svg.getDelimitedCardinalPointLabelList();
            //System.out.println(result);
            Assert.assertEquals(expected, result);
            outputStream.close();
        }
    }
}
