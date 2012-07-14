package in.drifted.planisphere;

import in.drifted.planisphere.renderer.svg.Renderer;
import in.drifted.planisphere.util.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/*
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
 */
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
//import org.apache.xmlgraphics.util.ClasspathResource;
import org.apache.pdfbox.util.PDFMergerUtility;

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

            OutputStream output_01 = new FileOutputStream("D:\\page_01.svg");
            OutputStream output_02 = new FileOutputStream("D:\\page_02.svg");

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
            //options.setLocaleValue("es|ES");
            options.setLocaleValue("cz|CS");

            Renderer svg = new Renderer(cache);

            svg.createFromTemplate("printInverse_01.svg", output_01, options);
            output_01.close();
/*
            svg.createFromTemplate("printDefault_02.svg", output_02, options);
            output_02.close();
  */          
            ByteArrayInputStream svg1 = new ByteArrayInputStream(svg.createFromTemplate("printInverse_01.svg", options));
            ByteArrayInputStream svg2 = new ByteArrayInputStream(svg.createFromTemplate("printDefault_02.svg", options));
            merge(new ByteArrayInputStream(getPDFByteArray(svg1)), new ByteArrayInputStream(getPDFByteArray(svg2)));

            
            //svg.createFromTemplate("screenBlue.svg");
            
            /*
            Producer p = new Producer();
            ArrayList<File> inputFileList = new ArrayList<File>();
            inputFileList.add(new File("D:/page_01.svg"));
            inputFileList.add(new File("D:/page_02.svg"));
            p.createPDF(inputFileList, new File("D:\\vystup.pdf"));
              
             */
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] getPDFByteArray(InputStream svgStream) throws Exception {

        Transcoder t = new PDFTranscoder();
        
        
        TranscoderInput input = new TranscoderInput(svgStream);
        
        //OutputStream outputStream = new FileOutputStream("D:\\temp.pdf");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2*1024*1024);
        //BufferedOutputStream outputBuff = new BufferedOutputStream(outputStream);
        //Buffer the OutputStream for better performance
        TranscoderOutput output = new TranscoderOutput(outputStream);
        //t.transcode(input, output);
        
        transcode(t, input, output);

        return outputStream.toByteArray();
    }        
    
    private static void transcode(Transcoder t, TranscoderInput input, TranscoderOutput output) throws Exception {
        t.transcode(input, output);
    }

    private static void merge(InputStream is1, InputStream is2) throws Exception {
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName("D:\\vystup.pdf");
        merger.addSource(is1);
        merger.addSource(is2);
        merger.mergeDocuments();
    }
}
