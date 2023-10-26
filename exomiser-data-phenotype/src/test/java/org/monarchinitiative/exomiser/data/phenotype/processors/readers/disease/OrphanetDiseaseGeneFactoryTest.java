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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.data.phenotype.config.ReleaseFileSystem;
import org.monarchinitiative.exomiser.data.phenotype.config.ResourceBuilder;
import org.monarchinitiative.exomiser.data.phenotype.config.ResourceConfigurationProperties;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGene;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease.OrphaOmimMapping.MappingType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.monarchinitiative.exomiser.core.prioritisers.model.Disease.DiseaseType.DISEASE;
import static org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class OrphanetDiseaseGeneFactoryTest {

    @Test
    void singleMappingMoiKnown() {
        String omimDiseaseId = "OMIM:154020";
        String omimGeneId = "OMIM:601814";

        List<DiseaseGene> omimDiseaseGenes = List.of(diseaseGene(omimDiseaseId, omimGeneId, 486, "FXYD2", AUTOSOMAL_DOMINANT));

        String orphaDiseaseId = "ORPHA:34528";

        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = ArrayListMultimap.create();
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping(omimDiseaseId, MappingType.EXACT));

        ListMultimap<String, DiseaseGene> orphaDiseaseGenes = ArrayListMultimap.create();
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, omimGeneId, 486, "FXYD2", UNKNOWN));

        ListMultimap<String, InheritanceMode> inheritanceModesMap = ArrayListMultimap.create();
        inheritanceModesMap.put(orphaDiseaseId, AUTOSOMAL_DOMINANT);

        OrphanetDiseaseGeneFactory instance = new OrphanetDiseaseGeneFactory(omimDiseaseGenes, orphaOmimMappings, orphaDiseaseGenes, inheritanceModesMap);

        List<DiseaseGene> expected = List.of(diseaseGene(orphaDiseaseId, omimGeneId, 486, "FXYD2", AUTOSOMAL_DOMINANT));

        assertThat(instance.buildDiseaseGeneAssociations(), equalTo(expected));
    }

    @Test
    void singleMappingMoiUnknownInOrphanet() {
        String omimDiseaseId = "OMIM:154020";
        String omimGeneId = "OMIM:601814";

        List<DiseaseGene> omimDiseaseGenes = List.of(diseaseGene(omimDiseaseId, omimGeneId, 486, "FXYD2", AUTOSOMAL_DOMINANT));

        String orphaDiseaseId = "ORPHA:34528";

        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = ArrayListMultimap.create();
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping(omimDiseaseId, MappingType.EXACT));

        ListMultimap<String, DiseaseGene> orphaDiseaseGenes = ArrayListMultimap.create();
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, omimGeneId, 486, "FXYD2", UNKNOWN));

        ListMultimap<String, InheritanceMode> inheritanceModesMap = ArrayListMultimap.create();

        OrphanetDiseaseGeneFactory instance = new OrphanetDiseaseGeneFactory(omimDiseaseGenes, orphaOmimMappings, orphaDiseaseGenes, inheritanceModesMap);

        List<DiseaseGene> expected = List.of(diseaseGene(orphaDiseaseId, omimGeneId, 486, "FXYD2", AUTOSOMAL_DOMINANT));

        assertThat(instance.buildDiseaseGeneAssociations(), equalTo(expected));
    }

    @Test
    void singleMappingMoiUnknownInOmimUsesOrphanet() {
        String omimDiseaseId = "OMIM:154020";
        String omimGeneId = "OMIM:601814";

        List<DiseaseGene> omimDiseaseGenes = List.of(diseaseGene(omimDiseaseId, omimGeneId, 486, "FXYD2", UNKNOWN));

        String orphaDiseaseId = "ORPHA:34528";

        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = ArrayListMultimap.create();
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping(omimDiseaseId, MappingType.EXACT));

        ListMultimap<String, DiseaseGene> orphaDiseaseGenes = ArrayListMultimap.create();
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, omimGeneId, 486, "FXYD2", UNKNOWN));

        ListMultimap<String, InheritanceMode> inheritanceModesMap = ArrayListMultimap.create();
        inheritanceModesMap.put(orphaDiseaseId, AUTOSOMAL_DOMINANT);

        OrphanetDiseaseGeneFactory instance = new OrphanetDiseaseGeneFactory(omimDiseaseGenes, orphaOmimMappings, orphaDiseaseGenes, inheritanceModesMap);

        List<DiseaseGene> expected = List.of(diseaseGene(orphaDiseaseId, omimGeneId, 486, "FXYD2", AUTOSOMAL_DOMINANT));

        assertThat(instance.buildDiseaseGeneAssociations(), equalTo(expected));
    }

    @Test
    void singleMappingMoiUnknownInBoth() {
        String omimDiseaseId = "OMIM:154020";
        String omimGeneId = "OMIM:601814";

        List<DiseaseGene> omimDiseaseGenes = List.of(diseaseGene(omimDiseaseId, omimGeneId, 486, "FXYD2", UNKNOWN));

        String orphaDiseaseId = "ORPHA:34528";

        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = ArrayListMultimap.create();
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping(omimDiseaseId, MappingType.EXACT));

        ListMultimap<String, DiseaseGene> orphaDiseaseGenes = ArrayListMultimap.create();
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, omimGeneId, 486, "FXYD2", UNKNOWN));

        ListMultimap<String, InheritanceMode> inheritanceModesMap = ArrayListMultimap.create();

        OrphanetDiseaseGeneFactory instance = new OrphanetDiseaseGeneFactory(omimDiseaseGenes, orphaOmimMappings, orphaDiseaseGenes, inheritanceModesMap);

        List<DiseaseGene> expected = List.of(diseaseGene(orphaDiseaseId, omimGeneId, 486, "FXYD2", UNKNOWN));

        assertThat(instance.buildDiseaseGeneAssociations(), equalTo(expected));
    }

    @Test
    void singleMappingOmimMoiOverridesOrphanet() {
        String omimDiseaseId = "OMIM:154020";
        String omimGeneId = "OMIM:601814";

        List<DiseaseGene> omimDiseaseGenes = List.of(diseaseGene(omimDiseaseId, omimGeneId, 486, "FXYD2", AUTOSOMAL_RECESSIVE));

        String orphaDiseaseId = "ORPHA:34528";

        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = ArrayListMultimap.create();
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping(omimDiseaseId, MappingType.EXACT));

        ListMultimap<String, DiseaseGene> orphaDiseaseGenes = ArrayListMultimap.create();
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, omimGeneId, 486, "FXYD2", UNKNOWN));

        ListMultimap<String, InheritanceMode> inheritanceModesMap = ArrayListMultimap.create();
        inheritanceModesMap.put(orphaDiseaseId, AUTOSOMAL_DOMINANT);

        OrphanetDiseaseGeneFactory instance = new OrphanetDiseaseGeneFactory(omimDiseaseGenes, orphaOmimMappings, orphaDiseaseGenes, inheritanceModesMap);

        List<DiseaseGene> expected = List.of(diseaseGene(orphaDiseaseId, omimGeneId, 486, "FXYD2", AUTOSOMAL_RECESSIVE));

        assertThat(instance.buildDiseaseGeneAssociations(), equalTo(expected));
    }

    @Test
    void noOrphanetOmimGeneUsesFirstOrphanetMoi() {
        String omimDiseaseId = "OMIM:154020";
        String omimGeneId = "OMIM:601814";

        List<DiseaseGene> omimDiseaseGenes = List.of(diseaseGene(omimDiseaseId, omimGeneId, 486, "FXYD2", UNKNOWN));

        String orphaDiseaseId = "ORPHA:34528";

        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = ArrayListMultimap.create();
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping(omimDiseaseId, MappingType.BTNT));

        ListMultimap<String, DiseaseGene> orphaDiseaseGenes = ArrayListMultimap.create();
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, omimGeneId, 486, "", UNKNOWN));

        ListMultimap<String, InheritanceMode> inheritanceModesMap = ArrayListMultimap.create();
        inheritanceModesMap.put(orphaDiseaseId, AUTOSOMAL_DOMINANT);
        inheritanceModesMap.put(orphaDiseaseId, AUTOSOMAL_RECESSIVE);

        OrphanetDiseaseGeneFactory instance = new OrphanetDiseaseGeneFactory(omimDiseaseGenes, orphaOmimMappings, orphaDiseaseGenes, inheritanceModesMap);
