package in.drifted.planisphere.util;

import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import in.drifted.planisphere.Settings;
import java.io.Serializable;

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

            if (doc == null) {
                System.out.println(doc.getNamespaceURI());
            }
            System.out.println(doc.getDocumentElement().getLocalName());
            for (int i = 0; i < Settings.paramElements.length; i++) {
                System.out.println(Settings.paramElements[i]);
                Element element = (Element) doc.getElementById(Settings.paramElements[i]).cloneNode(true);
                element.removeAttribute("id");
                paramElements.add(element);
            }
        } catch (DOMException e) {
            e.printStackTrace();
        }
        return paramElements;
    }
}
