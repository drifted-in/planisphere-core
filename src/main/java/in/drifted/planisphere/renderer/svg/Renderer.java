package in.drifted.planisphere.renderer.svg;

import com.ctc.wstx.api.ReaderConfig;
import com.ctc.wstx.dom.WstxDOMWrappingReader;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamReader;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.XMLEvent;
import java.awt.geom.Point2D;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

// polygon clipper
import com.seisw.util.geom.Poly;
import com.seisw.util.geom.PolyDefault;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.codehaus.stax2.ri.dom.DOMWrappingReader;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import in.drifted.planisphere.Options;
import in.drifted.planisphere.Settings;
import in.drifted.planisphere.util.*;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormatSymbols;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.events.ProcessingInstruction;

public final class Renderer {

    private CacheHandler cache;
    private XMLStreamWriter writer;
    private XMLInputFactory inputFactory;
    private XMLOutputFactory outputFactory;
    private DocumentBuilder docBuilder;
    // the following value should be calculated during import
    private Integer magnitudeRange = 8;
    private ArrayList<Point2D.Double> mapArea;
    private ArrayList<CardinalPointInfo> cardinalPoints;
    private Options options;
    private Localization localization;
    private FontManager fontManager;

    @Deprecated
    public Renderer(CacheHandler cache, OutputStream output, Options options) throws Exception {
        this.cache = cache;        
        inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        outputFactory = XMLOutputFactory.newInstance();
        writer = outputFactory.createXMLStreamWriter(output);
        docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        this.options = options;
        Settings.latitude = options.getLatitude();
        localization = new Localization(options.getCurrentLocale());
        fontManager = new FontManager(options.getCurrentLocale());
    }
    
    public Renderer(CacheHandler cache) throws Exception {
        this.cache = cache;
        inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        outputFactory = XMLOutputFactory.newInstance();
        docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }    
    
    public void createFromTemplate(String resourcePath, OutputStream output, Options options) throws Exception {
        writer = outputFactory.createXMLStreamWriter(output);
        this.options = options;
        Settings.latitude = options.getLatitude();
        localization = new Localization(options.getCurrentLocale());
        fontManager = new FontManager(options.getCurrentLocale());
        createFromTemplate(resourcePath);
    }
    
    public byte[] createFromTemplate(String resourcePath, Options options) throws Exception {        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writer = outputFactory.createXMLStreamWriter(output);
        this.options = options;
        Settings.latitude = options.getLatitude();
        localization = new Localization(options.getCurrentLocale());
        fontManager = new FontManager(options.getCurrentLocale());
        createFromTemplate(resourcePath);
        return output.toByteArray();
    }

    
    public void createFromTemplate(String resourcePath) throws Exception {
        InputStream input = getClass().getResourceAsStream(Settings.resourceBasePath + "templates/core/" + resourcePath);
        createFromTemplate(input);
        input.close();
    }

    public void createFromTemplate(InputStream input) throws Exception {

        ArrayList<Element> paramElements = new ArrayList<Element>();

        XMLEventReader parser = inputFactory.createXMLEventReader(input);

        Boolean isUsed = true;
        StartElement startElement;
        Characters characters;
        String elementName;
        Attribute idAttr;
        String id;

        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    startElement = (StartElement) event;
                    elementName = startElement.getName().getLocalPart();
                    idAttr = startElement.getAttributeByName(new QName("id"));

                    if (idAttr != null) {
                        id = idAttr.getValue();
                        isUsed = false;
                        if (id.equals("mapView")) {
                            renderDefsMapView();
                        } else if (id.equals("monthsView")) {
                            renderDefsMonthsView();
                        } else if (id.equals("dialHoursMarkerMajor")) {
                            paramElements.add(getParamElement(parser, event));
                        } else if (id.equals("dialHoursMarkerMinor")) {
                            paramElements.add(getParamElement(parser, event));
                        } else if (id.equals("dialMonthsLabelMajorPath")) {
                            renderDefsDialMonthsLabelMajorPath();
                        } else if (id.equals("dialMonthsLabelMinorPath")) {
                            renderDefsDialMonthsLabelMinorPath();
                        } else if (id.equals("cardinalPointLabelPaths")) {
                            writeGroupStart("cardinalPointLabelPaths");
                            renderDefsCardinalPointLabelPaths();
                            writeGroupEnd();
                        } else if (id.equals("coordLabelPaths")) {
                            writeGroupStart("coordLabelPaths");
                            renderDefsCoordLabelPaths();
                            writeGroupEnd();
                        } else if (id.equals("wheel")) {
                            writeGroupStart("wheel");

                            renderMapBackground();
                            renderDialMonths();
                            renderDialMonthsTicks();

                            if (options.isMilkyWay()) {
                                renderMilkyWay();
                            }

                            if (options.isCoordsRADec()) {
                                renderCoords();
                                renderCoordLabels();
                            }

                            if (options.isEcliptic()) {
                                renderEcliptic();
                            }

                            if (options.isConstellationBoundaries()) {
                                renderConstellationBoundaries();
                            }

                            if (options.isConstellationLines()) {
                                renderConstellationLines();
                            }

                            renderStars();

                            if (options.isConstellationLabels()) {
                                renderConstellationNames(options.getConstellationLabelsOptions());
                            }

                            writeGroupEnd();

                        } else if (id.equals("scales")) {

                            writeGroupStart("scales");
                            renderDialHours(paramElements, options.isDayLightSavingTimeScale());
                            renderCardinalPointsTicks();
                            renderCardinalPointsLabels();
                            writeGroupEnd();

                        } else if (id.equals("monthsViewBorder")) {

                            writeGroupStart("monthsViewBorder");
                            renderMonthsViewBorder();
                            writeGroupEnd();

                        } else if (id.equals("mapViewBorder")) {

                            writeGroupStart("mapViewBorder");
                            renderMapViewBorder();
                            writeGroupEnd();

                        } else if (id.equals("spacer")) {
                            renderSpacer();

                        } else if (id.equals("cover")) {
                            renderCover();
                            renderBendLine();
                            renderPinMark(1);

                        } else if (id.equals("pinMark")) {
                            renderPinMark(0);

                        } else if (id.equals("guide") || id.equals("worldmap") || id.equals("buttonSettings") || id.equals("buttonExport")) {
                            writer.writeStartElement("g");
                            if (id.equals("buttonSettings") || id.equals("buttonExport")) {
                                writer.writeAttribute("title", localization.getValue(id));
                            }
                            renderSymbol(id);
                            writer.writeEndElement();

                        } else if (id.equals("latitudeMarker")) {
                            writer.writeStartElement(elementName);
                            Iterator<Attribute> it = startElement.getAttributes();
                            while (it.hasNext()) {
                                Attribute a = it.next();
                                QName attr = a.getName();
                                if (attr.getLocalPart().equals("y")) {
                                    Double range = Double.valueOf(a.getValue());
                                    Double ratio = range / 180;
                                    Double y = ratio * (Math.abs(Settings.latitude - 90) - 90);
                                    writer.writeAttribute("y", y.toString());
                                } else {
                                    writer.writeAttribute(attr.getPrefix(), attr.getNamespaceURI(), attr.getLocalPart(), a.getValue());
                                }
                            }
                            writer.writeEndElement();

                        } else {
                            isUsed = true;
                            writer.writeStartElement(elementName);
                            writeAttributes(startElement.getAttributes());
                        }
                    } else if (elementName.equals("svg")) {
                        String[] values = startElement.getAttributeByName(new QName("viewBox")).getValue().split(" ");
                        Settings.scale = Math.min(Double.valueOf(values[2]) / 2, Double.valueOf(values[3]) / 2);

                        /*
                        Settings.x = Double.valueOf(values[0]);
                        Settings.y = Double.valueOf(values[1]);
                        Settings.width = Double.valueOf(values[2]);
                        Settings.height = Double.valueOf(values[3]);
                        Settings.shiftX = Settings.x + Settings.width / 2;
                        Settings.shiftY = Settings.y + Settings.height / 2;

                        //if (Settings.x == 0) {
                        Settings.scale = Math.min(Settings.height / 2, Settings.width / 2) ;
                         */

                        //} else {
                        //    Settings.scale = Math.abs(Settings.y);
                        //}
                        mapArea = getMapArea();
                        cardinalPoints = getCardinalPoints();
                        writer.writeStartElement(elementName);
                        writeNamespaces(startElement.getNamespaces());
                        writeAttributes(startElement.getAttributes());
                    } else {
                        writer.writeStartElement(elementName);
                        writeAttributes(startElement.getAttributes());
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if (isUsed) {
                        writer.writeEndElement();
                    } else {
                        isUsed = true;
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    if (isUsed) {
                        characters = (Characters) event;
                        String text = characters.getData();
                        if (!(text.trim().isEmpty())) {
                            writer.writeCharacters(localization.translate(text));
                        }
                    }
                    break;
                case XMLStreamConstants.CDATA:
                    if (isUsed) {
                        characters = (Characters) event;
                        writer.writeCData(fontManager.translate(characters.getData()));
                    }
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    if (isUsed) {
                        ProcessingInstruction pi = (ProcessingInstruction) event;
                        if (pi.getTarget().equals("mouseEventsScript")) {
                            writer.writeStartElement("script");
                            writer.writeAttribute("type", "application/ecmascript");

                            StringWriter cdata = new StringWriter();
                            InputStream is = getClass().getResourceAsStream(Settings.resourceBasePath + "templates/resources/js/mouseEvents.js");
                            char[] buffer = new char[1024];
                            Reader reader = new InputStreamReader(is, "UTF-8");
                            int n;
                            while ((n = reader.read(buffer)) != -1) {
                                cdata.write(buffer, 0, n);
                            }
                            writer.writeCData(cdata.toString());
                            writer.writeEndElement();
                        }
                    }
                    break;

                default:
                //System.out.println(event.getEventType());
            }
        }
        parser.close();
        close();
    }