// TODO: Implementation code is CORRECT - fix these tests!
        List<DiseaseGene> expected = List.of(diseaseGene(orphaDiseaseId, omimGeneId, 486, "", AUTOSOMAL_DOMINANT_AND_RECESSIVE));

        assertThat(instance.buildDiseaseGeneAssociations(), equalTo(expected));
    }


    @Test
    void noOrphanetOmimGeneAdAndAr() {
        String omimDiseaseId = "OMIM:154020";
        String omimGeneId = "OMIM:601814";

        List<DiseaseGene> omimDiseaseGenes = List.of(diseaseGene(omimDiseaseId, omimGeneId, 486, "FXYD2", AUTOSOMAL_RECESSIVE));

        String orphaDiseaseId = "ORPHA:34528";

        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = ArrayListMultimap.create();
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping(omimDiseaseId, MappingType.BTNT));

        ListMultimap<String, DiseaseGene> orphaDiseaseGenes = ArrayListMultimap.create();
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, "OMIM:UNMATCHED", 9999, "GENE1", UNKNOWN));

        ListMultimap<String, InheritanceMode> inheritanceModesMap = ArrayListMultimap.create();
        inheritanceModesMap.put(orphaDiseaseId, AUTOSOMAL_RECESSIVE);
        inheritanceModesMap.put(orphaDiseaseId, AUTOSOMAL_DOMINANT);

        OrphanetDiseaseGeneFactory instance = new OrphanetDiseaseGeneFactory(omimDiseaseGenes, orphaOmimMappings, orphaDiseaseGenes, inheritanceModesMap);

        List<DiseaseGene> expected = List.of(diseaseGene(orphaDiseaseId, "OMIM:UNMATCHED", 9999, "GENE1", AUTOSOMAL_DOMINANT_AND_RECESSIVE));

        assertThat(instance.buildDiseaseGeneAssociations(), equalTo(expected));
    }

    @Test
    void noOrphanetOmimGeneMultipleMoiReturnsFirstOrphanetMoi() {
        String omimDiseaseId = "OMIM:154020";
        String omimGeneId = "OMIM:601814";

        List<DiseaseGene> omimDiseaseGenes = List.of(diseaseGene(omimDiseaseId, omimGeneId, 486, "FXYD2", AUTOSOMAL_RECESSIVE));

        String orphaDiseaseId = "ORPHA:34528";

        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = ArrayListMultimap.create();
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping(omimDiseaseId, MappingType.BTNT));

        ListMultimap<String, DiseaseGene> orphaDiseaseGenes = ArrayListMultimap.create();
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, "OMIM:UNMATCHED", 9999, "GENE1", UNKNOWN));

        ListMultimap<String, InheritanceMode> inheritanceModesMap = ArrayListMultimap.create();
        inheritanceModesMap.put(orphaDiseaseId, X_RECESSIVE);
        inheritanceModesMap.put(orphaDiseaseId, MITOCHONDRIAL);

        OrphanetDiseaseGeneFactory instance = new OrphanetDiseaseGeneFactory(omimDiseaseGenes, orphaOmimMappings, orphaDiseaseGenes, inheritanceModesMap);

        List<DiseaseGene> expected = List.of(diseaseGene(orphaDiseaseId, "OMIM:UNMATCHED", 9999, "GENE1", X_RECESSIVE));

        assertThat(instance.buildDiseaseGeneAssociations(), equalTo(expected));
    }

    @Test
    void multiGeneDiseaseSingleMoi() {
        List<DiseaseGene> omimDiseaseGenes = List.of(
                diseaseGene("OMIM:312870", "OMIM:300037", 486, "FXYD2", X_RECESSIVE)
        );

        String orphaDiseaseId = "ORPHA:373";

        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = ArrayListMultimap.create();
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping("OMIM:312870", MappingType.EXACT));

        ListMultimap<String, DiseaseGene> orphaDiseaseGenes = ArrayListMultimap.create();
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, "OMIM:300037", 2719, "GPC3", UNKNOWN));
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, "OMIM:300168", 2239, "GPC4", UNKNOWN));

        ListMultimap<String, InheritanceMode> inheritanceModesMap = ArrayListMultimap.create();
        inheritanceModesMap.put(orphaDiseaseId, X_RECESSIVE);

        OrphanetDiseaseGeneFactory instance = new OrphanetDiseaseGeneFactory(omimDiseaseGenes, orphaOmimMappings, orphaDiseaseGenes, inheritanceModesMap);

        List<DiseaseGene> expected = List.of(
                diseaseGene(orphaDiseaseId, "OMIM:300037", 2719, "GPC3", X_RECESSIVE),
                diseaseGene(orphaDiseaseId, "OMIM:300168", 2239, "GPC4", X_RECESSIVE)
        );

        assertThat(instance.buildDiseaseGeneAssociations(), equalTo(expected));
    }

    @Test
    void multiGeneDiseaseMultiOmimMoi() {
        List<DiseaseGene> omimDiseaseGenes = List.of(
                diseaseGene("OMIM:618531", "OMIM:606936", 54795, "TRPM4", AUTOSOMAL_DOMINANT),
                diseaseGene("OMIM:617756", "OMIM:602765", 3889, "KRT83", AUTOSOMAL_RECESSIVE)
        );

        String orphaDiseaseId = "ORPHA:316";

        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = ArrayListMultimap.create();
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping("OMIM:618531", MappingType.BTNT));
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping("OMIM:617756", MappingType.BTNT));

        ListMultimap<String, DiseaseGene> orphaDiseaseGenes = ArrayListMultimap.create();
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, "OMIM:606936", 54795, "TRPM4", UNKNOWN));
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, "OMIM:602765", 3889, "KRT83", UNKNOWN));
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, "OMIM:136440", 2531, "KDSR", UNKNOWN));
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, "OMIM:152445", 4014, "LORICRIN", UNKNOWN));

        ListMultimap<String, InheritanceMode> inheritanceModesMap = ArrayListMultimap.create();
        inheritanceModesMap.put(orphaDiseaseId, AUTOSOMAL_DOMINANT);

        OrphanetDiseaseGeneFactory instance = new OrphanetDiseaseGeneFactory(omimDiseaseGenes, orphaOmimMappings, orphaDiseaseGenes, inheritanceModesMap);

        List<DiseaseGene> expected = List.of(
                diseaseGene(orphaDiseaseId, "OMIM:606936", 54795, "TRPM4", AUTOSOMAL_DOMINANT),
                diseaseGene(orphaDiseaseId, "OMIM:602765", 3889, "KRT83", AUTOSOMAL_RECESSIVE),
                diseaseGene(orphaDiseaseId, "OMIM:136440", 2531, "KDSR", AUTOSOMAL_DOMINANT),
                diseaseGene(orphaDiseaseId, "OMIM:152445", 4014, "LORICRIN", AUTOSOMAL_DOMINANT)
        );

        assertThat(instance.buildDiseaseGeneAssociations(), equalTo(expected));
    }

    @Test
    void multiGeneDiseaseMoi() {
        List<DiseaseGene> omimDiseaseGenes = List.of(
                diseaseGene("OMIM:617577", "OMIM:603332", 25981, "DNAH1", AUTOSOMAL_RECESSIVE),
                diseaseGene("OMIM:618801", "OMIM:610732", 54970, "TTC12", AUTOSOMAL_DOMINANT_AND_RECESSIVE),
                diseaseGene("OMIM:618781", "OMIM:618726", 152110, "NEK10 ", AUTOSOMAL_DOMINANT_AND_RECESSIVE),
                diseaseGene("OMIM:300991", "OMIM:300933", 139212, "DNAAF6 ", X_RECESSIVE)
        );

        String orphaDiseaseId = "ORPHA:244";

        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = ArrayListMultimap.create();
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping("OMIM:617577", MappingType.BTNT));
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping("OMIM:618801", MappingType.BTNT));
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping("OMIM:618781", MappingType.BTNT));
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping("OMIM:300991", MappingType.BTNT));

        ListMultimap<String, DiseaseGene> orphaDiseaseGenes = ArrayListMultimap.create();
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, "OMIM:603332", 25981, "DNAH1", UNKNOWN));
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, "OMIM:610732", 54970, "TTC12", UNKNOWN));
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, "OMIM:618726", 152110, "NEK10", UNKNOWN));
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, "OMIM:300933", 139212, "DNAAF6", UNKNOWN));
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, "", 0, "", UNKNOWN));

        ListMultimap<String, InheritanceMode> inheritanceModesMap = ArrayListMultimap.create();
        inheritanceModesMap.put(orphaDiseaseId, AUTOSOMAL_DOMINANT);
        inheritanceModesMap.put(orphaDiseaseId, AUTOSOMAL_RECESSIVE);

        OrphanetDiseaseGeneFactory instance = new OrphanetDiseaseGeneFactory(omimDiseaseGenes, orphaOmimMappings, orphaDiseaseGenes, inheritanceModesMap);

        List<DiseaseGene> expected = List.of(
                diseaseGene(orphaDiseaseId, "OMIM:603332", 25981, "DNAH1", AUTOSOMAL_RECESSIVE),
                diseaseGene(orphaDiseaseId, "OMIM:610732", 54970, "TTC12", AUTOSOMAL_DOMINANT_AND_RECESSIVE),
                diseaseGene(orphaDiseaseId, "OMIM:618726", 152110, "NEK10", AUTOSOMAL_DOMINANT_AND_RECESSIVE),
                diseaseGene(orphaDiseaseId, "OMIM:300933", 139212, "DNAAF6", X_RECESSIVE)
        );

        assertThat(instance.buildDiseaseGeneAssociations(), equalTo(expected));
    }

    @Test
    void emptyOmimGeneIdAreNotReturned() {
        List<DiseaseGene> omimDiseaseGenes = List.of(
                diseaseGene("OMIM:617577", "OMIM:603332", 25981, "DNAH1", AUTOSOMAL_RECESSIVE),
                diseaseGene("OMIM:618801", "OMIM:610732", 54970, "TTC12", AUTOSOMAL_DOMINANT_AND_RECESSIVE),
                diseaseGene("OMIM:618781", "OMIM:618726", 152110, "NEK10 ", AUTOSOMAL_DOMINANT_AND_RECESSIVE),
                diseaseGene("OMIM:300991", "OMIM:300933", 139212, "DNAAF6 ", X_RECESSIVE)
        );

        String orphaDiseaseId = "ORPHA:244";

        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = ArrayListMultimap.create();
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping("OMIM:617577", MappingType.BTNT));
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping("OMIM:618801", MappingType.BTNT));
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping("OMIM:618781", MappingType.BTNT));
        orphaOmimMappings.put(orphaDiseaseId, new OrphaOmimMapping("OMIM:300991", MappingType.BTNT));

        ListMultimap<String, DiseaseGene> orphaDiseaseGenes = ArrayListMultimap.create();
        // This is the key part - there are many OMIM-Orphanet mappings, but are missing an OMIM gene ID so can't be mapped.
        orphaDiseaseGenes.put(orphaDiseaseId, diseaseGene(orphaDiseaseId, "", 0, "", UNKNOWN));

        ListMultimap<String, InheritanceMode> inheritanceModesMap = ArrayListMultimap.create();
        inheritanceModesMap.put(orphaDiseaseId, AUTOSOMAL_DOMINANT);
        inheritanceModesMap.put(orphaDiseaseId, AUTOSOMAL_RECESSIVE);

        OrphanetDiseaseGeneFactory instance = new OrphanetDiseaseGeneFactory(omimDiseaseGenes, orphaOmimMappings, orphaDiseaseGenes, inheritanceModesMap);

        assertThat(instance.buildDiseaseGeneAssociations().isEmpty(), equalTo(true));
    }

    private DiseaseGene diseaseGene(String diseaseId, String omimGeneId, int entrezGeneId, String geneSymbol, InheritanceMode inheritanceMode) {
        return DiseaseGene.builder()
                .diseaseId(diseaseId)
                .diseaseType(DISEASE)
                .omimGeneId(omimGeneId)
                .entrezGeneId(entrezGeneId)
                .geneSymbol(geneSymbol)
                .inheritanceMode(inheritanceMode)
                .build();
    }

    private Resource resource(Path fileDirectory, String filename) {
        return Resource.builder()
                .fileDirectory(fileDirectory)
                .fileName(filename)
                .build();
    }

    @Test
    @Disabled("Integration test")
    void integrationTest() {
        Path inDir = Paths.get("/home/hhx640/Downloads/");

        ReleaseFileSystem releaseFileSystem = new ReleaseFileSystem(inDir, "test");
        ResourceBuilder resourceBuilder = new ResourceBuilder(releaseFileSystem);

        // OMIM resources
        DiseaseInheritanceCacheReader diseaseInheritanceCacheReader = new DiseaseInheritanceCacheReader(resource(inDir, "phenotype.hpoa"));
        OmimGeneMap2Reader omimGeneMap2Reader = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, resource(inDir, "genemap2.txt"));


        // Orphanet resources
        // Orphanet-OMIM Disease mappings
        Product1DiseaseXmlReader product1DiseaseXmlReader = new Product1DiseaseXmlReader(resource(inDir, "en_product1.xml"));
        // Orphanet-OMIM Gene mappings
        OmimMimToGeneReader mimToGeneReader = new OmimMimToGeneReader(resource(inDir, "mim2gene.txt"));
        Product6DiseaseGeneXmlReader product6DiseaseGeneXmlReader = new Product6DiseaseGeneXmlReader(mimToGeneReader, resource(inDir, "en_product6.xml"));
        // Orphanet MOI
        Product9InheritanceXmlReader product9InheritanceXmlReader = new Product9InheritanceXmlReader(resource(inDir, "en_product9_ages.xml"));

        List<DiseaseGene> omimDiseaseGenes = omimGeneMap2Reader.read();

        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = product1DiseaseXmlReader.read();
        ListMultimap<String, DiseaseGene> orphaDiseaseGenes = product6DiseaseGeneXmlReader.read();
        ListMultimap<String, InheritanceMode> inheritanceModesMap = product9InheritanceXmlReader.read();

        OrphanetDiseaseGeneFactory instance = new OrphanetDiseaseGeneFactory(omimDiseaseGenes, orphaOmimMappings, orphaDiseaseGenes, inheritanceModesMap);

        List<DiseaseGene> orphanetDiseaseGenes = instance.buildDiseaseGeneAssociations();

        try (BufferedWriter writer = Files.newBufferedWriter(inDir.resolve("orpha_new_integration.pg"))) {
            for (DiseaseGene diseaseGene : orphanetDiseaseGenes) {
                writer.write(diseaseGene.toOutputLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}