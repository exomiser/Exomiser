/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import com.google.common.collect.ImmutableList;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGene;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the OMIM genemap2.txt file.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class OmimGeneMap2Reader implements ResourceReader<List<DiseaseGene>> {

    private static final Logger logger = LoggerFactory.getLogger(OmimGeneMap2Reader.class);

    // Deafness, digenic, GJB2/GJB3, 220290 (3), Digenic dominant, Autosomal recessive
    // {Diabetes mellitus, insulin-dependent, 11}, 601208 (2)
    private final Pattern phenotypePattern = Pattern.compile("(?<name>.*?), (?<diseaseId>[0-9]{6}) (?<type>\\([]1-4]\\))*(?<moi>(, [\\w\\W]+))*+");

    private final DiseaseInheritanceCacheReader diseaseInheritanceCacheReader;
    private final Resource geneMap2Resource;

    public OmimGeneMap2Reader(DiseaseInheritanceCacheReader diseaseInheritanceCacheReader, Resource geneMap2Resource) {
        this.diseaseInheritanceCacheReader = diseaseInheritanceCacheReader;
        this.geneMap2Resource = geneMap2Resource;
    }

    @Override
    public List<DiseaseGene> read() {
        Map<String, InheritanceMode> diseaseInheritanceCache = diseaseInheritanceCacheReader.read();
        return readDiseaseGenes(diseaseInheritanceCache);
    }

    /**
     * Reads the OMIM genemap2.txt file WITHOUT adding the HPO MOI annotations. This is used for QA purposes when
     * comparing the HPO MOI annotations with those in OMIM.
     *
     * @return a list of {@link DiseaseGene} objects with the <bold>OMIM</bold> MOI.
     */
    public List<DiseaseGene> readRaw() {
        Map<String, InheritanceMode> diseaseInheritanceCache = Map.of();
        return readDiseaseGenes(diseaseInheritanceCache);
    }

    private List<DiseaseGene> readDiseaseGenes(Map<String, InheritanceMode> diseaseInheritanceCache) {
        Set<DiseaseGene> diseases = new LinkedHashSet<>();
        try (BufferedReader bufferedReader = geneMap2Resource.newBufferedReader()) {
            for (String line; (line = bufferedReader.readLine()) != null; ) {
                diseases.addAll(parseLine(diseaseInheritanceCache, line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Extracted {} OMIM disease-gene associations from {}", diseases.size(), geneMap2Resource.getResourcePath());
        return ImmutableList.copyOf(diseases);
    }

    protected List<DiseaseGene> parseLine(Map<String, InheritanceMode> diseaseInheritanceCache, String line) {
        if (line.startsWith("#")) {
            return List.of();
        }
        List<DiseaseGene> diseases = new ArrayList<>();
        String[] tokens = line.split("\t");
        // # Chromosome	Genomic Position Start	Genomic Position End	Cyto Location	Computed Cyto Location	MIM Number	Gene Symbols	Gene Name	Approved Symbol	Entrez Gene ID	Ensembl Gene ID	Comments	Phenotypes	Mouse Gene Symbol/ID
        // select the lines where there is a phenotype associated with the gene
        if (tokens.length >= 13 && !tokens[12].isEmpty()) {
            logger.debug("{}", line);
            String mim = tokens[5];
            String geneSymbol = tokens[8];
            String entrezGeneId = tokens[9];
            String ensemblGeneId = tokens[10];
            String phenotypes = tokens[12];
            String[] phenotypeTokens = phenotypes.split("; ");

            if (!entrezGeneId.isEmpty()){
                for (String phenotype : phenotypeTokens) {
                    Matcher phenotypeMatch = phenotypePattern.matcher(phenotype);
                    if (phenotypeMatch.matches()) {
                        logger.debug("Phenotype: {}", phenotype);

                        String diseaseId = "OMIM:" + phenotypeMatch.group("diseaseId");
                        String  geneMim = "OMIM:" + mim;
                        String diseaseName = trimDiseaseName(phenotypeMatch.group("name"));
                        Disease.DiseaseType diseaseType = parseDiseaseType(phenotypeMatch);
                        InheritanceMode inheritanceMode = parseInheritanceMode(phenotypeMatch.group("moi"));
                        // defer to the HPOA for MOI annotations
                        inheritanceMode = diseaseInheritanceCache.getOrDefault(diseaseId, inheritanceMode);
                        logger.debug("OMIM:{} geneMIM:{} {}, {} name: {}", diseaseId, geneMim, diseaseType, inheritanceMode.toString(), diseaseName);

                        if (!mim.equals(phenotypeMatch.group("diseaseId"))) {
                            DiseaseGene diseaseGene = DiseaseGene.builder()
                                    .diseaseId(diseaseId)
                                    .diseaseName(diseaseName)
                                    .geneSymbol(geneSymbol)
                                    .omimGeneId(geneMim)
                                    .ensemblGeneId(ensemblGeneId)
                                    .entrezGeneId(Integer.parseInt(entrezGeneId))
                                    .diseaseType(diseaseType)
                                    .inheritanceMode(inheritanceMode)
                                    .build();
                            logger.debug("{}", diseaseGene);
                            diseases.add(diseaseGene);
                        }
                    }
                }
            }
        }
        return diseases;
    }

    private String trimDiseaseName(String name) {
        if (name.indexOf('[') == 0) {
            return trimEnclosingChars(name, ']');
        }
        if (name.indexOf('{') == 0) {
            return trimEnclosingChars(name, '}');
        }
        // Leave the '?' unchanged
        // If we get here, just return the string
        return name.trim();
    }

    private String trimEnclosingChars(String diseaseName, char c) {
        int i = diseaseName.lastIndexOf(c);
        if (i > 0) {
            // remove first and last ]
            return diseaseName.substring(1, i);
        } else {
            // remove first [
            return diseaseName.substring(1);
        }
    }

    private Disease.DiseaseType parseDiseaseType(Matcher phenotypeMatch) {
        String name = phenotypeMatch.group("name");
        // https://omim.org/help/faq#1_6
        if (name.indexOf('[') == 0) {
            // Brackets, "[ ]", indicate "nondiseases
            return Disease.DiseaseType.NON_DISEASE;
        }
        if (name.indexOf('{') == 0) {
            // Braces, "{ }", indicate mutations that contribute to susceptibility to multifactorial disorders
            return Disease.DiseaseType.SUSCEPTIBILITY;
        }
        if (name.indexOf('?') == 0) {
            // A question mark, "?", before the disease name indicates an unconfirmed or possibly spurious mapping.
            // n.b. this can also happen inside [] or {}, so really the unconfirmed ought to be a distinct flag, not a type.
            // {?Epidermodysplasia verruciformis, susceptibility to, 4}, 618307 (3)	RHOH, ARHH, TTF	602037	4p14
            // [?Hypertryptophanemia], 600627 (3)	TDO2, TPH2, TRPO, HYPTRP	191070	4q32.1
            return Disease.DiseaseType.UNCONFIRMED;
        }
        String type = phenotypeMatch.group("type");
        if (type.equals("(4)")) {
            // (4) the disorder is a chromosome deletion or duplication syndrome.
            return Disease.DiseaseType.CNV;
        }
        // Default
        return Disease.DiseaseType.DISEASE;
    }

    private InheritanceMode parseInheritanceMode(String moi) {
        // MOIs: [?Autosomal dominant, Autosomal dominant, Autosomal recessive, Digenic dominant, Digenic recessive,
        // Isolated cases, Mitochondrial, Multifactorial, Pseudoautosomal dominant, Pseudoautosomal recessive,
        // Somatic mosaicism, Somatic mutation, X-linked, X-linked dominant, X-linked recessive, Y-linked]
        if (moi != null) {
            Set<InheritanceMode> inheritanceModes = EnumSet.noneOf(InheritanceMode.class);
            String[] localMois = moi.substring(2).split(", ");
            for (String localMoi : localMois) {
                logger.debug("'{}'", localMoi);
                switch (localMoi) {
                    case "Autosomal dominant":
                    case "Pseudoautosomal dominant":
                        inheritanceModes.add(InheritanceMode.AUTOSOMAL_DOMINANT);
                        break;
                    case "Autosomal recessive":
                    case "Pseudoautosomal recessive":
                        inheritanceModes.add(InheritanceMode.AUTOSOMAL_RECESSIVE);
                        break;
                    case "Mitochondrial":
                        inheritanceModes.add(InheritanceMode.MITOCHONDRIAL);
                        break;
                    case "X-linked":
                        inheritanceModes.add(InheritanceMode.X_LINKED);
                        break;
                    case "X-linked dominant":
                        inheritanceModes.add(InheritanceMode.X_DOMINANT);
                        break;
                    case "X-linked recessive":
                        inheritanceModes.add(InheritanceMode.X_RECESSIVE);
                        break;
                    case "Y-linked":
                        inheritanceModes.add(InheritanceMode.Y_LINKED);
                        break;
                    case "Somatic mosaicism":
                    case "Somatic mutation":
                        inheritanceModes.add(InheritanceMode.SOMATIC);
                        break;
                    case "Multifactorial":
                        inheritanceModes.add(InheritanceMode.POLYGENIC);
                        break;
                }
            }
            return InheritanceModeWrangler.wrangleInheritanceMode(inheritanceModes);
        }
        return InheritanceMode.UNKNOWN;
    }
}
