package in.drifted.planisphere.test;

import in.drifted.planisphere.Options;
import in.drifted.planisphere.renderer.svg.Renderer;
import in.drifted.planisphere.util.CacheHandler;
import in.drifted.util.pdf.MultiPageSvgToPdfTranscoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

public class PlanisphereTest {

    @Test
    public void generate() throws Exception {

        Options options = new Options();
        options.setLatitude(80d);
        options.setConstellationBoundaries(true);
        options.setConstellationLines(true);
        options.setConstellationLabels(true);
        options.setMilkyWay(true);
        options.setEcliptic(true);
        options.setConstellationLabelsOptions(0);
        options.setCoordsRADec(true);
        options.setDayLightSavingTimeScale(true);
        options.setLocaleValue("it|IT");

        List<String> templateList = new LinkedList<>();
        templateList.add("printDefault_01.svg");
        templateList.add("printDefault_02.svg");
        //templateList.add("screenWaves.svg");

        createPDF(templateList, "D:/vystup-multi.pdf", options);
        //createSVG("screenBlue.svg", "D:/test_plan.svg", options);
        //createSVG("printDefault_02.svg", "D:/test_plan.svg", options);
/*
         try {
         List<InputStream> inputStreamList = new ArrayList<InputStream>();
         inputStreamList.add(new FileInputStream("D:/gradient.svg"));
         Producer producer = new Producer();
         producer.createPDF(inputStreamList, "D:/gradient.pdf");
         } catch (Exception e) {
         }
         */
    }

    private void createSVG(String template, String outputPath, Options options) throws Exception {

        Renderer svg = new Renderer(new CacheHandler());

        OutputStream outputStream = new FileOutputStream(outputPath);
        svg.createFromTemplate(template, outputStream, options);
        outputStream.close();
    }

    private void createPDF(List<String> templateList, String outputPath, Options options) throws Exception {

        List<InputStream> inputStreamList = new LinkedList<>();

        Renderer svg = new Renderer(new CacheHandler());

        for (String template : templateList) {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            svg.createFromTemplate(template, outputStream, options);
            inputStreamList.add(new ByteArrayInputStream(outputStream.toByteArray()));
            outputStream.close();

        }

        MultiPageSvgToPdfTranscoder transcoder = new MultiPageSvgToPdfTranscoder();
        transcoder.transcode(inputStreamList, outputPath);
    }
}