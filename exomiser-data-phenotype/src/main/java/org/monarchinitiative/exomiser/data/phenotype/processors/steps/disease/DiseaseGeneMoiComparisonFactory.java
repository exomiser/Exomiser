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

package org.monarchinitiative.exomiser.data.phenotype.processors.steps.disease;

import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGene;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGeneMoiComparison;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class DiseaseGeneMoiComparisonFactory {

    private static final Logger logger = LoggerFactory.getLogger(DiseaseGeneMoiComparisonFactory.class);

    private final List<DiseaseGene> omimDiseaseGenes;
    private final Map<String, InheritanceMode> inheritanceModeMap;

    public DiseaseGeneMoiComparisonFactory(List<DiseaseGene> omimDiseaseGenes, Map<String, InheritanceMode> inheritanceModeMap) {
        this.omimDiseaseGenes = omimDiseaseGenes;
        this.inheritanceModeMap = inheritanceModeMap;
    }

    public List<DiseaseGeneMoiComparison> buildComparisons() {
        List<DiseaseGeneMoiComparison> comparisonListBuilder = new ArrayList<>();
        for (DiseaseGene geneMapDiseaseGene : omimDiseaseGenes) {
            InheritanceMode hpoaInheritanceMode = inheritanceModeMap.getOrDefault(geneMapDiseaseGene.getDiseaseId(), InheritanceMode.UNKNOWN);
            DiseaseGeneMoiComparison diseaseGeneMoiComparison = DiseaseGeneMoiComparison.of(geneMapDiseaseGene, hpoaInheritanceMode);
            if (diseaseGeneMoiComparison.isMissingHpoMoi()) {
                logger.debug("HPO MOI UNKNOWN: {}", diseaseGeneMoiComparison);
            } else if (diseaseGeneMoiComparison.isMissingOmimMoi()) {
                logger.debug("OMIM MOI UNKNOWN: {}", diseaseGeneMoiComparison);
            } else if (diseaseGeneMoiComparison.hasMismatchedMoi()) {
                logger.debug("MOI MISMATCH: {}", diseaseGeneMoiComparison);
            } else {
                logger.debug("MOI MATCH: {}", diseaseGeneMoiComparison);
            }
            comparisonListBuilder.add(diseaseGeneMoiComparison);
        }
        return comparisonListBuilder;
    }
//
//    public void doThing() throws IOException {
//        Path inDir = Paths.get("C:/Users/hhx640/Downloads/");
//        Path outDir = Files.createTempDirectory("Orphanet2GeneParserTest");
//
//        // Set-up and parse all the OMIM data
//        //first parseResource the mim2gene file
//        Resource mim2geneResource = new Resource("mim2gene");
//        mim2geneResource.setLocalFileName("mim2gene.txt");
//        mim2geneResource.setParsedFileName("mim2gene_test_out.txt");
//        MimToGeneParser mimParser = new MimToGeneParser( );
//        mimParser.parseResource(mim2geneResource, inDir, outDir);
//        Map<String, Integer> mim2geneMap = mimParser.getOmimGeneToEntrezId();
//
//        //Need to make the cache for the morbidmap resourceParser
//        // TODO What we're trying to do here is check that these annotations match those in the genemap2 file..
//        //  This is the key line: InheritanceMode inheritanceMode = diseaseInheritanceCache.getInheritanceMode("OMIM:" + diseaseId);
//
//        DiseaseInheritanceCache diseaseInheritanceCache = new DiseaseInheritanceCache();
//        Resource hpoPhenotypeAnnotations = new Resource("omimInheritance");
//        hpoPhenotypeAnnotations.setLocalFileName("phenotype_annotation.tab");
//        diseaseInheritanceCache.parseResource(hpoPhenotypeAnnotations, inDir, outDir);
//
//        //make the MimList which morbid map will populate
//        MorbidMapParser morbidMapParser = new MorbidMapParser(diseaseInheritanceCache, mim2geneMap);
//        Resource morbidMapResource = new Resource("morbidmap");
//        morbidMapResource.setLocalFileName("morbidmap.txt.1");
//        morbidMapResource.setParsedFileName("morbidmap_test_out.txt");
//        morbidMapParser.parseResource(morbidMapResource, inDir, outDir);
//        List<DiseaseGene> morbidMapDiseases = morbidMapParser.getDiseaseGenes();
//
//        Map<Integer, DiseaseGene> morbidMapDiseasesMap = morbidMapDiseases.stream()
//                .filter(diseaseGene -> !diseaseGene.getDiseaseId().isEmpty())
//                .collect(toMap(diseaseGeneIdKey(), Function.identity()));
//
//
//        ExternalResource phenotypeAnnotationsResource = ExternalResource.builder()
//                .fileDirectory(Paths.get("C:/Users/hhx640/Downloads/"))
//                .fileName("phenotype_annotation.tab")
//                .build();
//        DiseaseInheritanceCacheReader diseaseInheritanceCacheReader = new DiseaseInheritanceCacheReader(phenotypeAnnotationsResource);
//
//        ExternalResource geneMap2Resource = ExternalResource.builder()
//                .fileDirectory(Paths.get("C:/Users/hhx640/Downloads/"))
//                .fileName("genemap2.txt.1")
//                .build();
//        List<DiseaseGene> geneMapDiseases = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, geneMap2Resource).read();
//
//        Map<String, InheritanceMode> inheritanceModeMap = diseaseInheritanceCacheReader.read();
//
//        System.out.println("inheritanceModeMap");
//        inheritanceModeMap.entrySet().stream().limit(10).forEach(System.out::println);
//
//
//        System.out.println("geneMapDiseases");
//        geneMapDiseases.stream().limit(10).forEach(System.out::println);
//
//        Set<String> parsedOmimDiseasesFromGeneMap = geneMapDiseases.stream()
//                .map(DiseaseGene::getDiseaseId)
//                .collect(Collectors.toSet());
//        // these entries have no known causative gene or locus.
//        int missingOmimFromGeneMap = 0;
//        for (Map.Entry<String, InheritanceMode> entry : inheritanceModeMap.entrySet()) {
//            if (!parsedOmimDiseasesFromGeneMap.contains(entry.getKey())) {
//                missingOmimFromGeneMap++;
////                System.out.println("MISSING OMIM MOI PRESENT IN HPO ANNOTATIONS: " + entry);
//            }
//        }
//
//        // compare new with old
//        int notPresentInMorbidMap = 0;
//        List<DiseaseGeneMoiComparison> hpoaMorbidMapMismatches = new ArrayList<>();
//
//        List<DiseaseGeneMoiComparison> unknownInHpo = new ArrayList<>();
//        List<DiseaseGeneMoiComparison> unknownInOmim = new ArrayList<>();
//
//        for (DiseaseGene geneMapDiseaseGene : geneMapDiseases) {
//            InheritanceMode hpoaInheritanceMode = inheritanceModeMap.getOrDefault(geneMapDiseaseGene.getDiseaseId(), InheritanceMode.UNKNOWN);
////            Integer key = diseaseGeneIdKey().apply(geneMapDiseaseGene);
////            DiseaseGene hpoaMorbidMapDiseaseGene = morbidMapDiseasesMap.get(key);
//            if (geneMapDiseaseGene.getInheritanceMode() != hpoaInheritanceMode) {
//                DiseaseGeneMoiComparison moiMismatch = DiseaseGeneMoiComparison.of(geneMapDiseaseGene, hpoaInheritanceMode);
//                if (hpoaInheritanceMode == InheritanceMode.UNKNOWN) {
////                    System.out.println("HPO MOI UNKNOWN:");
////                    printComparison(geneMapDiseaseGene, hpoaMorbidMapDiseaseGene);
//                    unknownInHpo.add(moiMismatch);
//                    System.out.println(moiMismatch.toOutputLine());
//                }
//                else if (geneMapDiseaseGene.getInheritanceMode() == InheritanceMode.UNKNOWN) {
////                    System.out.println("OMIM MOI UNKNOWN:");
////                    printComparison(geneMapDiseaseGene, hpoaMorbidMapDiseaseGene);
//                    unknownInOmim.add(moiMismatch);
//                }
//                else {
////                    System.out.println("MOI MISMATCH:");
////                    printComparison(geneMapDiseaseGene, hpoaMorbidMapDiseaseGene);
////                    System.out.println(DiseaseGeneMoiMismatch.of(hpoaMorbidMapDiseaseGene, geneMapDiseaseGene));
//                    hpoaMorbidMapMismatches.add(moiMismatch);
//                }
//            }
//        }
//        System.out.println(geneMapDiseases.size() + " OMIM diseaseGene associations parsed from genemap2.txt");
//        System.out.println(missingOmimFromGeneMap + " OMIM phenotypes in HPO without specified gene association");
//        System.out.println((hpoaMorbidMapMismatches.size() + unknownInHpo.size() + unknownInOmim.size()) + " disagreements between HPO and OMIM annotations." );
//        System.out.println("Of these, there were: ");
//        System.out.println("  " + hpoaMorbidMapMismatches.size()  + " gene-disease annotations with mismatching MOI");
//        System.out.println("  " + unknownInHpo.size()  + " annotations with UNKNOWN MOI from HPO (annotation present in OMIM)");
//        System.out.println("  " + unknownInOmim.size()  + " annotations with UNKNOWN MOI from OMIM (annotation present in HPO)");
//
//        System.out.println(notPresentInMorbidMap  + " gene-disease associations not present in morbidmap data");
//        // compare old with new
//        int notPresentInGeneMap = 0;
//        int moiGeneMapMismatches = 0;
//
//        Map<Integer, DiseaseGene> geneMapDiseaseMap = geneMapDiseases.stream()
//                .collect(toMap(diseaseGeneIdKey(), Function.identity()));
//
//        for (DiseaseGene diseaseGene : morbidMapDiseases) {
//            Integer key = diseaseGeneIdKey().apply(diseaseGene);
//            DiseaseGene geneMapDisease = geneMapDiseaseMap.get(key);
//
//            if (geneMapDisease != null && diseaseGene.getInheritanceMode() != geneMapDisease.getInheritanceMode()) {
//                moiGeneMapMismatches++;
//            } else if (geneMapDisease == null) {
//                notPresentInGeneMap++;
//                System.out.println("Not present in genemap2: " + notPresentInGeneMap + " " + diseaseGene);
//            }
//        }
//        System.out.println(notPresentInGeneMap  + " gene-disease associations not present in genemap data");
//        System.out.println(moiGeneMapMismatches  + " gene-disease associations from genemap with mismatching MOI");
//    }
//
//    private void printComparison(DiseaseGene diseaseGene, DiseaseGene morbidMapDisease) {
//        System.out.println("  HPO  " +  morbidMapDisease);
//        System.out.println("  OMIM " +  diseaseGene);
//    }
//
//
//    private Function<DiseaseGene, String> diseaseGeneKey() {
//        return diseaseGene -> diseaseGene.getDiseaseId() + "_" + diseaseGene.getOmimGeneId();
//    }
//
//    private Function<DiseaseGene, Integer> diseaseGeneIdKey() {
//        return diseaseGene -> Objects.hash(diseaseGene.getDiseaseId(), diseaseGene.getOmimGeneId(), diseaseGene.getDiseaseName());
//    }
}
