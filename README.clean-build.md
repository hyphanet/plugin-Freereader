To get a completely clean build, you must compile the following and put them in lib/ :

jdom.jar (reference version 1.1.1)
jarjar-1.0.jar
jaxen-1.1.1.jar
rome-1.0.jar

Also, we follow the Freenet plugins convention: we expect a compiled Freenet tree in ../fred/. In particular we use freenet.jar and freenet-ext.jar from ../fred/lib/.

To use the freenet jars from a different directory, create the file `override.properties` and define the locations there.

Example:

    freenet-cvs-snapshot.location = ../freenet/freenet.jar
    freenet-ext.location = ../freenet/freenet-ext.jar
