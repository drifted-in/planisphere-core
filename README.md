Planisphere
===========
A planisphere is a star chart analog computing instrument in the form of two adjustable disks that rotate on a common pivot. It can be adjusted to display the visible stars for any time and date. It is an instrument to assist in learning how to recognize stars and constellations.

This Java commandline utility takes various parameters and produces printed output with all the required parts which can be cut and assembled together into a handy tool for astronomers. 

This code is used as the backend for the web application http://drifted.in/planisphere-app

How to build
============
  * Clone this repository to your local disc.
  * Ensure that JDK 8 is available on your system.
  * Open this Maven based project in your favorite IDE.
  * Build project using the goal `package assembly:single`.

The final jar file with all dependencies included is located in the `target` subfolder. It can be copied or renamed as needed.

How to use
==========
  * See the usage by typing `java -jar planisphere-core-jar-with-dependencies.jar`
  * Actually it requires two additional params:
    * path to the XML config
    * path to the final HTML file (with embedded graphics)

All available values for every particular option are listed in the `usage` screen. If anything is unclear, please let me know.

Aknowledgment
=============
  * Thanks to all who helps to spread the SVG standard (W3C, browser vendors). This format allows to create fancy and colorful vector graphics with minimal coding.
  * Thanks to all [translators](http://drifted.in/planisphere/planisphere-acknowledgment.html) for their time. 
  * This tool utilizes two handy libraries. While the first takes care of milky way clipping, the second reduces the processing time over default JDK StAX parser significantly. I appreciate a lot that both are open sourced and hence available for this project:
    * javaGeom polygon clipper
    * woodstox StAX pull parser
  


  
