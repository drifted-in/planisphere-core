package in.drifted.planisphere;

import in.drifted.planisphere.renderer.pdf.Producer;
import in.drifted.planisphere.renderer.svg.Renderer;
import in.drifted.planisphere.util.CacheHandler;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class Planisphere {

    public static void main(String[] args) {

        Options options = new Options();
        options.setLatitude(15d);
        options.setConstellationBoundaries(true);
        options.setConstellationLines(true);
        options.setConstellationLabels(true);
        options.setMilkyWay(true);
        options.setEcliptic(true);
        options.setConstellationLabelsOptions(0);
        options.setCoordsRADec(true);
        options.setDayLightSavingTimeScale(true);
        options.setLocaleValue("fi|FI");

        List<String> templateList = new LinkedList<String>();
        templateList.add("printDefault_01.svg");
        templateList.add("printDefault_02.svg");

        createPDF(templateList, "D:/vystup-multi.pdf", options);
        //createSVG("screenBlue.svg", "D:/test_plan.svg", options);

    }

    private static void createSVG(String template, String outputPath, Options options) {

        try {
            Renderer svg = new Renderer(new CacheHandler());

            OutputStream outputStream = new FileOutputStream(outputPath);
            svg.createFromTemplate(template, outputStream, options);
            outputStream.close();

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private static void createPDF(List<String> templateList, String outputPath, Options options) {

        List<InputStream> inputStreamList = new LinkedList<InputStream>();

        try {
            Renderer svg = new Renderer(new CacheHandler());

            for (String template : templateList) {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                svg.createFromTemplate(template, outputStream, options);
                inputStreamList.add(new ByteArrayInputStream(outputStream.toByteArray()));
                outputStream.close();

            }

            Producer producer = new Producer();
            producer.createPDF(inputStreamList, outputPath);

        } catch (Exception e) {

            e.printStackTrace();
        }

    }
}