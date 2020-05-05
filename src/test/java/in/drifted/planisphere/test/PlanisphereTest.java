package in.drifted.planisphere.test;

import in.drifted.planisphere.Options;
import in.drifted.planisphere.Settings;
import in.drifted.planisphere.renderer.html.HtmlRenderer;
import in.drifted.planisphere.renderer.svg.SvgRenderer;
import in.drifted.planisphere.util.SubsetUtil;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Before;

public class PlanisphereTest {

    private final Options options = new Options();

    @Before
    public void setUp() {

        options.setLatitude(50.0);
        options.setConstellationBoundaries(true);
        options.setConstellationLines(true);
        options.setConstellationLabels(true);
        options.setMilkyWay(true);
        options.setEcliptic(true);
        options.setAllVisibleStars(true);
        options.setConstellationLabelsMode(0);
        options.setCoordsRADec(true);
        options.setDayLightSavingTimeScale(true);
        options.setLocaleValue("en");
        //options.setLocaleValue("ar");
        //options.setDoubleSidedSign(1);
    }

    //@Test
    public void generateFontForgeSelectionScript() {
        System.out.println(SubsetUtil.getFontForgeSelectionScript(options.getCurrentLocale()));
    }

    //@Test
    public void generateHTML() throws Exception {
        new HtmlRenderer(new SvgRenderer()).createFromTemplate(options, Files.createTempFile("planisphere_printDefault", ".html"));
    }

    //@Test
    public void generateSVG() throws Exception {
        createSVG("printDefault_D_04", "printDefault_white", Files.createTempFile("planisphere_printDefault_01", ".svg"), options);
    }

    //@Test
    public void generateAllSVGs() throws Exception {
        for (String templateName : Settings.getTemplateNameCollection(Settings.MEDIA_SCREEN)) {
            for (String colorScheme : Settings.getColorSchemeCollection(templateName)) {
                createSVG(templateName, colorScheme, Files.createTempFile(colorScheme, ".svg"), options);
            }
        }
        for (String colorScheme : Settings.getColorSchemeCollection("printDefault")) {
            createSVG("printDefault_S_01", colorScheme, Files.createTempFile(colorScheme, "_A.svg"), options);
            createSVG("printDefault_S_02", colorScheme, Files.createTempFile(colorScheme, "_B.svg"), options);
        }
    }

    public void createSVG(String template, String colorScheme, Path outputPath, Options options) throws Exception {

        SvgRenderer svg = new SvgRenderer();

        try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
            svg.createFromTemplate(template, colorScheme, outputStream, options);
        }
    }

}
