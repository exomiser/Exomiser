The Exomiser - Web Code
===============================================================

##Setup

This is designed so that the database connection is provided to the application 
by the container using JNDI. Here we're using Tomcat so you need to configure 
Tomcat to connect to the database. In this case we're using the embedded version
of the H2 database as this performs well, in particular because the data is local
to the machine.

To do this you will need to:

1. Add H2.jar to the tomcat /lib directory   
2. Add the following snippet to tomcat server.xml under the GlobalNamingResources section:

    ```xml
    <Resource name="jdbc/exomiserDataSource" auth="Container" type="javax.sql.DataSource"
        maxActive="100" 
        maxIdle="3" 
        minIdle="3" 
        maxWait="10000" 
        factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
        username="sa" 
        password="" 
        driverClassName="org.h2.Driver"
        validationQuery="select 1" 
        testOnBorrow="true" 
        logAbandoned="true"
        url="jdbc:h2:file:{$path_to_h2_database};MODE=PostgreSQL;SCHEMA=EXOMISER;DATABASE_TO_UPPER=FALSE;IFEXISTS=TRUE;AUTO_RECONNECT=TRUE;DB_CLOSE_ON_EXIT=TRUE;AUTO_SERVER=TRUE;"/>
    ```

3. Add this snippet to the context.xml of the WAR file (this is already done, but if you change the name, you'll need to change this):

    ```xml    
    <ResourceLink global="jdbc/exomiserDataSource" name="jdbc/exomiserDataSource" type="javax.sql.DataSource"/>
    ```

4. If you're using a pre-built war file change the path of dataDir in WEB-INF/classes/exomiser.properties to point to the location of these files on your webserver.