    private Element getParamElement(XMLEventReader parser, XMLEvent event) throws Exception {
        Element paramElement = docBuilder.newDocument().createElement("g");
        XMLEventWriter xmlWriter = outputFactory.createXMLEventWriter(new DOMResult(paramElement));
        int level = 1;
        XMLEvent paramEvent = event;
        while (!(parser.hasNext() && paramEvent.getEventType() == XMLStreamConstants.END_ELEMENT && level == 0)) {
            paramEvent = parser.nextEvent();
            xmlWriter.add(paramEvent);
            if (paramEvent.isStartElement()) {
                level++;
            }
            if (paramEvent.isEndElement()) {
                level--;
            }
        }
        return paramElement;
    }

    private void writeAttributes(Iterator attributes) throws Exception {
        while (attributes.hasNext()) {
            Attribute attr = (Attribute) attributes.next();
            writer.writeAttribute(attr.getName().getPrefix(), attr.getName().getNamespaceURI(), attr.getName().getLocalPart(), attr.getValue());
        }
    }

    private void writeNamespaces(Iterator namespaces) throws Exception {
        while (namespaces.hasNext()) {
            Namespace ns = (Namespace) namespaces.next();
            writer.writeNamespace(ns.getPrefix(), ns.getNamespaceURI());
        }
    }

    public void create() throws Exception {

        writeSVGRootElement();
        /*
        writeScript();
         */
        //mapArea = getMapArea();
        writeDefs();


        // bottom layer

        writeGroupStart("wheel");
        //renderMapBackground();

        renderDialMonths();
        renderDialMonthsTicks();
        /*
        renderMilkyWay();
        renderCoords();
        renderEcliptic();
        renderConstellationBoundaries();
        renderConstellationLines();
        renderStars();
        writeGroupEnd();

        // front layer

        writeGroupStart("cover");
        writer.writeAttribute("mask", "url(#mask)");
        renderDummyRectangle("dummyRectBlue");
        writeGroupEnd();

        writeGroupStart("mouseAreas");
        renderMouseAreas();
        writeGroupEnd();

        // front graphics
        writeGroupStart("frontGraphics");
        //renderDialHours(markMajor, markMinor);
        renderMapViewBorder();
        renderMonthsViewBorder();
        renderCardinalPointsTicks();
        renderCardinalPointsLabels();
         */
        writeGroupEnd();

        close();
    }

    private void writeGroupStart(String id) throws Exception {
        writer.writeStartElement("g");
        if (id != null) {
            writer.writeAttribute("id", id);
        }
    }

    private void writeGroupEnd() throws Exception {
        writer.writeEndElement();
    }

    @Deprecated
    private void writeSVGRootElement() throws Exception {
        writer.writeStartDocument();
        writer.writeStartElement("svg");
        writer.writeDefaultNamespace("http://www.w3.org/2000/svg");
        writer.writeNamespace("xlink", "http://www.w3.org/1999/xlink");

        writer.writeAttribute("width", Settings.scale.toString());
        writer.writeAttribute("height", Settings.scale.toString());
        writer.writeAttribute("viewBox",
                -Settings.scale + " "
                + -Settings.scale + " "
                + 2 * Settings.scale + " "
                + 2 * Settings.scale);
        writer.writeAttribute("onload", "startup(evt)");
        writer.writeAttribute("style", "-moz-user-select: none;");
    }

    private void writeDefs() throws Exception {
        writer.writeStartElement("defs");

        //renderDefsMapView();
        //renderDefsMonthsView();
        renderDefsDialMonthsLabelMajorPath();
        renderDefsDialMonthsLabelMinorPath();
        /*
        renderDefsDialHoursMarkerMajor();
        renderDefsDialHoursMarkerMinor();
        renderPath(renderCircle(0.80), "dialHoursLabelDefaultPath", null);
        renderPath(renderCircle(0.75), "dialHoursLabelSummerPath", null);
        renderDefsCardinalPointLabelPaths();
        renderDefsMask("maskSkin", 0);
        renderDefsMask("maskWheel", 1);
         */
        renderDefsCSS();
        writer.writeEndElement();
    }

    public void close() throws Exception {
        writer.writeEndDocument();
        writer.flush();
        writer.close();
    }

    @Deprecated
    private void renderMouseAreas() throws Exception {
        writeGroupStart("passive");
        writer.writeAttribute("transform", "translate(-512, -384)");
        writer.writeAttribute("onmousemove", "wheelMouseMove(evt)");
        writer.writeAttribute("onmouseup", "wheelMouseUp(evt)");
        renderDummyRectangle("eventsTarget");
        writeGroupEnd();

        writeGroupStart("active");
        writer.writeAttribute("style", "cursor: url(../openhand.cur), pointer;");
        writer.writeAttribute("onmousedown", "wheelMouseDown(evt)");
        writer.writeAttribute("onmousemove", "wheelMouseMove(evt)");
        writer.writeAttribute("onmouseup", "wheelMouseUp(evt)");
        renderDefsInstance("mapView", 0d, 0d, null, "eventsTarget");
        renderDefsInstance("monthsView", 0d, 0d, null, "eventsTarget");
        writeGroupEnd();
    }

    private void renderMapBackground() throws Exception {
        renderPath(renderCircle(1.0), null, "mapBackground");
    }

    private void renderStars() throws Exception {
        Point2D coord = new Point2D.Double();
        StringWriter[] path = new StringWriter[magnitudeRange];
        String coordsChunk = "";
        Integer magnitudeIndex = 0;

        for (int i = 0; i < magnitudeRange; i++) {
            path[i] = new StringWriter();
        }
        for (Star star : cache.getStars()) {
            if (Coords.convert(star.getRA(), star.getDec(), coord)) {
                coordsChunk = Coords.getCoordsChunk(coord);
                magnitudeIndex = Math.round(star.getMag() + 1);
                path[magnitudeIndex].append("M" + coordsChunk + "L" + coordsChunk);
            }
        }
        for (Integer i = 0; i < magnitudeRange; i++) {
            renderPath(path[i].toString(), null, "star level" + i);
        }
    }

