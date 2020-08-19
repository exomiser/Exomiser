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
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGene;
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
import java.util.Map;

import static org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease.OrphanetXmlConstants.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class Product6DiseaseGeneXmlReader implements ResourceReader<ListMultimap<String, DiseaseGene>> {

    private static final Logger logger = LoggerFactory.getLogger(Product6DiseaseGeneXmlReader.class);

    public static final String DISORDER_GENE_ASSOCIATION_LIST = "DisorderGeneAssociationList";
    public static final String DISORDER_GENE_ASSOCIATION = "DisorderGeneAssociation";
    public static final String DISORDER_GENE_ASSOCIATION_TYPE = "DisorderGeneAssociationType";
    public static final String DISORDER_GENE_ASSOCIATION_STATUS = "DisorderGeneAssociationStatus";
    public static final String GENE = "Gene";
    public static final String GENE_TYPE = "GeneType";
    public static final String EXTERNAL_REFERENCE_LIST = "ExternalReferenceList";
    public static final String EXTERNAL_REFERENCE = "ExternalReference";

    boolean inDisorder = false;
    boolean inDisorderFlag = false;
    boolean inDisorderType = false;
    boolean inDisorderGroup = false;

    boolean inDisorderGeneAssociationType = false;
    boolean inDisorderGeneAssociationStatus = false;
    boolean inDisorderGeneAssociationList = false;
    boolean inDisorderGeneAssociation = false;
    boolean inGene = false;
    boolean inGeneType = false;
    boolean inExternalReferenceList = false;
    boolean inExternalReference = false;

    boolean isObsolete = false;
    String currentDiseaseName = null;
    String currentOrphanum = null;
    String currentExternalSource = null;

    private final OmimMimToGeneReader mimToGeneReader;
    private final Resource product6XmlResource;

    public Product6DiseaseGeneXmlReader(OmimMimToGeneReader mimToGeneReader, Resource product6XmlResource) {
        this.mimToGeneReader = mimToGeneReader;
        this.product6XmlResource = product6XmlResource;
    }

    public ListMultimap<String, DiseaseGene> read() {
        Map<String, Integer> mim2EntrezIdMap = mimToGeneReader.read();

        List<DiseaseGene> associatedGenes = new ArrayList<>();
        DiseaseGene.Builder currentDiseaseGeneAssociation = DiseaseGene.builder();

        ListMultimap<String, DiseaseGene> diseaseGeneMultimap = ArrayListMultimap.create();


        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        try (InputStream inputStream = Files.newInputStream(product6XmlResource.getResourcePath())) {
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
                    else if (inDisorder
                            && !inGene
                            && !inDisorderType
                            && !inDisorderGroup
                            && !inExternalReference
                            && !inDisorderGeneAssociationList
                            && !inDisorderGeneAssociationType
                            && !inDisorderGeneAssociationStatus
                            && localPart.equals(NAME)) {
                        event = eventReader.nextEvent();
                        currentDiseaseName = event.asCharacters().getData();
                        logger.debug("DiseaseName {}", currentDiseaseName);
                    }
                    else if (!inExternalReferenceList && localPart.equals(ORPHA_NUMBER)) {
                        event = eventReader.nextEvent();
                        currentOrphanum = "ORPHA:" + event.asCharacters().getData();
                        logger.debug("id: {}", currentOrphanum);
                    }
                    else if (localPart.equals(DISORDER_FLAG)) {
                        inDisorderFlag = true;
                    }
                    else if (inDisorderFlag && localPart.equals("Label")){
                        event = eventReader.nextEvent();
                        if (event.isCharacters()) {
                            String label = event.asCharacters().getData();
                            if (label.equals("Obsolete entity")) {
                                isObsolete = true;
                            }
                        }
                    }
                    else if (localPart.equals(DISORDER_TYPE)) {
                        inDisorderGeneAssociationType = true;
                    }
                    else if (localPart.equals(DISORDER_GROUP)) {
                        inDisorderGroup = true;
                    }
                    else if (localPart.equals(DISORDER_GENE_ASSOCIATION_LIST)) {
                        logger.debug("associatedGenes:");
                        inDisorderGeneAssociationList = true;
                    }
                    else if (localPart.equals(DISORDER_GENE_ASSOCIATION)) {
                        inDisorderGeneAssociation = true;
                        currentDiseaseGeneAssociation = DiseaseGene.builder();
                        currentDiseaseGeneAssociation.diseaseId(currentOrphanum);
                        currentDiseaseGeneAssociation.diseaseName(currentDiseaseName);
                    }
                    else if (localPart.equals(GENE)) {
                        inGene = true;
                    }
                    else if (inGene && !inGeneType && localPart.equals(NAME)) {
                        event = eventReader.nextEvent();
                        String geneName = event.asCharacters().getData();
                        currentDiseaseGeneAssociation.geneName(geneName);
                        logger.debug(" - geneName: {}", geneName);
                    }
                    else if (inGene && localPart.equals("Symbol")) {
                        event = eventReader.nextEvent();
                        String geneSymbol = event.asCharacters().getData();
                        currentDiseaseGeneAssociation.geneSymbol(geneSymbol);
                        logger.debug("   geneSymbol: {}", geneSymbol);
                    }
                    else if (localPart.equals(GENE_TYPE)) {
                        inGeneType = true;
                    }
                    else if (inGeneType && localPart.equals(NAME)) {
                        event = eventReader.nextEvent();
                        String geneType = event.asCharacters().getData();
                        logger.debug("   geneType: {}", geneType);
                    }
                    else if (localPart.equals(EXTERNAL_REFERENCE_LIST)) {
                        inExternalReferenceList = true;
                    }
                    else if (localPart.equals(EXTERNAL_REFERENCE)) {
                        inExternalReference = true;
                    }
                    else if (inExternalReference && localPart.equals("Source")) {
                        event = eventReader.nextEvent();
                        currentExternalSource = event.asCharacters().getData();
                    }
                    else if (inExternalReference && localPart.equals("Reference")) {
                        event = eventReader.nextEvent();
                        if ("OMIM".equals(currentExternalSource)) {
                            String omimGeneId = "OMIM:" + event.asCharacters().getData();
                            currentDiseaseGeneAssociation.omimGeneId(omimGeneId);
                            currentDiseaseGeneAssociation.entrezGeneId(mim2EntrezIdMap.getOrDefault(omimGeneId, 0));
                            logger.debug("   omimGeneId: {}", omimGeneId);
                        }
                        else if ("HGNC".equals(currentExternalSource)) {
                            String hgncGeneId = "HGNC:" + event.asCharacters().getData();
                            currentDiseaseGeneAssociation.hgncId(hgncGeneId);
                            logger.debug("   hgncId: {}", hgncGeneId);
                        }
                    }
                    else if (localPart.equals(DISORDER_GENE_ASSOCIATION_TYPE)) {
                        inDisorderGeneAssociationType = true;
                    }
                    else if (inDisorderGeneAssociationType && localPart.equals(NAME)) {
                        event = eventReader.nextEvent();
                        Disease.DiseaseType diseaseType = parseDiseaseType(event.asCharacters().getData());
                        currentDiseaseGeneAssociation.diseaseType(diseaseType);
                        logger.debug("   diseaseType: {}", diseaseType);
                    }
                    else if (localPart.equals(DISORDER_GENE_ASSOCIATION_STATUS)) {
                        inDisorderGeneAssociationStatus = true;
                    }
                } else if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    String localPart = endElement.getName().getLocalPart();
                    if (localPart.equals(DISORDER)) {
                        inDisorder = false;

                        if (!isObsolete) {
//                            diseaseBuilder.setAssociatedGenes(associatedGenes);
                            // add the disorder to the list
//                            orphanetDisorders.add(diseaseBuilder);
                            diseaseGeneMultimap.putAll(currentOrphanum, associatedGenes);
                        }
                        // reset to prevent spillage over to missing fields in next disorder
//                        diseaseBuilder = OrphanetDisease.builder();
                        associatedGenes = new ArrayList<>();
                        isObsolete = false;
                    }
                    else if (localPart.equals(DISORDER_FLAG)) {
                        inDisorderFlag = false;
                    }
                    else if (localPart.equals(DISORDER_GENE_ASSOCIATION_LIST)) {
                        inDisorderGeneAssociationList = false;
                    }
                    else if (localPart.equals(DISORDER_GENE_ASSOCIATION)) {
                        inDisorderGeneAssociation = false;
                        currentExternalSource = null;
                        DiseaseGene diseaseGene = currentDiseaseGeneAssociation.build();
                        logger.debug("Adding {}", diseaseGene);
                        associatedGenes.add(diseaseGene);
                    }
                    else if (localPart.equals(DISORDER_TYPE)) {
                        inDisorderGeneAssociationType = false;
                    }
                    else if (localPart.equals(DISORDER_GROUP)) {
                        inDisorderGroup = false;
                    }
                    else if (localPart.equals(GENE)) {
                        inGene = false;
                    }
                    else if (localPart.equals(GENE_TYPE)) {
                        inGeneType = false;
                    }
                    else if (localPart.equals(EXTERNAL_REFERENCE_LIST)) {
                        inExternalReferenceList = false;
                    }
                    else if (localPart.equals(EXTERNAL_REFERENCE)) {
                        inExternalReference = false;
                    }
                    else if (localPart.equals(DISORDER_GENE_ASSOCIATION_TYPE)) {
                        inDisorderGeneAssociationType = false;
                    }
                    else if (localPart.equals(DISORDER_GENE_ASSOCIATION_STATUS)) {
                        inDisorderGeneAssociationStatus = false;
                    }
                }
            }
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }

        return diseaseGeneMultimap;
    }

    private Disease.DiseaseType parseDiseaseType(String description) {
        // n.b we're using the Disease.DiseaseType here as these are what the data is transformed into and the
        // DiseaseType.code() is used to parse the values from the database.
        if (!description.isEmpty()){
            // note Orphanet defines candidate genes as those that are routinely tested for in clinical labs
            // but not fully proven - a review suggested these are worth knowing about still
            if (description.startsWith("Disease-causing germline mutation")
                    || description.startsWith("Candidate gene tested in")){
                return Disease.DiseaseType.DISEASE;
            }
            else if (description.startsWith("Major susceptibility factor")){
                return Disease.DiseaseType.SUSCEPTIBILITY;
            }
            else if (description.startsWith("Role in the phenotype")){
                return Disease.DiseaseType.CNV;
            }
            /* other types in Orphanet that we are just leaving as ? for now
            Disease-causing somatic mutation
            Biomarker tested in
            Modifying germline mutation in
            Part of a fusion gene in*/
        }
        return Disease.DiseaseType.UNCONFIRMED;
    }
}
