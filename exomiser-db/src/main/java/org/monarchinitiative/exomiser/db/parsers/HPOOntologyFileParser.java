/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.db.parsers;

import org.monarchinitiative.exomiser.db.resources.Resource;
import org.monarchinitiative.exomiser.db.resources.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
public class HPOOntologyFileParser implements ResourceParser {

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
     * @param resource
     * @param inDir Complete path to directory containing the human-phenotype-ontology.obo or hp.obo file.
     * @param outDir Directory where output file is to be written
     * @return
     */
    @Override
    public void parseResource(Resource resource, Path inDir, Path outDir) {

        Path inFile = inDir.resolve(resource.getExtractedFileName());
        Path outFile = outDir.resolve(resource.getParsedFileName());

        logger.info("Parsing {} file: {}. Writing out to: {}", resource.getName(), inFile, outFile);
        ResourceOperationStatus status;

        try (BufferedReader reader = Files.newBufferedReader(inFile, Charset.forName("UTF-8"));
             BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {

            int termCount = 0; /* count of terms */

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[Term]")) {
                    break; // comment.
                }
            }
            String id = null;
            String name = null;
            List<String> synonymLst = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
//                logger.info(line);
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
            reader.close();
            logger.info("Parsed {} term names/synonyms.", termCount);
            status = ResourceOperationStatus.SUCCESS;
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
            status = ResourceOperationStatus.FILE_NOT_FOUND;
        } catch (IOException ex) {
            logger.error(null, ex);
            status = ResourceOperationStatus.FAILURE;
        }
        resource.setParseStatus(status);
        logger.info("{}", status);
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
