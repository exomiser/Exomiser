package org.monarchinitiative.exomiser.data.genome.model.parsers;

import htsjdk.tribble.TribbleException;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderVersion;
import org.monarchinitiative.exomiser.core.genome.*;
import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.FrequencySource;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.svart.Strand;
import org.monarchinitiative.svart.util.VariantTrimmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

/**
 * AlleParser for ALFA allele frequencies:
 * https://www.ncbi.nlm.nih.gov/snp/docs/gsr/alfa/
 * https://www.ncbi.nlm.nih.gov/snp/docs/gsr/alfa/ALFA_20201027095038/
 */
public class AlfaAlleleParser implements AlleleParser {

    private static final Logger logger = LoggerFactory.getLogger(AlfaAlleleParser.class);
    private static final GenomeAssembly HG_38 = GenomeAssembly.HG38;
    private final VCFCodec vcfCodec;

    private final VariantTrimmer variantTrimmer = VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase());

    protected Set<String> allowedFilterValues = Set.of(".", "PASS");

    public AlfaAlleleParser(VCFHeader vcfHeader) {
        VCFHeaderVersion version = vcfHeader.getVCFHeaderVersion() == null ? VCFHeaderVersion.VCF4_1 : vcfHeader.getVCFHeaderVersion();
        VCFCodec codec = new VCFCodec();
        codec.setVCFHeader(vcfHeader, version);
        this.vcfCodec = codec;
        // Finished 'alfa' resource - processed 163349161 objects in 6449 sec. Total 163349161 objects written.
        // results in 6.9 GB mvStore
    }

    @Override
    public List<Allele> parseLine(String line) {
        if (line.startsWith("#")) {
            // comment line.
            return List.of();
        }

        VariantContext variantContext = parseVariantContext(line);
        if (variantContext == null) {
            return List.of();
        }

        List<Allele> alleles = parseAlleles(variantContext);
        if (alleles.isEmpty()) {
            return List.of();
        }

        for (Genotype genotype : variantContext.getGenotypesOrderedByName()) {
            var freqSource = parseFrequencySource(genotype.getSampleName());
            var an = Integer.parseInt((String) genotype.getExtendedAttribute("AN", 0));
            var acString = (String) genotype.getExtendedAttribute("AC", "0");
            if (alleles.size() > 1 && acString.contains(",")) {
                // multiallelic site
                String[] acs = acString.split(",");
                for (int i = 0; i < acs.length; i++) {
                    if (!acs[i].equals("0")) {
                        int ac = Integer.parseInt(acs[i]);
                        Allele allele = alleles.get(i);
                        allele.addFrequency(AlleleData.frequencyOf(freqSource, ac, an));
                    }
                }
            } else if (!acString.equals("0")) {
                int ac = Integer.parseInt(acString);
                Allele allele = alleles.get(0);
                allele.addFrequency(AlleleData.frequencyOf(freqSource, ac, an));
            }
        }
        return alleles.stream().filter(allele -> !allele.getFrequencies().isEmpty()).toList();
    }

    @Nullable
    private VariantContext parseVariantContext(String line) {
        //'NC_000003.12	16814155	rs73031361	R	G,A	.	.	.	AN:AC	4596:4555,41	0:0,0	0:0,0	48:48,0	0:0,0	0:0,0	0:0,0	0:0,0	196:196,0	48:48,0	0:0,0	4840:4799,41' due to The provided VCF file is malformed at approximately line number 152898503: unparsable vcf record with allele R
        //'NC_000003.12	16816464	rs73031366	Y	T,C	.	.	.	AN:AC	4596:4344,252	0:0,0	0:0,0	48:48,0	0:0,0	0:0,0	0:0,0	0:0,0	196:194,2	48:48,0	0:0,0	4840:4586,254' due to The provided VCF file is malformed at approximately line number 152899233: unparsable vcf record with allele Y
        //'NC_000003.12	16894811	rs73035176	W	A,T	.	.	.	AN:AC	4596:4555,41	0:0,0	0:0,0	48:48,0	0:0,0	0:0,0	0:0,0	0:0,0	196:193,3	48:48,0	0:0,0	4840:4796,44' due to The provided VCF file is malformed at approximately line number 152921931: unparsable vcf record with allele W
        //'NC_000003.12	16902883	rs56708014	B	BGCGC	.	.	.	AN:AC	4370:28	0:0	0:0	0:0	0:0	0:0	0:0	0:0	8:1	0:0	0:0	4378:29' due to The provided VCF file is malformed at approximately line number 152924346: unparsable vcf record with allele B
        //'NC_000003.12	16902883	rs782235056	B	BGTGC,BGTGTGTGTGTGTGCGC,BGTGTGTGTGTGTGTGTGCGC	.	.	.	AN:AC	4401:31,12,16	0:0,0,0	0:0,0,0	0:0,0,0	0:0,0,0	0:0,0,0	0:0,0,0	0:0,0,0	7:0,0,0	0:0,0,0	0:0,0,0	4408:31,12,16' due to The provided VCF file is malformed at approximately line number 152924347: unparsable vcf record with allele B
        try {
            return vcfCodec.decode(line);
        } catch (TribbleException e) {
            logger.warn("Skipping line '{}' due to {}", line, e.getMessage() );
        }
        return null;
    }

    /* Name 	Population Code 	Description 	BioSample ID
     * African Others 	AFO 	Individuals with African ancestry 	SAMN10492696
     * African American 	AFA 	African American 	SAMN10492698
     * African 	AFR 	All Africans, AFO and AFA Individuals 	SAMN10492703
     * European 	EUR 	European 	SAMN10492695
     * Latin American 1 	LAC 	Latin American individiuals with Afro-Caribbean ancestry 	SAMN10492699
     * Latin American 2 	LEN 	Latin American individiuals with mostly European and Native American Ancestry 	SAMN10492700
     * South Asian 	SAS 	South Asian 	SAMN10492702
     * East Asian 	EAS 	East Asian (95%) 	SAMN10492697
     * Asian 	ASN 	All Asian individuals (EAS and OAS) excluding South Asian (SAS) 	SAMN10492704
     * Other Asian 	OAS 	Asian individiuals excluding South or East Asian 	SAMN10492701
     * Other 	OTR 	The self-reported population is inconsistent with the GRAF-assigned population 	SAMN11605645
     * Total 	TOT 	Total (~global) across all populations 	SAMN10492705
     */
    private FrequencySource parseFrequencySource(String biosampleId) {
        return switch (biosampleId) {
            case "SAMN10492696" -> FrequencySource.ALFA_AFO;
            case "SAMN10492698" -> FrequencySource.ALFA_AFA;
            case "SAMN10492703" -> FrequencySource.ALFA_AFR;
            case "SAMN10492695" -> FrequencySource.ALFA_EUR;
            case "SAMN10492699" -> FrequencySource.ALFA_LAC;
            case "SAMN10492700" -> FrequencySource.ALFA_LEN;
            case "SAMN10492702" -> FrequencySource.ALFA_SAS;
            case "SAMN10492697" -> FrequencySource.ALFA_EAS;
            case "SAMN10492704" -> FrequencySource.ALFA_ASN;
            case "SAMN10492701" -> FrequencySource.ALFA_OAS;
            case "SAMN11605645" -> FrequencySource.ALFA_OTR;
            case "SAMN10492705" -> FrequencySource.ALFA_TOT;
            default -> throw new IllegalArgumentException("Unknown biosample identifier: " + biosampleId);
        };
    }

    private List<Allele> parseAlleles(VariantContext variantContext) {
        // ALFA is primarily called against hg38, yet it does contain some hg19 and hg18 coordinates, so we're going to
        // ignore those.
        int chr = HG_38.getContigByName(variantContext.getContig()).id();
        if (chr == 0) {
            return List.of();
        }

        int pos = variantContext.getStart();
        //A dbSNP rsID such as rs101432848. In rare cases may be multiple e.g., rs200118651;rs202059104
        String rsId = RsIdParser.parseRsId(variantContext.getID());
        //Uppercasing shouldn't be necessary acccording to the VCF standard,
        //but occasionally one sees VCF files with lower case for part of the
        //sequences, e.g., to show indels.
        String ref = variantContext.getReference().getDisplayString().toUpperCase();

        //dbSNP has introduced the concept of multiple minor alleles on the
        //same VCF line with their frequencies reported in same order in the
        //INFO field in the CAF section Because of this had to introduce a loop
        //and move the dbSNP freq parsing to here. Not ideal as ESP processing
        //also goes through this method but does not use the CAF field so
        //should be skipped
        List<String> alts = variantContext.getAlternateAlleles().stream().map(htsjdk.variant.variantcontext.Allele::getDisplayString).toList();

        List<Allele> alleles = new ArrayList<>(alts.size());
        for (String alt : alts) {
            Allele allele = makeAllele(chr, pos, ref, alt);
            allele.setRsId(rsId);
            alleles.add(allele);
        }
        return alleles;
    }

    private Allele makeAllele(int chr, int pos, String ref, String alt) {
        VariantTrimmer.VariantPosition variantPosition = variantTrimmer.trim(Strand.POSITIVE, pos, ref, alt);
        return new Allele(chr, variantPosition.start(), variantPosition.ref(), variantPosition.alt());
    }

}
