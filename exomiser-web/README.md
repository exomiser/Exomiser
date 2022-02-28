The Exomiser - Web Code
===============================================================

##Setup

This is a Spring Boot jar application meaning it runs using its own embedded Tomcat server. In this case we're using the embedded version
of the H2 database as this performs well, in particular because the data is local
to the machine.

Once built, to run the application will need to:

  1. Have the exomiser-cli data directory somewhere visible to the machine you're running the server from.
  1. Copy the ${project.name}-${project.version}.jar to the machine you want to run it from.
  1. Make sure you have the ``application.properties`` file alongside the jar file. The defaults can be found in /src/main/resources/application.properties:
  
  ```properties
     
    #due to hardware and page timeout constraints
    #we need to limit the maximum number of variants which will be analysed
    maxVariants=100000
    #max genes especially hits the RAM usage for rendering a page so this is limited
    maxGenes=200
    
    exomiser.data-directory=/full/system/path/to/exomiser/data
  ```
##Running
Launch the application by intoning the incantation thus:
 ```shell
   java -jar -XX:+UseG1GC -Xms8G -Xmx8G ${project.name}-${project.version}.jar
 ```
 The port will default to 8080, but it can be changed by specifying it either on the command line:
 ```shell
   java -jar -XX:+UseG1GC -Xms8G -Xmx8G ${project.name}-${project.version}.jar --server.port=8090
 ```
 or by adding it to the ``application.properties``
 ```properties
   server.port=8090
 ```
 Further details can be found on the [Spring Boot Deployment](http://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html) reference.
 
 Check all is well by visiting the submission page:
 ```
   http://localhost:8080/exomiser/submit
 ```

The submission page now contains a warning, shown by default, alerting users not to input sensitive clinical data. 
If the instance *is* running in a clinically-compliant setting, the banner can be removed by setting the field 
```exomiser.web.clinical-instance``` to ```true``` in the application.properties file:

```properties
exomiser.web.clinical-instance=true
```

or override this by launching the application using the command-line option

```shell
--exomiser.web.clinical-instance=true
```

## Running the Docker image

To run the image you will need the standard exomiser directory layout to mount as separate volumes and supply
an `application.properties` file or environmental variables to point to the data required _e.g._

```shell
docker run \
-v "/data/exomiser-data:/exomiser-data" \
-v "/opt/exomiser/exomiser-config/:/exomiser" \
-p 8080:8080 \
exomiser/exomiser-web:latest \
--spring.config.location=/exomiser/application.properties
```

or using Spring configuration arguments instead of the `application.properties`:

```shell
docker run -v "/data/exomiser-data:/exomiser-data" \
-p 8080:8080 \
exomiser/exomiser-web:latest \
--exomiser.data-directory=/exomiser-data \
--exomiser.hg19.data-version=2109 \
--exomiser.hg19.variant-white-list-path=2109_hg19_clinvar_whitelist.tsv.gz \
--exomiser.phenotype.data-version=2109
```

Here the contents of `/opt/exomiser/exomiser-config` is simply the `application.properties` file.

```shell
$ tree /opt/exomiser/exomiser-config/
exomiser-config/
└──application.properties
```

and the `application.properties` contents for hg38:

```yaml
exomiser.data-directory=/exomiser-data

## hg38 config
exomiser.hg38.data-version=2109
#exomiser.hg38.remm-path=${exomiser.data-directory}/remm/ReMM.v0.3.1.post1.hg38.tsv.gz
#exomiser.hg38.local-frequency-path=${exomiser.data-directory}/local/local_frequency_test_hg38.tsv.gz
exomiser.hg38.variant-white-list-path=${exomiser.hg38.data-version}_hg38_clinvar_whitelist.tsv.gz

## phenotype config
exomiser.phenotype.data-version=2109
```

Resizing the JVM
-----------------------

Running a WGS analysis can take a few GB of RAM, depending on the size of the sample in question. If you need to
increase the max memory of the JVM, include the following environment variable:

```shell
-e JAVA_TOOL_OPTIONS="-Xmx8G" 
```