    private void renderConstellationBoundaries() throws Exception {
        Point2D coordStart = new Point2D.Double();
        Point2D coordEnd = new Point2D.Double();
        StringWriter path = new StringWriter();
        for (Iterator<Point2D> i = cache.getConstellationBoundaries().iterator(); i.hasNext();) {
            Point2D coordStartRaw = i.next();
            if (Coords.convert(coordStartRaw.getX(), coordStartRaw.getY(), coordStart)) {
                Point2D coordEndRaw = i.next();
                if (Coords.convert(coordEndRaw.getX(), coordEndRaw.getY(), coordEnd)) {
                    if ((Math.abs(coordStartRaw.getY() - coordEndRaw.getY()) < 0.7) && (Math.abs(coordStartRaw.getX() - coordEndRaw.getX()) > 0)
                            || (Math.abs(coordStartRaw.getY()) > 86)) {

                        Integer bDiv = new Double(5 + (15 * ((Settings.latitude > 0) ? (90 - coordEndRaw.getY()) : (90 + coordEndRaw.getY())) / 90)).intValue();

                        Double startRA = coordStartRaw.getX();
                        Double endRA = coordEndRaw.getX();
                        Double incRA = endRA - startRA;

                        if (incRA > 12) {
                            startRA = coordEndRaw.getX();
                            endRA = coordStartRaw.getX() + 24;
                            incRA = 24 - incRA;
                        }
                        if (incRA < -12) {
                            incRA = incRA + 24;
                            endRA = coordEndRaw.getX() + 24;
                        }

                        Double incDec = (coordEndRaw.getY() - coordStartRaw.getY()) / bDiv;
                        Double Dec = coordStartRaw.getY();
                        Point2D coordTemp = new Point2D.Double();

                        for (int j = 0; j <= bDiv; j++) {
                            Coords.convert(startRA + j * incRA / bDiv, Dec, coordTemp);
                            if (j == 0) {
                                path.append("M" + Coords.getCoordsChunk(coordTemp));
                            } else {
                                path.append("L" + Coords.getCoordsChunk(coordTemp));
                            }
                            Dec = Dec + incDec;
                        }
                    } else {
                        path.append("M" + Coords.getCoordsChunk(coordStart));
                        path.append("L" + Coords.getCoordsChunk(coordEnd));
                    }
                }
            } else {
                i.next();
            }
        }
        renderPath(path.toString(), null, "constellationBoundaries");
    }

    private void renderConstellationLines() throws Exception {
        Point2D coordStart = new Point2D.Double();
        Point2D coordEnd = new Point2D.Double();
        StringWriter path = new StringWriter();
        for (Iterator<Point2D> i = cache.getConstellationLines().iterator(); i.hasNext();) {
            Point2D coordStartRaw = i.next();
            if (Coords.convert(coordStartRaw.getX(), coordStartRaw.getY(), coordStart)) {
                Point2D coordEndRaw = i.next();
                if (Coords.convert(coordEndRaw.getX(), coordEndRaw.getY(), coordEnd)) {
                    path.append("M" + Coords.getCoordsChunk(coordStart));
                    path.append("L" + Coords.getCoordsChunk(coordEnd));
                }
            } else {
                i.next();
            }
        }
        renderPath(path.toString(), null, "constellationLines");
    }

    private void renderConstellationNames(Integer mode) throws Exception {
        Point2D coordRaw = new Point2D.Double();
        Point2D coord = new Point2D.Double();
        for (Iterator<ConstellationName> i = cache.getConstellationNames().iterator(); i.hasNext();) {
            ConstellationName constellationName = i.next();
            String name = "";
            switch (mode) {
                case 0:
                    name = localization.getValue(constellationName.getId());
                    break;
                case 1:
                    name = constellationName.getLatin();
                    break;
                case 2:
                    name = constellationName.getAbbreviation();
            }
            //System.out.println(name + " : " + constellationName.getId());
            coordRaw = constellationName.getCoord();
            if (Coords.convert(coordRaw.getX(), coordRaw.getY(), coord)) {
                String id = "con" + constellationName.getAbbreviation();
                renderPath(renderCircleForConstellationName(coord), id, "constellationNamesPath");
                renderTextOnPath(id, 50d, name, "constellationNames");
            }
        }
    }

    private void renderEcliptic() throws Exception {

        Double epsilon = Math.toRadians(23.44);
        Double lambda;
        Double RA;
        Double Dec;
        Boolean flag = false;
        String coordsChunk = "";
        StringWriter path = new StringWriter();

        Point2D coord = new Point2D.Double();

        for (int i = 0; i <= 360; i = i + 2) {
            lambda = Math.toRadians(i);
            RA = (Math.atan2(Math.sin(lambda) * Math.cos(epsilon), Math.cos(lambda))) * 12 / Math.PI;
            Dec = Math.toDegrees(Math.asin(Math.sin(epsilon) * Math.sin(lambda)));
            if (Coords.convert(RA, Dec, coord)) {
                if (!flag) {
                    coordsChunk = Coords.getCoordsChunk(coord);
                    flag = true;
                } else {
                    path.append("M" + coordsChunk);
                    path.append("L" + Coords.getCoordsChunk(coord));
                    coordsChunk = Coords.getCoordsChunk(coord);
                }
            } else {
                flag = false;
            }
        }
        renderPath(path.toString(), null, "ecliptic");
    }

    private void renderCoords() throws Exception {

        StringWriter path = new StringWriter();
        Point2D coord = new Point2D.Double();

        // declination circle
        double sign = Math.signum(Settings.latitude);
        for (double j = sign * 90; sign * j >= sign * (Settings.latitude - sign * 90); j = j - sign * 30) {
            for (double i = 0; i <= 24; i = i + 0.5) {
                Coords.convert(i, j, coord);
                if (i == 0) {
                    path.append("M" + Coords.getCoordsChunk(coord));
                } else {
                    path.append("L" + Coords.getCoordsChunk(coord));
                }
            }
        }

        // RA
        int start;
        for (Integer j = 0; j < 24; j++) {
            switch (j % 6) {
                case 1:
                case 3:
                case 5:
                    start = 30;
                    break;
                case 2:
                case 4:
                    start = 60;
                    break;
                default:
                    start = 90;
            }

            Coords.convert(j.doubleValue(), sign * start, coord);
            path.append("M" + Coords.getCoordsChunk(coord));
            Coords.convert(j.doubleValue(), Settings.latitude - sign * 90, coord);
            path.append("L" + Coords.getCoordsChunk(coord));
        }
        renderPath(path.toString(), null, "coords");
    }

    private void renderCoordLabels() throws Exception {

        for (Integer i = 0; i < 24; i++) {
            renderTextOnPath("coordLabelPath00", i * 100.0 / 24, i + "h", "coordLabelRa");
        }

        double sign = Math.signum(Settings.latitude);
        for (Double j = sign * 90; sign * j >= sign * (Settings.latitude - sign * 90); j = j - sign * 30) {
            String pathId = "coordLabelPath" + j.intValue();
            String strSign = (j > 0) ? "+" : "";
            renderTextOnPath(pathId, 100.0, strSign + j.intValue() + "°", "coordLabelDec");
            for (Integer i = 1; i < 4; i++) {
                renderTextOnPath(pathId, i * 25.0, strSign + j.intValue() + "°", "coordLabelDec");
            }
        }

    }

    private void renderDefsCoordLabelPaths() throws Exception {

        Point2D coord = new Point2D.Double();

        double sign = Math.signum(Settings.latitude);
        for (Double j = sign * 90; sign * j >= sign * (Settings.latitude - sign * 90); j = j - sign * 30) {
            StringWriter path = new StringWriter();
            for (double i = 24; i >= 0; i = i - 0.5) {
                Coords.convert(i, j, coord);
                if (i == 24) {
                    path.append("M" + Coords.getCoordsChunk(coord));
                } else {
                    path.append("L" + Coords.getCoordsChunk(coord));
                }
            }
            renderPath(path.toString(), "coordLabelPath" + j.intValue(), null);
        }

        // slightly shifted for RA labels
        StringWriter path = new StringWriter();
        for (double i = 24; i >= 0; i = i - 0.5) {
            Coords.convert(i, -3.0, coord);
            if (i == 24) {
                path.append("M" + Coords.getCoordsChunk(coord));
            } else {
                path.append("L" + Coords.getCoordsChunk(coord));
            }
        }
        renderPath(path.toString(), "coordLabelPath00", null);

    }

    private void renderDefsMapView() throws Exception {
        StringBuilder path = new StringBuilder();
        Iterator iter = mapArea.iterator();
        path.append("M");
        path.append(Coords.getCoordsChunk((Point2D) iter.next()));
        while (iter.hasNext()) {
            path.append("L");
            path.append(Coords.getCoordsChunk((Point2D) iter.next()));
        }
        renderPath(path.toString(), "mapView", null);
    }

    private void renderMapViewBorder() throws Exception {
        renderDefsInstance("mapView", 0d, 0d, null, "mapViewBorder");
    }

    private void renderCardinalPointsTicks() throws Exception {
        StringBuilder path = new StringBuilder();
        for (CardinalPointInfo point : cardinalPoints) {
            path.append("M");
            path.append(Coords.getCoordsChunk(point.getTickStart()));
            path.append("L");
            path.append(Coords.getCoordsChunk(point.getTickEnd()));
        }
        renderPath(path.toString(), null, "cardinalPointTick");
    }

