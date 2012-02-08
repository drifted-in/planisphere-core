package in.drifted.planisphere;

import in.drifted.planisphere.renderer.pdf.Producer;
import in.drifted.planisphere.renderer.svg.Renderer;
import in.drifted.planisphere.util.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
//import org.apache.xmlgraphics.util.ClasspathResource;

public class Planisphere {

    public static void main(String[] args) {
        try {

/*
    Test a = new Test();
    a.test1();
    System.exit(0);
 */

//InputStream resourceStream = ;

            /*
            String[] FONT_MIMETYPES = {
                "application/x-font", "application/x-font-truetype"
            };
             *
             */
            // classpath font finding
            //List<EmbedFontInfo> fontInfoList;
            /*
            ClasspathResource resource = ClasspathResource.getInstance();
            for (int i = 0; i < FONT_MIMETYPES.length; i++) {
                List list = resource.listResourcesOfMimeType(FONT_MIMETYPES[i]);
                System.out.println(list.size());
            }
            System.exit(0);
            

            BezierCircle circle = new BezierCircle(new Point2D.Double(250,250),100);
            System.out.println(circle.render());

            //System.exit(0);
             */

            CacheHandler cache = new CacheHandler();
            //InputStream input = new FileInputStream("D:\\om_skin_print.svg");

            OutputStream output = new FileOutputStream("D:\\data1.svg");

            Options options = new Options();
            options.setLatitude(55d);
            options.setConstellationBoundaries(true);
            options.setConstellationLines(true);
            options.setConstellationLabels(true);
            options.setMilkyWay(true);
            options.setEcliptic(true);
            options.setConstellationLabelsOptions(0);
            options.setCoordsRADec(true);
            options.setDayLightSavingTimeScale(true);
            options.setLocaleValue("es|ES");

            
            Renderer svg = new Renderer(cache, output, options);
            svg.createFromTemplate("printDefault_02.svg");
            //svg.createFromTemplate("screenBlue.svg");
            output.close();

            
            Producer p = new Producer();
            ArrayList<File> inputFileList = new ArrayList<File>();
            inputFileList.add(new File("D:\\data1.svg"));
            p.createPDF(inputFileList, new File("D:\\vystup.pdf"));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
