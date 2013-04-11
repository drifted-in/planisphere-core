package in.drifted.planisphere;

import in.drifted.planisphere.renderer.pdf.ProducerPDFBox;
import in.drifted.planisphere.renderer.svg.Renderer;
import in.drifted.planisphere.util.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.pdfbox.util.PDFMergerUtility;

public class Planisphere {

    public static void main(String[] args) {
        try {
            
            CacheHandler cache = new CacheHandler();

            OutputStream output_01 = new FileOutputStream("D:/test_plan.svg");
            //ByteArrayOutputStream output_01 = new ByteArrayOutputStream();
            ByteArrayOutputStream output_02 = new ByteArrayOutputStream();

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
            
            //options.setLocaleValue("lt|LT");
            //options.setLocaleValue("uk|UA");
            options.setLocaleValue("fi|FI");

            Renderer svg = new Renderer(cache);

            svg.createFromTemplate("printDefault_01.svg", output_01, options);            
            //svg.createFromTemplate("screenBlue.svg", output_01, options);
            
            output_01.close();


            svg.createFromTemplate("printDefault_01.svg", output_02, options);
            output_02.close();
                                
            ProducerPDFBox p = new ProducerPDFBox();
            ArrayList<InputStream> inputStreamList = new ArrayList<InputStream>();
            //inputStreamList.add(new ByteArrayInputStream(output_01.toByteArray()));
            inputStreamList.add(new ByteArrayInputStream(output_02.toByteArray()));
            p.createPDF(inputStreamList, "D:\\vystup.pdf");              

            
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