    private void renderDefsCardinalPointLabelPaths() throws Exception {

        int cq = 2;
        if (Math.abs(Settings.latitude) > 78) {
            cq = 0;
        } else if (Math.abs(Settings.latitude) > 70) {
            cq = 1;
        }

        String pathID;
        CardinalPointInfo point;

        //writeGroupStart(null);
        //writer.writeAttribute("transform", "translate()");
        Double gap = Settings.scale * 0.006;
        Double letterHeight = Settings.scale * 0.032;
        for (Integer i = 0; i <= cq; i++) {
            pathID = "cid" + i;
            point = cardinalPoints.get(i);
            renderPath(renderCircle(point.getCenter(), point.getRadius() + gap), pathID, "dialMonthsTick");
        }
        for (Integer i = 3; i <= 5; i++) {
            pathID = "cid" + i;
            point = cardinalPoints.get(i);
            renderPath(renderCircleInv(point.getCenter(), point.getRadius() + gap + letterHeight), pathID, "dialMonthsTick");
        }
        if (Math.abs(Settings.latitude) < 78) {
            for (Integer i = 8 - cq; i <= 7; i++) {
                pathID = "cid" + i;
                point = cardinalPoints.get(i);
                renderPath(renderCircle(point.getCenter(), point.getRadius() + gap), pathID, "dialMonthsTick");
            }
        }
        //writeGroupEnd();
    }

    private void renderCardinalPointsLabels() throws Exception {

        ArrayList<String> points = new ArrayList<String>();
        points.add(localization.getValue("cardinalPointNorth"));
        points.add(localization.getValue("cardinalPointNorthWest"));
        points.add(localization.getValue("cardinalPointWest"));
        points.add(localization.getValue("cardinalPointSouthWest"));
        points.add(localization.getValue("cardinalPointSouth"));
        points.add(localization.getValue("cardinalPointSouthEast"));
        points.add(localization.getValue("cardinalPointEast"));
        points.add(localization.getValue("cardinalPointNorthEast"));

        String[] cardinalPointLabels = new String[points.size()];
        points.toArray(cardinalPointLabels);

        int cq = 2;
        if (Math.abs(Settings.latitude) > 78) {
            cq = 0;
        } else if (Math.abs(Settings.latitude) > 70) {
            cq = 1;
        }

        if (Settings.latitude < 0) {
            String tmp;
            tmp = cardinalPointLabels[0];
            cardinalPointLabels[0] = cardinalPointLabels[4];
            cardinalPointLabels[4] = tmp;
            tmp = cardinalPointLabels[1];
            cardinalPointLabels[1] = cardinalPointLabels[3];
            cardinalPointLabels[3] = tmp;
            tmp = cardinalPointLabels[5];
            cardinalPointLabels[5] = cardinalPointLabels[7];
            cardinalPointLabels[7] = tmp;
        }

        String pathID;
        CardinalPointInfo point;

        for (Integer i = 0; i <= cq; i++) {
            pathID = "cid" + i;
            point = cardinalPoints.get(i);
            renderTextOnPath(pathID, point.getStartOffset(), cardinalPointLabels[i], "cardinalPointLabel");
        }
        for (Integer i = 3; i <= 5; i++) {
            pathID = "cid" + i;
            point = cardinalPoints.get(i);
            renderTextOnPath(pathID, 100 - point.getStartOffset(), cardinalPointLabels[i], "cardinalPointLabel");
        }
        if (Math.abs(Settings.latitude) < 78) {
            for (Integer i = 8 - cq; i <= 7; i++) {
                pathID = "cid" + i;
                point = cardinalPoints.get(i);
                renderTextOnPath(pathID, point.getStartOffset(), cardinalPointLabels[i], "cardinalPointLabel");
            }
        }
    }

    private void renderDialHours(ArrayList<Element> paramElements, Boolean isDayLightSavingTimeScale) throws Exception {


        Element markMajor = paramElements.get(0);
        Element markMinor = paramElements.get(1);
        //Element markHalf = paramElements.get(2);

        int q = 8;
        int qq = 7;
        Double latitudeAbs = Math.abs(Settings.latitude);

        if (latitudeAbs > 76) {
            q = 5;
        } else if (latitudeAbs > 70) {
            q = 6;
            qq = 5;
        } else if (latitudeAbs > 60) {
            q = 7;
            qq = 5;
        }

        // default labels
        Integer c;

        for (int i = -q; i <= q; i++) {
            c = Settings.latitude < 0 ? i : -i;
            if (c < 0) {
                c = c + 24;
            }
            renderDialHoursMarker(replaceTextElementContent(markMajor, "#", c.toString()), i * 15.0, "dialHoursMarkerMajor");
        }

        // summer time labels
        if (isDayLightSavingTimeScale && latitudeAbs <= 75) {
            for (int i = -qq; i <= qq; i++) {
                c = Settings.latitude < 0 ? i + 1 : -i + 1;
                if (c < 0) {
                    c = c + 24;
                }
                if (c > 23) {
                    c = c - 24;
                }
                renderDialHoursMarker(replaceTextElementContent(markMinor, "#", c.toString()), i * 15.0, "dialHoursMarkerMinor");
            }
        }

        for (Double i = 112.5; i >= -120; i = i - 15) {
            String strTranslate = Coords.format(0.9 * Settings.scale);
            renderDefsInstance("dialHoursMarkerHalf", 0d, 0d, "translate(0,-" + strTranslate + ") rotate(" + i + ",0," + strTranslate + ")", null);
            //renderDialHoursMarker(markHalf, i);
        }
    }

    private void renderSymbol(String id) throws Exception {
        writeStreamContent(this.getClass().getResourceAsStream(Settings.resourceBasePath + "templates/resources/symbols/" + id + ".svg"));
    }

    private void renderDefsCSS() throws Exception {
        CSSDataLoader dl = new CSSDataLoader("D:\\om.css");
        writer.writeStartElement("style");
        writer.writeAttribute("type", "text/css");
        writer.writeCData(dl.getCssData());
        writer.writeEndElement();
    }

    private void writeScript() throws Exception {
        CSSDataLoader dl = new CSSDataLoader("D:\\om.js");
        writer.writeStartElement("script");
        writer.writeAttribute("type", "application/ecmascript");
        writer.writeCData(dl.getCssData());
        writer.writeEndElement();
    }

    @Deprecated
    private void renderDefsMask(String id, Integer mode) throws Exception {

        String classBottom = (mode == 0) ? "maskWhite" : "maskBlack";
        String classTop = (mode == 0) ? "maskBlack" : "maskWhite";
        writer.writeStartElement("mask");
        writer.writeAttribute("id", id);
        // do not place mouse events here as they are ignored
        /*
        writer.writeStartElement("rect");
        writer.writeAttribute("x", Coords.format(Settings.x));
        writer.writeAttribute("y", Coords.format(Settings.y));
        writer.writeAttribute("width", String.valueOf(Settings.width));
        writer.writeAttribute("height", String.valueOf(Settings.height));
        writer.writeAttribute("class", classBottom);
        writer.writeEndElement();
         */
        writeGroupStart(null);
        /*
        if (mode == 0 && (Settings.shiftX != 0 || Settings.shiftY != 0)) {
        writer.writeAttribute("transform", "translate(" + Settings.shiftX + "," + Settings.shiftY + ")");
        }
         */
        renderDefsInstance("mapView", 0d, 0d, null, classTop);
        renderDefsInstance("monthsView", 0d, 0d, null, classTop);
        writeGroupEnd();

        writer.writeEndElement();
    }

    @Deprecated
    private void renderDummyRectangle(String style) throws Exception {
        /*
        writer.writeStartElement("rect");
        writer.writeAttribute("x", Coords.format(Settings.x));
        writer.writeAttribute("y", Coords.format(Settings.y));
        writer.writeAttribute("width", String.valueOf(Settings.width));
        writer.writeAttribute("height", String.valueOf(Settings.height));
        writer.writeAttribute("class", style);
        writer.writeEndElement();
         */
    }

