package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.resources.ResourceOperationStatus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse the good old human-phenotype-ontology.obo file (or alternatively the
 * hp.obo file from our Hudson server). We want to create a table in the
 * database with lcname - HP:id - preferred name, where lcname is the lower-case
 * name or synonym, ID is the HPO id, and preferred name is the HPO Term name.
 * We lower-case the name ans synonyms to be able to search only over lower
 * cased names for the autosuggestion. However, we want to display the preferred
 * name in the end.
 *
 * @version 0.04 (27 November, 2013)
 * @author Peter Robinson
 */
public class HPOOntologyFileParser implements Parser {

    private static final Logger logger = LoggerFactory.getLogger(HPOOntologyFileParser.class);

    /**
     * A variable that keeps count of the number of rows added to the database.
     */
    int n_row = 0;

    /**
     * @param conn COnnection to the Exomiser database.
     */
    public HPOOntologyFileParser() {
    }

    /**
     * This function does the actual work of parsing the HPO file.
     *
     * @param inPath Complete path to human-phenotype-ontology.obo or hp.obo file.
     * @param outPath PAth where output file is to be written
     */
    @Override
    public ResourceOperationStatus parse(String inPath, String outPath) {
        logger.info("Parsing HPO file: {}. Writing out to: {}", inPath, outPath);
        int termCount = 0; /* count of terms */

        try (FileReader fileReader = new FileReader(inPath);
                BufferedReader br = new BufferedReader(fileReader);
                FileWriter fileWriter = new FileWriter(new File(outPath));
                BufferedWriter writer = new BufferedWriter(fileWriter)) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("[Term]")) {
                    break; // comment.
                }
            }
            String id = null;
            String name = null;
            List<String> synonymLst = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                if (line.startsWith("id:")) {
                    id = line.substring(3).trim(); /* Gets rid of "id:" and any whitespace in e.g., HP:0000003 */

                } else if (line.startsWith("name:")) {
                    name = line.substring(5).trim();
                    termCount++;
                    synonymLst.add(name);
                } else if (line.startsWith("synonym:")) {
                    int i, j;
                    i = line.indexOf("\"", 8);
                    if (i > 0) {
                        j = line.indexOf("\"", i + 1);
                        if (j > 0) {
                            String syno = line.substring(i + 1, j);
//			    synonymLst.add(syno);
                        }
                    }
                    termCount++;
                } else if (line.startsWith("is_obsolete")) {
                    id = null;
                    name = null;
                    synonymLst.clear();
                } else if (line.startsWith("[Term]") && name != null && id != null) {
                    writer.write(String.format("%s|%s|%s", name, id, synonymLst));
                    writer.newLine();
//		    logger.info("{} {} {}", name,id,synonymLst);
                    name = null;
                    id = null;
                    synonymLst.clear();
                }
            }
            if (name != null && id != null) {
                writer.write(String.format("%s|%s|%s", name, id, synonymLst));
                writer.newLine();
//                logger.info("{} {} {}", name,id,synonymLst);

            }
            writer.close();
            br.close();
            logger.info("Parsed {} term names/synonyms.", termCount);

        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
            return ResourceOperationStatus.FILE_NOT_FOUND;
        } catch (IOException ex) {
            logger.error(null, ex);
            return ResourceOperationStatus.FAILURE;
        }
        return ResourceOperationStatus.SUCCESS;
    }

//    /**
//     * This function directly enters name/id pairs into the Exomiser database
//     * @param preferred The actual term name of an HPO term
//     * @param id The corresponding HPO term id
//     * @param synLst A list of synonyms for this term (including the preferred name itself).
//     */
//    private void populateHPOTable(String preferred,String id, List<String>synLst) {
//	String insert = "INSERT INTO hpo (lcname,id,prefname) VALUES(?,?,?);";
//	try {
//	    PreparedStatement instPS = connection.prepareStatement(insert);
//	    for (String name:synLst) {
//		String lcname = name.toLowerCase();
//		instPS.setString(1,lcname);
//		instPS.setString(2,id);
//		instPS.setString(3,preferred);
//		System.out.println(n_row + ": " + instPS);
//		instPS.executeUpdate();
//		n_row++;
//	
//	    }
//	    
//	} catch (SQLException e) {
//	    e.printStackTrace();
//	    System.err.println("[ERROR] SQLException");
//	    System.out.println("Unable to insert into table, preferredname=\""+preferred +
//			       "\", id=\""+id+"\"");
//	    System.exit(1);
//	} catch (Exception e) {
//	    System.out.println("Unable to insert into table, preferredname=\""+preferred +
//			       "\", id=\""+id+"\"");
//	    e.printStackTrace();
//	    System.exit(1);
//
//	}
//
//
//    }
}
/* eof */
