package in.drifted.planisphere.util;

import in.drifted.planisphere.Settings;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ElementParser implements Serializable {

    public ElementParser() {
    }

    public static ArrayList<Element> getParamElements(InputStream input) throws Exception {
        ArrayList<Element> paramElements = new ArrayList<Element>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        /*
         domFactory.setNamespaceAware(Boolean.TRUE);
         domFactory.setValidating(Boolean.FALSE);
         domFactory.setFeature("http://xml.org/sax/features/validation", Boolean.FALSE);
         domFactory.setFeature("http://xml.org/sax/features/namespaces", Boolean.TRUE);
         domFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", Boolean.FALSE);
         */
        //domFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE);

        DocumentBuilder builder = domFactory.newDocumentBuilder();
        
        try {
            Document doc = builder.parse(input);

            if (doc != null) {
                System.out.println(doc.getDocumentElement().getLocalName());
                for (int i = 0; i < Settings.PARAM_ELEMENTS.length; i++) {
                    System.out.println(Settings.PARAM_ELEMENTS[i]);
                    Element element = (Element) doc.getElementById(Settings.PARAM_ELEMENTS[i]).cloneNode(true);
                    element.removeAttribute("id");
                    paramElements.add(element);
                }
            }
        } catch (DOMException e) {
            e.printStackTrace();
        }
        return paramElements;
    }
}
