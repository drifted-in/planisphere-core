package in.drifted.planisphere.test;

import in.drifted.planisphere.Options;
import in.drifted.planisphere.renderer.html.HtmlRenderer;
import in.drifted.planisphere.renderer.svg.SvgRenderer;
import in.drifted.util.pdf.MultiPageSvgToPdfTranscoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class PlanisphereTest {

    private final Options options = new Options();

    @Before
    public void setUp() {

        options.setLatitude(30.0);
        options.setConstellationBoundaries(true);
        options.setConstellationLines(true);
        options.setConstellationLabels(true);
        options.setMilkyWay(true);
        options.setEcliptic(true);
        options.setConstellationLabelsOptions(0);
        options.setCoordsRADec(true);
        options.setDayLightSavingTimeScale(true);
        options.setLocaleValue("cs|CZ");
    }

    @Test
    public void generateSVG() throws Exception {

        createSVG("screenBlue.svg", "D:/planisphere_screenBlue.svg", options);
        //createSVG("printDefault_01.svg", "D:/planisphere_printDefault_01.svg", options);
        //createSVG("printDefault_02.svg", "D:/planisphere_printDefault_02.svg", options);
    }

    //@Test
    public void generateHTML() throws Exception {

        List<String> templateList = new LinkedList<>();
        templateList.add("printDefault_01.svg");
        //templateList.add("printDefault_02.svg");

        //createHTML(templateList, "D:/planisphere_printDefault.html", options);
    }

    //@Test
    public void generatePDF() throws Exception {

        List<String> templateList = new LinkedList<>();
        templateList.add("printDefault_01.svg");
        templateList.add("printDefault_02.svg");

        createPDF(templateList, "D:/planisphere_printDefault.pdf", options);
    }

    private void createSVG(String template, String outputPath, Options options) throws Exception {

        SvgRenderer svg = new SvgRenderer();
        try (OutputStream outputStream = new FileOutputStream(outputPath)) {
            svg.createFromTemplate(template, outputStream, options);
        }
    }

    private void createHTML(List<String> templateList, String outputPath, Options options) throws Exception {

        SvgRenderer svg = new SvgRenderer();
        HtmlRenderer html = new HtmlRenderer(svg);
        html.createFromTemplateList(templateList, outputPath, options);
    }

    private void createPDF(List<String> templateList, String outputPath, Options options) throws Exception {

        List<InputStream> inputStreamList = new LinkedList<>();

        SvgRenderer svg = new SvgRenderer();

        for (String template : templateList) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                svg.createFromTemplate(template, outputStream, options);
                inputStreamList.add(new ByteArrayInputStream(outputStream.toByteArray()));
            }
        }

        MultiPageSvgToPdfTranscoder transcoder = new MultiPageSvgToPdfTranscoder();
        transcoder.transcode(inputStreamList, outputPath);
    }
}
