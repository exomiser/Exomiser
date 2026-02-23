Exomiser Prioritiser REST API
===

Requirements
--
The jar file built from this maven module, or pre-built versions can be found on GitHub e.g.
https://github.com/exomiser/Exomiser/releases/download/14.1.0/exomiser-rest-prioritiser-14.1.0.jar

And a current version of the phenotype data. The data is updated a few times a year and the release announcements are
also on GitHub: https://github.com/exomiser/Exomiser/discussions/categories/data-release

In this example we're using the 2410_phenotype data release which can be found here:
https://g-879a9f.f5dc97.75bc.dn.glob.us/data/2410_phenotype.zip


Setup
--
This is a Spring Boot application, which means it can probably be configured to run the way you need for your setup. It
will require configuration either using a properties file, which you can find in
`src/main/resources/application.properties`. Alternatively, these can be provided as command-line arguments or environment
variables when launching the application. More info on configuration of Spring Boot applications can be found in their
[docs](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files)

To set up on your local machine:
-  Create a new directory called `exomiser` and download the jar, application.properties and zip files into this. Extract
   the zip file so that you have should now have a `2410_phenotype` subfolder.

- Edit the application.properties so that it looks like this:
  ```
  exomiser.data-directory=full/path/to/your/new/exomiser/dir
  exomiser.phenotype.data-version=2410
  ```
  You might need to delete the keys starting `info` - they will be present in the app, and you shouldn't need to change them.

- In a terminal, launch the app using `java -jar exomiser-rest-prioritiser-14.1.0.jar` from the `exomiser` folder. You
  should see a bunch of logging output which stops after a few seconds with these lines:
    ```
    2024-12-18T10:23:33.827Z  INFO 452968 --- [exomiser-prioritiser-service] [           main] o.m.e.r.p.api.PrioritiserController      : Started PrioritiserController with GeneIdentifier cache of 19762 entries
    2024-12-18T10:23:34.249Z  INFO 452968 --- [exomiser-prioritiser-service] [           main] o.s.b.a.e.web.EndpointLinksResolver      : Exposing 1 endpoint(s) beneath base path '/actuator'
    2024-12-18T10:23:34.305Z  INFO 452968 --- [exomiser-prioritiser-service] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8085 (http) with context path '/exomiser-prioritiser'
    2024-12-18T10:23:34.322Z  INFO 452968 --- [exomiser-prioritiser-service] [           main] o.m.e.r.p.ExomiserPrioritiserServer      : Started ExomiserPrioritiserServer in 6.056 seconds (process running for 6.676)
    ```

It is now ready to use. Where you keep the jar and the data files is up to you. You just need to tell the application
where the data can be found using the full path to the parent directory where the data has been unpacked in the
application.properties. For example, if you unpacked the data to `/data/2410_phenotype` then`exomiser.data-directory=/data`
and `exomiser.phenotype.data-version=2410`.

Note that if you have an existing Exomiser CLI installation, you can add this jar file to that directory and the REST
service will use the properties from the existing `application.properties`. Alternatively the `exomiser.data-directory`
and `exomiser.phenotype.data-version` can be supplied as command-line arguments when starting the jar without the need
for an `application.properties` file.


Running
---
There is an OpenAPI 3 page which should be accessible here if everything went successfully:

```shell
http://localhost:8085/exomiser-prioritiser/swagger-ui/index.html
```

This contains examples of the input parameters and the expected output.