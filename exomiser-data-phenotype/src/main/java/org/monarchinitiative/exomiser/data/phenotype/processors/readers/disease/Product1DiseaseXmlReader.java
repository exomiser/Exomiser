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
import com.google.common.collect.ListMultimap;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease.OrphanetXmlConstants.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class Product1DiseaseXmlReader implements ResourceReader<ListMultimap<String, OrphaOmimMapping>> {

    private static final Logger logger = LoggerFactory.getLogger(Product1DiseaseXmlReader.class);

    public static final String EXTERNAL_REFERENCE_LIST = "ExternalReferenceList";
    public static final String EXTERNAL_REFERENCE = "ExternalReference";
    public static final String DISORDER_MAPPING_RELATION = "DisorderMappingRelation";
    public static final String DISORDER_DISORDER_ASSOCIATION_LIST = "DisorderDisorderAssociationList";
    public static final String TEXTUAL_ASSOCIATION_LIST = "TextualInformationList";

    private boolean inDisorder = false;
    private boolean inDisorderFlag = false;
    private boolean inDisorderType = false;
    private boolean inDisorderGroup = false;

    private boolean inExternalReferenceList = false;
    private boolean inExternalReference = false;
    private boolean inDisorderMappingRelation = false;
    private boolean inDisorderDisorderAssociationList = false;
    private boolean inTextualInformationList = false;

    private boolean isObsolete = false;
    private String currentOrphanum = null;
    private String currentDiseaseName = null;
    private String currentExternalSource = null;
    private String currentOmimDiseaseMapping = null;

    private final Resource product1XmlResource;

    public Product1DiseaseXmlReader(Resource product1XmlResource) {
        this.product1XmlResource = product1XmlResource;
    }


    public ListMultimap<String, OrphaOmimMapping> read() {

        ListMultimap<String, OrphaOmimMapping> orphanetOmimMappings = ArrayListMultimap.create();

        List<OrphaOmimMapping> tempOmimMappings = new ArrayList<>();

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        try (InputStream inputStream = Files.newInputStream(product1XmlResource.getResourcePath())) {
            XMLEventReader eventReader = inputFactory.createXMLEventReader(inputStream);

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    final String localPart = startElement.getName().getLocalPart();
                    if (localPart.equals(DISORDER)) {
                        logger.debug("---");
                        inDisorder = true;
                    }
                    else if (!inDisorderDisorderAssociationList && localPart.equals(ORPHA_NUMBER)) {
                        event = eventReader.nextEvent(); // go to the contents of the node
                        currentOrphanum = "ORPHA:" + event.asCharacters().getData();
                        logger.debug("id: " + currentOrphanum);
                    }
                    else if (inDisorder
                            && !inDisorderType
                            && !inDisorderGroup
                            && !inExternalReferenceList
                            && !inDisorderDisorderAssociationList
                            && !inTextualInformationList
                            && localPart.equals(NAME)) {
                        event = eventReader.nextEvent(); // go to the contents of the node
                        currentDiseaseName = event.asCharacters().getData();
                        logger.debug("name: " + currentDiseaseName);
                    }
                    else if (localPart.equals(DISORDER_FLAG)) {
                        inDisorderFlag = true;
                    }
                    else if (inDisorderFlag && localPart.equals("Label")){
                        event = eventReader.nextEvent(); // go to the contents of the node
                        if (event.isCharacters()) {
                            String label = event.asCharacters().getData();
                            if (label.equals("Obsolete entity")) {
                                isObsolete = true;
                            }
                        }
                    }
                    else if (localPart.equals(DISORDER_TYPE)) {
                        inDisorderType = true;
                    }
                    else if (inDisorderType && localPart.equals(NAME)) {
                        event = eventReader.nextEvent(); // go to the contents of the node
                        String currentDisorderType = event.asCharacters().getData();
//                        diseaseBuilder.setDisorderType(currentDisorderType);
                        logger.debug("disorderType: " + currentDisorderType);
                    }
                    else if (localPart.equals(DISORDER_GROUP)) {
                        inDisorderGroup = true;
                    }
                    else if (inDisorderGroup && localPart.equals(NAME)) {
                        event = eventReader.nextEvent(); // go to the contents of the node
                        String currentDisorderGroup = event.asCharacters().getData();
//                        diseaseBuilder.setDisorderGroup(currentDisorderGroup);
                        logger.debug("disorderGroup: " + currentDisorderGroup);
                    }
                    else if (localPart.equals(EXTERNAL_REFERENCE)) {
                        inExternalReference = true;
                    }
                    else if (localPart.equals(EXTERNAL_REFERENCE_LIST)) {
                        logger.debug("omimDiseaseMappings:");
                        inExternalReferenceList = true;
                    }
                    else if (inExternalReference && localPart.equals("Source")) {
                        event = eventReader.nextEvent(); // go to the contents of the node
                        currentExternalSource = event.asCharacters().getData();
                    }
                    else if (inExternalReference && "OMIM".equals(currentExternalSource) && localPart.equals("Reference")) {
                        event = eventReader.nextEvent(); // go to the contents of the node
                        currentOmimDiseaseMapping = "OMIM:" + event.asCharacters().getData();
                        logger.debug("  - " + currentOmimDiseaseMapping);
                    }
                    else if (localPart.equals(DISORDER_MAPPING_RELATION)) {
                        inDisorderMappingRelation = true;
                    }
                    else if (inDisorderMappingRelation && "OMIM".equals(currentExternalSource) && localPart.equals(NAME)) {
                        event = eventReader.nextEvent(); // go to the contents of the node
                        String mappingRelation = event.asCharacters().getData();
//                        System.out.println("  - " + currentOmimDiseaseMapping + " (" + mappingRelation + ")");
                        if (mappingRelation.startsWith("E")) {
                            // exact mapping (the terms and the concepts are equivalent)
                            tempOmimMappings.add(new OrphaOmimMapping(currentOmimDiseaseMapping, OrphaOmimMapping.MappingType.EXACT));
                        } else if (mappingRelation.startsWith("NTBT")) {
                            // narrower term maps to a broader term
                            tempOmimMappings.add(new OrphaOmimMapping(currentOmimDiseaseMapping, OrphaOmimMapping.MappingType.NTBT));
                        } else if (mappingRelation.startsWith("BTNT")) {
                            // broader term maps to a narrower term
                            tempOmimMappings.add(new OrphaOmimMapping(currentOmimDiseaseMapping, OrphaOmimMapping.MappingType.BTNT));
                        }
                    }
                    else if (localPart.equals(DISORDER_DISORDER_ASSOCIATION_LIST)) {
                        inDisorderDisorderAssociationList = true;
                    }
                    else if (localPart.equals(TEXTUAL_ASSOCIATION_LIST)) {
                        inTextualInformationList = true;
                    }
                } else if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    String localPart = endElement.getName().getLocalPart();
//                    System.out.println("EndElement: " + localPart);
                    if (localPart.equals(DISORDER)) {
                        inDisorder = false;

                        if (!isObsolete) {
//                            diseaseBuilder.setOmimDiseaseMappings(omimMappings);
                            // add the disorder to the list
//                            orphanetDisorders.add(diseaseBuilder);
                            orphanetOmimMappings.putAll(currentOrphanum, tempOmimMappings);
                        }
                        // reset to prevent spillage over to missing fields in next disorder
//                        diseaseBuilder = OrphanetDisease.builder();
                        tempOmimMappings = new ArrayList<>();
                        isObsolete = false;
                    }
                    if (localPart.equals(DISORDER_TYPE)) {
                        inDisorderType = false;
                    }
                    if (localPart.equals(DISORDER_GROUP)) {
                        inDisorderGroup = false;
                    }
                    else if (localPart.equals(DISORDER_FLAG)) {
                        inDisorderFlag = false;
                    }
                    if (localPart.equals(EXTERNAL_REFERENCE_LIST)) {
                        inExternalReferenceList = false;
                    }
                    if (localPart.equals(EXTERNAL_REFERENCE)) {
                        inExternalReference = false;
                        currentExternalSource = null;
                        currentOmimDiseaseMapping = null;
                    }
                    if (localPart.equals(DISORDER_MAPPING_RELATION)) {
                        inDisorderMappingRelation = false;
                    }
                    if (localPart.equals(DISORDER_DISORDER_ASSOCIATION_LIST)) {
                        inDisorderDisorderAssociationList = false;
                    }
                    if (localPart.equals(TEXTUAL_ASSOCIATION_LIST)) {
                        inTextualInformationList = false;
                    }
                }
            }
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }

        return orphanetOmimMappings;
    }
}
