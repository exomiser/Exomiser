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

package org.monarchinitiative.exomiser.data.genome.model.parsers.sv;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.data.genome.model.SvPathogenicity;
import org.monarchinitiative.svart.VariantType;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ClinVarSvParserTest implements SvParserTest {

    ClinVarSvParser instance = new ClinVarSvParser(GenomeAssembly.HG19);

    @Test
    public void parseHeaderLine() {
        List<SvPathogenicity> result = instance.parseLine("#AlleleID\tType\tName\tGeneID\tGeneSymbol\tHGNC_ID\tClinicalSignificance\tClinSigSimple\tLastEvaluated\tRS# (dbSNP)\tnsv/esv (dbVar)\tRCVaccession\tPhenotypeIDS\tPhenotypeList\tOrigin\tOriginSimple\tAssembly\tChromosomeAccession\tChromosome\tStart\tStop\tReferenceAllele\tAlternateAllele\tCytogenetic\tReviewStatus\tNumberSubmitters\tGuidelines\tTestedInGTR\tOtherIDs\tSubmitterCategories\tVariationID\tPositionVCF\tReferenceAlleleVCF\tAlternateAlleleVCF\n");
        assertThat(result, equalTo(List.of()));
    }

    @Nested
    public class AssemblyTests {

        @Test
        public void wrongAssemblyProducesEmptyResult() {
            List<SvPathogenicity> result = instance.parseLine("15064\tIndel\tNM_015600.4(ABHD12):c.-6898_191+7002delinsCC\t26090\tABHD12\tHGNC:15868\tPathogenic\t1\tSep 10, 2010\t-1\tnsv1067853\tRCV000000042\tMONDO:MONDO:0012984,MedGen:C2675204,OMIM:612674,Orphanet:171848\tPolyneuropathy, hearing loss, ataxia, retinitis pigmentosa, and cataract\tgermline\tgermline\tGRCh38\tNC_000020.10\t20\t25364147\t25378237\tna\tGG\t20p11.21\tno assertion criteria provided\t1\t-\tN\tdbVar:nssv3761628,OMIM:613599.0002\t1\t25\t-1\tna\tna\n");
            assertThat(result, equalTo(List.of()));
        }

        @Test
        public void unrecognisedAssemblyProducesEmptyResult() {
            List<SvPathogenicity> result = instance.parseLine("15064\tIndel\tNM_015600.4(ABHD12):c.-6898_191+7002delinsCC\t26090\tABHD12\tHGNC:15868\tPathogenic\t1\tSep 10, 2010\t-1\tnsv1067853\tRCV000000042\tMONDO:MONDO:0012984,MedGen:C2675204,OMIM:612674,Orphanet:171848\tPolyneuropathy, hearing loss, ataxia, retinitis pigmentosa, and cataract\tgermline\tgermline\tna\tNC_000020.10\t20\t25364147\t25378237\tna\tGG\t20p11.21\tno assertion criteria provided\t1\t-\tN\tdbVar:nssv3761628,OMIM:613599.0002\t1\t25\t-1\tna\tna\n");
            assertThat(result, equalTo(List.of()));
        }

    }

    @Test
    public void noDbVarIdProducesEmptyResult() {
        List<SvPathogenicity> result = instance.parseLine("15041\tIndel\tNM_014855.3(AP5Z1):c.80_83delinsTGCTGTAAACTGTAACTGTAAA (p.Arg27_Ile28delinsLeuLeuTer)\t9907\tAP5Z1\tHGNC:22197\tPathogenic\t1\tJun 29, 2010\t397704705\t-\tRCV000000012\tMONDO:MONDO:0013342,MedGen:C3150901,OMIM:613647,Orphanet:306511\tSpastic paraplegia 48, autosomal recessive\tgermline\tgermline\tGRCh37\tNC_000007.13\t7\t4820844\t4820847\tna\tna\t7p22.1\tno assertion criteria provided\t1\t-\tN\tClinGen:CA215070,OMIM:613653.0001\t1\t2\t4820844\tGGAT\tTGCTGTAAACTGTAACTGTAAA\n");
        assertThat(result, equalTo(List.of()));
    }

    @Test
    public void dbVarIdProducesResult() {
        List<SvPathogenicity> result = instance.parseLine("15064\tIndel\tNM_015600.4(ABHD12):c.-6898_191+7002delinsCC\t26090\tABHD12\tHGNC:15868\tPathogenic\t1\tSep 10, 2010\t-1\tnsv1067853\tRCV000000042\tMONDO:MONDO:0012984,MedGen:C2675204,OMIM:612674,Orphanet:171848\tPolyneuropathy, hearing loss, ataxia, retinitis pigmentosa, and cataract\tgermline\tgermline\tGRCh37\tNC_000020.10\t20\t25364147\t25378237\tna\tGG\t20p11.21\tno assertion criteria provided\t1\t-\tN\tdbVar:nssv3761628,OMIM:613599.0002\t1\t25\t-1\tna\tna\n");
        assertThat(result, equalTo(List.of(new SvPathogenicity(20, 25364147, 25378237, -14091, VariantType.DEL, "nsv1067853", "CLINVAR", "RCV000000042", "25", ClinVarData.ClinSig.PATHOGENIC, ClinVarData.ReviewStatus.NO_ASSERTION_CRITERIA_PROVIDED))));
    }

    @Test
    public void multipleRcvIds() {
        List<SvPathogenicity> result = instance.parseLine("190774\tDeletion\tNM_000157.4(GBA):c.1265_1319del (p.Leu422fs)\t2629\tGBA\tHGNC:4177\tPathogenic\t1\tDec 03, 2019\t80356768\tnsv1067916\tRCV000004555|RCV000020147|RCV000173718|RCV000723462\tMONDO:MONDO:0011945,MedGen:C1842704,OMIM:608013,Orphanet:85212|MONDO:MONDO:0018150,MedGen:C0017205,Orphanet:355|MONDO:MONDO:0009265,MedGen:C1961835,OMIM:230800,Orphanet:355,Orphanet:77259|MedGen:CN517202\tGaucher disease, perinatal lethal|Gaucher disease|Gaucher's disease, type 1|not provided\tgermline\tgermline\tGRCh37\tNC_000001.10\t1\t155205541\t155205595\tna\tna\t1q22\tcriteria provided, multiple submitters, no conflicts\t5\t-\tY\tClinGen:CA253083,dbVar:nssv3761540,OMIM:606463.0023\t3\t193611\t155205540\tGGGACTGTCGACAAAGTTACGCACCCAATTGGGTCCTCCTTCGGGGTTCAGGGCAA\tG\n");
        assertThat(result, equalTo(List.of(new SvPathogenicity(1, 155205541, 155205595, -55, VariantType.DEL, "nsv1067916", "CLINVAR", "RCV000004555", "193611", ClinVarData.ClinSig.PATHOGENIC, ClinVarData.ReviewStatus.CRITERIA_PROVIDED_MULTIPLE_SUBMITTERS_NO_CONFLICTS))));

    }

    @Test
    public void insertion() {
        List<SvPathogenicity> result = instance.parseLine("21676\tInsertion\tNM_007129.4(ZIC2):c.177_178ins56\t7546\tZIC2\tHGNC:12873\tPathogenic\t1\tAug 29, 2013\t-1\tnsv1067866\tRCV000007016\tMONDO:MONDO:0012322,MedGen:C1864827,OMIM:609637,Orphanet:2162\tHoloprosencephaly 5\tgermline\tgermline\tGRCh37\tNC_000013.10\t13\t100634495\t100634496\tna\tna\t13q32.3\tno assertion criteria provided\t2\t-\tN\tdbVar:nssv3761594,OMIM:603073.0001\t1\t16637\t-1\tna\tna\n");
        assertThat(result, equalTo(List.of(new SvPathogenicity(13, 100634495, 100634496, 2, VariantType.INS, "nsv1067866", "CLINVAR", "RCV000007016", "16637", ClinVarData.ClinSig.PATHOGENIC, ClinVarData.ReviewStatus.NO_ASSERTION_CRITERIA_PROVIDED))));
    }

    @Test
    public void deletionPrecise() {
        // This variant is also in the standard ClinVar whitelist for precise variants.
        List<SvPathogenicity> result = instance.parseLine("190774\tDeletion\tNM_000157.4(GBA):c.1265_1319del (p.Leu422fs)\t2629\tGBA\tHGNC:4177\tPathogenic\t1\tDec 03, 2019\t80356768\tnsv1067916\tRCV000004555|RCV000020147|RCV000173718|RCV000723462\tMONDO:MONDO:0011945,MedGen:C1842704,OMIM:608013,Orphanet:85212|MONDO:MONDO:0018150,MedGen:C0017205,Orphanet:355|MONDO:MONDO:0009265,MedGen:C1961835,OMIM:230800,Orphanet:355,Orphanet:77259|MedGen:CN517202\tGaucher disease, perinatal lethal|Gaucher disease|Gaucher's disease, type 1|not provided\tgermline\tgermline\tGRCh37\tNC_000001.10\t1\t155205541\t155205595\tna\tna\t1q22\tcriteria provided, multiple submitters, no conflicts\t5\t-\tY\tClinGen:CA253083,dbVar:nssv3761540,OMIM:606463.0023\t3\t193611\t155205540\tGGGACTGTCGACAAAGTTACGCACCCAATTGGGTCCTCCTTCGGGGTTCAGGGCAA\tG\n");
        assertThat(result, equalTo(List.of(new SvPathogenicity(1, 155205541, 155205595, -55, VariantType.DEL, "nsv1067916", "CLINVAR", "RCV000004555", "193611", ClinVarData.ClinSig.PATHOGENIC, ClinVarData.ReviewStatus.CRITERIA_PROVIDED_MULTIPLE_SUBMITTERS_NO_CONFLICTS))));
    }

    @Test
    public void deletion() {
        List<SvPathogenicity> result = instance.parseLine("361919\tDeletion\tGRCh37/hg19 17q12(chr17:34815072-36192492)x1\t-1\tsubset of 15 genes: HNF1B\t-\tPathogenic\t1\tFeb 09, 2016\t-1\tnsv491563\tRCV000416291\tMONDO:MONDO:0013797,MedGen:C3281138,OMIM:614527,Orphanet:261265\tChromosome 17q12 deletion syndrome\tgermline\tgermline\tGRCh37\tNC_000017.10\t17\t34815072\t36192492\tna\tna\t17q12\tno assertion criteria provided\t1\t-\tN\t-\t1\t375218\t-1\tna\tna\n");
        assertThat(result, equalTo(List.of(new SvPathogenicity(17, 34815072, 36192492, -1377421, VariantType.DEL, "nsv491563", "CLINVAR", "RCV000416291", "375218", ClinVarData.ClinSig.PATHOGENIC, ClinVarData.ReviewStatus.NO_ASSERTION_CRITERIA_PROVIDED))));
    }

    @Test
    public void duplication() {
        List<SvPathogenicity> result = new ClinVarSvParser(GenomeAssembly.HG38).parseLine("31935\tDuplication\tNC_000022.11:g.(?_42126499)_(42130881_?)dup\t1565\tCYP2D6\tHGNC:2625\tdrug response\t0\tJan 30, 2015\t-1\tnsv1197529\tRCV000030944\tMedGen:C1837160\tCodeine response\tgermline\tgermline\tGRCh38\tNC_000022.11\t22\t42126499\t42130881\tna\tna\t22q13.1\tno assertion criteria provided\t1\t-\tN\tdbVar:nssv7487166,OMIM:124030.0008\t1\t16896\t-1\tna\tna\n");
        assertThat(result, equalTo(List.of(new SvPathogenicity(22, 42126499, 42130881, 4383, VariantType.DUP, "nsv1197529", "CLINVAR", "RCV000030944", "16896", ClinVarData.ClinSig.DRUG_RESPONSE, ClinVarData.ReviewStatus.NO_ASSERTION_CRITERIA_PROVIDED))));
    }

    @Override
    public void inversion() {
        // empty - there are no inversions with an nsv
    }

    @Override
    public void breakend() {
        // empty - there are no breakends with an nsv
    }

    @Test
    public void cnvLoss() {
        List<SvPathogenicity> result = instance.parseLine("384301\tcopy number loss\tGRCh37/hg19 22q11.21(chr22:19478208-20168230)x1\t-1\tsubset of 15 genes: TBX1\t-\tPathogenic\t1\t-\t-1\tnsv2779061\tRCV000446507\t-\tSee cases\tnot provided\tnot provided\tGRCh37\tNC_000022.10\t22\t19478208\t20168230\tna\tna\t22q11.21\tno assertion criteria provided\t1\t-\tN\tdbVar:nssv13647419\t2\t397408\t-1\tna\tna\n");
        assertThat(result, equalTo(List.of(new SvPathogenicity(22, 19478208, 20168230, -690023, VariantType.CNV_LOSS, "nsv2779061", "CLINVAR", "RCV000446507", "397408", ClinVarData.ClinSig.PATHOGENIC, ClinVarData.ReviewStatus.NO_ASSERTION_CRITERIA_PROVIDED))));
    }

    @Test
    public void cnvGain() {
        List<SvPathogenicity> result = instance.parseLine("384303\tcopy number gain\tGRCh37/hg19 11p15.5-15.4(chr11:230615-6644927)x3\t-1\tsubset of 180 genes: CDKN1C:DCHS1:KCNQ1:KCNQ1OT1\t-\tPathogenic\t1\t-\t-1\tnsv2779077\tRCV000449417\t-\tSee cases\tnot provided\tnot provided\tGRCh37\tNC_000011.9\t11\t230615\t6644927\tna\tna\t11p15.5-15.4\tno assertion criteria provided\t1\tACMG2013,ACMG2016\tN\tdbVar:nssv13652492\t2\t397410\t-1\tna\tna\n");
        assertThat(result, equalTo(List.of(new SvPathogenicity(11, 230615, 6644927, 6414313, VariantType.CNV_GAIN, "nsv2779077", "CLINVAR", "RCV000449417", "397410", ClinVarData.ClinSig.PATHOGENIC, ClinVarData.ReviewStatus.NO_ASSERTION_CRITERIA_PROVIDED))));
    }
}