The Exomiser - DB build
===============================================================

This maven project is used to build the Exomiser database used by the Exomiser. 
It can build both a local H2 (http://h2database.com/html/main.html) and PostgreSQL 
(http://www.postgresql.org/) versions of the database.

The application requires configuration in a couple of places depending on how it
is to be used.

It functions by loading in the Resources from ResourceConfig and then will attempt to
download, extract, parse and then import the parsed resources into a freshly created
database schema called EXOMISER. 

Flyway (http://flywaydb.org/) maven migrations for importing data into the database
can be performed from an IDE or the command-line using the appropriate maven
profile, for example:

    mvn -P migrate-H2 flyway:info
    
or specified from the command-line:

    java -jar exomiser-db-${project.version}.jar --exomiser.h2.url="jdbc:h2:file:/data/exomiser-build/exomiser;MODE=Postg
    reSQL;LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0;MV_STORE=FALSE" --data.path=/data/exomiser-build/

In order for this to work the settings in the POM file may need to be changed. These
won't work without any data to import which is what App.main is all about.   

Running the application from App.main requires that the files in src/main/resources 
are correctly configured. These files and other configurations are injected into 
the application by the classes in the exomiser.config package:

* org.monarchinitiative.exomiser.config.AppConfig
    
Injects the data from the app.properties 

* org.monarchinitiative.exomiser.config.DataSourceConfig
    
Injects the DataSources for the database connections

* org.monarchinitiative.exomiser.config.ResourceConfig
    
Injects the Resources - these specify where the data should be downloaded 
from, how it should be handled in order that the parser can parse it, what parser 
is needed to parse it, which other files should be parsed with it and what the 
output file should be called.

Commenting out a resource means it won't be processed. If the resource was part 
of a parserGroup then that entire group will not be handled and ultimately the 
database build will halt at the migration of the resource or group's parsedFileName.
    
The application can also be run from the command line. The full download, unpack, parse and load database looks like so:

```bash
java -jar exomiser-db-${project.version}-SNAPSHOT.jar 
        --data.path=/data/exomiser-build/data 
        --downloadResources=true 
        --extractResources=true
        --parseResources=true 
        --dumpPhenoDigmData=true
        --migrateH2=true
        --exomiser.h2.url="jdbc:h2:file:/data/exomiser-build/exomiser;MODE=PostgreSQL;LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0;MV_STORE=FALSE"
```
These are overriding the properties in ```app.properties``` which default to false. The ```exomiser.h2.url``` overrides the database properties in ```jdbc.properties```.

    
In general you shouldn't need to touch anything but, in case you do the resources
are detailed below and check the log output:

## src/main/resources

### app.properties

This will provide the application with the location of where you want it to
run, download, unpack and process the resources. The resources are described in
the exomiser.config.ResourceConfig class.  
    
### jdbc.properties
    
Contains the database connection settings for jdbc used by Flyway when called
from App.main.

## src/main/resources/data

This is where some static data required by some parsers but which requires 
manual processing to produce is stored. The resources are referred to in the 
exomiser.config.ResourceConfig class but are moved into the process directory by 
spring.

### pheno2gene.txt

This requires extensive messing about with a one-off dump-file from OMIM, a 
perl parser, data from Entrez Gene and some java parsing. Given this was a one-off
it seemed best to just include the final processed file which can be updated 
whenever appropriate. 

### ucsc_hg19.ser

Requires the following UCSC known genes files - these are defined and downloaded as resources
so they will be found, ready to use in data/extracted:

        http://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/knownGene.txt.gz
        http://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/kgXref.txt.gz
        http://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/knownGeneMrna.txt.gz
        http://hgdownload.soe.ucsc.edu/goldenPath/hg19/database/knownToLocusLink.txt.gz

then convert to ucsc.ser with jannovar:

    ant annotator

    java -Xmx2G -jar Annotator.jar -U knownGene.txt -M knownGeneMrna.txt -X kgXref.txt -L knownToLocusLink.txt -S ucsc.ser
or...
    java -Xms1G -Xmx2G -jar Jannovar.jar --create-ucsc


## src/main/resources/db/migration

Contains the database schema and the import sql files for the H2 database. The
PostgreSQL migrations are run by Flyway as java migrations. Consequently they are
found in src/main/java/db/migration/postgres.


## Adding a Resource

* Add a new Bean to org.monarchinitiative.exomiser.config.ResourceConfig and ensure 
this is loaded in the ResourceConfig.resources() method.

* Add a new Parser and or ParserGroup if there are several parsers which need to 
exchange data.

* Add a new migration for the data which needs loading. These need to be 
duplicated - one for the H2 database, the other for PostgreSQL.
