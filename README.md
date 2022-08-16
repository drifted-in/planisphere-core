# Planisphere

A planisphere is a star chart analog computing instrument in the form of two adjustable
disks that rotate on a common pivot. It can be adjusted to display the visible stars
for any time and date. It is an instrument to assist in learning how to recognize stars
and constellations.

This Java command-line utility takes various parameters and produces printed output with
all the required parts which can be cut and assembled together into a handy tool for astronomers.

This code is used as the backend for the web application https://drifted.in/planisphere-app

# How to build

  * Clone this repository to your local disc.
  * Ensure that JDK 17 is available on your system.
  * Open this Maven based project in your favorite IDE.
  * Build the project.

The final jar file with all dependencies included is located in the `target` subfolder.
It can be copied or renamed as needed.

# How to use

  * See the usage by typing `java -jar planisphere-core-jar-with-dependencies.jar`
  * Actually it requires two additional params:
    * path to the XML config
    * path to the final HTML file (with embedded graphics)

All available values for every particular option are listed in the `usage` screen.
If anything is unclear, please let me know.

# Acknowledgments

  * Thanks to all who helped spreading the SVG standard on the web (W3C, browser vendors).
  * This tool utilizes two handy open source libraries. Kudos to all involved in these
    projects:
    * [JTS Topology Suite](https://github.com/locationtech/jts) (intersecting polygons)
    * [Woodstox StAX pull parser](https://github.com/FasterXML/woodstox) (speeding up
      XML processing)
  * This tool would have never been spread so widely without translating to so many
    languages. Let me thank all volunteers (alphabetically by country):
    * Kutaibaa Akraa (Arabic)
    * Boriana Bontcheva (Bulgarian)
    * Han-Chiang Chou (Chinese, Japanese)
    * Jan-Gerard van der Toorn (Dutch)
    * Lauri Säisä (Finnish)
    * Nicolas Desmoulins (French)
    * Harald Greier (German)
    * Dobos Vera (Hungarian)
    * Hariyadi Putraga (Indonesian)
    * Andrea Di Dato (Italian)
    * Mantas Kubis (Lithuanian)
    * Kiarash Danesh (Persian)
    * Bartosz Wiklak (Polish)
    * Eslley Scatena (Portuguese - Brazil)
    * Frincu Marc (Romanian)
    * Rok Vidmar, Andrej Lajovic (Slovenian)
    * Mario Gaitano (Spanish)
    * Anders Rydén (Swedish)
    * Андрій Стрільчук (Ukrainian)

# Localization

All texts you can see in the application can be translated online via
[Transifex](https://www.transifex.com/) web application. You just need to sign up
to this service, visit the [Planisphere](https://www.transifex.com/drifted-in/planisphere)
project and ask for creating a new localization group for your language (or join
any current one).

There is a single localizable resource for the Planisphere project called messages.
When it is selected and appropriate language is chosen, the following interface is displayed.

![Transifex Interface](/transifex.png)

On the left there is the list of all the texts available. On the right there are details
for the every particular item. The Key helps in understanding the context.

In the example above we need to translate N. The Key tells we are translating the north
cardinal point abbreviation. When S (czech localization) is entered into the appropriate
box and Save Changes button is pressed, we can continue to the next item.

After completing and final reviewing the translation (by administrator) it is automatically
stored in the [GitHub repository](https://github.com/drifted-in/planisphere-core/tree/master/src/main/resources/in/drifted/planisphere/resources/localizations).
The online application is updated usually in next few days.

# History

  * 2022, Aug - Non-functional improvements (replacing abandoned polygon clipper library
                with active one, switching to more recent JDK, code cleanup).
  * 2014, Jun - Adding a RTL support. Enhancing theming support. Open sourcing the code
                via GitHub. Managing translations via Transifex.
  * 2014, Apr - Adding a support for equatorial latitudes. Generating the set of HTML
                pages instead of PDF outputs for printing purposes.
  * 2012, Feb - Releasing the Planisphere as an SVG based web application with Unicode
                support and rendering both graphics and PDF outputs using SVG templates.
  * 2004, Oct - Implementing Milky Way and Ra/Dec coords.
  * 2004, Apr - Adding a support for the southern hemisphere.
  * 2003, Mar - Releasing the first version. At that time as an OpenGL based Windows
                desktop application.
  * 2002, May - Writing first few lines of code :-)
