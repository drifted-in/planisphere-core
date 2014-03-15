package in.drifted.planisphere.renderer.svg;

import com.seisw.util.geom.Point2D;
import com.seisw.util.geom.Poly;
import com.seisw.util.geom.PolyDefault;
import in.drifted.planisphere.Options;
import in.drifted.planisphere.Settings;
import in.drifted.planisphere.model.CardinalPoint;
import in.drifted.planisphere.model.ConstellationName;
import in.drifted.planisphere.model.Coord;
import in.drifted.planisphere.model.MilkyWay;
import in.drifted.planisphere.model.Star;
import in.drifted.planisphere.util.CacheHandler;
import in.drifted.planisphere.util.CoordUtil;
import in.drifted.planisphere.util.FontManager;
import in.drifted.planisphere.util.LocalizationUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public final class SvgRenderer {

    private XMLInputFactory inputFactory;
    private XMLOutputFactory outputFactory;
    private XMLEventFactory eventFactory;
    private XMLStreamWriter writer;
    private CacheHandler cacheHandler;
    private LocalizationUtil localizationUtil;
    private final List<Coord> mapAreaPointList = new LinkedList<>();
    private final List<CardinalPoint> cardinalPointList = new LinkedList<>();
    private Double latitudeFixed;
    private Double latitude;
    private Double scaleFixed; // cover and main layout
    private Double scale; // map content
    private Boolean isDoubleSided;
    private Integer doubleSidedSign;
    private Integer latitudeSign;

    public SvgRenderer() {
        initRenderer();
    }

    private void initRenderer() {
        inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
        outputFactory = XMLOutputFactory.newInstance();
        eventFactory = XMLEventFactory.newInstance();
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
        Integer latitudeFixedSign = (latitudeFixed >= 0) ? 1 : -1;
        doubleSidedSign = (int) (options.getDoubleSidedSign() * latitudeFixedSign);
        latitude = isDoubleSided ? doubleSidedSign * 65.0 : latitudeFixed;
        latitudeSign = (latitude >= 0) ? 1 : -1; // Math.signum() returns 0 for zero latitude
        Locale locale = options.getCurrentLocale();
        localizationUtil = new LocalizationUtil(locale);
        FontManager fontManager = new FontManager(locale);
        XMLEventReader parser = inputFactory.createXMLEventReader(input);

        Map<String, byte[]> paramMap = new HashMap<>();

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
                            case "mapArea":
                                renderDefsMapArea();
                                break;
                            case "monthsArea":
                                renderDefsMonthsArea();
                                break;
                            case "dialHoursMarkerMajorSingle":
                            case "dialHoursMarkerMajorDouble":
                            case "dialHoursMarkerMinorSingle":
                            case "dialHoursMarkerMinorDouble":
                                paramMap.put(id, RendererUtil.getParamStream(outputFactory, eventFactory, parser, event));
                                break;
                            case "dialMonthsLabelMajorPath":
                                renderDefsDialMonthsLabelMajorPath();
                                break;
                            case "dialMonthsLabelMinorPath":
                                renderDefsDialMonthsLabelMinorPath();
                                break;
                            case "coordLabelPaths":
                                RendererUtil.writeGroupStart(writer, "coordLabelPaths");
                                renderDefsCoordLabelPaths();
                                RendererUtil.writeGroupEnd(writer);
                                break;
                            case "wheel":
                                RendererUtil.writeGroupStart(writer, "wheel");
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
                                RendererUtil.writeGroupEnd(writer);
                                break;
                            case "scales":
                                RendererUtil.writeGroupStart(writer, "scales");
                                renderDialHours(paramMap, options.getDayLightSavingTimeScale());
                                renderCardinalPointsTicks();
                                renderCardinalPointsLabels();
                                RendererUtil.writeGroupEnd(writer);
                                break;
                            case "monthsAreaBorder":
                                RendererUtil.writeGroupStart(writer, "monthsAreaBorder");
                                renderMonthsAreaBorder();
                                RendererUtil.writeGroupEnd(writer);
                                break;
                            case "mapAreaBorder":
                                RendererUtil.writeGroupStart(writer, "mapAreaBorder");
                                renderMapAreaBorder();
                                RendererUtil.writeGroupEnd(writer);
                                break;
                            case "spacer":
                                renderSpacer();
                                break;
                            case "cover":
                                renderCover();
                                break;
                            case "pinMark":
                                renderPinMark(false);
                                break;
                            case "guide_S":
                            case "guide_D":
                            case "worldmap":
                            case "starSizeComparison":
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
                                RendererUtil.renderSymbol(inputFactory, writer, id, localizationUtil);
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
                                        Double y = ratio * (Math.abs(latitudeFixed - 90) - 90);
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
                                RendererUtil.writeAttributes(writer, startElement.getAttributes());
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
                        RendererUtil.writeNamespaces(writer, startElement.getNamespaces());
                        RendererUtil.writeAttributes(writer, startElement.getAttributes());
                        String direction = localizationUtil.getValue("writingDirection");
                        if (!direction.equals("writingDirection")) {
                            writer.writeAttribute("direction", direction);
                        }
                    } else {
                        writer.writeStartElement(elementName);
                        RendererUtil.writeAttributes(writer, startElement.getAttributes());
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

    private Coord getMapAreaPoint(Double Az, Double latitudeInDegs, Double latitudeInRads, Double mapAreaScale, Boolean useDoubleSidedSign) {

        Double shift = isDoubleSided ? 6.0 : -6.0;
        Double AzInRads = Math.toRadians(Az);
        Double Dec = Math.asin(Math.cos(AzInRads) * Math.cos(latitudeInRads));
        Double RA = Math.atan2(Math.sin(AzInRads), Math.tan(latitudeInRads) * Math.sin(Dec));

        Coord mapAreaPoint = new Coord();
        CoordUtil.convertWithoutCheck(Math.toDegrees(RA) / 15.0 + shift, Math.toDegrees(Dec), mapAreaPoint, latitudeInDegs, mapAreaScale);
        if (useDoubleSidedSign) {
            mapAreaPoint.setLocation(mapAreaPoint.getX(), Math.signum(latitudeFixed) * doubleSidedSign * mapAreaPoint.getY());
        }

        return mapAreaPoint;
    }

    private void createCardinalPointList() {

        Double latitudeAbs = Math.abs(latitude);
        List<String> cardinalPointLabelList = getCardinalPointLabelList();

        cardinalPointList.clear();

        if (isDoubleSided) {

            //Double latitudeInRads = Math.toRadians(latitudeAbs);
            Double latitudeFixedAbs = Math.abs(latitudeFixed);
            Double latitudeFixedInRads = Math.toRadians(latitudeFixedAbs);
            Double scaleFix = scale * (180.0 - latitudeFixedAbs) / (180.0 - latitudeAbs);

            if (latitudeFixed == 0) {

                Coord pointRight = mapAreaPointList.get(0);
                Coord pointLeft = mapAreaPointList.get(1);
                Double x0 = pointRight.getX();
                Double y0 = pointRight.getY();
                Double step = CoordUtil.getDistance(pointRight, pointLeft) / 4.0;

                for (int i = 0; i <= 4; i++) {

                    Double x = x0 - i * step;
                    CardinalPoint cardinalPoint = new CardinalPoint();
                    cardinalPoint.setTickStart(new Coord(x, y0));
                    cardinalPoint.setTickEnd(new Coord(x, y0 + scale * 0.03));
                    Integer labelIndex = doubleSidedSign < 0 ? i : (i + 4) % 8;
                    cardinalPoint.setLabel(cardinalPointLabelList.get(labelIndex));
                    cardinalPointList.add(cardinalPoint);
                }

            } else {

                // right edge
                Coord pointOutsideRight = getMapAreaPoint(-10.0, latitudeFixedAbs, latitudeFixedInRads, scaleFix, true);
                cardinalPointList.add(getCardinalPoint(pointOutsideRight, mapAreaPointList.get(0), mapAreaPointList.get(10), cardinalPointLabelList.get(0)));

                for (int i = 1; i <= 3; i++) {

                    Integer index = i * 15;
                    Coord pointA = mapAreaPointList.get(index - 10);
                    Coord pointB = mapAreaPointList.get(index);
                    Coord pointC = mapAreaPointList.get(index + 10);

                    cardinalPointList.add(getCardinalPoint(pointA, pointB, pointC, cardinalPointLabelList.get(i)));
                }

                // left edge
                Coord pointOutsideLeft = getMapAreaPoint(100.0, latitudeFixedAbs, latitudeFixedInRads, scaleFix, true);
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
                    Coord pointA = mapAreaPointList.get((index - 10 + 120) % 120);
                    Coord pointB = mapAreaPointList.get(index);
                    Coord pointC = mapAreaPointList.get(index + 10);

                    cardinalPointList.add(getCardinalPoint(pointA, pointB, pointC, cardinalPointLabelList.get(i)));
                }
            }
        }
    }

    private CardinalPoint getCardinalPoint(Coord pointA, Coord pointB, Coord pointC, String label) {

        Double ax = pointA.getX();
        Double ay = pointA.getY();
        Double bx = pointB.getX();
        Double by = pointB.getY();
        Double cx = pointC.getX();
        Double cy = pointC.getY();

        Coord intersection = PathUtil.getIntersection(ax, ay, bx, by, cx, cy);
        Double radius = CoordUtil.getDistance(intersection, pointB);

        Double sign = isDoubleSided ? doubleSidedSign * Math.signum(latitudeFixed) : 1.0;
        Double delta = -sign * Math.PI / 2.0 + Math.atan2(bx - intersection.getX(), by - intersection.getY());

        Double dx = bx + scale * 0.03 * Math.cos(delta);
        Double dy = by - scale * 0.03 * Math.sin(delta);

        CardinalPoint cardinalPoint = new CardinalPoint();
        cardinalPoint.setTickStart(pointB);
        cardinalPoint.setTickEnd(new Coord(dx, dy));
        cardinalPoint.setRadius(radius);
        cardinalPoint.setStartOffset(getNormalizedPercent(100.0 * (90.0 - Math.toDegrees(delta)) / 360.0));
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

    private void renderDefsMapArea() throws XMLStreamException {
        StringBuilder path = new StringBuilder();
        Iterator iter = mapAreaPointList.iterator();
        path.append("M");
        path.append(PathUtil.getCoordsChunk((Coord) iter.next()));
        while (iter.hasNext()) {
            path.append("L");
            path.append(PathUtil.getCoordsChunk((Coord) iter.next()));
        }
        path.append("z");
        RendererUtil.renderPath(writer, path.toString(), "mapArea", null);
    }

    private void renderDefsMonthsArea() throws XMLStreamException {

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
        pathData.append(PathUtil.format(-x1));
        pathData.append(" ");
        pathData.append(PathUtil.format(y1));
        pathData.append("A");
        // rx
        pathData.append(PathUtil.format(0.9 * scaleFixed));
        pathData.append(" ");
        // ry
        pathData.append(PathUtil.format(0.9 * scaleFixed));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 1 ");
        pathData.append(sweep1);
        pathData.append(" ");
        pathData.append(PathUtil.format(x1));
        pathData.append(" ");
        pathData.append(PathUtil.format(y1));

        pathData.append("L");
        pathData.append(PathUtil.format(x2));
        pathData.append(" ");
        pathData.append(PathUtil.format(y2));

        // outer arc
        pathData.append("A");
        pathData.append(PathUtil.format(scaleFixed));
        pathData.append(" ");
        pathData.append(PathUtil.format(scaleFixed));
        pathData.append(" 0 1 ");
        pathData.append(sweep2);
        pathData.append(" ");
        pathData.append(PathUtil.format(-x2));
        pathData.append(" ");
        pathData.append(PathUtil.format(y2));

        pathData.append("Z");

        RendererUtil.renderPath(writer, pathData.toString(), "monthsArea", "null");
    }

    private void renderDefsDialMonthsLabelMajorPath() throws XMLStreamException {
        String pathData = isDoubleSided ? PathUtil.getCirclePathDataInv(new Coord(0.0, 0.0), 0.98 * scaleFixed) : PathUtil.getCirclePathData(0.95 * scaleFixed);
        RendererUtil.renderPath(writer, pathData, "dialMonthsLabelMajorPath", null);
    }

    private void renderDefsDialMonthsLabelMinorPath() throws XMLStreamException {
        String pathData = isDoubleSided ? PathUtil.getCirclePathDataInv(new Coord(0.0, 0.0), 0.935 * scaleFixed) : PathUtil.getCirclePathData(0.92 * scaleFixed);
        RendererUtil.renderPath(writer, pathData, "dialMonthsLabelMinorPath", null);
    }

    private void renderDefsCoordLabelPaths() throws XMLStreamException {

        for (Double Dec = 60.0; Dec >= Math.abs(latitude) - 90.0; Dec = Dec - 30.0) {
            StringBuilder path = new StringBuilder("M");
            for (Double RA = 24.0; RA >= 0.0; RA = RA - 0.5) {
                if (RA < 24.0) {
                    path.append("L");
                }
                Double finalRA = (latitude >= 0) ? RA : 24.0 - RA;
                Coord coord = new Coord();
                CoordUtil.convert(finalRA, latitudeSign * Dec, coord, latitude, scale);
                path.append(PathUtil.getCoordsChunk(coord));
            }
            RendererUtil.renderPath(writer, path.toString(), "coordLabelPath" + Dec.intValue(), null);
        }

        // slightly shifted for RA labels
        StringBuilder path = new StringBuilder("M");
        for (Double RA = 24.0; RA >= 0.0; RA = RA - 0.5) {
            if (RA < 24.0) {
                path.append("L");
            }
            Double finalRA = (latitude >= 0) ? RA : 24.0 - RA;
            Coord coord = new Coord();
            CoordUtil.convert(finalRA, -latitudeSign * 3.0, coord, latitude, scale);
            path.append(PathUtil.getCoordsChunk(coord));
        }
        RendererUtil.renderPath(writer, path.toString(), "coordLabelPath00", null);

    }

    private void renderMapBackground() throws XMLStreamException {
        RendererUtil.renderPath(writer, PathUtil.getCirclePathData(1.0 * scaleFixed), null, "mapBackground");
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
            angleInPercent = getNormalizedPercent(percent * sign * (angle + daysInMonth[month] / 2.0));
            angle = angle + daysInMonth[month] * dayIncrement;
            // december
            if (month == 11) {
                RendererUtil.writeGroupStart(writer, null);
                writer.writeAttribute("transform", "rotate(180)");
                RendererUtil.renderTextOnPath(writer, "dialMonthsLabelMajorPath", getNormalizedPercent(angleInPercent - 50), monthName, "dialMonthsLabelMajor");
                RendererUtil.writeGroupEnd(writer);
            } else {
                RendererUtil.renderTextOnPath(writer, "dialMonthsLabelMajorPath", angleInPercent, monthName, "dialMonthsLabelMajor");
            }
        }

        angle = 90.0 - startAngle;
        for (int month = 0; month < 12; month++) {
            for (Integer day = 0; day < daysInMonth[month]; day++) {
                angleInPercent = getNormalizedPercent(percent * sign * angle);
                angle = angle + dayIncrement;
                if (day != 0 && day % 5 == 0) {
                    if (month == 11) {
                        RendererUtil.writeGroupStart(writer, null);
                        writer.writeAttribute("transform", "rotate(180)");
                        RendererUtil.renderTextOnPath(writer, "dialMonthsLabelMinorPath", getNormalizedPercent(angleInPercent - 50), day.toString(), "dialMonthsLabelMinor");
                        RendererUtil.writeGroupEnd(writer);
                    } else {
                        RendererUtil.renderTextOnPath(writer, "dialMonthsLabelMinorPath", angleInPercent, day.toString(), "dialMonthsLabelMinor");
                    }
                }
            }
        }
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

    private void renderDialMonthsTicks() throws XMLStreamException {

        Integer[] daysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        Integer daysInYear = 365;

        GregorianCalendar calendar = new GregorianCalendar();
        if (calendar.isLeapYear(Calendar.getInstance().get(Calendar.YEAR))) {
            daysInMonth[1] = 29;
            daysInYear = 366;
        }

        Integer sign = isDoubleSided ? doubleSidedSign : latitudeSign;
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
                        pathData.append(getDialMonthsTickPathData(angleInRads, 1.0));
                        break;
                    case 5:
                    case 10:
                    case 15:
                    case 20:
                    case 25:
                    case 30:
                        pathData.append(getDialMonthsTickPathData(angleInRads, 0.914));
                        break;
                    default:
                        pathData.append(getDialMonthsTickPathData(angleInRads, 0.908));
                }
            }
            RendererUtil.renderPath(writer, pathData.toString(), null, "dialMonthsTick");
        }
    }

    private String getDialMonthsTickPathData(Double angle, Double radius) {
        return "M" + PathUtil.format(Math.cos(angle) * 0.89 * scaleFixed)
                + " " + PathUtil.format(-Math.sin(angle) * 0.89 * scaleFixed)
                + "L" + PathUtil.format(Math.cos(angle) * radius * scaleFixed)
                + " " + PathUtil.format(-Math.sin(angle) * radius * scaleFixed);
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
                pathData.append(PathUtil.format(contour.getX(j)));
                pathData.append(" ");
                pathData.append(PathUtil.format(contour.getY(j)));
            }
        }
        RendererUtil.renderPath(writer, pathData.toString(), null, style);
    }

    private void renderCoords() throws XMLStreamException {

        StringBuilder path = new StringBuilder();
        Coord coord = new Coord(0.0, 0.0);

        // declination circle (it cannot be rendered using circles because of the rotation at vernal point)        
        for (Double Dec = 60.0; Dec >= Math.abs(latitude) - 90.0; Dec = Dec - 30.0) {
            for (Double RA = 0.0; RA <= 24.0; RA = RA + 0.5) {
                CoordUtil.convert(RA, latitudeSign * Dec, coord, latitude, scale);
                if (RA == 0.0) {
                    path.append("M");
                } else {
                    path.append("L");
                }
                path.append(PathUtil.getCoordsChunk(coord));
            }
        }

        // RA        
        Double start;
        for (Integer RA = 0; RA < 24; RA++) {
            switch (RA % 6) {
                case 1:
                case 3:
                case 5:
                    start = 30.0;
                    break;
                case 2:
                case 4:
                    start = 60.0;
                    break;
                default:
                    start = 90.0;
            }

            CoordUtil.convert(RA.doubleValue(), latitudeSign * start, coord, latitude, scale);
            path.append("M");
            path.append(PathUtil.getCoordsChunk(coord));
            CoordUtil.convert(RA.doubleValue(), latitude - latitudeSign * 90.0, coord, latitude, scale);
            path.append("L");
            path.append(PathUtil.getCoordsChunk(coord));
        }
        RendererUtil.renderPath(writer, path.toString(), null, "coords");
    }

    private void renderCoordLabels() throws XMLStreamException {

        for (int RA = 1; RA < 24; RA++) {
            Integer finalRA = (latitude >= 0) ? RA : 24 - RA;
            RendererUtil.renderTextOnPath(writer, "coordLabelPath00", 100 - (RA * 100.0 / 24), finalRA + "h", "coordLabelRa");
        }
        RendererUtil.renderTextOnPath(writer, "coordLabelPath00", 0.0, "0h", "coordLabelRa");

        for (Double Dec = 60.0; Dec >= Math.abs(latitude) - 90.0; Dec = Dec - 30.0) {
            Double finalDec = latitudeSign * Dec;
            String pathId = "coordLabelPath" + Dec.intValue();
            String strSign = (finalDec > 0) ? "+" : "";
            RendererUtil.renderTextOnPath(writer, pathId, 100.0, strSign + finalDec.intValue() + "°", "coordLabelDec");
            for (Integer i = 1; i < 4; i++) {
                RendererUtil.renderTextOnPath(writer, pathId, i * 25.0, strSign + finalDec.intValue() + "°", "coordLabelDec");
            }
        }
    }

    private void renderEcliptic() throws XMLStreamException {

        Double epsilon = Math.toRadians(23.44);
        Boolean flag = false;
        String coordsChunk = "";
        StringBuilder pathData = new StringBuilder();

        for (Integer i = 0; i <= 360; i = i + 2) {
            Double lambda = Math.toRadians(i);
            Double RA = (Math.atan2(Math.sin(lambda) * Math.cos(epsilon), Math.cos(lambda))) * 12 / Math.PI;
            Double Dec = Math.toDegrees(Math.asin(Math.sin(epsilon) * Math.sin(lambda)));
            Coord coord = new Coord();
            if (CoordUtil.convert(RA, Dec, coord, latitude, scale)) {
                if (!flag) {
                    coordsChunk = PathUtil.getCoordsChunk(coord);
                    flag = true;
                } else {
                    pathData.append("M");
                    pathData.append(coordsChunk);
                    pathData.append("L");
                    coordsChunk = PathUtil.getCoordsChunk(coord);
                    pathData.append(coordsChunk);
                }
            } else {
                flag = false;
            }
        }
        RendererUtil.renderPath(writer, pathData.toString(), null, "ecliptic");
    }

    private void renderConstellationBoundaries() throws XMLStreamException, IOException {

        StringBuilder pathData = new StringBuilder();

        for (Iterator<Coord> i = cacheHandler.getConstellationBoundaryList().iterator(); i.hasNext();) {
            Coord coordStartRaw = i.next();
            Coord coordStart = new Coord();
            if (CoordUtil.convert(coordStartRaw.getX(), coordStartRaw.getY(), coordStart, latitude, scale)) {
                Coord coordEndRaw = i.next();
                Coord coordEnd = new Coord();
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
                        Coord coordTemp = new Coord();

                        for (Integer j = 0; j <= bDiv; j++) {
                            CoordUtil.convert(startRA + j * incRA / bDiv, Dec, coordTemp, latitude, scale);
                            if (j == 0) {
                                pathData.append("M");

                            } else {
                                pathData.append("L");
                            }
                            pathData.append(PathUtil.getCoordsChunk(coordTemp));
                            Dec = Dec + incDec;
                        }
                    } else {
                        pathData.append("M");
                        pathData.append(PathUtil.getCoordsChunk(coordStart));
                        pathData.append("L");
                        pathData.append(PathUtil.getCoordsChunk(coordEnd));
                    }
                }
            } else {
                i.next();
            }
        }
        RendererUtil.renderPath(writer, pathData.toString(), null, "constellationBoundaries");
    }

    private void renderConstellationLines() throws XMLStreamException, IOException {

        StringBuilder pathData = new StringBuilder();

        for (Iterator<Coord> i = cacheHandler.getConstellationLineList().iterator(); i.hasNext();) {
            Coord coordStartRaw = i.next();
            Coord coordStart = new Coord();
            if (CoordUtil.convert(coordStartRaw.getX(), coordStartRaw.getY(), coordStart, latitude, scale)) {
                Coord coordEndRaw = i.next();
                Coord coordEnd = new Coord();
                if (CoordUtil.convert(coordEndRaw.getX(), coordEndRaw.getY(), coordEnd, latitude, scale)) {
                    pathData.append("M");
                    pathData.append(PathUtil.getCoordsChunk(coordStart));
                    pathData.append("L");
                    pathData.append(PathUtil.getCoordsChunk(coordEnd));
                }
            } else {
                i.next();
            }
        }
        RendererUtil.renderPath(writer, pathData.toString(), null, "constellationLines");
    }

    private void renderConstellationNames(Integer mode) throws XMLStreamException, IOException {

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

            Coord coordRaw = constellationName.getCoord();
            Coord coord = new Coord();
            if (CoordUtil.convert(coordRaw.getX(), coordRaw.getY(), coord, latitude, scale)) {
                String id = "con" + constellationName.getAbbreviation();
                RendererUtil.renderPath(writer, getCirclePathDataForConstellationName(coord), id, "constellationNamesPath");
                RendererUtil.renderTextOnPath(writer, id, 50d, name, "constellationNames");
            }
        }
    }

    private void renderStars() throws XMLStreamException, IOException {

        Map<Integer, StringBuilder> pathMap = new HashMap<>();

        for (Star star : cacheHandler.getStarList()) {
            Coord coord = new Coord();
            if (CoordUtil.convert(star.getRA(), star.getDec(), coord, latitude, scale)) {
                String coordsChunk = PathUtil.getCoordsChunk(coord);

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
            RendererUtil.renderPath(writer, entry.getValue().toString(), null, "star level" + entry.getKey());
        }
    }

    private void renderMapAreaBorder() throws XMLStreamException {
        RendererUtil.renderDefsInstance(writer, "mapArea", 0d, 0d, null, "mapAreaBorder");
    }

    private void renderCardinalPointsTicks() throws XMLStreamException {
        StringBuilder path = new StringBuilder();
        for (CardinalPoint cardinalPoint : cardinalPointList) {
            if (cardinalPoint.getLabel() != null) {
                path.append("M");
                path.append(PathUtil.getCoordsChunk(cardinalPoint.getTickStart()));
                path.append("L");
                path.append(PathUtil.getCoordsChunk(cardinalPoint.getTickEnd()));
            }
        }
        RendererUtil.renderPath(writer, path.toString(), null, "cardinalPointTick");
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
                        Coord point = cardinalPoint.getTickStart();
                        Coord center = new Coord(point.getX(), point.getY() + innerOffset);
                        RendererUtil.renderPath(writer, PathUtil.getLineHorizontalPathData(center, textLineLength), pathId, "invisible");
                        RendererUtil.renderTextOnPath(writer, pathId, 50.0, cardinalPoint.getLabel(), "cardinalPointLabel");

                    } else {
                        if (latitudeFixed * doubleSidedSign > 0) {
                            RendererUtil.renderPath(writer, PathUtil.getCirclePathDataInv(cardinalPoint.getCenter(), cardinalPoint.getRadius() + innerOffset), pathId, "invisible");
                            RendererUtil.renderTextOnPath(writer, pathId, 100.0 - cardinalPoint.getStartOffset(), cardinalPoint.getLabel(), "cardinalPointLabel");
                        } else {
                            RendererUtil.renderPath(writer, PathUtil.getCirclePathData(cardinalPoint.getCenter(), cardinalPoint.getRadius() - innerOffset), pathId, "invisible");
                            if (index == 2) {
                                RendererUtil.writeGroupStart(writer, null);
                                writer.writeAttribute("transform", "rotate(180, " + cardinalPoint.getCenter().getX() + "," + cardinalPoint.getCenter().getY() + ")");
                                RendererUtil.renderTextOnPath(writer, pathId, 50.0, cardinalPoint.getLabel(), "cardinalPointLabel");
                                RendererUtil.writeGroupEnd(writer);
                            } else {
                                RendererUtil.renderTextOnPath(writer, pathId, getNormalizedPercent(cardinalPoint.getStartOffset() - 50.0), cardinalPoint.getLabel(), "cardinalPointLabel");
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
                        RendererUtil.renderPath(writer, PathUtil.getCirclePathDataInv(cardinalPoint.getCenter(), cardinalPoint.getRadius() + innerOffset), pathId, "invisible");
                        RendererUtil.renderTextOnPath(writer, pathId, 100.0 - cardinalPoint.getStartOffset(), cardinalPoint.getLabel(), "cardinalPointLabel");

                    } else {
                        RendererUtil.renderPath(writer, PathUtil.getCirclePathData(cardinalPoint.getCenter(), cardinalPoint.getRadius() + outerOffset), pathId, "invisible");
                        if (index == 6) {
                            RendererUtil.writeGroupStart(writer, null);
                            writer.writeAttribute("transform", "rotate(180, " + cardinalPoint.getCenter().getX() + "," + cardinalPoint.getCenter().getY() + ")");
                            RendererUtil.renderTextOnPath(writer, pathId, 50.0, cardinalPoint.getLabel(), "cardinalPointLabel");
                            RendererUtil.writeGroupEnd(writer);
                        } else {
                            RendererUtil.renderTextOnPath(writer, pathId, cardinalPoint.getStartOffset(), cardinalPoint.getLabel(), "cardinalPointLabel");
                        }
                    }
                }
                index++;
            }
        }
    }

    private void renderDialHours(Map<String, byte[]> paramMap, Boolean isDayLightSavingTimeScale) throws XMLStreamException {

        byte[] markMajor = isDoubleSided ? paramMap.get("dialHoursMarkerMajorDouble") : paramMap.get("dialHoursMarkerMajorSingle");
        byte[] markMinor = isDoubleSided ? paramMap.get("dialHoursMarkerMinorDouble") : paramMap.get("dialHoursMarkerMinorSingle");

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
            renderDialHoursMarker(markMajor, hour.toString(), i * 15.0 + shiftAngle, "dialHoursMarkerMajor");
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
                renderDialHoursMarker(markMinor, hour.toString(), i * 15.0 + shiftAngle, "dialHoursMarkerMinor");
            }
        }

        for (Double i = 112.5; i >= -120; i = i - 15) {
            String strTranslate = PathUtil.format(0.9 * scaleFixed);
            RendererUtil.renderDefsInstance(writer, "dialHoursMarkerHalf", 0d, 0d, "translate(0,-" + strTranslate + ") rotate(" + (i + shiftAngle) + ",0," + strTranslate + ")", null);
        }
    }

    private void renderDialHoursMarker(byte[] mark, String replacement, Double angle, String style) throws XMLStreamException {
        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put("#", replacement);
        String strTranslate = PathUtil.format(0.9 * scaleFixed);
        writer.writeStartElement("g");
        writer.writeAttribute("class", style);
        writer.writeAttribute("transform", "translate(0,-" + strTranslate + ") rotate(" + angle + ",0," + strTranslate + ")");
        RendererUtil.writeStreamContent(inputFactory, writer, new ByteArrayInputStream(mark), replacementMap, localizationUtil);
        writer.writeEndElement();
    }

    private void renderMonthsAreaBorder() throws XMLStreamException {
        RendererUtil.renderDefsInstance(writer, "monthsArea", 0d, 0d, null, "monthsAreaBorder");
    }

    private void renderCover() throws XMLStreamException {

        if (isDoubleSided) {
            renderCoverDoubleSided();
        } else {
            renderCoverSingleSided();
        }

        renderPinMark(true);
        renderBendLine();
    }

    private void renderCoverSingleSided() throws XMLStreamException {

        StringBuilder pathData = new StringBuilder();

        Double x1 = Math.cos(Math.toRadians(30.0)) * 0.9 * scaleFixed;
        Double y1 = Math.sin(Math.toRadians(30.0)) * 0.9 * scaleFixed;
        // main arc
        pathData.append("M");
        pathData.append(PathUtil.format(-x1));
        pathData.append(" ");
        pathData.append(PathUtil.format(y1));
        pathData.append("A");
        // rx
        pathData.append(PathUtil.format(0.9 * scaleFixed));
        pathData.append(" ");
        // ry
        pathData.append(PathUtil.format(0.9 * scaleFixed));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 1 1 ");
        pathData.append(PathUtil.format(x1));
        pathData.append(" ");
        pathData.append(PathUtil.format(y1));

        // joiners
        Double y2 = Math.tan(Math.toRadians(30.0)) * scaleFixed;
        Double height = 1.12 * scaleFixed - y2;
        Double y3 = y2 + 2 * height;
        Double y4 = y3 + (y2 - y1);
        Double x5 = Math.cos(Math.toRadians(340d)) * 0.9 * scaleFixed;
        Double y5 = 2.24 * scaleFixed + Math.sin(Math.toRadians(20d)) * 0.9 * scaleFixed;

        // right side
        pathData.append("L");
        pathData.append(PathUtil.format(scaleFixed));
        pathData.append(" ");
        pathData.append(PathUtil.format(y2));
        pathData.append("L");
        pathData.append(PathUtil.format(scaleFixed));
        pathData.append(" ");
        pathData.append(PathUtil.format(y3));
        pathData.append("L");
        pathData.append(PathUtil.format(x1));
        pathData.append(" ");
        pathData.append(PathUtil.format(y4));
        pathData.append("A");
        // rx
        pathData.append(PathUtil.format(0.9 * scaleFixed));
        pathData.append(" ");
        // ry
        pathData.append(PathUtil.format(0.9 * scaleFixed));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 0 1 ");
        pathData.append(PathUtil.format(x5));
        pathData.append(" ");
        pathData.append(PathUtil.format(y5));

        // left side
        pathData.append("L");
        pathData.append(PathUtil.format(-x5));
        pathData.append(" ");
        pathData.append(PathUtil.format(y5));
        pathData.append("A");
        // rx
        pathData.append(PathUtil.format(0.9 * scaleFixed));
        pathData.append(" ");
        // ry
        pathData.append(PathUtil.format(0.9 * scaleFixed));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 0 1 ");
        pathData.append(PathUtil.format(-x1));
        pathData.append(" ");
        pathData.append(PathUtil.format(y4));
        pathData.append("L");
        pathData.append(PathUtil.format(-scaleFixed));
        pathData.append(" ");
        pathData.append(PathUtil.format(y3));
        pathData.append("L");
        pathData.append(PathUtil.format(-scaleFixed));
        pathData.append(" ");
        pathData.append(PathUtil.format(y2));
        pathData.append("L");
        pathData.append(PathUtil.format(-x1));
        pathData.append(" ");
        pathData.append(PathUtil.format(y1));
        pathData.append("Z");

        RendererUtil.renderPath(writer, pathData.toString(), "cover", "cover");

    }

    private void renderCoverDoubleSided() throws XMLStreamException {

        StringBuilder pathData = new StringBuilder();

        Double x1 = Math.cos(Math.toRadians(30.0)) * 0.9 * scaleFixed;
        Double y1 = -Math.sin(Math.toRadians(30.0)) * 0.9 * scaleFixed;
        Double y2 = -Math.tan(Math.toRadians(30.0)) * scaleFixed;

        // right side
        pathData.append("M");
        pathData.append(PathUtil.format(scaleFixed));
        pathData.append(" ");
        pathData.append(PathUtil.format(-1.12 * scaleFixed));
        pathData.append("V");
        pathData.append(PathUtil.format(y2));
        pathData.append("L");
        pathData.append(PathUtil.format(x1));
        pathData.append(" ");
        pathData.append(PathUtil.format(y1));

        // main arc        
        pathData.append("A");
        // rx
        pathData.append(PathUtil.format(0.9 * scaleFixed));
        pathData.append(" ");
        // ry
        pathData.append(PathUtil.format(0.9 * scaleFixed));
        // rotation, large arc flag, sweep flag
        pathData.append(" 0 1 1 ");
        pathData.append(PathUtil.format(-x1));
        pathData.append(" ");
        pathData.append(PathUtil.format(y1));

        // left side
        pathData.append("L");
        pathData.append(PathUtil.format(-scaleFixed));
        pathData.append(" ");
        pathData.append(PathUtil.format(y2));
        pathData.append("V");
        pathData.append(PathUtil.format(-1.12 * scaleFixed));
        pathData.append("z");

        RendererUtil.renderPath(writer, pathData.toString(), "cover", "cover");
    }

    private void renderBendLine() throws XMLStreamException {

        StringBuilder pathData = new StringBuilder();

        Double y = 1.12 * scaleFixed;
        if (isDoubleSided) {
            y = -y;
        }

        pathData.append("M");
        pathData.append(PathUtil.format(-scaleFixed));
        pathData.append(" ");
        pathData.append(PathUtil.format(y));
        pathData.append("L");
        pathData.append(PathUtil.format(scaleFixed));
        pathData.append(" ");
        pathData.append(PathUtil.format(y));

        RendererUtil.renderPath(writer, pathData.toString(), "bendLine", "bendLine");
    }

    private void renderSpacer() throws XMLStreamException {

        StringBuilder pathData = new StringBuilder();

        Double angle = Math.toRadians(210.0);
        Double ratio = 1.03 * scaleFixed;
        String strRadius = PathUtil.format(ratio);

        // main arc
        pathData.append("M");
        pathData.append(PathUtil.format(Math.cos(angle) * ratio));
        pathData.append(" ");
        pathData.append(PathUtil.format(-Math.sin(angle) * ratio));
        pathData.append("A");
        // rx
        pathData.append(strRadius);
        pathData.append(" ");
        // ry
        pathData.append(strRadius);
        // rotation, large arc flag, sweep flag
        angle = Math.toRadians(330.0);
        pathData.append(" 0 0 0 ");
        pathData.append(PathUtil.format(Math.cos(angle) * ratio));
        pathData.append(" ");
        pathData.append(PathUtil.format(-Math.sin(angle) * ratio));

        List<Coord> coordList = new LinkedList<>();
        Double dy = Math.tan(Math.toRadians(30.0)) * scaleFixed;
        coordList.add(new Coord(scaleFixed, dy));
        coordList.add(new Coord(scaleFixed, 1.12 * scaleFixed));
        coordList.add(new Coord(-scaleFixed, 1.12 * scaleFixed));
        coordList.add(new Coord(-scaleFixed, dy));

        RendererUtil.renderPath(writer, pathData + PathUtil.getPathData(coordList, true), "spacer", null);
    }

    private void renderPinMark(Boolean isCover) throws XMLStreamException {
        StringBuilder pathData = new StringBuilder();
        Double size = 0.02 * scaleFixed;
        Double dy = 0d;
        if (isCover && !isDoubleSided) {
            dy = 2.24 * scaleFixed;
        }
        pathData.append("M");
        pathData.append(PathUtil.format(-size));
        pathData.append(" ");
        pathData.append(PathUtil.format(dy));
        pathData.append("L");
        pathData.append(PathUtil.format(size));
        pathData.append(" ");
        pathData.append(PathUtil.format(dy));
        pathData.append("M 0 ");
        pathData.append(PathUtil.format(dy - size));
        pathData.append("L 0 ");
        pathData.append(PathUtil.format(dy + size));

        RendererUtil.renderPath(writer, pathData.toString(), "pinMark", "pinMark");
    }

    private Poly createContour(List<Coord> coords) {
        Poly contour = new PolyDefault();
        for (Coord coordRaw : coords) {
            Coord coord = new Coord();
            CoordUtil.convertWithoutCheck(coordRaw.getX(), coordRaw.getY(), coord, latitude, scale);
            contour.add(new Point2D(coord.getX(), coord.getY()));
        }
        return contour;
    }

    private Poly createClipArea() {
        Poly contour = new PolyDefault();
        Double Dec = latitude > 0 ? latitude - 90 : latitude + 90;
        for (Double RA = 0.0; RA <= 24; RA = RA + 0.5) {
            Coord coord = new Coord();
            CoordUtil.convertWithoutCheck(RA, Dec, coord, latitude, scale);
            contour.add(new Point2D(coord.getX(), coord.getY()));
        }
        return contour;
    }

    public String getCirclePathDataForConstellationName(Coord coord) {
        Double radius = CoordUtil.getDistance(coord, new Coord(0.0, 0.0));
        Double angle = 90.0 + Math.toDegrees(Math.atan2(coord.getY(), coord.getX()));
        return PathUtil.getCirclePathData(new Coord(0.0, 0.0), radius, angle);
    }

    private Double getNormalizedPercent(Double percent) {
        return (100.0 + percent % 100.0) % 100.0;
    }

}
