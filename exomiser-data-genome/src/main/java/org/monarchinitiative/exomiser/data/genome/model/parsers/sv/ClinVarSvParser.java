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

import org.monarchinitiative.exomiser.core.genome.Contigs;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.data.genome.model.SvLengthCalculator;
import org.monarchinitiative.exomiser.data.genome.model.SvPathogenicity;
import org.monarchinitiative.exomiser.data.genome.model.parsers.Parser;
import org.monarchinitiative.svart.VariantType;

import java.util.List;

import static org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData.ClinSig.*;

public class ClinVarSvParser implements Parser<SvPathogenicity> {

    private final GenomeAssembly genomeAssembly;

    public ClinVarSvParser(GenomeAssembly genomeAssembly) {
        this.genomeAssembly = genomeAssembly;
    }

    //#AlleleID	Type	Name	GeneID	GeneSymbol	HGNC_ID	ClinicalSignificance	ClinSigSimple	LastEvaluated	RS# (dbSNP)	nsv/esv (dbVar)	RCVaccession	PhenotypeIDS	PhenotypeList	Origin	OriginSimple	Assembly	ChromosomeAccession	Chromosome	Start	Stop	ReferenceAllele	AlternateAllele	Cytogenetic	ReviewStatus	NumberSubmitters	Guidelines	TestedInGTR	OtherIDs	SubmitterCategories	VariationID	PositionVCF	ReferenceAlleleVCF	AlternateAlleleVCF
    //15041	Indel	NM_014855.3(AP5Z1):c.80_83delinsTGCTGTAAACTGTAACTGTAAA (p.Arg27_Ile28delinsLeuLeuTer)	9907	AP5Z1	HGNC:22197	Pathogenic	1	Jun 29, 2010	397704705	-	RCV000000012	MONDO:MONDO:0013342,MedGen:C3150901,OMIM:613647,Orphanet:306511	Spastic paraplegia 48, autosomal recessive	germline	germline	GRCh37	NC_000007.13	7	4820844	4820847	na	na	7p22.1	no assertion criteria provided	1	-	N	ClinGen:CA215070,OMIM:613653.0001	1	2	4820844	GGAT	TGCTGTAAACTGTAACTGTAAA
    //15041	Indel	NM_014855.3(AP5Z1):c.80_83delinsTGCTGTAAACTGTAACTGTAAA (p.Arg27_Ile28delinsLeuLeuTer)	9907	AP5Z1	HGNC:22197	Pathogenic	1	Jun 29, 2010	397704705	-	RCV000000012	MONDO:MONDO:0013342,MedGen:C3150901,OMIM:613647,Orphanet:306511	Spastic paraplegia 48, autosomal recessive	germline	germline	GRCh38	NC_000007.14	7	4781213	4781216	na	na	7p22.1	no assertion criteria provided	1	-	N	ClinGen:CA215070,OMIM:613653.0001	1	2	4781213	GGAT	TGCTGTAAACTGTAACTGTAAA

    @Override
    public List<SvPathogenicity> parseLine(String line) {
        if (line.startsWith("#")) {
            return List.of();
        }

        String[] tokens = line.split("\t");

        String assembly = tokens[16];
        if (!isSupportedAssembly(assembly)) {
            return List.of();
        }
        int chr = Contigs.parseId(tokens[18]);
        int start = Integer.parseInt(tokens[19]);
        int end = Integer.parseInt(tokens[20]);
        VariantType variantType = parseType(tokens[1]);
        int svLen = SvLengthCalculator.calculateLength(start, end, variantType);
        String dbVarId = tokens[10];
        // Only include SV variants - there are a lot of small variants in the file too.
        if (dbVarId.isEmpty() || dbVarId.equals("-")) {
            return List.of();
        }

        String rcvId = parseRcv(tokens[11]);
        String variationId = tokens[30];
        ClinVarData.ClinSig clinSig = parseClinSig(tokens[6]);
        ClinVarData.ReviewStatus clinRevStat = ClinVarData.ReviewStatus.parseReviewStatus(tokens[24]);
        return List.of(new SvPathogenicity(chr, start, end, svLen, variantType, dbVarId, "CLINVAR", rcvId, variationId, clinSig, clinRevStat));
    }

    private String parseRcv(String token) {
        if (token.isEmpty()) {
            return token;
        }
        String[] rcvIds = token.split("\\|");
        return rcvIds[0];
    }

    private boolean isSupportedAssembly(String assembly) {
        try {
            return genomeAssembly == GenomeAssembly.parseAssembly(assembly);
        } catch (GenomeAssembly.InvalidGenomeAssemblyException ex) {
            // swallow
        }
        return false;
    }

    private VariantType parseType(String type) {
        // zcat variant_summary.txt.gz | awk -F '\t' '{ print $2 }' | sort | uniq -c
        //     62 Complex
        //  34927 copy number gain
        //  35178 copy number loss
        //  99138 Deletion
        //  43668 Duplication
        //      6 fusion
        //  11383 Indel
        //   8485 Insertion
        //    744 Inversion
        //  23055 Microsatellite
        //     99 protein only
        //1437956 single nucleotide variant
        //      1 Tandem duplication
        //    313 Translocation
        //      1 Type
        //   3526 Variation
        switch (type) {
            case "single nucleotide variant":
                return VariantType.SNV;
            case "Deletion":
            case "Indel":
                // we're going to consider Indels as deletions
                return VariantType.DEL;
            case "copy number gain":
                return VariantType.CNV_GAIN;
            case "copy number loss":
                return VariantType.CNV_LOSS;
            case "Duplication":
            case "Tandem duplication":
                return VariantType.DUP;
            case "Microsatellite":
                return VariantType.STR;
            case "Insertion":
                return VariantType.INS;
            case "Inversion":
                return VariantType.INV;
            case "Translocation":
                return VariantType.TRA;
            default:
                return VariantType.UNKNOWN;
        }
    }

    private ClinVarData.ClinSig parseClinSig(String clinsig) {
        switch (clinsig) {
            case "Uncertain significance":
                return UNCERTAIN_SIGNIFICANCE;
            case "Benign":
                return BENIGN;
            case "Benign/Likely benign":
                return BENIGN_OR_LIKELY_BENIGN;
            case "Likely benign":
                return LIKELY_BENIGN;
            case "Conflicting interpretations of pathogenicity":
                return CONFLICTING_PATHOGENICITY_INTERPRETATIONS;
            case "Likely pathogenic":
                return LIKELY_PATHOGENIC;
            case "Pathogenic/Likely pathogenic":
                return PATHOGENIC_OR_LIKELY_PATHOGENIC;
            case "Pathogenic":
                return PATHOGENIC;
            case "Affects":
                return AFFECTS;
            case "association":
                return ASSOCIATION;
            case "drug response":
                return DRUG_RESPONSE;
            case "other":
                return OTHER;
            case "protective":
                return PROTECTIVE;
            case "risk factor":
                return RISK_FACTOR;
            case "not provided":
            default:
                return NOT_PROVIDED;
        }
    }

}

