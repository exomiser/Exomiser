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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGene;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class OmimGeneMap2ReaderTest {

    private DiseaseGene omimDisease(int diseaseMim, int geneMim, String name, String geneSymbol, int entrezId, String ensembleGeneId, Disease.DiseaseType diseaseType, InheritanceMode inheritanceMode) {
        return DiseaseGene.builder()
                .diseaseId("OMIM:" + diseaseMim)
                .omimGeneId("OMIM:" + geneMim)
                .diseaseName(name)
                .geneSymbol(geneSymbol)
                .entrezGeneId(entrezId)
                .ensemblGeneId(ensembleGeneId)
                .diseaseType(diseaseType)
                .inheritanceMode(inheritanceMode)
                .build();
    }

    private final Resource emptyResource = Resource.builder()
            .fileDirectory(Paths.get(""))
            .fileName("genemap2.txt")
            .build();

    private final Resource phenotypeAnnotationsResource = resource("phenotype_annotations_test.tab");

    private final DiseaseInheritanceCacheReader diseaseInheritanceCacheReader = new DiseaseInheritanceCacheReader(phenotypeAnnotationsResource);

    private Resource resource(String filename) {
        return Resource.builder()
                .fileDirectory(Paths.get("src/test/resources/data/"))
                .fileName(filename)
                .build();
    }

    @Disabled
    @Test
    void parse() {
        OmimGeneMap2Reader instance = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, resource("genemap2.txt"));
        List<DiseaseGene> diseaseGenes = instance.read();
        diseaseGenes.forEach(diseaseGene -> {
            if ( diseaseGene.getDiseaseId().equals("OMIM:146520") || diseaseGene.getDiseaseId().equals("OMIM:146550")) {
                System.out.println(diseaseGene);
            }
        });
    }

    @Test
    void parseLineNonPhenotype() {
        OmimGeneMap2Reader instance = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, emptyResource);
        List<DiseaseGene> diseaseGenes = instance.parseLine(Map.of(),"chrY\t0\t10400000\tYp11\t\t489500\tXGR\tXG regulator\t\t\t\tin pseudoautosomal region\t\t\n");
        assertTrue(diseaseGenes.isEmpty());
    }

    @Test
    void parseLineYlinked() {
        OmimGeneMap2Reader instance = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, emptyResource);
        List<DiseaseGene> diseaseGenes = instance.parseLine(Map.of(),"chrY\t12701230\t12860838\tYq11.2\tYq11.221\t400005\tUSP9Y, DFFRY, SPGFY2\tUbiquitin-specific protease-9, Y chromosome (Drosophila fat facets related, Y-linked)\tUSP9Y\t8287\tENSG00000114374\t\tSpermatogenic failure, Y-linked, 2, 415000 (3), Y-linked\tUsp9y (MGI:1313274)\n");
        List<DiseaseGene> expected = List.of(omimDisease(415000,400005, "Spermatogenic failure, Y-linked, 2", "USP9Y", 8287, "ENSG00000114374", Disease.DiseaseType.DISEASE, InheritanceMode.Y_LINKED));
        assertThat(diseaseGenes, equalTo(expected));
    }

    @Test
    void phenotypeMimEqualsGeneMim() {
        OmimGeneMap2Reader instance = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, emptyResource);
        List<DiseaseGene> diseaseGenes = instance.parseLine(Map.of(),"chr1\t46300000\t60800000\t1p33-p32\t\t612728\tBMND14\tBone mineral density quantitative trait locus 14\t\t100294718\t\tassociated with rs17131547\t[Bone mineral density QTL 14], 612728 (2)\t\n");
        assertTrue(diseaseGenes.isEmpty());
    }

    @Test
    void parseMultipleConditions() {
        OmimGeneMap2Reader instance = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, emptyResource);
        List<DiseaseGene> diseaseGenes = instance.parseLine(Map.of(),"chr10\t101007678\t101031156\t10q24.3\t10q24.31\t612971\tPDZD7, DFNB57\tPDZ domain-containing 7\tPDZD7\t79955\tENSG00000186862\t\tUsher syndrome, type IIC, GPR98/PDZD7 digenic, 605472 (3), Digenic dominant, Autosomal recessive; Deafness, autosomal recessive 57, 618003 (3), Autosomal recessive; {Retinal disease in Usher syndrome type IIA, modifier of}, 276901 (3), Autosomal recessive\tPdzd7 (MGI:3608325)\n");
        List<DiseaseGene> expected = List.of(
                // Usher syndrome, type IIC, GPR98/PDZD7 digenic, 605472 (3), Digenic dominant, Autosomal recessive;
                omimDisease(605472,612971, "Usher syndrome, type IIC, GPR98/PDZD7 digenic", "PDZD7", 79955, "ENSG00000186862", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_RECESSIVE),
                // Deafness, autosomal recessive 57, 618003 (3), Autosomal recessive;
                omimDisease(618003,612971, "Deafness, autosomal recessive 57", "PDZD7", 79955, "ENSG00000186862", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_RECESSIVE),
                //  {Retinal disease in Usher syndrome type IIA, modifier of}, 276901 (3), Autosomal recessive
                omimDisease(276901,612971, "Retinal disease in Usher syndrome type IIA, modifier of", "PDZD7", 79955, "ENSG00000186862", Disease.DiseaseType.SUSCEPTIBILITY, InheritanceMode.AUTOSOMAL_RECESSIVE)
        );
        assertThat(diseaseGenes, equalTo(expected));
    }

    @Test
    void multipleConditionsMultipleMoi() {
        OmimGeneMap2Reader instance = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, emptyResource);
        List<DiseaseGene> diseaseGenes = instance.parseLine(Map.of(),"chr18\t31318159\t31359245\t18q12.1-q12.2\t18q12.1\t125670\tDSG1, PPKS1, SPPK1, EPKHE\tDesmoglein-1\tDSG1\t1828\tENSG00000134760\tpemphigus foliaceous antigen\tKeratosis palmoplantaris striata I, AD, 148700 (3), Autosomal dominant; Erythroderma, congenital, with palmoplantar keratoderma, hypotrichosis, and hyper IgE, 615508 (3), Autosomal recessive\tDsg1a,Dsg1b (MGI:94930,MGI:2664357)\n");
        List<DiseaseGene> expected = List.of(
                // Keratosis palmoplantaris striata I, AD, 148700 (3), Autosomal dominant;
                omimDisease(148700,125670, "Keratosis palmoplantaris striata I, AD", "DSG1", 1828, "ENSG00000134760", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT),
                // Erythroderma, congenital, with palmoplantar keratoderma, hypotrichosis, and hyper IgE, 615508 (3), Autosomal recessive
                omimDisease(615508,125670, "Erythroderma, congenital, with palmoplantar keratoderma, hypotrichosis, and hyper IgE", "DSG1", 1828, "ENSG00000134760", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_RECESSIVE)
        );
        assertThat(diseaseGenes, equalTo(expected));
    }

    @Test
    void multipleConditionsSomeMissingPhenotypeIdAndMoi() {
        OmimGeneMap2Reader instance = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, emptyResource);
        List<DiseaseGene> diseaseGenes = instance.parseLine(Map.of(),"chr10\t121478329\t121598457\t10q26\t10q26.13\t176943\tFGFR2, BEK, CFD1, JWS, TK14, BBDS\tFibroblast growth factor receptor-2 (bacteria-expressed kinase)\tFGFR2\t2263\tENSG00000066468\t\tApert syndrome, 101200 (3), Autosomal dominant; Craniosynostosis, nonspecific (3); Jackson-Weiss syndrome, 123150 (3), Autosomal dominant; Scaphocephaly and Axenfeld-Rieger anomaly (3); Saethre-Chotzen syndrome, 101400 (3), Autosomal dominant; Gastric cancer, somatic, 613659 (3); Scaphocephaly, maxillary retrusion, and mental retardation, 609579 (3); Bent bone dysplasia syndrome, 614592 (3), Autosomal dominant; LADD syndrome, 149730 (3), Autosomal dominant; Craniofacial-skeletal-dermatologic dysplasia, 101600 (3), Autosomal dominant; Pfeiffer syndrome, 101600 (3), Autosomal dominant; Crouzon syndrome, 123500 (3), Autosomal dominant; Beare-Stevenson cutis gyrata syndrome, 123790 (3), Autosomal dominant; Antley-Bixler syndrome without genital anomalies or disordered steroidogenesis, 207410 (3), Autosomal dominant\tFgfr2 (MGI:95523)\n");
        List<DiseaseGene> expected = List.of(
                // Apert syndrome, 101200 (3), Autosomal dominant;
                omimDisease(101200,176943, "Apert syndrome", "FGFR2", 2263, "ENSG00000066468", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT),
                // Craniosynostosis, nonspecific (3);
                //   no phenotype id
                // Jackson-Weiss syndrome, 123150 (3), Autosomal dominant;
                omimDisease(123150,176943, "Jackson-Weiss syndrome", "FGFR2", 2263, "ENSG00000066468", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT),
                // Scaphocephaly and Axenfeld-Rieger anomaly (3);
                //   no phenotype id
                // Saethre-Chotzen syndrome, 101400 (3), Autosomal dominant;
                omimDisease(101400,176943, "Saethre-Chotzen syndrome", "FGFR2", 2263, "ENSG00000066468", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT),
                // Gastric cancer, somatic, 613659 (3);
                omimDisease(613659,176943, "Gastric cancer, somatic", "FGFR2", 2263, "ENSG00000066468", Disease.DiseaseType.DISEASE, InheritanceMode.UNKNOWN),
                // Scaphocephaly, maxillary retrusion, and mental retardation, 609579 (3);
                omimDisease(609579,176943, "Scaphocephaly, maxillary retrusion, and mental retardation", "FGFR2", 2263, "ENSG00000066468", Disease.DiseaseType.DISEASE, InheritanceMode.UNKNOWN),
                // Bent bone dysplasia syndrome, 614592 (3), Autosomal dominant;
                omimDisease(614592,176943, "Bent bone dysplasia syndrome", "FGFR2", 2263, "ENSG00000066468", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT),
                // LADD syndrome, 149730 (3), Autosomal dominant;
                omimDisease(149730,176943, "LADD syndrome", "FGFR2", 2263, "ENSG00000066468", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT),
                // Craniofacial-skeletal-dermatologic dysplasia, 101600 (3), Autosomal dominant;
                omimDisease(101600,176943, "Craniofacial-skeletal-dermatologic dysplasia", "FGFR2", 2263, "ENSG00000066468", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT),
                // Pfeiffer syndrome, 101600 (3), Autosomal dominant;
                omimDisease(101600,176943, "Pfeiffer syndrome", "FGFR2", 2263, "ENSG00000066468", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT),
                // Crouzon syndrome, 123500 (3), Autosomal dominant;
                omimDisease(123500,176943, "Crouzon syndrome", "FGFR2", 2263, "ENSG00000066468", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT),
                // Beare-Stevenson cutis gyrata syndrome, 123790 (3), Autosomal dominant;
                omimDisease(123790,176943, "Beare-Stevenson cutis gyrata syndrome", "FGFR2", 2263, "ENSG00000066468", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT),
                // Antley-Bixler syndrome without genital anomalies or disordered steroidogenesis, 207410 (3), Autosomal dominant
                omimDisease(207410,176943, "Antley-Bixler syndrome without genital anomalies or disordered steroidogenesis", "FGFR2", 2263, "ENSG00000066468", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT)
        );
        assertThat(diseaseGenes, equalTo(expected));
    }

    @Test
    void genePhenotypeCombined() {
        OmimGeneMap2Reader instance = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, emptyResource);
        List<DiseaseGene> diseaseGenes = instance.parseLine(Map.of(),"chr1\t34781213\t34786363\t1p35.1\t1p34.3\t603324\tGJB3, CX31, DFNA2B, EKVP1\tGap junction protein, beta-3\tGJB3\t2707\tENSG00000188910\tsame YAC as GJA4\tDeafness, autosomal dominant 2B, 612644 (3), Autosomal dominant; Deafness, autosomal recessive (3); Deafness, autosomal dominant, with peripheral neuropathy (3); Deafness, digenic, GJB2/GJB3, 220290 (3), Digenic dominant, Autosomal recessive; Erythrokeratodermia variabilis et progressiva 1, 133200 (3), Autosomal dominant, Autosomal recessive\tGjb3 (MGI:95721)\n");
        List<DiseaseGene> expected = List.of(
                // Deafness, autosomal dominant 2B, 612644 (3), Autosomal dominant;
                omimDisease(612644,603324, "Deafness, autosomal dominant 2B", "GJB3", 2707,	"ENSG00000188910", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT),
                // Deafness, autosomal recessive (3);
                // no phenotype id
                // Deafness, autosomal dominant, with peripheral neuropathy (3);
                // no phenotype id
                // Deafness, digenic, GJB2/GJB3, 220290 (3), Digenic dominant, Autosomal recessive;
                omimDisease(220290,603324, "Deafness, digenic, GJB2/GJB3", "GJB3",2707,"ENSG00000188910", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_RECESSIVE),
                // Erythrokeratodermia variabilis et progressiva 1, 133200 (3), Autosomal dominant, Autosomal recessive
                omimDisease(133200,603324, "Erythrokeratodermia variabilis et progressiva 1", "GJB3", 2707,	"ENSG00000188910", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT_AND_RECESSIVE)
                );
        assertThat(diseaseGenes, equalTo(expected));
    }

    // chrX	154541237	154565045	Xq28	Xq28	300248	IKBKG, NEMO, FIP3, IP, IPD2, AMCBX1, IMD33, EDAID1	Inhibitor of kappa light polypeptide gene enhancer in B cells, kinase of, gamma (NF-kappa-B essential modulator)	IKBKG	8517	ENSG00000269335

    // Immunodeficiency 33, 300636 (3), X-linked recessive;
    // Incontinentia pigmenti, 308300 (3), X-linked dominant;
    // Immunodeficiency, isolated, 300584 (3);
    // Ectodermal dysplasia and immunodeficiency 1, 300291 (3);
    // Invasive pneumococcal disease, recurrent isolated, 2, 300640 (3);
    // Ectodermal, dysplasia, anhidrotic, lymphedema and immunodeficiency, 300301 (3)


    @Test
    void xLinkedPreferredOverSomatic() {
        OmimGeneMap2Reader instance = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, emptyResource);
        List<DiseaseGene> diseaseGenes = instance.parseLine(Map.of(),"chrX\t48903182\t48911957\tXp11.23-p11.22\tXp11.23\t314375\tSLC35A2, UGALT, UGTL, UGT2, CDGX, CDG2M\tSolute carrier family 35 (UDP-galactose transporter), member 2\tSLC35A2\t7355\tENSG00000102100\t\tCongenital disorder of glycosylation, type IIm, 300896 (3), X-linked dominant, Somatic mosaicism\n");
        List<DiseaseGene> expected = List.of(
                // Congenital disorder of glycosylation, type IIm, 300896 (3), X-linked dominant, Somatic mosaicism
                omimDisease(300896,314375, "Congenital disorder of glycosylation, type IIm", "SLC35A2", 7355, "ENSG00000102100", Disease.DiseaseType.DISEASE, InheritanceMode.X_DOMINANT)
        );
        assertThat(diseaseGenes, equalTo(expected));
    }

    @Test
    void susceptibilityMultipleMoi() {
        OmimGeneMap2Reader instance = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, emptyResource);
        List<DiseaseGene> diseaseGenes = instance.parseLine(Map.of(),"chr1\t196774799\t196795406\t1q31-q32.1\t1q31.3\t605336\tCFHR3, FHR3, HLF4, CFHL3\tComplement factor H-related 3\tCFHR3\t10878\tENSG00000116785\t\t{Macular degeneration, age-related, reduced risk of}, 603075 (3), Autosomal dominant; {Hemolytic uremic syndrome, atypical, susceptibility to}, 235400 (3), Autosomal dominant, Autosomal recessive\t\n");
        List<DiseaseGene> expected = List.of(
                // {Macular degeneration, age-related, reduced risk of}, 603075 (3), Autosomal dominant;
                omimDisease(603075,605336, "Macular degeneration, age-related, reduced risk of", "CFHR3", 10878, "ENSG00000116785", Disease.DiseaseType.SUSCEPTIBILITY, InheritanceMode.AUTOSOMAL_DOMINANT),
                // {Hemolytic uremic syndrome, atypical, susceptibility to}, 235400 (3), Autosomal dominant, Autosomal recessive
                omimDisease(235400,605336, "Hemolytic uremic syndrome, atypical, susceptibility to", "CFHR3", 10878, "ENSG00000116785", Disease.DiseaseType.SUSCEPTIBILITY, InheritanceMode.AUTOSOMAL_DOMINANT_AND_RECESSIVE)
                );
        assertThat(diseaseGenes, equalTo(expected));
    }

    @Test
    void testPseudoautosomalInheritance() {
        OmimGeneMap2Reader instance = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, emptyResource);
        List<DiseaseGene> diseaseGenes = instance.parseLine(Map.of(),"chrY\t624343\t659410\tYpter-p11.2\tYp11.2\t400020\tSHOXY\tShort stature homeo box, Y-linked\tSHOX\t6473\tENSG00000185960\tpseudoautosomal\tLeri-Weill dyschondrosteosis, 127300 (3), Pseudoautosomal dominant; Langer mesomelic dysplasia, 249700 (3), Pseudoautosomal recessive; Short stature, idiopathic familial, 300582 (3)\t\n");
        List<DiseaseGene> expected = List.of(
                // Leri-Weill dyschondrosteosis, 127300 (3), Pseudoautosomal dominant;
                omimDisease(127300,400020, "Leri-Weill dyschondrosteosis", "SHOX", 6473, "ENSG00000185960", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT),
                // Langer mesomelic dysplasia, 249700 (3), Pseudoautosomal recessive;
                omimDisease(249700,400020, "Langer mesomelic dysplasia", "SHOX", 6473, "ENSG00000185960", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_RECESSIVE),
                // Short stature, idiopathic familial, 300582 (3)
                omimDisease(300582,400020, "Short stature, idiopathic familial", "SHOX", 6473, "ENSG00000185960", Disease.DiseaseType.DISEASE, InheritanceMode.UNKNOWN)
        );
        assertThat(diseaseGenes, equalTo(expected));
    }

    @Test
    void testMultifactorialInheritance() {
        OmimGeneMap2Reader instance = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, emptyResource);
        List<DiseaseGene> diseaseGenes = instance.parseLine(Map.of(),"chr6\t31268748\t31272091\t6p21.3\t6p21.33\t142840\tHLA-C, PSORS1\tMajor histocompatibility complex, class I, C\tHLA-C\t3107\tENSG00000204525\t\t{Psoriasis susceptibility 1}, 177900 (3), Multifactorial; {HIV-1 viremia, susceptibility to}, 609423 (3)\t\n");
        List<DiseaseGene> expected = List.of(
                // {Psoriasis susceptibility 1}, 177900 (3), Multifactorial;
                omimDisease(177900,142840, "Psoriasis susceptibility 1", "HLA-C", 3107, "ENSG00000204525", Disease.DiseaseType.SUSCEPTIBILITY, InheritanceMode.POLYGENIC),
                // {HIV-1 viremia, susceptibility to}, 609423 (3)
                omimDisease(609423,142840, "HIV-1 viremia, susceptibility to", "HLA-C", 3107, "ENSG00000204525", Disease.DiseaseType.SUSCEPTIBILITY, InheritanceMode.UNKNOWN)
        );
        assertThat(diseaseGenes, equalTo(expected));
    }

    @Test
    void noPhenotype() {
        OmimGeneMap2Reader instance = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, emptyResource);
        List<DiseaseGene> diseaseGenes = instance.parseLine(Map.of(),"chr1\t204154818\t204166336\t1q32\t1q32.1\t179820\tREN, HNFJ2\tRenin\tREN\t5972\tENSG00000143839\t~24cM distal to AT3\t[Hyperproreninemia] (3); Renal tubular dysgenesis, 267430 (3), Autosomal recessive; Hyperuricemic nephropathy, familial juvenile 2, 613092 (3), Autosomal dominant\tRen1 (MGI:97898)\n");
        List<DiseaseGene> expected = List.of(
                // ~24cM distal to AT3	[Hyperproreninemia] (3);
                // no phenotype
                // Renal tubular dysgenesis, 267430 (3), Autosomal recessive;
                omimDisease(267430,179820, "Renal tubular dysgenesis", "REN", 5972, "ENSG00000143839", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_RECESSIVE),
                // Hyperuricemic nephropathy, familial juvenile 2, 613092 (3), Autosomal dominant
                omimDisease(613092,179820, "Hyperuricemic nephropathy, familial juvenile 2", "REN", 5972, "ENSG00000143839", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT)
        );
        assertThat(diseaseGenes, equalTo(expected));
    }

    @Test
    void moiFromDiseaseInheritanceCacheOverridesOmim() {
        OmimGeneMap2Reader instance = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, emptyResource);
        // This annotation is supplied by the diseaseInheritanceCacheReader coming from the HPO annotations in phenotype_annotation.tab
        Map<String, InheritanceMode> diseaseInheritanceCache = Map.of("OMIM:267430", InheritanceMode.AUTOSOMAL_DOMINANT_AND_RECESSIVE);
        List<DiseaseGene> diseaseGenes = instance.parseLine(diseaseInheritanceCache,"chr1\t204154818\t204166336\t1q32\t1q32.1\t179820\tREN, HNFJ2\tRenin\tREN\t5972\tENSG00000143839\t~24cM distal to AT3\t[Hyperproreninemia] (3); Renal tubular dysgenesis, 267430 (3), Autosomal recessive; Hyperuricemic nephropathy, familial juvenile 2, 613092 (3), Autosomal dominant\tRen1 (MGI:97898)\n");
        List<DiseaseGene> expected = List.of(
                // ~24cM distal to AT3	[Hyperproreninemia] (3);
                // no phenotype
                // Renal tubular dysgenesis, 267430 (3), Autosomal recessive;
                omimDisease(267430,179820, "Renal tubular dysgenesis", "REN", 5972, "ENSG00000143839", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT_AND_RECESSIVE),
                // Hyperuricemic nephropathy, familial juvenile 2, 613092 (3), Autosomal dominant
                omimDisease(613092,179820, "Hyperuricemic nephropathy, familial juvenile 2", "REN", 5972, "ENSG00000143839", Disease.DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT)
        );
        assertThat(diseaseGenes, equalTo(expected));
    }

    @Test
    void xLinkedDominantAndRecessive() {
        OmimGeneMap2Reader instance = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, emptyResource);
        // This annotation is supplied by the diseaseInheritanceCacheReader coming from the HPO annotations in phenotype_annotation.tab
        Map<String, InheritanceMode> diseaseInheritanceCache = Map.of();
        List<DiseaseGene> diseaseGenes = instance.parseLine(diseaseInheritanceCache,"chrX\t41333308\t41364472\tXp11.3-p11.23\tXp11.4\t300160\tDDX3X, DDX3, DBX, MRX102, MRXSSB\tDEAD-box helicase 3, X-linked\tDDX3X\t1654\tENSG00000215301\t\tIntellectual developmental disorder, X-linked syndromic, Snijders Blok type, 300958 (3), X-linked recessive, X-linked dominant\tD1Pas1,Ddx3x (MGI:103064,MGI:91842)\n");
        List<DiseaseGene> expected = List.of(
                // Intellectual developmental disorder, X-linked syndromic, Snijders Blok type, 300958 (3), X-linked recessive, X-linked dominant
                omimDisease(300958, 300160,"Intellectual developmental disorder, X-linked syndromic, Snijders Blok type", "DDX3X", 1654, "ENSG00000215301", Disease.DiseaseType.DISEASE, InheritanceMode.X_LINKED)
        );
        assertThat(diseaseGenes, equalTo(expected));
    }
}