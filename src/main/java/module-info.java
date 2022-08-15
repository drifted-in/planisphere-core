module in.drifted.planisphere {

    requires java.xml;
    requires org.locationtech.jts;

    exports in.drifted.planisphere;
    exports in.drifted.planisphere.l10n;
    exports in.drifted.planisphere.model;
    exports in.drifted.planisphere.renderer.html;
    exports in.drifted.planisphere.renderer.svg;
    exports in.drifted.planisphere.resources.loader;
    exports in.drifted.planisphere.util;
}
