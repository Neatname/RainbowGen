# RainbowGen
This is a web app that makes art. You can see a live version [here](https://rainbowgen-nmiles.herokuapp.com/).

To build the project, use `mvn clean install`

To run the project locally, use `java -jar target/Server.jar`

You must also have a local environment variable named PORT with the value "8080". This is because
this project is designed to run on Heroku, and Heroku requires applications to bind to the port 
specified in the PORT variable.

For more information about how the image generation algorithms work, see the documentation for each
algorithm. Currently, I only have
[StainedGlass](https://github.com/Neatname/RainbowGen/blob/master/src/main/java/com/nmiles/rainbowgen/generator/StainedGlass.java)
,
[FastIterator](https://github.com/Neatname/RainbowGen/blob/master/src/main/java/com/nmiles/rainbowgen/generator/FastIterator.java)
and
[GlassIterator](https://github.com/Neatname/RainbowGen/blob/master/src/main/java/com/nmiles/rainbowgen/generator/GlassIterator.java)
in the web application, but I have many more algorithms that take much longer to run in the desktop version.
Sadly, they are just too slow to reasonably generate images on a shared server such as the Heroku platform.
