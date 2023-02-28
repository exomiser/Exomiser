PhenIX Dependencies
==

This module exists to install the missing maven artifacts required to build the phenix prioritiser in exomiser-core. It 
is a dirty hack as they were never released to maven central and the server they were hosted on (sonumina.de/maven) has
since vanished leaving the project un-buildable. 

This module will install the two artefacts to the local .m2 repository enabling the rest of the project to be built. It
uses the `maven-install-plugin` to automatically perform the equivalent of this script:

```shell
#!/bin/sh
chmod +x mvnw
./mvnw install:install-file -DgroupId=de.sonumina -DartifactId=javautil -Dversion=0.0.1 -Dpackaging=jar -Dfile=./lib/javautil-0.0.1.jar
./mvnw install:install-file -DgroupId=ontologizer -DartifactId=ontologizer -Dversion=0.0.1 -Dpackaging=jar -Dfile=./lib/ontologizer-0.0.1.jar
```