    private void renderDefsMonthsView() throws Exception {

        StringBuilder pathData = new StringBuilder();
        double angle = Math.toRadians(30d);
        Double x1 = Math.cos(angle) * 0.9 * Settings.scale;
        Double y1 = Math.sin(angle) * 0.9 * Settings.scale;
        Double x2 = Math.cos(angle) * Settings.scale;
        Double y2 = Math.sin(angle) * Settings.scale;

        // inner arc
        pathData.append("M");
        pathData.append(Coords.format(-x1));
        pathData.append(" ");
        pathData.append(Coords.format(y1));
        pathData.append("A");
        // rx
        pathData.append(Coords.format(0.9 * Settings.scale));
        pathData.append(" ");
        // ry
        pathData.append(Coords.format(0.9 * Settings.scale));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 1 1 ");
        pathData.append(Coords.format(x1));
        pathData.append(" ");
        pathData.append(Coords.format(y1));

        pathData.append("L");
        pathData.append(Coords.format(x2));
        pathData.append(" ");
        pathData.append(Coords.format(y2));

        // outer arc
        pathData.append("A");
        pathData.append(Coords.format(Settings.scale));
        pathData.append(" ");
        pathData.append(Coords.format(Settings.scale));
        pathData.append(" 0 1 0 ");
        pathData.append(Coords.format(-x2));
        pathData.append(" ");
        pathData.append(Coords.format(y2));

        pathData.append("Z");

        renderPath(pathData.toString(), "monthsView", "null");

    }

    private void renderMonthsViewBorder() throws Exception {
        renderDefsInstance("monthsView", 0d, 0d, null, "monthsViewBorder");
    }

