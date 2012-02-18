package in.drifted.planisphere.renderer.pdf;

import in.drifted.planisphere.Settings;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.MimeConstants;

public class Producer {

    FopFactory fopFactory = FopFactory.newInstance();
    Transformer transformer = TransformerFactory.newInstance().newTransformer();

    public Producer() throws Exception {
        fopFactory.setUserConfig(getFopConfig());
    }

    private Configuration getFopConfig() throws Exception {
        DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
        return cfgBuilder.build(this.getClass().getResourceAsStream(Settings.resourceBasePath + "config/fop.xml"));
    }

    public void createPDF(ArrayList<File> inputFileList, File output) throws Exception {

        File foFile = createFO(inputFileList);
        
        OutputStream out = new BufferedOutputStream(new FileOutputStream(output));
        
        Fop fop = null;
        Source foSource = null;
        Result pdfResult = null;
        
        try {
            fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
            foSource = new StreamSource(foFile);
            pdfResult = new SAXResult(fop.getDefaultHandler());
            transform(foSource, pdfResult);
            foFile.delete();
        } finally {
            out.close();
            fop = null;            
        }
    }

    private void transform(Source src, Result res) throws Exception {
        transformer.transform(src, res);
    }

    private File createFO(ArrayList<File> inputFileList) throws Exception {
        File output = File.createTempFile("planisphere", ".fo");
        //System.out.println(output.getAbsolutePath());
        StringBuilder foFile = new StringBuilder();
        foFile.append("<?xml version='1.0' encoding='utf-8'?>");
        foFile.append("<fo:root xmlns:fo='http://www.w3.org/1999/XSL/Format'>");
        foFile.append("<fo:layout-master-set>");
        foFile.append("<fo:simple-page-master master-name='A4' page-width='210mm' page-height='297mm' margin='0'>");
        foFile.append("<fo:region-body region-name='xsl-region-body' margin='0' padding='0'/>");
        foFile.append("</fo:simple-page-master>");
        foFile.append("</fo:layout-master-set>");
        foFile.append("<fo:page-sequence master-reference='A4'>");
        foFile.append("<fo:flow flow-name='xsl-region-body'>");

        for (File file:inputFileList) {
            foFile.append("<fo:block>");
            //foFile.append("nazdar");
            //foFile.append("<fo:block>");
            
            foFile.append("<fo:external-graphic src='url(");
            foFile.append(file.toURI().toString());
            //System.out.println(file.toURI().toString());
            foFile.append(")'/>");
            
            foFile.append("</fo:block>");
            
            //foFile.append("Nazdar světe</fo:block>");
        }
        foFile.append("</fo:flow>");
        foFile.append("</fo:page-sequence>");
        foFile.append("</fo:root>");

        FileWriter writer = new FileWriter(output);
        writer.append(foFile);
        writer.close();

        return output;
    }
}
