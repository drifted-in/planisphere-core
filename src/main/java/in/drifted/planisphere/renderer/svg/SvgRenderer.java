package in.drifted.planisphere.renderer.svg;

import com.seisw.util.geom.Poly;
import com.seisw.util.geom.PolyDefault;
import in.drifted.planisphere.Options;
import in.drifted.planisphere.Settings;
import in.drifted.planisphere.model.CardinalPoint;
import in.drifted.planisphere.model.ConstellationName;
import in.drifted.planisphere.model.MilkyWay;
import in.drifted.planisphere.model.Star;
import in.drifted.planisphere.util.CacheHandler;
import in.drifted.planisphere.util.CoordUtil;
import in.drifted.planisphere.util.FontManager;
import in.drifted.planisphere.util.LocalizationUtil;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class SvgRenderer {

    private transient XMLInputFactory inputFactory;
    private transient XMLOutputFactory outputFactory;
    private transient XMLStreamWriter writer;
    private transient DocumentBuilder documentBuilder;
    private transient CacheHandler cacheHandler;
    private final List<Point2D> mapAreaPointList = new LinkedList<>();
    private final List<CardinalPoint> cardinalPointList = new LinkedList<>();
    private LocalizationUtil localizationUtil;
    private Double latitudeFixed;
    private Double latitude;
    private Double scaleFixed; // cover and main layout
    private Double scale; // map content
    private Boolean isDoubleSided;
    private Integer doubleSidedSign;

    public SvgRenderer() {
        initRenderer();
    }

    private void initRenderer() {
        inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
        outputFactory = XMLOutputFactory.newInstance();
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            // LOGGER
        }
        cacheHandler = CacheHandler.getInstance();
    }

    public void createFromTemplate(String resourcePath, OutputStream output, Options options) throws XMLStreamException, IOException {
        writer = outputFactory.createXMLStreamWriter(output);
        try (InputStream input = getClass().getResourceAsStream(Settings.RESOURCE_BASE_PATH + "templates/core/" + resourcePath)) {
            createFromTemplate(input, options);
        }
        writer.flush();
        writer.close();
    }

    private void createFromTemplate(InputStream input, Options options) throws XMLStreamException, IOException {

        latitudeFixed = options.getLatitude();
        isDoubleSided = options.getDoubleSided();
        doubleSidedSign = (int) (options.getDoubleSidedSign() * Math.signum(latitudeFixed)) ;
        latitude = isDoubleSided ? doubleSidedSign * 65.0 : latitudeFixed;
        Locale locale = options.getCurrentLocale();
        localizationUtil = new LocalizationUtil(locale);
        FontManager fontManager = new FontManager(locale);
        XMLEventReader parser = inputFactory.createXMLEventReader(input);

        Map<String, Element> paramMap = new HashMap<>();

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
                        switch (id) {
                            case "mapView":
                                renderDefsMapView();
                                break;
                            case "monthsView":
                                renderDefsMonthsView();
                                break;
                            case "dialHoursMarkerMajorSingle":
                            case "dialHoursMarkerMajorDouble":
                            case "dialHoursMarkerMinorSingle":
                            case "dialHoursMarkerMinorDouble":
                                paramMap.put(id, getParamElement(parser, event));
                                break;
                            case "dialMonthsLabelMajorPath":
                                renderDefsDialMonthsLabelMajorPath();
                                break;
                            case "dialMonthsLabelMinorPath":
                                renderDefsDialMonthsLabelMinorPath();
                                break;
                            /*
                             case "cardinalPointLabelPaths":
                             writeGroupStart("cardinalPointLabelPaths");
                             renderDefsCardinalPointLabelPaths();
                             writeGroupEnd();
                             break;
                             */
                            case "coordLabelPaths":
                                writeGroupStart("coordLabelPaths");
                                renderDefsCoordLabelPaths();
                                writeGroupEnd();
                                break;
                            case "wheel":
                                writeGroupStart("wheel");
                                renderMapBackground();
                                renderDialMonths(locale);
                                renderDialMonthsTicks();
                                if (options.getMilkyWay()) {
                                    renderMilkyWay();
                                }
                                if (options.getCoordsRADec()) {
                                    renderCoords();
                                    renderCoordLabels();
                                }
                                if (options.getEcliptic()) {
                                    renderEcliptic();
                                }
                                if (options.getConstellationBoundaries()) {
                                    renderConstellationBoundaries();
                                }
                                if (options.getConstellationLines()) {
                                    renderConstellationLines();
                                }
                                renderStars();
                                if (options.getConstellationLabels()) {
                                    renderConstellationNames(options.getConstellationLabelsOptions());
                                }
                                writeGroupEnd();
                                break;
                            case "scales":
                                writeGroupStart("scales");
                                renderDialHours(paramMap, options.getDayLightSavingTimeScale());
                                renderCardinalPointsTicks();
                                renderCardinalPointsLabels();
                                writeGroupEnd();
                                break;
                            case "monthsViewBorder":
                                writeGroupStart("monthsViewBorder");
                                renderMonthsViewBorder();
                                writeGroupEnd();
                                break;
                            case "mapViewBorder":
                                writeGroupStart("mapViewBorder");
                                renderMapViewBorder();
                                writeGroupEnd();
                                break;
                            case "spacer":
                                renderSpacer();
                                break;
                            case "cover":
                                renderCover();
                                renderBendLine();
                                renderPinMark(1);
                                break;
                            case "pinMark":
                                renderPinMark(0);
                                break;
                            case "guide":
                            case "worldmap":
                            case "buttonFlip":
                            case "buttonSettings":
                            case "buttonExport":
                            case "buttonMoreInfo":
                                if (id.equals("buttonFlip") && !isDoubleSided) {
                                // ignore
                            } else {
                                writer.writeStartElement("g");
                                if (id.contains("button")) {
                                    writer.writeStartElement("title");
                                    writer.writeCharacters(localizationUtil.getValue(id));
                                    writer.writeEndElement();
                                }
                                renderSymbol(id);
                                writer.writeEndElement();
                            }
                                break;
                            case "latitudeMarker":
                                writer.writeStartElement(elementName);
                                Iterator<Attribute> it = startElement.getAttributes();
                                while (it.hasNext()) {
                                    Attribute a = it.next();
                                    QName attr = a.getName();
                                    if (attr.getLocalPart().equals("y")) {
                                        Double range = Double.valueOf(a.getValue());
                                        Double ratio = range / 180;
                                        Double y = ratio * (Math.abs(latitude - 90) - 90);
                                        writer.writeAttribute("y", y.toString());
                                    } else {
                                        writer.writeAttribute(attr.getPrefix(), attr.getNamespaceURI(), attr.getLocalPart(), a.getValue());
                                    }
                                }
                                writer.writeEndElement();
                                break;
                            default:
                                isUsed = true;
                                writer.writeStartElement(elementName);
                                writeAttributes(startElement.getAttributes());
                                break;
                        }
                    } else if (elementName.equals("svg")) {
                        String[] values = startElement.getAttributeByName(new QName("viewBox")).getValue().split(" ");
                        scaleFixed = Math.min(Double.valueOf(values[2]) / 2, Double.valueOf(values[3]) / 2);
                        // the 'scale' for a map is set to maximum by default, but if an extra space on cover is needed, the map can be smaller
                        scale = 1.0 * scaleFixed;
                        createMapAreaPointList();
                        createCardinalPointList();
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
                        writer.writeCharacters(localizationUtil.translate(text, latitudeFixed));
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
                        InputStream is = getClass().getResourceAsStream(Settings.RESOURCE_BASE_PATH + "templates/resources/js/mouseEvents.js");

                        try (Reader reader = new InputStreamReader(is, "UTF-8")) {
                            char[] buffer = new char[1024];
                            Integer n;
                            while ((n = reader.read(buffer)) >= 0) {
                                cdata.write(buffer, 0, n);
                            }
                        }
                        writer.writeCData(cdata.toString());
                        writer.writeEndElement();
                    }
                }
                    break;

                default:
            }
        }
        parser.close();
        writer.writeEndDocument();
    }

    private Element getParamElement(XMLEventReader parser, XMLEvent event) throws XMLStreamException {
        Element paramElement = documentBuilder.newDocument().createElement("g");
        XMLEventWriter xmlWriter = outputFactory.createXMLEventWriter(new DOMResult(paramElement));
        Integer level = 1;
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

    private void writeAttributes(Iterator attributes) throws XMLStreamException {
        while (attributes.hasNext()) {
            Attribute attr = (Attribute) attributes.next();
            writer.writeAttribute(attr.getName().getPrefix(), attr.getName().getNamespaceURI(), attr.getName().getLocalPart(), attr.getValue());
        }
    }

    private void writeNamespaces(Iterator namespaces) throws XMLStreamException {
        while (namespaces.hasNext()) {
            Namespace ns = (Namespace) namespaces.next();
            writer.writeNamespace(ns.getPrefix(), ns.getNamespaceURI());
        }
    }

    private void writeGroupStart(String id) throws XMLStreamException {
        writer.writeStartElement("g");
        if (id != null) {
            writer.writeAttribute("id", id);
        }
    }

    private void writeGroupEnd() throws XMLStreamException {
        writer.writeEndElement();
    }

    private void renderMapBackground() throws XMLStreamException {
        renderPath(renderCircle(1.0 * scaleFixed), null, "mapBackground");
    }

    private void renderStars() throws XMLStreamException, IOException {

        Point2D coord = new Point2D.Double();
        Map<Integer, StringBuilder> pathMap = new HashMap<>();

        for (Star star : cacheHandler.getStarList()) {
            if (CoordUtil.convert(star.getRA(), star.getDec(), coord, latitude, scale)) {
                String coordsChunk = CoordUtil.getCoordsChunk(coord);

                StringBuilder path = new StringBuilder();
                path.append("M");
                path.append(coordsChunk);
                path.append("L");
                path.append(coordsChunk);

                Integer magnitudeIndex = Math.round(star.getMag().floatValue() + 1);
                if (pathMap.containsKey(magnitudeIndex)) {
                    pathMap.get(magnitudeIndex).append(path);
                } else {
                    pathMap.put(magnitudeIndex, path);
                }
            }
        }
        for (Map.Entry<Integer, StringBuilder> entry : pathMap.entrySet()) {
            renderPath(entry.getValue().toString(), null, "star level" + entry.getKey());
        }
    }

    private void renderConstellationBoundaries() throws XMLStreamException, IOException {
        Point2D coordStart = new Point2D.Double();
        Point2D coordEnd = new Point2D.Double();
        StringBuilder pathData = new StringBuilder();
        for (Iterator<Point2D> i = cacheHandler.getConstellationBoundaryList().iterator(); i.hasNext();) {
            Point2D coordStartRaw = i.next();
            if (CoordUtil.convert(coordStartRaw.getX(), coordStartRaw.getY(), coordStart, latitude, scale)) {
                Point2D coordEndRaw = i.next();
                if (CoordUtil.convert(coordEndRaw.getX(), coordEndRaw.getY(), coordEnd, latitude, scale)) {
                    if ((Math.abs(coordStartRaw.getY() - coordEndRaw.getY()) < 0.7) && (Math.abs(coordStartRaw.getX() - coordEndRaw.getX()) > 0)
                            || (Math.abs(coordStartRaw.getY()) > 86)) {

                        Integer bDiv = (int) (5 + (15 * ((latitude > 0) ? (90 - coordEndRaw.getY()) : (90 + coordEndRaw.getY())) / 90));

                        Double startRA = coordStartRaw.getX();
                        Double endRA = coordEndRaw.getX();
                        Double incRA = endRA - startRA;

                        if (incRA > 12) {
                            startRA = coordEndRaw.getX();
                            incRA = 24 - incRA;
                        }
                        if (incRA < -12) {
                            incRA = incRA + 24;
                        }

                        Double incDec = (coordEndRaw.getY() - coordStartRaw.getY()) / bDiv;
                        Double Dec = coordStartRaw.getY();
                        Point2D coordTemp = new Point2D.Double();

                        for (Integer j = 0; j <= bDiv; j++) {
                            CoordUtil.convert(startRA + j * incRA / bDiv, Dec, coordTemp, latitude, scale);
                            if (j == 0) {
                                pathData.append("M");

                            } else {
                                pathData.append("L");
                            }
                            pathData.append(CoordUtil.getCoordsChunk(coordTemp));
                            Dec = Dec + incDec;
                        }
                    } else {
                        pathData.append("M");
                        pathData.append(CoordUtil.getCoordsChunk(coordStart));
                        pathData.append("L");
                        pathData.append(CoordUtil.getCoordsChunk(coordEnd));
                    }
                }
            } else {
                i.next();
            }
        }
        renderPath(pathData.toString(), null, "constellationBoundaries");
    }

    private void renderConstellationLines() throws XMLStreamException, IOException {
        Point2D coordStart = new Point2D.Double();
        Point2D coordEnd = new Point2D.Double();
        StringBuilder pathData = new StringBuilder();
        for (Iterator<Point2D> i = cacheHandler.getConstellationLineList().iterator(); i.hasNext();) {
            Point2D coordStartRaw = i.next();
            if (CoordUtil.convert(coordStartRaw.getX(), coordStartRaw.getY(), coordStart, latitude, scale)) {
                Point2D coordEndRaw = i.next();
                if (CoordUtil.convert(coordEndRaw.getX(), coordEndRaw.getY(), coordEnd, latitude, scale)) {
                    pathData.append("M");
                    pathData.append(CoordUtil.getCoordsChunk(coordStart));
                    pathData.append("L");
                    pathData.append(CoordUtil.getCoordsChunk(coordEnd));
                }
            } else {
                i.next();
            }
        }
        renderPath(pathData.toString(), null, "constellationLines");
    }

    private void renderConstellationNames(Integer mode) throws XMLStreamException, IOException {
        Point2D coordRaw;
        Point2D coord = new Point2D.Double();
        for (ConstellationName constellationName : cacheHandler.getConstellationNameList()) {
            String name = "";
            switch (mode) {
                case 0:
                    name = localizationUtil.getValue(constellationName.getId());
                    break;
                case 1:
                    name = constellationName.getLatin();
                    break;
                case 2:
                    name = constellationName.getAbbreviation();
                    break;
                default:
            }
            //System.out.println(name + " : " + constellationName.getId());
            coordRaw = constellationName.getCoord();
            if (CoordUtil.convert(coordRaw.getX(), coordRaw.getY(), coord, latitude, scale)) {
                String id = "con" + constellationName.getAbbreviation();
                renderPath(renderCircleForConstellationName(coord), id, "constellationNamesPath");
                renderTextOnPath(id, 50d, name, "constellationNames");
            }
        }
    }

    private void renderEcliptic() throws XMLStreamException {

        Double epsilon = Math.toRadians(23.44);
        Double lambda;
        Double RA;
        Double Dec;
        Boolean flag = false;
        String coordsChunk = "";
        StringBuilder pathData = new StringBuilder();

        Point2D coord = new Point2D.Double();

        for (Integer i = 0; i <= 360; i = i + 2) {
            lambda = Math.toRadians(i);
            RA = (Math.atan2(Math.sin(lambda) * Math.cos(epsilon), Math.cos(lambda))) * 12 / Math.PI;
            Dec = Math.toDegrees(Math.asin(Math.sin(epsilon) * Math.sin(lambda)));
            if (CoordUtil.convert(RA, Dec, coord, latitude, scale)) {
                if (!flag) {
                    coordsChunk = CoordUtil.getCoordsChunk(coord);
                    flag = true;
                } else {
                    pathData.append("M");
                    pathData.append(coordsChunk);
                    pathData.append("L");
                    coordsChunk = CoordUtil.getCoordsChunk(coord);
                    pathData.append(coordsChunk);
                }
            } else {
                flag = false;
            }
        }
        renderPath(pathData.toString(), null, "ecliptic");
    }

    private void renderCoords() throws XMLStreamException {

        StringBuilder path = new StringBuilder();
        Point2D coord = new Point2D.Double();
        Double sign = Math.signum(latitude);

        // declination circle (it cannot be rendered using circles because of the rotation at vernal point)        
        for (Double Dec = 60.0; Dec >= Math.abs(latitude) - 90.0; Dec = Dec - 30.0) {
            for (Double RA = 0.0; RA <= 24.0; RA = RA + 0.5) {
                CoordUtil.convert(RA, sign * Dec, coord, latitude, scale);
                if (RA == 0.0) {
                    path.append("M");
                } else {
                    path.append("L");
                }
                path.append(CoordUtil.getCoordsChunk(coord));
            }
        }

        // RA        
        Integer start;
        for (Integer RA = 0; RA < 24; RA++) {
            switch (RA % 6) {
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

            CoordUtil.convert(RA.doubleValue(), sign * start, coord, latitude, scale);
            path.append("M");
            path.append(CoordUtil.getCoordsChunk(coord));
            CoordUtil.convert(RA.doubleValue(), latitude - sign * 90, coord, latitude, scale);
            path.append("L");
            path.append(CoordUtil.getCoordsChunk(coord));
        }
        renderPath(path.toString(), null, "coords");
    }

    private void renderCoordLabels() throws XMLStreamException {

        Double sign = Math.signum(latitude);

        for (int RA = 1; RA < 24; RA++) {
            Integer finalRA = (latitude >= 0) ? RA : 24 - RA;
            renderTextOnPath("coordLabelPath00", 100 - (RA * 100.0 / 24), finalRA + "h", "coordLabelRa");
        }
        renderTextOnPath("coordLabelPath00", 0.0, "0h", "coordLabelRa");

        for (Double Dec = 60.0; Dec >= Math.abs(latitude) - 90.0; Dec = Dec - 30.0) {
            Double finalDec = sign * Dec;
            String pathId = "coordLabelPath" + Dec.intValue();
            String strSign = (finalDec > 0) ? "+" : "";
            renderTextOnPath(pathId, 100.0, strSign + finalDec.intValue() + "°", "coordLabelDec");
            for (Integer i = 1; i < 4; i++) {
                renderTextOnPath(pathId, i * 25.0, strSign + finalDec.intValue() + "°", "coordLabelDec");
            }
        }
    }

    private void renderDefsCoordLabelPaths() throws XMLStreamException {

        Point2D coord = new Point2D.Double();
        Double sign = Math.signum(latitude);

        for (Double Dec = 60.0; Dec >= Math.abs(latitude) - 90.0; Dec = Dec - 30.0) {
            StringBuilder path = new StringBuilder("M");
            for (Double RA = 24.0; RA >= 0.0; RA = RA - 0.5) {
                if (RA < 24.0) {
                    path.append("L");
                }
                Double finalRA = (latitude >= 0) ? RA : 24.0 - RA;
                CoordUtil.convert(finalRA, sign * Dec, coord, latitude, scale);
                path.append(CoordUtil.getCoordsChunk(coord));
            }
            renderPath(path.toString(), "coordLabelPath" + Dec.intValue(), null);
        }

        // slightly shifted for RA labels
        StringBuilder path = new StringBuilder("M");
        for (Double RA = 24.0; RA >= 0.0; RA = RA - 0.5) {
            if (RA < 24.0) {
                path.append("L");
            }
            Double finalRA = (latitude >= 0) ? RA : 24.0 - RA;
            CoordUtil.convert(finalRA, -sign * 3.0, coord, latitude, scale);
            path.append(CoordUtil.getCoordsChunk(coord));
        }
        renderPath(path.toString(), "coordLabelPath00", null);

    }

    private void renderDefsMapView() throws XMLStreamException {
        StringBuilder path = new StringBuilder();
        Iterator iter = mapAreaPointList.iterator();
        path.append("M");
        path.append(CoordUtil.getCoordsChunk((Point2D) iter.next()));
        while (iter.hasNext()) {
            path.append("L");
            path.append(CoordUtil.getCoordsChunk((Point2D) iter.next()));
        }
        path.append("z");
        renderPath(path.toString(), "mapView", null);
    }

    private void renderMapViewBorder() throws XMLStreamException {
        renderDefsInstance("mapView", 0d, 0d, null, "mapViewBorder");
    }

    private void renderCardinalPointsTicks() throws XMLStreamException {
        StringBuilder path = new StringBuilder();
        for (CardinalPoint cardinalPoint : cardinalPointList) {
            if (cardinalPoint.getLabel() != null) {
                path.append("M");
                path.append(CoordUtil.getCoordsChunk(cardinalPoint.getTickStart()));
                path.append("L");
                path.append(CoordUtil.getCoordsChunk(cardinalPoint.getTickEnd()));
            }
        }
        renderPath(path.toString(), null, "cardinalPointTick");
    }

    private void renderCardinalPointsLabels() throws XMLStreamException {

        Double gap = scale * 0.006;
        Double tickLength = scale * 0.03;
        Double letterHeight = scale * 0.032;
        Double textLineLength = scale * 0.25;
        Double outerOffset = tickLength + gap;
        Double innerOffset = outerOffset + letterHeight;

        if (isDoubleSided) {

            int index = 0;
            for (CardinalPoint cardinalPoint : cardinalPointList) {
                if (cardinalPoint.getLabel() != null) {

                    String pathId = "cid" + index;

                    if (latitudeFixed == 0) {
                        Point2D point = cardinalPoint.getTickStart();
                        Point2D center = new Point2D.Double(point.getX(), point.getY() + innerOffset);
                        renderPath(renderLineHorizontal(center, textLineLength), pathId, "invisible");
                        renderTextOnPath(pathId, 50.0, cardinalPoint.getLabel(), "cardinalPointLabel");

                    } else {
                        if (latitudeFixed * doubleSidedSign > 0) {
                            renderPath(renderCircleInv(cardinalPoint.getCenter(), cardinalPoint.getRadius() + innerOffset), pathId, "invisible");
                            renderTextOnPath(pathId, 100.0 - cardinalPoint.getStartOffset(), cardinalPoint.getLabel(), "cardinalPointLabel");
                        } else {
                            renderPath(renderCircle(cardinalPoint.getCenter(), cardinalPoint.getRadius() - innerOffset), pathId, "invisible");
                            if (index == 2) {
                                writeGroupStart(null);
                                writer.writeAttribute("transform", "rotate(180, " + cardinalPoint.getCenter().getX() + "," + cardinalPoint.getCenter().getY() + ")");
                                renderTextOnPath(pathId, 50.0, cardinalPoint.getLabel(), "cardinalPointLabel");
                                writeGroupEnd();
                            } else {
                                renderTextOnPath(pathId, normalizePercent(cardinalPoint.getStartOffset() - 50.0), cardinalPoint.getLabel(), "cardinalPointLabel");
                            }
                        }
                    }
                }
                index++;
            }

        } else {

            int index = 0;

            for (CardinalPoint cardinalPoint : cardinalPointList) {

                if (cardinalPoint.getLabel() != null) {

                    String pathId = "cid" + index;
                    Boolean isInner = index > 0 && index < 4;

                    if (isInner) {
                        renderPath(renderCircleInv(cardinalPoint.getCenter(), cardinalPoint.getRadius() + innerOffset), pathId, "invisible");
                        renderTextOnPath(pathId, 100.0 - cardinalPoint.getStartOffset(), cardinalPoint.getLabel(), "cardinalPointLabel");

                    } else {
                        renderPath(renderCircle(cardinalPoint.getCenter(), cardinalPoint.getRadius() + outerOffset), pathId, "invisible");
                        if (index == 6) {
                            writeGroupStart(null);
                            writer.writeAttribute("transform", "rotate(180, " + cardinalPoint.getCenter().getX() + "," + cardinalPoint.getCenter().getY() + ")");
                            renderTextOnPath(pathId, 50.0, cardinalPoint.getLabel(), "cardinalPointLabel");
                            writeGroupEnd();
                        } else {
                            renderTextOnPath(pathId, cardinalPoint.getStartOffset(), cardinalPoint.getLabel(), "cardinalPointLabel");
                        }
                    }
                }
                index++;
            }
        }
    }

    private void renderDialHours(Map<String, Element> paramMap, Boolean isDayLightSavingTimeScale) throws XMLStreamException {

        Element markMajor = isDoubleSided ? paramMap.get("dialHoursMarkerMajorDouble") : paramMap.get("dialHoursMarkerMajorSingle");
        Element markMinor = isDoubleSided ? paramMap.get("dialHoursMarkerMinorDouble") : paramMap.get("dialHoursMarkerMinorSingle");

        Integer rangeMajor = 8;
        Integer rangeMinor = 7;
        Double latitudeAbs = Math.abs(latitude);
        Double shiftAngle = isDoubleSided ? 180.0 : 0.0;

        if (latitudeAbs > 76) {
            rangeMajor = 5;
        } else if (latitudeAbs > 70) {
            rangeMajor = 6;
            rangeMinor = 5;
        } else if (latitudeAbs > 60) {
            rangeMajor = 7;
            rangeMinor = 5;
        }

        // default labels
        Integer hour;

        for (Integer i = -rangeMajor; i <= rangeMajor; i++) {
            hour = latitude < 0 ? i : -i;
            if (hour < 0) {
                hour = hour + 24;
            }
            renderDialHoursMarker(replaceTextElementContent(markMajor, "#", hour.toString()), i * 15.0 + shiftAngle, "dialHoursMarkerMajor");
        }

        // summer time labels
        if (isDayLightSavingTimeScale && latitudeAbs <= 75) {
            for (Integer i = -rangeMinor; i <= rangeMinor; i++) {
                hour = latitude < 0 ? i + 1 : -i + 1;
                if (hour < 0) {
                    hour = hour + 24;
                }
                if (hour > 23) {
                    hour = hour - 24;
                }
                renderDialHoursMarker(replaceTextElementContent(markMinor, "#", hour.toString()), i * 15.0 + shiftAngle, "dialHoursMarkerMinor");
            }
        }

        for (Double i = 112.5; i >= -120; i = i - 15) {
            String strTranslate = CoordUtil.format(0.9 * scaleFixed);
            renderDefsInstance("dialHoursMarkerHalf", 0d, 0d, "translate(0,-" + strTranslate + ") rotate(" + (i + shiftAngle) + ",0," + strTranslate + ")", null);
        }
    }

    private void renderSymbol(String id) throws XMLStreamException, IOException {
        try (InputStream is = getClass().getResourceAsStream(Settings.RESOURCE_BASE_PATH + "templates/resources/symbols/" + id + ".svg")) {
            writeStreamContent(is);
        }
    }

    private void renderDefsMonthsView() throws XMLStreamException {

        Double angle = Math.toRadians(30d);
        Double x1 = Math.cos(angle) * 0.9 * scaleFixed;
        Double y1 = Math.sin(angle) * 0.9 * scaleFixed;
        String sweep1 = "1";
        Double x2 = Math.cos(angle) * scaleFixed;
        Double y2 = Math.sin(angle) * scaleFixed;
        String sweep2 = "0";

        if (isDoubleSided) {
            y1 = -y1;
            y2 = -y2;
            sweep1 = "0";
            sweep2 = "1";
        }

        StringBuilder pathData = new StringBuilder();
        // inner arc
        pathData.append("M");
        pathData.append(CoordUtil.format(-x1));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y1));
        pathData.append("A");
        // rx
        pathData.append(CoordUtil.format(0.9 * scaleFixed));
        pathData.append(" ");
        // ry
        pathData.append(CoordUtil.format(0.9 * scaleFixed));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 1 ");
        pathData.append(sweep1);
        pathData.append(" ");
        pathData.append(CoordUtil.format(x1));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y1));

        pathData.append("L");
        pathData.append(CoordUtil.format(x2));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y2));

        // outer arc
        pathData.append("A");
        pathData.append(CoordUtil.format(scaleFixed));
        pathData.append(" ");
        pathData.append(CoordUtil.format(scaleFixed));
        pathData.append(" 0 1 ");
        pathData.append(sweep2);
        pathData.append(" ");
        pathData.append(CoordUtil.format(-x2));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y2));

        pathData.append("Z");

        renderPath(pathData.toString(), "monthsView", "null");
    }

    private void renderMonthsViewBorder() throws XMLStreamException {
        renderDefsInstance("monthsView", 0d, 0d, null, "monthsViewBorder");
    }

    private void renderDialMonthsTicks() throws XMLStreamException {

        Integer[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        Integer daysInYear = 365;

        GregorianCalendar calendar = new GregorianCalendar();
        if (calendar.isLeapYear(Calendar.getInstance().get(Calendar.YEAR))) {
            daysInMonth[1] = 29;
            daysInYear = 366;
        }

        Double sign = isDoubleSided ? doubleSidedSign : Math.signum(latitude);
        Double angleInRads;

        Double dayIncrement = 360.0 / daysInYear;
        Double startAngle = (daysInMonth[0] + daysInMonth[1] + 21) * dayIncrement;

        Double angle = startAngle + (sign < 0 ? 180 : 0);

        for (int month = 0; month < 12; month++) {

            StringBuilder pathData = new StringBuilder();
            for (int day = 0; day < daysInMonth[month]; day++) {
                angleInRads = Math.toRadians(sign * angle);
                angle = angle - dayIncrement;
                switch (day) {
                    case 0:
                        pathData.append(renderDialMonthsTick(angleInRads, 1.0));
                        break;
                    case 5:
                    case 10:
                    case 15:
                    case 20:
                    case 25:
                    case 30:
                        pathData.append(renderDialMonthsTick(angleInRads, 0.914));
                        break;
                    default:
                        pathData.append(renderDialMonthsTick(angleInRads, 0.908));
                }
            }
            renderPath(pathData.toString(), null, "dialMonthsTick");
        }
    }

    private void renderMilkyWay() throws XMLStreamException, IOException {

        MilkyWay milkyWay = cacheHandler.getMilkyWay();
        Poly sourcePolygon = new PolyDefault();
        Poly destPolygon = new PolyDefault();

        destPolygon.add(createClipArea());

        sourcePolygon.add(createContour(milkyWay.getDarkNorth()));
        sourcePolygon.add(createContour(milkyWay.getDarkSouth()));

        renderMilkyWay(sourcePolygon.intersection(destPolygon), "milkyWayBright");

        sourcePolygon.add(createContour(milkyWay.getBrightNorth()));
        sourcePolygon.add(createContour(milkyWay.getBrightSouth()));

        renderMilkyWay(sourcePolygon.intersection(destPolygon), "milkyWayDark");

    }

    private void renderMilkyWay(Poly polygon, String style) throws XMLStreamException {
        StringBuilder pathData = new StringBuilder();
        for (int i = 0; i < polygon.getNumInnerPoly(); i++) {
            Poly contour = polygon.getInnerPoly(i);
            for (int j = 0; j < contour.getNumPoints(); j++) {
                if (j == 0) {
                    pathData.append("M");
                } else {
                    pathData.append("L");
                }
                pathData.append(CoordUtil.format(contour.getX(j)));
                pathData.append(" ");
                pathData.append(CoordUtil.format(contour.getY(j)));
            }
        }
        renderPath(pathData.toString(), null, style);
    }

    private void renderSpacer() throws XMLStreamException {
        StringBuilder pathData = new StringBuilder();
        Double angle = Math.toRadians(210d);
        Double ratio = 1.03 * scaleFixed;
        String strRadius = CoordUtil.format(ratio);

        // main arc
        pathData.append("M");
        pathData.append(CoordUtil.format(Math.cos(angle) * ratio));
        pathData.append(" ");
        pathData.append(CoordUtil.format(-Math.sin(angle) * ratio));
        pathData.append("A");
        // rx
        pathData.append(strRadius);
        pathData.append(" ");
        // ry
        pathData.append(strRadius);
        // rotation, large arc flag, sweep flag
        angle = Math.toRadians(330d);
        pathData.append(" 0 0 0 ");
        pathData.append(CoordUtil.format(Math.cos(angle) * ratio));
        pathData.append(" ");
        pathData.append(CoordUtil.format(-Math.sin(angle) * ratio));

        List<Point2D.Double> coords = new ArrayList<>();
        Double dy = Math.tan(Math.toRadians(30d)) * scaleFixed;
        coords.add(new Point2D.Double(scaleFixed, dy));
        coords.add(new Point2D.Double(scaleFixed, 1.12 * scaleFixed));
        coords.add(new Point2D.Double(-scaleFixed, 1.12 * scaleFixed));
        coords.add(new Point2D.Double(-scaleFixed, dy));

        renderPath(pathData + getPathData(coords, true), "spacer", null);
    }

    private void renderCover() throws XMLStreamException {
        StringBuilder pathData = new StringBuilder();
        Double x1 = Math.cos(Math.toRadians(30d)) * 0.9 * scaleFixed;
        Double y1 = Math.sin(Math.toRadians(30d)) * 0.9 * scaleFixed;
        // main arc
        pathData.append("M");
        pathData.append(CoordUtil.format(-x1));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y1));
        pathData.append("A");
        // rx
        pathData.append(CoordUtil.format(0.9 * scaleFixed));
        pathData.append(" ");
        // ry
        pathData.append(CoordUtil.format(0.9 * scaleFixed));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 1 1 ");
        pathData.append(CoordUtil.format(x1));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y1));

        // joiners
        Double y2 = Math.tan(Math.toRadians(30d)) * scaleFixed;
        Double height = 1.12 * scaleFixed - y2;
        Double y3 = y2 + 2 * height;
        Double y4 = y3 + (y2 - y1);
        Double x5 = Math.cos(Math.toRadians(340d)) * 0.9 * scaleFixed;
        Double y5 = 2.24 * scaleFixed + Math.sin(Math.toRadians(20d)) * 0.9 * scaleFixed;

        // right side
        pathData.append("L");
        pathData.append(CoordUtil.format(scaleFixed));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y2));
        pathData.append("L");
        pathData.append(CoordUtil.format(scaleFixed));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y3));
        pathData.append("L");
        pathData.append(CoordUtil.format(x1));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y4));
        pathData.append("A");
        // rx
        pathData.append(CoordUtil.format(0.9 * scaleFixed));
        pathData.append(" ");
        // ry
        pathData.append(CoordUtil.format(0.9 * scaleFixed));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 0 1 ");
        pathData.append(CoordUtil.format(x5));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y5));

        // left side
        pathData.append("L");
        pathData.append(CoordUtil.format(-x5));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y5));
        pathData.append("A");
        // rx
        pathData.append(CoordUtil.format(0.9 * scaleFixed));
        pathData.append(" ");
        // ry
        pathData.append(CoordUtil.format(0.9 * scaleFixed));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 0 1 ");
        pathData.append(CoordUtil.format(-x1));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y4));
        pathData.append("L");
        pathData.append(CoordUtil.format(-scaleFixed));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y3));
        pathData.append("L");
        pathData.append(CoordUtil.format(-scaleFixed));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y2));
        pathData.append("L");
        pathData.append(CoordUtil.format(-x1));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y1));
        pathData.append("Z");

        renderPath(pathData.toString(), "cover", "cover");

    }

    private void renderBendLine() throws XMLStreamException {
        StringBuilder pathData = new StringBuilder();
        Double y = 1.12 * scaleFixed;

        pathData.append("M");
        pathData.append(CoordUtil.format(-scaleFixed));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y));
        pathData.append("L");
        pathData.append(CoordUtil.format(scaleFixed));
        pathData.append(" ");
        pathData.append(CoordUtil.format(y));

        renderPath(pathData.toString(), "bendLine", "bendLine");
    }

    private void renderPinMark(Integer mode) throws XMLStreamException {
        StringBuilder pathData = new StringBuilder();
        Double size = 0.02 * scaleFixed;
        Double dy = 0d;
        if (mode > 0) {
            dy = 2.24 * scaleFixed;
        }
        pathData.append("M");
        pathData.append(CoordUtil.format(-size));
        pathData.append(" ");
        pathData.append(CoordUtil.format(dy));
        pathData.append("L");
        pathData.append(CoordUtil.format(size));
        pathData.append(" ");
        pathData.append(CoordUtil.format(dy));
        pathData.append("M 0 ");
        pathData.append(CoordUtil.format(dy - size));
        pathData.append("L 0 ");
        pathData.append(CoordUtil.format(dy + size));

        renderPath(pathData.toString(), "pinMark", "pinMark");
    }

    private String getPathData(List<Point2D.Double> coords, Boolean append) {
        StringBuilder pathData = new StringBuilder();
        Boolean isFirst = true;
        for (Point2D.Double coord : coords) {
            if (isFirst) {
                if (append) {
                    pathData.append("L");
                } else {
                    pathData.append("M");
                }
                pathData.append(CoordUtil.format(coord.getX()));
                pathData.append(" ");
                pathData.append(CoordUtil.format(coord.getY()));
                isFirst = false;
            }
            pathData.append("L");
            pathData.append(CoordUtil.format(coord.getX()));
            pathData.append(" ");
            pathData.append(CoordUtil.format(coord.getY()));
        }
        pathData.append("z");
        return pathData.toString();
    }

    private void renderPath(String pathData, String id, String style) throws XMLStreamException {
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

    private void renderTextOnPath(String pathID, Double startOffset, String text, String style) throws XMLStreamException {

        writer.writeStartElement("text");
        writer.writeStartElement("textPath");
        writer.writeAttribute("xlink:href", "#" + pathID);
        writer.writeAttribute("startOffset", CoordUtil.format(startOffset) + "%");
        if (style != null) {
            writer.writeAttribute("class", style);
        }
        writer.writeCharacters(text);
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private Poly createContour(List<Point2D> coords) {
        Poly contour = new PolyDefault();
        for (Point2D coordRaw : coords) {
            Point2D coord = new Point2D.Double();
            CoordUtil.convertWithoutCheck(coordRaw.getX(), coordRaw.getY(), coord, latitude, scale);
            contour.add(coord);
        }
        return contour;
    }

    private Poly createClipArea() {
        Poly contour = new PolyDefault();
        Double Dec = latitude > 0 ? latitude - 90 : latitude + 90;
        for (Double RA = 0.0; RA <= 24; RA = RA + 0.5) {
            Point2D coord = new Point2D.Double();
            CoordUtil.convertWithoutCheck(RA, Dec, coord, latitude, scale);
            contour.add(coord);
        }
        return contour;
    }

    private void renderDefsDialMonthsLabelMajorPath() throws XMLStreamException {
        String pathData = isDoubleSided ? renderCircleInv(new Point2D.Double(0.0, 0.0), 0.98 * scaleFixed) : renderCircle(0.95 * scaleFixed);
        renderPath(pathData, "dialMonthsLabelMajorPath", null);
    }

    private void renderDefsDialMonthsLabelMinorPath() throws XMLStreamException {
        String pathData = isDoubleSided ? renderCircleInv(new Point2D.Double(0.0, 0.0), 0.935 * scaleFixed) : renderCircle(0.92 * scaleFixed);
        renderPath(pathData, "dialMonthsLabelMinorPath", null);
    }

    private String[] getMonthNames(Locale locale) {

        String[] monthNames = new String[12];
        String[] monthNamesEn = new DateFormatSymbols(Locale.ENGLISH).getMonths();

        if (localizationUtil.getValue("january").equals("january")) {
            monthNames = new DateFormatSymbols(locale).getMonths();
            for (Integer i = 0; i < 12; i++) {
                monthNames[i] = monthNames[i].substring(0, 1).toUpperCase(locale) + monthNames[i].substring(1);
            }
        } else {
            for (Integer i = 0; i < 12; i++) {
                monthNames[i] = localizationUtil.getValue(monthNamesEn[i].toLowerCase(Locale.ENGLISH));
            }
        }
        return monthNames;
    }

    private void renderDialMonths(Locale locale) throws XMLStreamException {

        String[] monthNames = getMonthNames(locale);
        Integer[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        Integer daysInYear = 365;

        GregorianCalendar calendar = new GregorianCalendar();
        if (calendar.isLeapYear(Calendar.getInstance().get(Calendar.YEAR))) {
            daysInMonth[1] = 29;
            daysInYear = 366;
        }

        Double sign = isDoubleSided ? -doubleSidedSign : Math.signum(latitude);
        Double angleInPercent;

        Double dayIncrement = 360.0 / daysInYear;
        Double percent = 100.0 / 360.0;
        Double startAngle = (daysInMonth[0] + daysInMonth[1] + 21) * dayIncrement;

        Double angle = 90.0 - startAngle;
        for (int month = 0; month < 12; month++) {
            String monthName = monthNames[month];
            angleInPercent = normalizePercent(percent * sign * (angle + daysInMonth[month] / 2.0));
            angle = angle + daysInMonth[month] * dayIncrement;
            // december
            if (month == 11) {
                writeGroupStart(null);
                writer.writeAttribute("transform", "rotate(180)");
                renderTextOnPath("dialMonthsLabelMajorPath", normalizePercent(angleInPercent - 50), monthName, "dialMonthsLabelMajor");
                writeGroupEnd();
            } else {
                renderTextOnPath("dialMonthsLabelMajorPath", angleInPercent, monthName, "dialMonthsLabelMajor");
            }
        }

        angle = 90.0 - startAngle;
        for (int month = 0; month < 12; month++) {
            for (Integer day = 0; day < daysInMonth[month]; day++) {
                angleInPercent = normalizePercent(percent * sign * angle);
                angle = angle + dayIncrement;
                if (day != 0 && day % 5 == 0) {
                    if (month == 11) {
                        writeGroupStart(null);
                        writer.writeAttribute("transform", "rotate(180)");
                        renderTextOnPath("dialMonthsLabelMinorPath", normalizePercent(angleInPercent - 50), day.toString(), "dialMonthsLabelMinor");
                        writeGroupEnd();
                    } else {
                        renderTextOnPath("dialMonthsLabelMinorPath", angleInPercent, day.toString(), "dialMonthsLabelMinor");
                    }
                }
            }
        }
    }

    private String renderDialMonthsTick(Double angle, Double radius) {
        return "M" + CoordUtil.format(Math.cos(angle) * 0.89 * scaleFixed)
                + " " + CoordUtil.format(-Math.sin(angle) * 0.89 * scaleFixed)
                + "L" + CoordUtil.format(Math.cos(angle) * radius * scaleFixed)
                + " " + CoordUtil.format(-Math.sin(angle) * radius * scaleFixed);
    }

    private Element replaceTextElementContent(Element element, String originalText, String newText) {
        Element resultElement = (Element) element.cloneNode(true);
        NodeList textNodes = resultElement.getElementsByTagName("text");
        for (Integer i = 0; i < textNodes.getLength(); i++) {
            if (textNodes.item(i).getTextContent().contains(originalText)) {
                textNodes.item(i).setTextContent(newText);
            }
        }
        return resultElement;
    }

    private void writeElementContent(Element element) throws XMLStreamException {

        //http://stackoverflow.com/questions/7257508/convert-java-w3c-document-to-xmlstreamreader
        //http://bugs.sun.com/view_bug.do?bug_id=6631274
        DOMSource domSource = new DOMSource(element);
        XMLEventReader parser = inputFactory.createXMLEventReader(domSource);

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
                default:
            }
        }
        parser.close();
    }

    private void writeStreamContent(InputStream content) throws XMLStreamException {

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
                default:
            }
        }
        parser.close();
    }

    private void renderDialHoursMarker(Element mark, Double angle, String style) throws XMLStreamException {
        String strTranslate = CoordUtil.format(0.9 * scaleFixed);
        writer.writeStartElement("g");
        writer.writeAttribute("class", style);
        writer.writeAttribute("transform", "translate(0,-" + strTranslate + ") rotate(" + angle + ",0," + strTranslate + ")");
        writeElementContent(mark);
        writer.writeEndElement();
    }

    private void renderDefsInstance(String id, Double x, Double y, String transform, String style) throws XMLStreamException {
        writer.writeStartElement("use");
        if (x != 0) {
            writer.writeAttribute("x", CoordUtil.format(x));
        }
        if (y != 0) {
            writer.writeAttribute("y", CoordUtil.format(y));
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

        BezierCircle circle = new BezierCircle(radius);
        return circle.render();
    }

    private String renderCircle(Point2D center, Double radius) {

        BezierCircle circle = new BezierCircle(center, radius);
        return circle.render();
    }

    public String renderCircleForConstellationName(Point2D coord) {
        Double radius = coord.distance(0.0, 0.0);
        Double angle = 90.0 + Math.toDegrees(Math.atan2(coord.getY(), coord.getX()));
        BezierCircle circle = new BezierCircle(new Point2D.Double(0.0, 0.0), radius, angle);
        return circle.render();
    }

    public String renderCircleInv(Point2D center, Double radius) {

        BezierCircle circle = new BezierCircle(center, radius);
        return circle.renderInv();
    }

    private String renderLineHorizontal(Point2D center, Double length) {

        StringBuilder path = new StringBuilder();
        path.append("M");
        path.append(CoordUtil.format(center.getX() - length / 2.0));
        path.append(" ");
        path.append(CoordUtil.format(center.getY()));
        path.append("h");
        path.append(length);

        return path.toString();
    }

    private Double normalizePercent(Double percent) {
        return (100.0 + percent % 100.0) % 100.0;
    }

    private Point2D getIntersection(Double ax, Double ay, Double bx, Double by, Double cx, Double cy) {

        // Get the perpendicular bisector of (x1, y1) and (x2, y2)
        Double x1, y1, dx1, dy1;
        x1 = (bx + ax) / 2.0;
        y1 = (by + ay) / 2.0;
        dy1 = bx - ax;
        dx1 = -(by - ay);

        // Get the perpendicular bisector of (x2, y2) and (x3, y3)
        Double x2, y2, dx2, dy2;
        x2 = (cx + bx) / 2.0;
        y2 = (cy + by) / 2.0;
        dy2 = cx - bx;
        dx2 = -(cy - by);

        // See where the lines intersect
        Double ox, oy;
        ox = (y1 * dx1 * dx2 + x2 * dx1 * dy2 - x1 * dy1 * dx2 - y2 * dx1 * dx2)
                / (dx1 * dy2 - dy1 * dx2);
        oy = (ox - x1) * dy1 / dx1 + y1;

        return new Point2D.Double(ox, oy);
    }

    private void createMapAreaPointList() {

        mapAreaPointList.clear();

        Double step = 3.0;

        // in this mode we don't care the path direction, so absolute values are used
        Double latitudeAbs = Math.abs(latitude);
        Double latitudeInRads = Math.toRadians(latitudeAbs);

        if (isDoubleSided) {

            Double latitudeFixedAbs = Math.abs(latitudeFixed);
            Double latitudeFixedInRads = Math.toRadians(latitudeFixedAbs);
            Double scaleFix = scale * (180.0 - latitudeFixedAbs) / (180.0 - latitudeAbs);

            if (latitudeFixedAbs == 0.0) {
                // just starting point
                mapAreaPointList.add(getMapAreaPoint(-90.0, latitudeAbs, latitudeInRads, scale, false));
            } else {
                for (Double Az = -90.0; Az < 90.0; Az = Az + step) {
                    mapAreaPointList.add(getMapAreaPoint(Az, latitudeFixedAbs, latitudeFixedInRads, scaleFix, true));
                }
            }
            for (Double Az = 90.0; Az < 270.0; Az = Az + step) {
                mapAreaPointList.add(getMapAreaPoint(Az, latitudeAbs, latitudeInRads, scale, false));
            }

        } else {

            for (Double Az = 90.0; Az < 450.0; Az = Az + step) {
                mapAreaPointList.add(getMapAreaPoint(Az, latitudeAbs, latitudeInRads, scale, false));
            }
        }
    }

    private Point2D getMapAreaPoint(Double Az, Double latitudeInDegs, Double latitudeInRads, Double mapAreaScale, Boolean useDoubleSidedSign) {

        Double shift = isDoubleSided ? 6.0 : -6.0;
        Double AzInRads = Math.toRadians(Az);
        Double Dec = Math.asin(Math.cos(AzInRads) * Math.cos(latitudeInRads));
        Double RA = Math.atan2(Math.sin(AzInRads), Math.tan(latitudeInRads) * Math.sin(Dec));

        Point2D.Double mapAreaPoint = new Point2D.Double();
        CoordUtil.convertWithoutCheck(Math.toDegrees(RA) / 15.0 + shift, Math.toDegrees(Dec), mapAreaPoint, latitudeInDegs, mapAreaScale);
        if (useDoubleSidedSign) {
            mapAreaPoint.setLocation(mapAreaPoint.getX(), Math.signum(latitudeFixed) * doubleSidedSign * mapAreaPoint.getY());
        }

        return mapAreaPoint;
    }

    private void createCardinalPointList() {

        Point2D pointA, pointB, pointC;

        Double latitudeAbs = Math.abs(latitude);
        List<String> cardinalPointLabelList = getCardinalPointLabelList();

        cardinalPointList.clear();

        if (isDoubleSided) {

            //Double latitudeInRads = Math.toRadians(latitudeAbs);
            Double latitudeFixedAbs = Math.abs(latitudeFixed);
            Double latitudeFixedInRads = Math.toRadians(latitudeFixedAbs);
            Double scaleFix = scale * (180.0 - latitudeFixedAbs) / (180.0 - latitudeAbs);

            if (latitudeFixed == 0) {

                Point2D pointRight = mapAreaPointList.get(0);
                Point2D pointLeft = mapAreaPointList.get(1);
                Double x0 = pointRight.getX();
                Double y0 = pointRight.getY();
                Double step = pointRight.distance(pointLeft) / 4.0;

                for (int i = 0; i <= 4; i++) {

                    Double x = x0 - i * step;
                    CardinalPoint cardinalPoint = new CardinalPoint();
                    cardinalPoint.setTickStart(new Point2D.Double(x, y0));
                    cardinalPoint.setTickEnd(new Point2D.Double(x, y0 + scale * 0.03));
                    Integer labelIndex = doubleSidedSign < 0 ? i : (i + 4) % 8;
                    cardinalPoint.setLabel(cardinalPointLabelList.get(labelIndex));
                    cardinalPointList.add(cardinalPoint);
                }

            } else {

                // right edge
                Point2D pointOutsideRight = getMapAreaPoint(-10.0, latitudeFixedAbs, latitudeFixedInRads, scaleFix, true);
                cardinalPointList.add(getCardinalPoint(pointOutsideRight, mapAreaPointList.get(0), mapAreaPointList.get(10), cardinalPointLabelList.get(0)));

                for (int i = 1; i <= 3; i++) {

                    Integer index = i * 15;
                    pointA = mapAreaPointList.get(index - 10);
                    pointB = mapAreaPointList.get(index);
                    pointC = mapAreaPointList.get(index + 10);

                    cardinalPointList.add(getCardinalPoint(pointA, pointB, pointC, cardinalPointLabelList.get(i)));
                }

                // left edge
                Point2D pointOutsideLeft = getMapAreaPoint(100.0, latitudeFixedAbs, latitudeFixedInRads, scaleFix, true);
                cardinalPointList.add(getCardinalPoint(mapAreaPointList.get(50), mapAreaPointList.get(60), pointOutsideLeft, cardinalPointLabelList.get(4)));
            }

            for (int i = 0; i < 3; i++) {
                cardinalPointList.add(new CardinalPoint());
            }

        } else {

            for (int i = 0; i < 8; i++) {

                if (((i == 0 || i == 4) && latitudeAbs > 70) || ((i == 5 || i == 7) && latitude > 78)) {
                    cardinalPointList.add(new CardinalPoint());

                } else {

                    Integer index = i * 15;
                    pointA = mapAreaPointList.get((index - 10 + 120) % 120);
                    pointB = mapAreaPointList.get(index);
                    pointC = mapAreaPointList.get(index + 10);

                    cardinalPointList.add(getCardinalPoint(pointA, pointB, pointC, cardinalPointLabelList.get(i)));
                }
            }
        }
    }

    private CardinalPoint getCardinalPoint(Point2D pointA, Point2D pointB, Point2D pointC, String label) {

        Double ax = pointA.getX();
        Double ay = pointA.getY();
        Double bx = pointB.getX();
        Double by = pointB.getY();
        Double cx = pointC.getX();
        Double cy = pointC.getY();

        Point2D intersection = getIntersection(ax, ay, bx, by, cx, cy);
        Double radius = intersection.distance(pointB);

        Double sign = isDoubleSided ? doubleSidedSign * Math.signum(latitudeFixed) : 1.0;
        Double delta = -sign * Math.PI / 2.0 + Math.atan2(bx - intersection.getX(), by - intersection.getY());

        Double dx = bx + scale * 0.03 * Math.cos(delta);
        Double dy = by - scale * 0.03 * Math.sin(delta);

        CardinalPoint cardinalPoint = new CardinalPoint();
        cardinalPoint.setTickStart(pointB);
        cardinalPoint.setTickEnd(new Point2D.Double(dx, dy));
        cardinalPoint.setRadius(radius);
        cardinalPoint.setStartOffset(normalizePercent(100.0 * (90.0 - Math.toDegrees(delta)) / 360.0));
        cardinalPoint.setCenter(intersection);
        cardinalPoint.setLabel(label);

        return cardinalPoint;
    }

    private List<String> getCardinalPointLabelList() {

        List<String> cardinalPointLabelList = new LinkedList<>();
        cardinalPointLabelList.add(localizationUtil.getValue("cardinalPointWest"));
        cardinalPointLabelList.add(localizationUtil.getValue("cardinalPointSouthWest"));
        cardinalPointLabelList.add(localizationUtil.getValue("cardinalPointSouth"));
        cardinalPointLabelList.add(localizationUtil.getValue("cardinalPointSouthEast"));
        cardinalPointLabelList.add(localizationUtil.getValue("cardinalPointEast"));
        cardinalPointLabelList.add(localizationUtil.getValue("cardinalPointNorthEast"));
        cardinalPointLabelList.add(localizationUtil.getValue("cardinalPointNorth"));
        cardinalPointLabelList.add(localizationUtil.getValue("cardinalPointNorthWest"));

        if (isDoubleSided) {
            if (doubleSidedSign > 0 && latitudeFixed != 0) {
                List<String> copyList = new LinkedList<>();
                for (int i = 0; i < 4; i++) {
                    copyList.add(i, cardinalPointLabelList.get(i + 4));
                }
                for (int i = 0; i < 4; i++) {
                    copyList.add(i + 4, cardinalPointLabelList.get(i));
                }
                cardinalPointLabelList = copyList;
            }
        } else {
            if (latitude < 0) {
                String tmp;
                tmp = cardinalPointLabelList.get(0);
                cardinalPointLabelList.set(0, cardinalPointLabelList.get(4));
                cardinalPointLabelList.set(4, tmp);
                tmp = cardinalPointLabelList.get(1);
                cardinalPointLabelList.set(1, cardinalPointLabelList.get(3));
                cardinalPointLabelList.set(3, tmp);
                tmp = cardinalPointLabelList.get(5);
                cardinalPointLabelList.set(5, cardinalPointLabelList.get(7));
                cardinalPointLabelList.set(7, tmp);
            }
        }

        return cardinalPointLabelList;
    }
}
