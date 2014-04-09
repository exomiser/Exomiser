package de.charite.exomiser;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Iterator;

import jannovar.common.Constants;
import jannovar.exome.Variant;
import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exception.ExomizerSQLException;
import de.charite.compbio.exomiser.exception.ExomizerException;
import de.charite.compbio.exomiser.filter.FrequencyFilter;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the class FrequencyFilter. 
 * Look manually for data in the postgreSQL database and make sure the FrequencyFilter manages to get it.
 * @author peter.robinson@charite.de
 * @date April 13,2013.
 */
public class FrequencyFilterTest implements Constants {

    /** Database handle to the postgreSQL database used by this application. */
    private Connection connection=null;
    /** A prepared SQL statement for thousand genomes frequenc or Exome Server Project frequency. */
    private PreparedStatement findFreqStatement = null;
     /** This is the tolerance for checking equality of floating point numbers for junit.*/
    private float EPSILON = 0.000001f;



     @Before
	 public void setUp() throws SQLException,ClassNotFoundException
    {
	openDatabaseConnection();
	setUpSQLPreparedStatements();


    }



    private void set_up_Query(int chrom,int position,String ref, String alt) throws SQLException
    {
	this.findFreqStatement.setInt(1,chrom);
	this.findFreqStatement.setInt(2,position);
	this.findFreqStatement.setString(3,ref);
	this.findFreqStatement.setString(4,alt);
    }


       


  /**
     * Connect to mysql database and store connection in handle
     * this.connect.*/
    public void openDatabaseConnection() throws SQLException,ClassNotFoundException {
	String URL = "jdbc:postgresql://localhost/exomizer";
        String username = "exome";
        String password = "vcfanalysis";
	Class.forName("org.postgresql.Driver");
	this.connection = DriverManager.getConnection (URL, username,password);
	
    }
     private void setUpSQLPreparedStatements() throws SQLException 
    {
	String query = String.format("SELECT dbsnpmaf,espeamaf,espaamaf,espallmaf " +
				     "FROM frequency " +
				     "WHERE chromosome = ? "+
				     "AND position = ? " +
				     "AND ref = ? " +
				     "AND alt = ? ");

	this.findFreqStatement  = connection.prepareStatement(query);
      
    }


    
    /*
     * The following data is in the db 
     * By hand, it is found that this SNP has a population frequency of 0.32 % in dbSNP.
    */
     @Test
	public void testTGFreq1()  throws SQLException 
	{
	    set_up_Query(5,240563,"C", "T");
	    float dbsnp_AF =  UNINITIALIZED_FLOAT;
	    ResultSet rs =  findFreqStatement.executeQuery();
	    if ( rs.next() ) { /* The way the db was constructed, there is just one line for each such query. */
	      dbsnp_AF = rs.getFloat(1);
	    }
	    rs.close();
	   
	    Assert.assertEquals(0.32,dbsnp_AF,EPSILON);
	}


    /** THe following tests a nonexistent variant. */
      @Test
	public void testTGFreq2()  throws SQLException 
	{
	    set_up_Query(5,24,"C", "T");
	    int TG_AC =  UNINITIALIZED_INT;
	    float TG_AF =  UNINITIALIZED_FLOAT;
	    ResultSet rs =  findFreqStatement.executeQuery();
	    if ( rs.next() ) { /* The way the db was constructed, there is just one line for each such query. */
	      TG_AC = rs.getInt(1);
	      TG_AF = rs.getFloat(2);
	  }
	  rs.close();
	    
	  Assert.assertEquals(UNINITIALIZED_INT,TG_AC);
	  Assert.assertEquals(UNINITIALIZED_FLOAT,TG_AF,EPSILON);
	}

   
     /**
     *  We should find not this data, it does not exist
     */
     @Test
	public void testESPFreq2()  throws SQLException 
	{
	    set_up_Query(15,201011,"A", "G");
	  

	    int esp_minor = UNINITIALIZED_INT;
	    int esp_major = UNINITIALIZED_INT;
	    float esp_freq =  UNINITIALIZED_FLOAT;
	    ResultSet rs = findFreqStatement.executeQuery();
	    if ( rs.next() ) { /* The way the db was constructed, there is just one line for each such query. */
	      esp_minor = rs.getInt(1);
	      esp_major  = rs.getInt(2);
	      esp_freq  = rs.getFloat(3);
	    }
	    rs.close();

	  Assert.assertEquals(UNINITIALIZED_INT, esp_minor);
	  Assert.assertEquals(UNINITIALIZED_INT, esp_major);
	  Assert.assertEquals(UNINITIALIZED_FLOAT,esp_freq,EPSILON);
	}



}