/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease;

import com.google.common.collect.ArrayListMultimap;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode.*;
import static org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease.OrphanetXmlConstants.*;

/**
 * Parser for extracting Disease MOI and age of onset from Orphanet en_product9_ages.xml file. This was shamelessly
 * adapted from the Phenol class OrphanetInheritanceXMLParser
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class Product9InheritanceXmlReader implements ResourceReader<ArrayListMultimap<String, InheritanceMode>> {

    private static final Logger logger = LoggerFactory.getLogger(Product9InheritanceXmlReader.class);

    // XML Parsing
    private static final String TYPE_OF_INHERITANCE_LIST = "TypeOfInheritanceList";
    private static final String TYPE_OF_INHERITANCE = "TypeOfInheritance";
    private static final String AVERAGE_AGE_OF_ONSET_LIST = "AverageAgeOfOnsetList";
    private static final String AVERAGE_AGE_OF_DEATH_LIST = "AverageAgeOfDeathList";
    /**
     * Orphanet marks some of its intheritance entries as Not applicable. We will just skip them.
     * This is the corresponding ID.
     */
    // TODO: WARNING! These ids might change from release to release and therefore might be the wrong things to be using.
    private static final String NOT_APPLICABLE_ID = "23494"; // Not applicable
    /**
     * similar to above. We will skip this.
     */
    private static final String UNKNOWN_ID = "23480"; // Unknown
    /**
     * similar to above. We will skip this.
     */
    private static final String NO_DATA_AVAILABLE = "23487"; // No data available

    private boolean inDisorder = false;
    private boolean inDisorderType = false;
    private boolean inDisorderGroup = false;

    private boolean isInAverageAgeOfDeathList = false;
    private boolean inAverageAgeOfOnsetList = false;
    private boolean inTypeOfInheritanceList = false;
    private boolean inTypeOfInheritance = false;

    // Path to en_product9_age.xml file.
    private final Resource product9Resource;

    public Product9InheritanceXmlReader(Resource product9Resource) {
        this.product9Resource = product9Resource;
    }

    @Override
    public ArrayListMultimap<String, InheritanceMode> read() {
        ArrayListMultimap<String, InheritanceMode> disease2inheritanceMultimap = ArrayListMultimap.create();

//    <Disorder id = "17601" >
//      <OrphaNumber > 166024 </OrphaNumber >
//      <ExpertLink lang = "en" > http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&amp;Expert=166024</ExpertLink>
//      <Name lang = "en" > Multiple epiphyseal dysplasia, Al -Gazali type</Name >
//      <DisorderType id = "21394" >
//        <Name lang = "en" > Disease </Name >
//      </DisorderType >
//      <DisorderGroup id = "36547" >
//        <Name lang = "en" > Disorder </Name >
//      </DisorderGroup >
//      <AverageAgeOfOnsetList count = "2" >
//        <AverageAgeOfOnset id = "23522" >
//          <Name lang = "en" > Infancy </Name >
//        </AverageAgeOfOnset >
//        <AverageAgeOfOnset id = "23515" >
//          <Name lang = "en" > Neonatal </Name >
//        </AverageAgeOfOnset >
//      </AverageAgeOfOnsetList >
//      <AverageAgeOfDeathList count = "0" >
//      </AverageAgeOfDeathList >
//      <TypeOfInheritanceList count = "1" >
//        <TypeOfInheritance id = "23417" >
//          <Name lang = "en" > Autosomal recessive</Name >
//        </TypeOfInheritance >
//      </TypeOfInheritanceList >
//    </Disorder >

        try (InputStream in = Files.newInputStream(product9Resource.getResourcePath())) {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            String currentOrphanum = null;
            String currentDiseaseName = null;
            String currentInheritanceId = null;
            String currentModeOfInheritanceLabel = null;
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    final String localPart = startElement.getName().getLocalPart();
                    if (localPart.equals(DISORDER)) {
                        inDisorder = true;
                    } else if (inDisorder &&
                            !inDisorderType &&
                            !inDisorderGroup &&
                            !inAverageAgeOfOnsetList &&
                            !isInAverageAgeOfDeathList &&
                            !inTypeOfInheritance &&
                            localPart.equals(ORPHA_NUMBER)) {
                        event = eventReader.nextEvent(); // go to the contents of the node
                        currentOrphanum = event.asCharacters().getData();
                    } else if (inDisorder &&
                            !inDisorderType &&
                            !inDisorderGroup &&
                            !inAverageAgeOfOnsetList &&
                            !isInAverageAgeOfDeathList &&
                            !inTypeOfInheritance &&
                            localPart.equals(NAME)) {
                        event = eventReader.nextEvent();
                        currentDiseaseName = event.asCharacters().getData();
                    } else if (inDisorder && localPart.equals(TYPE_OF_INHERITANCE_LIST)) {
                        inTypeOfInheritanceList = true;
                        Attribute countAttribute = startElement.getAttributeByName(QName.valueOf("count"));
                        if (countAttribute == null || "0".equals(countAttribute.getValue())) {
                            // there are no inheritance modes for this disease - add
                            logger.debug("Skipping ORPHA:{} {} due to no known MOI", currentOrphanum, currentDiseaseName);
                        }
                    } else if (inTypeOfInheritanceList && localPart.equals(TYPE_OF_INHERITANCE)) {
                        inTypeOfInheritance = true;
                        // TODO: WARNING! These ids might change from release to release and therefore might be the wrong
                        //  things to be using.
                        Attribute idAttribute = startElement.getAttributeByName(QName.valueOf("id"));
                        currentInheritanceId = idAttribute.getValue();
                    } else if (inDisorder &&
                            !inAverageAgeOfOnsetList &&
                            !isInAverageAgeOfDeathList &&
                            !inDisorderType &&
                            inTypeOfInheritance &&
                            localPart.equals(NAME)) {
                        event = eventReader.nextEvent(); // go to the contents of the node
                        currentModeOfInheritanceLabel = event.asCharacters().getData();
                        if (currentInheritanceId == null ||
                                currentInheritanceId.equals(NOT_APPLICABLE_ID) ||
                                currentInheritanceId.equals(UNKNOWN_ID) ||
                                currentInheritanceId.equals(NO_DATA_AVAILABLE)) {
                            continue;
                        }
                        InheritanceMode inheritanceMode = getHpoInheritanceTermId(currentInheritanceId, currentModeOfInheritanceLabel);
                        if (inheritanceMode != null) {
                            String disId = String.format("ORPHA:%s", currentOrphanum);
                            logger.debug("{} {} {} {}", disId, currentModeOfInheritanceLabel, inheritanceMode.getInheritanceCode(), currentDiseaseName);
                            disease2inheritanceMultimap.put(disId, inheritanceMode);
                        }
                    } else if (localPart.equals(AVERAGE_AGE_OF_ONSET_LIST)) {
                        inAverageAgeOfOnsetList = true;
                    } else if (localPart.equals(AVERAGE_AGE_OF_DEATH_LIST)) {
                        isInAverageAgeOfDeathList = true;
                    } else if (localPart.equals(TYPE_OF_INHERITANCE)) {
                        inTypeOfInheritance = true;
                    } else if (localPart.equals(DISORDER_TYPE)) {
                        inDisorderType = true;
                    } else if (localPart.equals(DISORDER_GROUP)) {
                        inDisorderGroup = true;
                    }
                } else if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    String localPart = endElement.getName().getLocalPart();
                    if (localPart.equals(DISORDER)) {
                        inDisorder = false;
                    } else if (localPart.equals(TYPE_OF_INHERITANCE_LIST)) {
                        inTypeOfInheritanceList = false;
                    } else if (localPart.equals(TYPE_OF_INHERITANCE)) {
                        inTypeOfInheritance = false;
                    } else if (localPart.equals(AVERAGE_AGE_OF_ONSET_LIST)) {
                        inAverageAgeOfOnsetList = false;
                    } else if (localPart.equals(AVERAGE_AGE_OF_DEATH_LIST)) {
                        isInAverageAgeOfDeathList = false;
                    } else if (localPart.equals(DISORDER_TYPE)) {
                        inDisorderType = false;
                    } else if (localPart.equals(DISORDER_GROUP)) {
                        inDisorderGroup = false;
                    }
                }
            }
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }
        return disease2inheritanceMultimap;
    }

    private InheritanceMode getHpoInheritanceTermId(String orphaInheritanceId, String orphaLabel) {
        if (orphaInheritanceId.equals("23417") && orphaLabel.equals("Autosomal recessive")) {
            return AUTOSOMAL_RECESSIVE;
        } else if (orphaInheritanceId.equals("23410") && orphaLabel.equals("Autosomal dominant")) {
            return AUTOSOMAL_DOMINANT;
        } else if (orphaInheritanceId.equals("23424") && orphaLabel.equals("Multigenic/multifactorial")) {
            return POLYGENIC; //TODO MULTIFACTORIAL - currently InheritanceMode only contains monogenic or polygenic MOIs
        } else if (orphaInheritanceId.equals("23431") && orphaLabel.equals("X-linked recessive")) {
            return X_RECESSIVE;
        } else if (orphaInheritanceId.equals("23438") && orphaLabel.equals("Mitochondrial inheritance")) {
            return MITOCHONDRIAL;
        } else if (orphaInheritanceId.equals("23445") && orphaLabel.equals("X-linked dominant")) {
            return X_DOMINANT;
        } else if (orphaInheritanceId.equals("23473") && orphaLabel.equals("Y-linked")) {
            return Y_LINKED;
        } else if (orphaInheritanceId.equals("23466") && orphaLabel.equals("Semi-dominant")) {
            return UNKNOWN; //TODO SEMIDOMINANT
        } else if (orphaInheritanceId.equals("23459") && orphaLabel.equals("Oligogenic")) {
            return POLYGENIC; //TODO OLIGOGENIC
        } else if (orphaInheritanceId.equals("23480") && orphaLabel.equals("Unknown")) {
            return UNKNOWN;
        } else if (orphaInheritanceId.equals("23487") && orphaLabel.equals("No data available")) {
            return UNKNOWN;
        } else if (orphaInheritanceId.equals("23494") && orphaLabel.equals("Not applicable")) {
            return null;
        } else {
            logger.warn("Could not find HPO id for Orphanet inheritence entry: {} ({})", orphaLabel, orphaInheritanceId);
            return null;
        }
    }
}
