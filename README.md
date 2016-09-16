# RainbowGen
This is a web app that makes art. You can see a live version [here](https://rainbowgen-nmiles.herokuapp.com/).

To build the project, use `mvn clean install`

To run the project, use `java -jar target/Server.jar`

For more information about how the image generation algorithms work, see the documentation for each
algorithm. Currently, I only have
[StainedGlass](https://github.com/Neatname/RainbowGen/blob/master/src/main/java/com/nmiles/rainbowgen/generator/StainedGlass.java)
and
[FastIterator](https://github.com/Neatname/RainbowGen/blob/master/src/main/java/com/nmiles/rainbowgen/generator/FastIterator.java)
in the web application, but I have many more algorithms that take much longer to run in the desktop version.
Sadly, they are just too slow to reasonably generate images on a shared server.