    private void renderDialMonthsTicks() throws Exception {

        Integer[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        Integer daysInYear = 365;

        GregorianCalendar calendar = new GregorianCalendar();
        if (calendar.isLeapYear(Calendar.getInstance().get(Calendar.YEAR))) {
            daysInMonth[1] = 29;
            daysInYear = 366;
        }

        double sign = Math.signum(Settings.latitude);
        double angle = 19 + 60 + (sign < 0 ? 180 : 0);
        double angleInRads;

        for (int month = 0; month < 12; month++) {

            StringWriter path = new StringWriter();
            for (int day = 0; day < daysInMonth[month]; day++) {
                angleInRads = sign * Math.toRadians(angle);
                angle = angle - 360.0 / daysInYear;
                switch (day) {
                    case 0:
                        path.append(renderDialMonthsTick(angleInRads, 1.0));
                        break;
                    case 5:
                    case 10:
                    case 15:
                    case 20:
                    case 25:
                    case 30:
                        path.append(renderDialMonthsTick(angleInRads, 0.914));
                        break;
                    default:
                        path.append(renderDialMonthsTick(angleInRads, 0.908));
                }
            }
            renderPath(path.toString(), null, "dialMonthsTick");
        }
    }

    private void renderMilkyWay() throws Exception {

        MilkyWayDataSet milkyWayDataSet = cache.getMilkyWayDataSet();
        Poly sourcePolygon = new PolyDefault();
        Poly destPolygon = new PolyDefault();

        destPolygon.add(createClipArea());

        sourcePolygon.add(createContour(milkyWayDataSet.getDarkNorth()));
        sourcePolygon.add(createContour(milkyWayDataSet.getDarkSouth()));

        renderMilkyWay(sourcePolygon.intersection(destPolygon), "milkyWayBright");

        sourcePolygon.add(createContour(milkyWayDataSet.getBrightNorth()));
        sourcePolygon.add(createContour(milkyWayDataSet.getBrightSouth()));

        renderMilkyWay(sourcePolygon.intersection(destPolygon), "milkyWayDark");

    }

    private void renderMilkyWay(Poly polygon, String style) throws Exception {
        StringWriter path = new StringWriter();
        for (int i = 0; i < polygon.getNumInnerPoly(); i++) {
            Poly contour = polygon.getInnerPoly(i);
            for (int j = 0; j < contour.getNumPoints(); j++) {
                if (j == 0) {
                    path.append("M" + Coords.format(contour.getX(j)) + " " + Coords.format(contour.getY(j)));
                } else {
                    path.append("L" + Coords.format(contour.getX(j)) + " " + Coords.format(contour.getY(j)));
                }
            }
        }
        renderPath(path.toString(), null, style);
    }

    private void renderSpacer() throws Exception {
        StringBuilder pathData = new StringBuilder();
        Double angle = Math.toRadians(210d);
        Double ratio = 1.03 * Settings.scale;
        String strRadius = Coords.format(ratio);

        // main arc
        pathData.append("M");
        pathData.append(Coords.format(Math.cos(angle) * ratio));
        pathData.append(" ");
        pathData.append(Coords.format(-Math.sin(angle) * ratio));
        pathData.append("A");
        // rx
        pathData.append(strRadius);
        pathData.append(" ");
        // ry
        pathData.append(strRadius);
        // rotation, large arc flag, sweep flag
        angle = Math.toRadians(330d);
        pathData.append(" 0 0 0 ");
        pathData.append(Coords.format(Math.cos(angle) * ratio));
        pathData.append(" ");
        pathData.append(Coords.format(-Math.sin(angle) * ratio));

        ArrayList<Point2D.Double> coords = new ArrayList<Point2D.Double>();
        double dy = Math.tan(Math.toRadians(30d)) * Settings.scale;
        coords.add(new Point2D.Double(Settings.scale, dy));
        coords.add(new Point2D.Double(Settings.scale, 1.12 * Settings.scale));
        coords.add(new Point2D.Double(-Settings.scale, 1.12 * Settings.scale));
        coords.add(new Point2D.Double(-Settings.scale, dy));

        renderPath(pathData + getPathData(coords, true), "spacer", null);
    }

    private void renderCover() throws Exception {
        StringBuilder pathData = new StringBuilder();
        Double x1 = Math.cos(Math.toRadians(30d)) * 0.9 * Settings.scale;
        Double y1 = Math.sin(Math.toRadians(30d)) * 0.9 * Settings.scale;
        // main arc
        pathData.append("M");
        pathData.append(Coords.format(-x1));
        pathData.append(" ");
        pathData.append(Coords.format(y1));
        pathData.append("A");
        // rx
        pathData.append(Coords.format(0.9 * Settings.scale));
        pathData.append(" ");
        // ry
        pathData.append(Coords.format(0.9 * Settings.scale));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 1 1 ");
        pathData.append(Coords.format(x1));
        pathData.append(" ");
        pathData.append(Coords.format(y1));

        // joiners
        Double y2 = Math.tan(Math.toRadians(30d)) * Settings.scale;
        Double height = 1.12 * Settings.scale - y2;
        Double y3 = y2 + 2 * height;
        Double y4 = y3 + (y2 - y1);
        Double x5 = Math.cos(Math.toRadians(340d)) * 0.9 * Settings.scale;
        Double y5 = 2.24 * Settings.scale + Math.sin(Math.toRadians(20d)) * 0.9 * Settings.scale;

        // right side
        pathData.append("L");
        pathData.append(Coords.format(Settings.scale));
        pathData.append(" ");
        pathData.append(Coords.format(y2));
        pathData.append("L");
        pathData.append(Coords.format(Settings.scale));
        pathData.append(" ");
        pathData.append(Coords.format(y3));
        pathData.append("L");
        pathData.append(Coords.format(x1));
        pathData.append(" ");
        pathData.append(Coords.format(y4));
        pathData.append("A");
        // rx
        pathData.append(Coords.format(0.9 * Settings.scale));
        pathData.append(" ");
        // ry
        pathData.append(Coords.format(0.9 * Settings.scale));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 0 1 ");
        pathData.append(Coords.format(x5));
        pathData.append(" ");
        pathData.append(Coords.format(y5));

        // left side
        pathData.append("L");
        pathData.append(Coords.format(-x5));
        pathData.append(" ");
        pathData.append(Coords.format(y5));
        pathData.append("A");
        // rx
        pathData.append(Coords.format(0.9 * Settings.scale));
        pathData.append(" ");
        // ry
        pathData.append(Coords.format(0.9 * Settings.scale));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 0 1 ");
        pathData.append(Coords.format(-x1));
        pathData.append(" ");
        pathData.append(Coords.format(y4));
        pathData.append("L");
        pathData.append(Coords.format(-Settings.scale));
        pathData.append(" ");
        pathData.append(Coords.format(y3));
        pathData.append("L");
        pathData.append(Coords.format(-Settings.scale));
        pathData.append(" ");
        pathData.append(Coords.format(y2));
        pathData.append("L");
        pathData.append(Coords.format(-x1));
        pathData.append(" ");
        pathData.append(Coords.format(y1));
        pathData.append("Z");

        renderPath(pathData.toString(), "cover", "cover");

    }

    private void renderBendLine() throws Exception {
        StringBuilder pathData = new StringBuilder();
        Double y = 1.12 * Settings.scale;

        pathData.append("M");
        pathData.append(Coords.format(-Settings.scale));
        pathData.append(" ");
        pathData.append(Coords.format(y));
        pathData.append("L");
        pathData.append(Coords.format(Settings.scale));
        pathData.append(" ");
        pathData.append(Coords.format(y));

        renderPath(pathData.toString(), "bendLine", "bendLine");
    }

    private void renderPinMark(Integer mode) throws Exception {
        StringBuilder pathData = new StringBuilder();
        Double size = 0.02 * Settings.scale;
        Double dy = 0d;
        if (mode > 0) {
            dy = 2.24 * Settings.scale;
        }
        pathData.append("M");
        pathData.append(Coords.format(-size));
        pathData.append(" ");
        pathData.append(Coords.format(dy));
        pathData.append("L");
        pathData.append(Coords.format(size));
        pathData.append(" ");
        pathData.append(Coords.format(dy));
        pathData.append("M 0 ");
        pathData.append(Coords.format(dy - size));
        pathData.append("L 0 ");
        pathData.append(Coords.format(dy + size));

        renderPath(pathData.toString(), "pinMark", "pinMark");
    }

    private String getPathData(ArrayList<Point2D.Double> coords, Boolean append) {
        StringBuilder pathData = new StringBuilder();
        Boolean isFirst = true;
        for (Point2D.Double coord : coords) {
            if (isFirst) {
                if (append) {
                    pathData.append("L");
                } else {
                    pathData.append("M");
                }
                pathData.append(Coords.format(coord.getX()));
                pathData.append(" ");
                pathData.append(Coords.format(coord.getY()));
                isFirst = false;
            }
            pathData.append("L");
            pathData.append(Coords.format(coord.getX()));
            pathData.append(" ");
            pathData.append(Coords.format(coord.getY()));
        }
        pathData.append("z");
        return pathData.toString();
    }

    private String getPathData2(ArrayList<CoordAsString> coords) {
        StringBuilder pathData = new StringBuilder();
        Boolean isFirst = true;
        for (CoordAsString coord : coords) {
            if (isFirst) {
                pathData.append("M");
                pathData.append(coord.getX());
                pathData.append(" ");
                pathData.append(coord.getY());
                isFirst = false;
            }
            pathData.append("L");
            pathData.append(coord.getX());
            pathData.append(" ");
            pathData.append(coord.getY());
        }
        pathData.append("z");
        return pathData.toString();
    }

    private void renderPath(String pathData, String id, String style) throws Exception {
        writer.writeStartElement("path");
        if (id != null) {
            writer.writeAttribute("id", id);
        }
        writer.writeAttribute("d", pathData);
        if (style != null) {
            writer.writeAttribute("class", style);
        }
        writer.writeEndElement();
    }

    private void renderPathEx(String pathData, String style) throws Exception {
        Map<String, String> attributes = Styles.getAttributes(style);
        writer.writeStartElement("path");
        writer.writeAttribute("d", pathData);
        for (String key : attributes.keySet()) {
            writer.writeAttribute(key, attributes.get(key));
        }
        writer.writeEndElement();
    }

    private void renderPathEx(String pathData, String id, String style) throws Exception {
        Map<String, String> attributes = Styles.getAttributes(style);
        writer.writeStartElement("path");
        writer.writeAttribute("id", id);
        writer.writeAttribute("d", pathData);
        for (String key : attributes.keySet()) {
            writer.writeAttribute(key, attributes.get(key));
        }
        writer.writeEndElement();
    }

    private void renderPathEx(String pathData, String style, Map<String, String> attributes) throws Exception {
        writer.writeStartElement("path");
        writer.writeAttribute("d", pathData);
        if (style != null) {
            Map<String, String> defaultAttributes = Styles.getAttributes(style);
            for (String key : defaultAttributes.keySet()) {
                writer.writeAttribute(key, defaultAttributes.get(key));
            }
        }
        for (String key : attributes.keySet()) {
            writer.writeAttribute(key, attributes.get(key));
        }
        writer.writeEndElement();
    }

    private void renderTextOnPath(String pathID, Double startOffset, String text, String style) throws Exception {

        writer.writeStartElement("text");

        writer.writeStartElement("textPath");
        writer.writeAttribute("xlink:href", "#" + pathID);
        writer.writeAttribute("startOffset", Coords.format(startOffset) + "%");
        if (style != null) {
            writer.writeAttribute("class", style);
        }
        writer.writeCharacters(text);
        writer.writeEndElement();
        /*
        if (startOffset < 5 && text.length() > 1) {
        writer.writeStartElement("textPath");
        writer.writeAttribute("xlink:href", "#" + pathID);
        writer.writeAttribute("startOffset", Coords.format(100 + startOffset) + "%");
        if (style != null) {
        writer.writeAttribute("class", style);
        }
        writer.writeCharacters(text);
        writer.writeEndElement();
        }

        if (startOffset > 95 && text.length() > 1) {
        writer.writeStartElement("textPath");
        writer.writeAttribute("xlink:href", "#" + pathID);
        writer.writeAttribute("startOffset", Coords.format(-100 + startOffset) + "%");
        if (style != null) {
        writer.writeAttribute("class", style);
        }
        writer.writeCharacters(text);
        writer.writeEndElement();
        }
         */

        writer.writeEndElement();
    }

    private void renderTextOnPath(String pathID, Double startOffset, Integer dx, Integer dy, String text, String style) throws Exception {

        writer.writeStartElement("text");

        writer.writeStartElement("textPath");
        writer.writeAttribute("xlink:href", "#" + pathID);
        writer.writeAttribute("startOffset", Coords.format(startOffset) + "%");
        if (style != null) {
            writer.writeAttribute("class", style);
        }
        writer.writeStartElement("tspan");
        writer.writeAttribute("dx", dx + "px");
        writer.writeAttribute("dy", dy + "px");
        writer.writeCharacters(text);
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
    }

    @Deprecated
    private void renderTextOnPathShifted(String pathID, Double startOffset, String text, String style) throws Exception {

        writer.writeStartElement("text");

        writer.writeStartElement("textPath");
        writer.writeAttribute("xlink:href", "#" + pathID);
        writer.writeAttribute("startOffset", Coords.format(startOffset) + "%");
        if (style != null) {
            writer.writeAttribute("class", style);
        }
        writer.writeCharacters(text);
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private Poly createContour(ArrayList<Point2D> coords) throws Exception {
        Poly contour = new PolyDefault();
        for (Point2D coordRaw : coords) {
            Point2D coord = new Point2D.Double();
            Coords.convertWithoutCheck(coordRaw.getX(), coordRaw.getY(), coord);
            contour.add(coord);
        }
        return contour;
    }

    private Poly createClipArea() throws Exception {
        Poly contour = new PolyDefault();
        Double Dec = Settings.latitude > 0 ? Settings.latitude - 90 : Settings.latitude + 90;
        for (Double RA = 0.0; RA <= 24; RA = RA + 0.5) {
            Point2D coord = new Point2D.Double();
            Coords.convertWithoutCheck(RA, Dec, coord);
            contour.add(coord);
        }
        return contour;
    }

    private void renderDefsDialMonthsLabelMajorPath() throws Exception {
        renderPath(renderCircle(0.95), "dialMonthsLabelMajorPath", null);
        renderPath(renderCircle(new Point2D.Double(0, 0), 0.95 * Settings.scale, 0d), "dialMonthsLabelMajorPathShifted", null);
    }

    private void renderDefsDialMonthsLabelMinorPath() throws Exception {
        renderPath(renderCircle(0.92), "dialMonthsLabelMinorPath", null);
        renderPath(renderCircle(new Point2D.Double(0, 0), 0.92 * Settings.scale, 0d), "dialMonthsLabelMinorPathShifted", null);
    }

    private void renderDialMonths() throws Exception {
        String[] localeFragments = options.getLocaleValue().split("\\|");
        Locale locale;
        if (localeFragments.length > 1) {
            locale = new Locale(localeFragments[0], localeFragments[1]);
        } else {
            locale = new Locale(localeFragments[0]);
        }
        DateFormatSymbols symbols = new DateFormatSymbols(locale);
        String[] monthNames = symbols.getMonths();
        Integer[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        Integer daysInYear = 365;

        GregorianCalendar calendar = new GregorianCalendar();
        if (calendar.isLeapYear(Calendar.getInstance().get(Calendar.YEAR))) {
            daysInMonth[1] = 29;
            daysInYear = 366;
        }

        Double sign = Math.signum(Settings.latitude);
        double angleInPercent;

        double angle = 90 - (19 + 60);
        for (int month = 0; month < 12; month++) {
            String monthName = monthNames[month];
            monthName = monthName.substring(0, 1).toUpperCase(locale) + monthName.substring(1);
            angleInPercent = normalizePercent(100d * (sign * (angle + daysInMonth[month] / 2d)) / 360d);
            angle = angle + daysInMonth[month] * 360d / daysInYear;
            // december
            if (month == 11) {
                double percent = normalizePercent(angleInPercent - 50);
                writeGroupStart(null);
                writer.writeAttribute("transform", "rotate(180)");
                renderTextOnPath("dialMonthsLabelMajorPath", percent, monthName, "dialMonthsLabelMajor");
                writeGroupEnd();
            } else {
                renderTextOnPath("dialMonthsLabelMajorPath", angleInPercent, monthName, "dialMonthsLabelMajor");
            }
        }

        angle = 90 - (19 + 60);
        for (int month = 0; month < 12; month++) {
            for (Integer day = 0; day < daysInMonth[month]; day++) {
                angleInPercent = normalizePercent(100d * (sign * angle) / 360d);
                angle = angle + 360d / daysInYear;
                if (day != 0 && day % 5 == 0) {
                    if (month == 11) {
                        double percent = normalizePercent(angleInPercent - 50);
                        writeGroupStart(null);
                        writer.writeAttribute("transform", "rotate(180)");
                        renderTextOnPath("dialMonthsLabelMinorPath", percent, day.toString(), "dialMonthsLabelMinor");
                        writeGroupEnd();
                    } else {
                        renderTextOnPath("dialMonthsLabelMinorPath", angleInPercent, day.toString(), "dialMonthsLabelMinor");
                    }
                }
            }
        }
    }

    private String renderDialMonthsTick(Double angle, Double radius) {
        return "M" + Coords.format(Math.cos(angle) * 0.89 * Settings.scale)
                + " " + Coords.format(-Math.sin(angle) * 0.89 * Settings.scale)
                + "L" + Coords.format(Math.cos(angle) * radius * Settings.scale)
                + " " + Coords.format(-Math.sin(angle) * radius * Settings.scale);
    }

    private void renderDefsDialHoursMarkerMajor() throws Exception {
        Double side = Math.sqrt(3) / 3;
        renderPath("M0 0"
                + "L" + Coords.format(-side * 0.1 * Settings.scale) + " " + Coords.format(0.1 * Settings.scale)
                + "H" + Coords.format(side * 0.1 * Settings.scale) + "Z", "dialHoursMarkerMajor", "dialHoursMarkerMajor");
    }

    private void renderDefsDialHoursMarkerMinor() throws Exception {
        Double side = Math.sqrt(3) / 3;
        renderPath(
                "M0 0"
                + "L" + Coords.format(-side * 0.02 * Settings.scale) + " " + Coords.format(0.02 * Settings.scale)
                + "H" + Coords.format(side * 0.02 * Settings.scale) + "Z", "dialHoursMarkerMinor", "dialHoursMarkerMinor");
    }

    private Element replaceTextElementContent(Element element, String originalText, String newText) {
        Element resultElement = (Element) element.cloneNode(true);
        NodeList textNodes = resultElement.getElementsByTagName("text");
        for (int i = 0; i < textNodes.getLength(); i++) {
            if (textNodes.item(i).getTextContent().contains(originalText)) {
                textNodes.item(i).setTextContent(newText);
            }
        }
        return resultElement;
    }

    private void writeElementContent(Element element) throws Exception {

        XMLEventReader parser = inputFactory.createXMLEventReader(elementToStreamReader(element));
        StartElement startElement;
        Characters characters;
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    startElement = (StartElement) event;
                    writer.writeStartElement(startElement.getName().getLocalPart());
                    writeAttributes(startElement.getAttributes());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    writer.writeEndElement();
                    break;
                case XMLStreamConstants.CHARACTERS:
                    characters = (Characters) event;
                    writer.writeCharacters(characters.getData());
                    break;
            }
        }
        parser.close();
    }

    private void writeStreamContent(InputStream content) throws Exception {

        XMLEventReader parser = inputFactory.createXMLEventReader(content);
        StartElement startElement;
        Characters characters;
        while (parser.hasNext()) {
            XMLEvent event = parser.nextEvent();
            switch (event.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    startElement = (StartElement) event;
                    writer.writeStartElement(startElement.getName().getLocalPart());
                    writeAttributes(startElement.getAttributes());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    writer.writeEndElement();
                    break;
                case XMLStreamConstants.CHARACTERS:
                    characters = (Characters) event;
                    writer.writeCharacters(characters.getData());
                    break;
                /*
                case XMLStreamConstants.CDATA:
                characters = (Characters) event;
                writer.writeCData(characters.getData());
                break;
                 */
            }
        }
        parser.close();
    }

    private void renderDialHoursMarker(Element mark, Double angle, String style) throws Exception {
        String strTranslate = Coords.format(0.9 * Settings.scale);
        writer.writeStartElement("g");
        writer.writeAttribute("class", style);
        writer.writeAttribute("transform", "translate(0,-" + strTranslate + ") rotate(" + angle + ",0," + strTranslate + ")");
        writeElementContent(mark);
        writer.writeEndElement();
    }

    /*
    private void writeGroupAttributes(Double x, Double y, String transform) throws Exception {
    if (x != 0) {
    writer.writeAttribute("x", Coords.format(x));
    }
    if (y != 0) {
    writer.writeAttribute("y", Coords.format(y));
    }
    if (transform != null) {
    writer.writeAttribute("transform", transform);
    }
    }
     */
    private void renderDefsInstance(String id, Double x, Double y, String transform, String style) throws Exception {
        writer.writeStartElement("use");
        if (x != 0) {
            writer.writeAttribute("x", Coords.format(x));
        }
        if (y != 0) {
            writer.writeAttribute("y", Coords.format(y));
        }
        writer.writeAttribute("xlink", "http://www.w3.org/1999/xlink", "href", "#" + id);
        if (transform != null) {
            writer.writeAttribute("transform", transform);
        }
        if (style != null) {
            writer.writeAttribute("class", style);
        }
        writer.writeEndElement();
    }

    private String renderCircle(Double radius) {

        BezierCircle circle = new BezierCircle(radius * Settings.scale);
        return circle.render();
        /*
        Double kappa = 0.5522847498;
        String radiusPositive = Coords.format(radius * Settings.scale);
        String radiusNegative = "-" + radiusPositive;
        String rKappaPositive = Coords.format(radius * kappa * Settings.scale);
        String rKappaNegative = "-" + rKappaPositive;

        return // First quadrant
        "M0 " + radiusNegative
        + // curve to
        "C" + rKappaPositive + " " + radiusNegative + " " + radiusPositive + " " + rKappaNegative + " " + radiusPositive + " 0"
        + // Second
        "C" + radiusPositive + " " + rKappaPositive + " " + rKappaPositive + " " + radiusPositive + " 0 " + radiusPositive
        + // Third
        "C" + rKappaNegative + " " + radiusPositive + " " + radiusNegative + " " + rKappaPositive + " " + radiusNegative + " 0"
        + // Last
        "C" + radiusNegative + " " + rKappaNegative + " " + rKappaNegative + " " + radiusNegative + " 0 " + radiusNegative;
         */
    }

    private String renderCircle(Point2D center, Double radius) {

        BezierCircle circle = new BezierCircle(center, radius);
        return circle.render();
        /*
        Double rKappa = radius * 0.5522847498;
        Double xPlusR = center.getX() + radius;
        Double xMinusR = center.getX() - radius;
        Double yPlusR = center.getY() + radius;
        Double yMinusR = center.getY() - radius;

        return "M" + Coords.format(center.getX()) + " " + Coords.format(yMinusR)
        + "C" + Coords.format(center.getX() + rKappa) + " " + Coords.format(yMinusR) + " " + Coords.format(xPlusR) + " " + Coords.format(center.getY() - rKappa) + " " + Coords.format(xPlusR) + " " + Coords.format(center.getY())
        + "C" + Coords.format(xPlusR) + " " + Coords.format(center.getY() + rKappa) + " " + Coords.format(center.getX() + rKappa) + " " + Coords.format(yPlusR) + " " + Coords.format(center.getX()) + " " + Coords.format(yPlusR)
        + "C" + Coords.format(center.getX() - rKappa) + " " + Coords.format(yPlusR) + " " + Coords.format(xMinusR) + " " + Coords.format(center.getY() + rKappa) + " " + Coords.format(xMinusR) + " " + Coords.format(center.getY())
        + "C" + Coords.format(xMinusR) + " " + Coords.format(center.getY() - rKappa) + " " + Coords.format(center.getX() - rKappa) + " " + Coords.format(yMinusR) + " " + Coords.format(center.getX()) + " " + Coords.format(yMinusR);
         */
    }

    private String renderCircle(Point2D center, Double radius, Double angle) {

        BezierCircle circle = new BezierCircle(center, radius, angle);
        return circle.render();
        /*

        Double rKappa = radius * 0.5522847498;

        Double xPlusR = center.getX() + radius;
        Double xMinusR = center.getX() - radius;
        Double yPlusR = center.getY() + radius;
        Double yMinusR = center.getY() - radius;

        return "M" + Coords.format(center.getX()) + " " + Coords.format(yMinusR)
        + "C" + Coords.format(center.getX() + rKappa) + " " + Coords.format(yMinusR) + " " + Coords.format(xPlusR) + " " + Coords.format(center.getY() - rKappa) + " " + Coords.format(xPlusR) + " " + Coords.format(center.getY())
        + "C" + Coords.format(xPlusR) + " " + Coords.format(center.getY() + rKappa) + " " + Coords.format(center.getX() + rKappa) + " " + Coords.format(yPlusR) + " " + Coords.format(center.getX()) + " " + Coords.format(yPlusR)
        + "C" + Coords.format(center.getX() - rKappa) + " " + Coords.format(yPlusR) + " " + Coords.format(xMinusR) + " " + Coords.format(center.getY() + rKappa) + " " + Coords.format(xMinusR) + " " + Coords.format(center.getY())
        + "C" + Coords.format(xMinusR) + " " + Coords.format(center.getY() - rKappa) + " " + Coords.format(center.getX() - rKappa) + " " + Coords.format(yMinusR) + " " + Coords.format(center.getX()) + " " + Coords.format(yMinusR);
         */
    }

    public String renderCircleForConstellationName(Point2D coord) {
        Double radius = coord.distance(0d, 0d);
        Double angle = 90 + Math.toDegrees(Math.atan2(coord.getY(), coord.getX()));
        BezierCircle circle = new BezierCircle(new Point2D.Double(0d, 0d), radius, angle);
        return circle.render();
    }

    public String renderCircleInv(Point2D center, Double radius) {

        BezierCircle circle = new BezierCircle(center, radius);
        return circle.renderInv();
        /*
        Double rKappa = radius * 0.5522847498;
        Double xPlusR = center.getX() + radius;
        Double xMinusR = center.getX() - radius;
        Double yPlusR = center.getY() + radius;
        Double yMinusR = center.getY() - radius;

        return "M" + Coords.format(center.getX()) + " " + Coords.format(yMinusR)
        + "C" + Coords.format(center.getX() - rKappa) + " " + Coords.format(yMinusR) + " " + Coords.format(xMinusR) + " " + Coords.format(center.getY() - rKappa) + " " + Coords.format(xMinusR) + " " + Coords.format(center.getY())
        + "C" + Coords.format(xMinusR) + " " + Coords.format(center.getY() + rKappa) + " " + Coords.format(center.getX() - rKappa) + " " + Coords.format(yPlusR) + " " + Coords.format(center.getX()) + " " + Coords.format(yPlusR)
        + "C" + Coords.format(center.getX() + rKappa) + " " + Coords.format(yPlusR) + " " + Coords.format(xPlusR) + " " + Coords.format(center.getY() + rKappa) + " " + Coords.format(xPlusR) + " " + Coords.format(center.getY())
        + "C" + Coords.format(xPlusR) + " " + Coords.format(center.getY() - rKappa) + " " + Coords.format(center.getX() + rKappa) + " " + Coords.format(yMinusR) + " " + Coords.format(center.getX()) + " " + Coords.format(yMinusR);
         */
    }

    private double normalizePercent(double percent) {
        double tmp = 100d * (percent / 100d - new Double(percent / 100d).intValue());
        return tmp < 0 ? tmp + 100 : tmp;
    }

    private void findCircle(double ax, double ay, double bx, double by, double cx, double cy, Point2D intersection) {
        // Get the perpendicular bisector of (x1, y1) and (x2, y2)
        double x1, y1, dx1, dy1;
        x1 = (bx + ax) / 2d;
        y1 = (by + ay) / 2d;
        dy1 = bx - ax;
        dx1 = -(by - ay);

        // Get the perpendicular bisector of (x2, y2) and (x3, y3)
        double x2, y2, dx2, dy2;
        x2 = (cx + bx) / 2d;
        y2 = (cy + by) / 2d;
        dy2 = cx - bx;
        dx2 = -(cy - by);

        // See where the lines intersect
        double ox, oy;
        ox = (y1 * dx1 * dx2 + x2 * dx1 * dy2 - x1 * dy1 * dx2 - y2 * dx1 * dx2)
                / (dx1 * dy2 - dy1 * dx2);
        oy = (ox - x1) * dy1 / dx1 + y1;

        intersection.setLocation(ox, oy);
    }

    private Double getRadius(Point2D a, Point2D b) {
        return Math.sqrt(Math.pow(b.getX() - a.getX(), 2) + Math.pow(b.getY() - a.getY(), 2));
    }

    private ArrayList<Point2D.Double> getMapArea() {

        ArrayList<Point2D.Double> mapAreaCoords = new ArrayList<Point2D.Double>();
        Double latitudeInRads = Math.toRadians(Settings.latitude);
        Double Dec;

        for (double RA = 0.0; RA <= 24; RA = RA + 0.2) {
            Point2D.Double coord = new Point2D.Double();

            Dec = Math.toDegrees(Math.atan(Math.cos(RA * Math.PI / 12d) * Math.cos(latitudeInRads) / Math.sin(latitudeInRads)));

            Coords.convertWithoutCheck(RA - 6d, Dec, coord);
            mapAreaCoords.add(coord);
        }
        return mapAreaCoords;
    }

    private ArrayList<CardinalPointInfo> getCardinalPoints() {

        ArrayList<CardinalPointInfo> cardinalPointList = new ArrayList<CardinalPointInfo>();

        double ax, ay, bx, by, cx, cy, dx, dy, sx, sy, radius, delta;
        Point2D intersection = new Point2D.Double();

        for (int i = 0; i <= 105; i = i + 15) {
            int j = i - 10;
            j = (j < 0) ? 120 + j : j;
            CardinalPointInfo cpi = new CardinalPointInfo();

            ax = (mapArea.get(j).getX() + 1) / 2;
            ay = (1 - mapArea.get(j).getY()) / 2;
            bx = (mapArea.get(i).getX() + 1) / 2;
            by = (1 - mapArea.get(i).getY()) / 2;
            cx = (mapArea.get(i + 10).getX() + 1) / 2;
            cy = (1 - mapArea.get(i + 10).getY()) / 2;

            findCircle(ax, ay, bx, by, cx, cy, intersection);
            radius = getRadius(new Point2D.Double(ax, ay), intersection);

            delta = -Math.PI / 2 + Math.atan2(bx - intersection.getX(), by - intersection.getY());

            dx = mapArea.get(i).getX() + Settings.scale * 0.03 * Math.cos(delta);
            dy = mapArea.get(i).getY() + Settings.scale * 0.03 * Math.sin(delta);

            sx = 2 * intersection.getX() - 1;
            sy = -2 * intersection.getY() + 1;

            cpi.setTickStart(mapArea.get(i));
            cpi.setTickEnd(new Point2D.Double(dx, dy));
            cpi.setRadius(2 * radius + Settings.scale * 0.03);
            cpi.setStartOffset(normalizePercent(100d * (Math.toDegrees(delta + Math.PI / 2d)) / 360d));
            cpi.setCenter(new Point2D.Double(sx, sy));

            cardinalPointList.add(cpi);
        }
        return cardinalPointList;
    }

    /*
     * Convert a w3c dom node to a InputStream
     */
    private InputStream elementToInputStream(Element element) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Result outputTarget = new StreamResult(outputStream);
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.transform(new DOMSource(element), outputTarget);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private XMLStreamReader elementToStreamReader(Element element) throws Exception {
        DOMSource domSource = new DOMSource(element);
        ReaderConfig config = ReaderConfig.createFullDefaults();
        DOMWrappingReader wrappingReader = WstxDOMWrappingReader.createFrom(domSource, config);
        return wrappingReader;
    }
}
