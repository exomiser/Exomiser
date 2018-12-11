/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.phenotype.parsers;

import com.google.common.collect.ImmutableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class OboOntologyParser {

    private OboOntologyParser() {
    }

    static OboOntology parseOboFile(Path oboFile) {
        String dataVersion = parseDataVersion(oboFile);
        List<OboOntologyTerm> ontologyTerms = parseOntologyTerms(oboFile);
        return new OboOntology(dataVersion, ontologyTerms);
    }

    private static String parseDataVersion(Path oboFile) {
        try(BufferedReader bufferedReader = Files.newBufferedReader(oboFile, Charset.forName("UTF-8"))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("data-version:")){
                    return line.substring(13).trim(); }
            }
        } catch (IOException ex){
            throw new OboOntologyParseException("Error parsing OBO file " + oboFile, ex);
        }
        return "";
    }

    private static List<OboOntologyTerm> parseOntologyTerms(Path oboFile) {
        ImmutableList.Builder<OboOntologyTerm> ontologyTerms =  new ImmutableList.Builder<>();

        try(BufferedReader bufferedReader = Files.newBufferedReader(oboFile, Charset.forName("UTF-8"))) {
            // [Term]
            // id: HP:0000316
            // name: Hypertelorism
            // alt_id: HP:0000578
            // alt_id: HP:0002001
            // alt_id: HP:0004657
            // alt_id: HP:0007871

            // [Term]
            // id: HP:0000284
            // name: obsolete Abnormality of the ocular region
            // synonym: "ocular abnormalities" EXACT []
            // is_obsolete: true
            // replaced_by: HP:0000315

            String id = null;
            OboOntologyTerm.Builder termBuilder = OboOntologyTerm.builder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("id:")) {
                    id = line.substring(3).trim();
                    termBuilder.id(id);
                }
                else if (line.startsWith("name:")) {
                    termBuilder.label(line.substring(5).trim());
                }
                else if (line.startsWith("is_obsolete")) {
                    termBuilder.obsolete(true);
                }
                else if (line.startsWith("alt_id:")){
                    termBuilder.addAltId(line.substring(7).trim());
                }
                else if (line.startsWith("replaced_by:")){
                    termBuilder.replacedBy(line.substring(12).trim());
                }
                else if (line.isEmpty() && id != null) {
                    // add current term
                    ontologyTerms.add(termBuilder.build());
                    // reset builder
                    termBuilder = OboOntologyTerm.builder();
                }
            }

        } catch (IOException ex) {
            throw new OboOntologyParseException("Error parsing OBO file " + oboFile, ex);
        }

        return ontologyTerms.build();
    }

    private static class OboOntologyParseException extends RuntimeException {

        OboOntologyParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
