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
   java -XX:+useG1GC -Xms8G -Xmx8G ${project.name}-${project.version}.jar
 ```
 The port will default to 8080, but it can be changed by specifying it either on the command line:
 ```shell
   java -XX:+useG1GC -Xms8G -Xmx8G ${project.name}-${project.version}.jar --server.port=8090
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
