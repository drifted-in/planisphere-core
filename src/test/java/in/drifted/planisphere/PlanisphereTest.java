package in.drifted.planisphere;

import in.drifted.planisphere.renderer.html.HtmlRenderer;
import in.drifted.planisphere.renderer.svg.SvgRenderer;
import in.drifted.planisphere.util.SubsetUtil;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class PlanisphereTest {

    private final Options options = getOptions(50);

    //@Test
    public void generateFontForgeSelectionScript() {
        System.out.println(SubsetUtil.getFontForgeSelectionScript(options.getLocale()));
    }

    //@Test
    public void generateHTML() throws Exception {
        Path path = Files.createTempFile("planisphere_printDefault", ".html");
        HtmlRenderer.createFromTemplate(options, path);
        System.out.println(path);
    }

    //@Test
    public void generateLatitudeCollectionHTML() throws Exception {
        for (double latitude = -60; latitude < 65; latitude = latitude + 30) {
            Path path = Files.createTempFile("planisphere_printDefault_" + latitude, ".html");
            HtmlRenderer.createFromTemplate(getOptions(latitude), path);
            System.out.println(path);
        }
    }

    //@Test
    public void generateSVG() throws Exception {
        Path path = Files.createTempFile("planisphere_printDefault_01", ".svg");
        createSVG("printDefault_D_04", "printDefault_light", path, options);
        System.out.println(path);
    }

    //@Test
    public void generateAllSVGs() throws Exception {
        for (String templateName : Settings.getTemplateNameCollection(Settings.MEDIA_SCREEN)) {
            for (String colorScheme : Settings.getColorSchemeCollection(templateName)) {
                createSVG(templateName, colorScheme, Files.createTempFile(colorScheme, ".svg"), options);
            }
        }
        for (String colorScheme : Settings.getColorSchemeCollection("printDefault")) {
            Path page01Path = Files.createTempFile(colorScheme, "_01.svg");
            Path page02Path = Files.createTempFile(colorScheme, "_02.svg");
            createSVG("printDefault_S_01", colorScheme, page01Path, options);
            createSVG("printDefault_S_02", colorScheme, page02Path, options);
            System.out.println(page01Path);
            System.out.println(page02Path);
        }
    }

    public void createSVG(String template, String colorScheme, Path outputPath, Options options) throws Exception {
        try ( OutputStream outputStream = Files.newOutputStream(outputPath)) {
            SvgRenderer.createFromTemplate(template, colorScheme, outputStream, options);
        }
    }

    private Options getOptions(double latitude) {
        return new Options(latitude, Locale.ENGLISH, Settings.THEME_PRINT_DEFAULT,
            Settings.THEME_PRINT_DEFAULT, 1, true, true, 0, true, true, true, true, true, true);
    }
}
