package de.charite.compbio.exomiser.io;

import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is meant to encapsulate operations needed to connect to the
 * Exomiser database, to enter new data and tables into the database, and to
 * extract statistics from the database. Note that we only use high-confidence
 * interactions, corresponding to those with a score of at least 0.7 (700).
 *
 * @see <a href="http://www.ncbi.nlm.nih.gov/pubmed/15608232">STRING: known and
 * predicted protein-protein associations, integrated and transferred across
 * organisms</a>
 * @version 0.17 (16 February, 2014)
 * @author Peter Robinson
 */
public class ExomiserDatabase {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserDatabase.class);
    
    /**
     * Default database settings - these should be overwritten with the settings
     * in jdbc.properties
     */
    private static String driverName = "org.postgresql.Driver";

    /**
     * The database connection url
     */
    private static String url = "jdbc:postgresql://localhost/exomizer";

    private static String user = "exome";

    private static String password = "vcfanalysis";

    /**
     * Set the connection url
     */
    public static void setURL(String URL) {
        ExomiserDatabase.url = URL;
    }

    /**
     * Registering the JDBC driver for the database.
     */
    private static void registerJdbcDriver(String driverClassName) throws ExomizerInitializationException {
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException cnfe) {
            String e = String.format("Couldn't find the driver class: %s", cnfe.toString());
            throw new ExomizerInitializationException(e);
        }
    }

    /**
     * @return true if there is already an entry for the given resource (then we
     * update), otherwise false (then we insert).
     */
    private static boolean versionEntryExists(String resource, Connection connection)
            throws SQLException {
        String query = String.format("SELECT COUNT(*) FROM metadata WHERE resource = ?;");
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, resource);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            int c = rs.getInt(1);
            if (c == 0) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Update version information about a resource for which there is already a
     * column in the database.
     *
     * @param resource A string such as "human-phenotype-ontology.obo"
     * @param versionInfo A string such as "20-11-2013"
     * @param connection Connection to the postgreSQL Exomiser database.
     */
    private static void updateVersion(String resource, String versionInfo, Connection connection)
            throws SQLException {
        String update = "UPDATE metadata SET version = ? WHERE resource = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(update);

        preparedStatement.setString(1, versionInfo);
        preparedStatement.setString(2, resource);
        //System.out.println(preparedStatement);
        preparedStatement.executeUpdate();
    }

    /**
     * Insert new information about a resource for which there is already a
     * column in the database.
     *
     * @param resource A string such as "human-phenotype-ontology.obo"
     * @param versionInfo A string such as "20-11-2013"
     * @param connection Connection to the postgreSQL Exomiser database.
     */
    private static void insertVersion(String resource, String versionInfo, Connection connection)
            throws SQLException {
        String insert = "INSERT INTO metadata(resource,version) VALUES(?,?);";
        PreparedStatement preparedStatement = connection.prepareStatement(insert);
        preparedStatement.setString(1, resource);
        preparedStatement.setString(2, versionInfo);
//        System.out.println(preparedStatement);
        preparedStatement.executeUpdate();
    }

    /**
     * Update or Insert new information about a resource for which there is
     * already a column in the database.
     *
     * @param resource A string such as "human-phenotype-ontology.obo"
     * @param versionInfo A string such as "20-11-2013"
     * @param connection Connection to the postgreSQL Exomiser database.
     */
    public static void updateVersionInfo(String resource, String versionInfo, Connection connection)
            throws ExomizerInitializationException {
        logger.info("Adding version information for {} version: {}", resource, versionInfo);

        try {
            if (versionEntryExists(resource, connection)) {
                updateVersion(resource, versionInfo, connection);
            } else {
                insertVersion(resource, versionInfo, connection);
            }
        } catch (SQLException e) {
            String error = "Problem connecting to SQL Exomizer Database: "
                    + e.toString();
            throw new ExomizerInitializationException(error);
        }
    }

    /**
     * Connect to the configured database.
     *
     * @return the database connection.
     * @throws ExomizerInitializationException
     */
    public static Connection openNewDatabaseConnection()
            throws ExomizerInitializationException {
        try {

            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream("jdbc.properties"));
                //set the connection details here
                driverName = properties.getProperty("exomiser.driverClassName");
                url = properties.getProperty("exomiser.url");
                user = properties.getProperty("exomiser.username");
                password = properties.getProperty("exomiser.password");

            } catch (IOException e) {
                String propertiesError = "Unable to find the jdbc.properties file";
                throw new ExomizerInitializationException(propertiesError + e);
            }
            
            registerJdbcDriver(driverName);
            Connection c = DriverManager.getConnection(url, user, password);
            if (url.startsWith("jdbc:postgresql:")){
                    String sql = "set search_path to 'EXOMISER'";
                    PreparedStatement searchPathStatement = c.prepareStatement(sql);
                    searchPathStatement.execute();
                }
            return c;

        } catch (SQLException e) {
            String error = "Problem connecting to SQL Exomizer Database: "
                    + e.toString();
            throw new ExomizerInitializationException(error);
        }
    }

    /**
     * Deregister the postgreSQL driver when we finish to avoid a resource leak.
     */
    public static void deregisterDriver() {
        try {
            Driver d = DriverManager.getDriver(driverName);
            DriverManager.deregisterDriver(d);
        } catch (SQLException ignore) {
            logger.error("Unable to deregister JDBC driver: {}", ignore);
        }
    }

    /**
     * @param connection A connection to the Exomizer postgreSQL database.
     */
    public static void closeDatabaseConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            logger.error("Problem closing the SQL Exomizer Database: {}", e);
        }
    }

    /**
     * @param connection A connection to the Exomizer postgreSQL database.
     */
    public static void printStatusReport(Connection connection) {
        logger.info("Status of the Exomiser Database:");
        showExomiserTables(connection);
    }

    public static void printVersionReport(Connection connection) {
        logger.info("Version information for the Exomiser Database:");
        Map<String, String> metaDataMap = getVersionInfoMap(connection);
        for (Entry<String, String> entry : metaDataMap.entrySet()) {
                logger.info(String.format("%s%t%s", entry.getKey(), entry.getValue()));
        }
    }

    public static Map<String, String> getVersionInfoMap(Connection connection) {
        Map<String, String> mp = new LinkedHashMap<String, String>();
        String query = "SELECT resource, version FROM metadata;";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                String res = rs.getString(1);
                String ver = rs.getString(2);
                mp.put(res, ver);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            logger.info("problems getting version info {}", e);
        }
        return mp;
    }

    public static String getHPOTermNameAsHTMLAnchor(Connection connection, String id) {
        if (connection == null) {
            return String.format("<a href=\"http://www.human-phenotype-ontology.org/hpoweb/showterm?id=%s\">%s</a>",
                    id, id);
        }
        try {
            String query = "SELECT prefname FROM hpo WHERE id=?";
            PreparedStatement qps = connection.prepareStatement(query);
            qps.setString(1, id);
            ResultSet rs = qps.executeQuery();
            if (rs.next()) {
                String name = rs.getString(1);
                return String.format("<a href=\"http://www.human-phenotype-ontology.org/hpoweb/showterm?id=%s\">%s</a>",
                        id, name);
            }
        } catch (SQLException e) {
            /* no-op */
        }
        /* we get here only if no name was found by the SQL query. */
        return String.format("<a href=\"http://www.human-phenotype-ontology.org/hpoweb/showterm?id=%s\">%s</a>",
                id, id);

    }

    /**
     * Print out the name of the table and the count of rows in the table.
     *
     * @param table Name of the table in the Exomiser database
     * @param connection Handle to the Exomizer postgreSQL database.
     */
    private static void showQueryCount(String table, Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        NumberFormat f = new DecimalFormat("###,###,###,###");
        String query = String.format("SELECT COUNT(*) FROM %s", table);
        ResultSet rs = st.executeQuery(query);
        while (rs.next()) {
            Integer i = rs.getInt(1);
            String d = f.format(i);
            logger.info(String.format("[INFO]\t\t%s: %s rows", table, d));
        }
        rs.close();
        st.close();
    }

    /**
     * Print out a list of all of the tables currently present in the Exomiser
     * database
     *
     * @param connection Handle to the Exomizer postgreSQL database.
     */
    private static void showExomiserTables(Connection connection) {
        ArrayList<String> tableList = new ArrayList<String>();
        System.out.println("[INFO] Tables of the Exomiser database");
        String query = "SELECT table_name\n"
                + "FROM information_schema.tables\n"
                + "WHERE table_schema=\'public\'\n"
                + "AND table_catalog=\'exomizer\'";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                String table = rs.getString(1);
                tableList.add(table);
            }
            Collections.sort(tableList);
            for (String s : tableList) {
                showQueryCount(s, connection);
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("problems showing all Exomiser tables");
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * @param csl comma separated list of Entrez Gene ids.
     */
    public static ArrayList<String> getSeedGeneURLs(Connection connection, String csl) {
        ArrayList<String> urlList = new ArrayList<String>();
        try {

            String query = "SELECT genesymbol FROM omim2gene WHERE entrezgeneid=?";
            PreparedStatement qps = connection.prepareStatement(query);
            String A[] = csl.split(",");
            for (String a : A) {
                String url = String.format("http://http://www.ncbi.nlm.nih.gov/gene/%s", a.trim());
                Integer i = Integer.parseInt(a);
                qps.setInt(1, i);
                ResultSet rs = qps.executeQuery();
                while (rs.next()) {
                    String symbol = rs.getString(1);
                    String anchor = String.format("<a href=\"%s\">%s</a>", url, symbol);
                    urlList.add(anchor);
                    break;
                }
            }
        } catch (SQLException e) {
            System.err.println("[WARN] ExomiserDatabase.getSeedGeneURLs: " + e.getMessage());
        }
        return urlList;
    }

    /**
     * Create the table {@code variant} of the <B>Exomizer</B> SQL database Note
     * that this program will not add data to the databases because it is very
     * much faster to import the data into the database as a "dump", using the
     * postgreSQL COPY command.
     *
     * @param connection A connection to the Exomizer postgreSQL database.
     */
    public static void createNSFPTable(Connection connection) {
        Statement statement = null;
        String drop = "DROP TABLE IF EXISTS variant";
        System.out.println("[INFO] Dropping previous version of variant");
        try {
            statement = connection.createStatement();
            statement.execute(drop);
        } catch (SQLException e) {
            System.err.println("problems dropping variant table");
            System.err.println(e);
            System.exit(1);
        }

        String create = "CREATE TABLE variant ("
                + "chromosome      SMALLINT,"
                + "position        INT,"
                + "ref             CHAR(1),"
                + "alt             CHAR(1),"
                + "aaref           CHAR(1),"
                + "aaalt           CHAR(1),"
                + "aapos           INT,"
                + "sift            FLOAT,"
                + "polyphen        FLOAT,"
                + "mut_taster      FLOAT,"
                + "phyloP          FLOAT,"
                + "PRIMARY KEY(chromosome,position,ref,alt))";
        try {
            System.out.println("[INFO] Creating new table \"variant\"");
            statement = connection.createStatement();
            statement.execute(create);
        } catch (SQLException e) {
            System.err.println("problems creating variant table");
            System.err.println(e);
            System.exit(1);
        }

    }

    public static void createEntrezGeneTable(Connection connection) {
        Statement statement = null;
        String drop = "DROP TABLE IF EXISTS entrez2sym";
        System.out.println("[INFO] Dropping previous version of \"entrez2sym\"");
        try {
            statement = connection.createStatement();
            statement.execute(drop);
        } catch (SQLException e) {
            System.err.println("problems dropping table \"entrez2sym\"");
            System.err.println(e);
            System.exit(1);
        }
        String create = "CREATE TABLE entrez2sym ("
                + "entrezID INTEGER PRIMARY KEY, "
                + "symbol VARCHAR(24))";
        try {
            System.out.println("[INFO] Creating new table \"entrez2sym\"");
            statement = connection.createStatement();
            statement.execute(create);
        } catch (SQLException e) {
            System.err.println("problems creating \"entrez2sym\" table");
            System.err.println(e);
            System.exit(1);
        }
    }

    public static void createStringTable(Connection connection) {
        Statement statement = null;
        String drop = "DROP TABLE IF EXISTS string";
        System.out.println("[INFO] Dropping previous version of \"string\"");
        try {
            statement = connection.createStatement();
            statement.execute(drop);
        } catch (SQLException e) {
            System.err.println("problems dropping table \"string\"");
            System.err.println(e);
            System.exit(1);
        }
        String create = "CREATE TABLE string ("
                + "entrezA INTEGER, "
                + "entrezB INTEGER, "
                + "score INTEGER,"
                + "PRIMARY KEY(entrezA,entrezB));";
        try {
            System.out.println("[INFO] Creating new table \"string\"");
            statement = connection.createStatement();
            statement.execute(create);
        } catch (SQLException e) {
            System.err.println("problems creating \"string\" table");
            System.err.println(e);
            System.exit(1);
        }

    }

    /**
     * Create the two main tables of the <B>Exomizer</B> SQL database Note that
     * this program will not add data to the databases because it is very much
     * faster to import the data into the database as a "dump", using the
     * postgreSQL COPY command. Therefore, the function is the only one in the
     * program that uses the jbdc interface to do something with the database.
     * <P>
     * Note that the combination of MIM number and gene id should be unique in
     * the table, but more than one MIM can be attached to some gene.
     *
     * @param connection A connection to the Exomizer postgreSQL database.
     */
    public static void createOMIMTable(Connection connection) {
        Statement statement = null;
        String drop = "DROP TABLE IF EXISTS omim";
        System.out.println("[INFO] Dropping previous version of \"omim\"");
        try {
            statement = connection.createStatement();
            statement.execute(drop);
        } catch (SQLException e) {
            System.err.println("problems dropping table omim");
            System.err.println(e);
            System.exit(1);
        }

        String omimcreate = "CREATE TABLE omim ("
                + "phenmim INTEGER, "
                + "genemim INTEGER, "
                + "diseasename VARCHAR(2056),"
                + "gene_id INTEGER,"
                + "type CHAR,"
                + "inheritance CHAR,"
                + "PRIMARY KEY(gene_id,phenmim,genemim));";
        try {
            System.out.println("[INFO] Creating new table \"omim\"");
            statement = connection.createStatement();
            statement.execute(omimcreate);
        } catch (SQLException e) {
            System.err.println("problems creating omim table");
            System.err.println(e);
            System.exit(1);
        }
    }

    public static void createOrphanetTable(Connection connection) {
        Statement statement = null;
        String drop = "DROP TABLE IF EXISTS orphanet";
        System.out.println("[INFO] Dropping previous version of \"orphanet\"");
        try {
            statement = connection.createStatement();
            statement.execute(drop);
        } catch (SQLException e) {
            System.err.println("problems dropping table orphanet");
            System.err.println(e);
            System.exit(1);
        }
        String omimcreate = "CREATE TABLE orphanet ("
                + "orphaNumber INTEGER, "
                + "entrezGeneID INTEGER,"
                + "diseasename VARCHAR(2056),"
                + "PRIMARY KEY(orphaNumber,entrezGeneID));";
        try {
            System.out.println("[INFO] Creating new table \"orphanet\"");
            statement = connection.createStatement();
            statement.execute(omimcreate);
        } catch (SQLException e) {
            System.err.println("problems creating orphanet table");
            System.err.println(e);
            System.exit(1);
        }

    }

    /**
     * Create the postgresql table {@code frequency} of the <B>Exomizer</B>
     * postgreSQL database.
     * <P>
     * Note that this program will not add data to the databases because it is
     * very much faster to import the data into the database as a "dump", using
     * the postgreSQL COPY command (This has to be done separately when setting
     * up the Exomizer database).
     *
     * @param connection A connection to the Exomizer postgreSQL database.
     */
    public static void createFrequencyTable(Connection connection) {
        Statement statement = null;
        String drop = "DROP TABLE IF EXISTS frequency";
        System.err.println("[INFO] Dropping previous version of \"frequency\"");
        try {
            statement = connection.createStatement();
            statement.execute(drop);
        } catch (SQLException e) {
            System.err.println("problems dropping table frequency");
            System.err.println(e);
            System.exit(1);
        }

        String indexStmt = "CREATE INDEX freqQ ON frequency(chromosome,position,ref,alt)";

        String createStmt = "CREATE TABLE frequency ("
                + "chromosome      SMALLINT,"
                + "position        INT,"
                + "ref             VARCHAR(1024),"
                + "alt             VARCHAR(1024),"
                + "rsid            INT,"
                + "dbSNPmaf        FLOAT,"
                + "espEAmaf        FLOAT,"
                + "espAAmaf        FLOAT,"
                + "espAllmaf       FLOAT)";  /*PRIMARY KEY(chromosome,position,ref,alt)*/

        try {
            System.err.println("[INFO] Creating \"frequency\" table and index");
            statement = connection.createStatement();
            statement.execute(createStmt);
            statement = connection.createStatement();
            statement.execute(indexStmt);
        } catch (SQLException e) {
            System.err.println("problems creating frequency table");
            System.err.println(e);
            System.exit(1);
        }

    }

    /**
     * Create the two a tables of the <B>Exomizer</B> SQL database that will
     * hold the name/synonym to ID mappings for HPO terms.
     *
     * @param connection A connection to the Exomizer postgreSQL database.
     */
    public static void createHPOTable(Connection connection) {
        Statement statement = null;
        String drop = "DROP TABLE IF EXISTS hpo";
        System.err.println("[INFO] Dropping previous version of \"hpo\"");
        try {
            statement = connection.createStatement();
            statement.execute(drop);
        } catch (SQLException e) {
            System.err.println("problems dropping table \"hpo\"");
            System.err.println(e);
            System.exit(1);
        }
        String createStmt = "CREATE TABLE hpo ("
                + "lcname   VARCHAR(256) PRIMARY KEY,"
                + "id       CHAR(10),"
                + "prefname VARCHAR(256));";
        String idxStmt = "CREATE INDEX hpoidx ON hpo(id);";
        try {
            System.err.println("[INFO] Creating \"hpo\" table and index");
            statement = connection.createStatement();
            statement.execute(createStmt);
            statement = connection.createStatement();
            statement.execute(idxStmt);
        } catch (SQLException e) {
            System.err.println("problems creating \"hpo\" table");
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * Create a table for holding NCBI ClinVar information.
     *
     * @param connection A connection to the Exomizer postgreSQL database.
     */
    public static void createClinVarTable(Connection connection) {
        Statement statement = null;
        String drop = "DROP TABLE IF EXISTS clinvar";
        String create = "CREATE TABLE clinvar ("
                + "id  INT PRIMARY KEY,"
                + /* This is the RSV accession number */ "chromosome      SMALLINT,"
                + "position        INT,"
                + "signif  CHAR);";
        String idxStmt = "CREATE INDEX cvidx ON clinvar(chromosome,position);";
        try {
            statement = connection.createStatement();
            statement.execute(drop);
            statement.execute(create);
            statement.execute(idxStmt);
        } catch (SQLException e) {
            System.err.println("problems dropping table \"hgmdpro\"");
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * Create the two a tables of the <B>Exomizer</B> SQL database that will
     * hold the data from the HGMD pro version. One table holds the disease/gene
     * combinations, and the other holds the individual mutations.
     *
     * @param connection A connection to the Exomizer postgreSQL database.
     */
    public static void createHGMDproTable(Connection connection) {
        Statement statement = null;
        String drop = "DROP TABLE IF EXISTS hgmdpro";
        String drop2 = "DROP TABLE IF EXISTS hgmddisease";
        System.err.println("[INFO] Dropping previous version of \"hgmd\"");
        try {
            statement = connection.createStatement();
            statement.execute(drop);
            statement.execute(drop2);
        } catch (SQLException e) {
            System.err.println("problems dropping table \"hgmdpro\"");
            System.err.println(e);
            System.exit(1);
        }
        String createStmt1 = "CREATE TABLE hgmddisease ("
                + "id  INT PRIMARY KEY,"
                + "disease     VARCHAR(256),"
                + "genesym     VARCHAR(24));";

        String createStmt2 = "CREATE TABLE hgmdpro ("
                + "id   INT REFERENCES hgmddisease(id),"
                + "cDNA        VARCHAR(128),"
                + "prot        VARCHAR(128),"
                + "chromosome      SMALLINT,"
                + "position        INT,"
                + "pmid            INT,"
                + "hgmdAcc     VARCHAR(24));";

        String idxStmt = "CREATE INDEX hgmdproidx ON hgmdpro(chromosome,position);";
        /* Note that the id is a foreign key for hgmdpro */

        try {
            System.err.println("[INFO] Creating \"hpo\" table and index");
            statement = connection.createStatement();
            statement.execute(createStmt1);
            statement.execute(createStmt2);
            statement.execute(idxStmt);
        } catch (SQLException e) {
            System.err.println("problems creating \"hpo\" table");
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * This function directly enters name/id pairs into the Exomiser database
     *
     * @param mp A map with key ("abnormality of X"), value ("HP:1234567").
     * @param connection A connection to the Exomizer postgreSQL database.
     */
    public static void populateHPOTable(Connection connection, HashMap<String, String> mp) {
        String insert = "INSERT INTO hpo (lcname,id,prefname) VALUES(?,?,?);";
        Iterator<String> it = mp.keySet().iterator();
        int c = 0;
        try {
            PreparedStatement instPS = connection.prepareStatement(insert);
            while (it.hasNext()) {
                String name = it.next();
                String id = mp.get(name);
                instPS.setString(1, name);
                instPS.setString(2, id);
                instPS.setString(3, id);
                //System.out.println("XX:" + instPS);
                instPS.executeUpdate();
                c++;
                if (c % 1000 == 0) {
                    System.out.println("[INFO] Inserted " + c + " entries into database");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Could not load HPO data");
            System.exit(1);
        }
        System.out.println("[INFO] Finished - Inserted " + c + " entries into database");

    }

    /**
     * Create the two main tables of the <B>Exomizer</B> SQL database Note that
     * this program will not add data to the databases because it is very much
     * faster to import the data into the database as a "dump", using the
     * postgreSQL COPY command. Therefore, the function is the only one in the
     * program that uses the jbdc interface to do something with the database.
     * <P>
     * Note that the combination of MIM number and gene id should be unique in
     * the table, but more than one MIM can be attached to some gene.
     */
    public static void createPhenoseriesTables(Connection connection) {
        Statement statement = null;
        String drop = "DROP TABLE IF EXISTS phenoseries";
        try {
            System.out.println("[INFO] dropping table \"phenoseries\"");
            statement = connection.createStatement();
            statement.execute(drop);
        } catch (SQLException e) {
            System.err.println("problems dropping table phenoseries");
            System.err.println(e);
            System.exit(1);
        }

        String create = "CREATE TABLE phenoseries ("
                + "seriesID INTEGER, "
                + "name VARCHAR(2056),"
                + "genecount INTEGER,"
                + "PRIMARY KEY(seriesID));";
        try {
            System.out.println("[INFO] creating table \"phenoseries\"");
            statement = connection.createStatement();
            statement.execute(create);
        } catch (SQLException e) {
            System.err.println("problems creating phenoseries table");
            System.err.println(e);
            System.exit(1);
        }
        create = "CREATE TABLE omim2gene ("
                + "mimDiseaseID INTEGER, "
                + "mimDiseaseName  VARCHAR(2056),"
                + "cytoBand VARCHAR(64), "
                + "mimGeneID INTEGER, "
                + "entrezGeneID INTEGER, "
                + "geneSymbol VARCHAR(64), "
                + "seriesID INTEGER, "
                + "PRIMARY KEY(mimDiseaseID,seriesID));";
        drop = "DROP TABLE IF EXISTS omim2gene";
        String indexStmt = "CREATE INDEX omim2geneQ ON omim2gene(entrezGeneID)";
        try {
            System.out.println("[INFO] dropping table \"omim2gene\"");
            statement = connection.createStatement();
            statement.execute(drop);
        } catch (SQLException e) {
            System.err.println("problems dropping table omim2gene");
            System.err.println(e);
            System.exit(1);
        }
        try {
            System.out.println("[INFO] creating table \"omim2gene\"");
            statement = connection.createStatement();
            statement.execute(create);
        } catch (SQLException e) {
            System.err.println("problems creating omim2gene table");
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * Create a table to hold metadata about the data used to create the
     * database, including mainly the version and the date last modified.
     * <P>
     * Note that the combination of MIM number and gene id should be unique in
     * the table, but more than one MIM can be attached to some gene.
     */
    public static void createMetaTable(Connection connection) {
        Statement statement = null;
        String drop = "DROP TABLE IF EXISTS metadata";
        try {
            System.out.println("[INFO] dropping table \"metadata\"");
            statement = connection.createStatement();
            statement.execute(drop);
        } catch (SQLException e) {
            System.err.println("problems dropping table metadata");
            System.err.println(e);
            System.exit(1);
        }

        String create = "CREATE TABLE metadata ("
                + "resource VARCHAR(1024), "
                + "version  VARCHAR(1024));";
        try {
            System.out.println("[INFO] creating table \"metadata\"");
            statement = connection.createStatement();
            statement.execute(create);
        } catch (SQLException e) {
            System.err.println("problems creating metadata table");
            System.err.println(e);
            System.exit(1);
        }
    }

}
/* eof */
