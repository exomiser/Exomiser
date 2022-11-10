package org.monarchinitiative.exomiser.data.genome.model.parsers;

import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.FrequencySource;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.monarchinitiative.exomiser.core.proto.AlleleProto.FrequencySource.*;

public class Gnomad3MitoAlleleParser extends VcfAlleleParser {

    private static final Logger logger = LoggerFactory.getLogger(Gnomad3MitoAlleleParser.class);

    //##INFO=<ID=pop_AN,Number=1,Type=String,Description="List of overall allele number for each population, population order: ['afr', 'ami', 'amr', 'asj', 'eas', 'fin', 'nfe', 'oth', 'sas', 'mid']">
    //##INFO=<ID=pop_AC_het,Number=1,Type=String,Description="List of AC_het for each population, population order: ['afr', 'ami', 'amr', 'asj', 'eas', 'fin', 'nfe', 'oth', 'sas', 'mid']">
    //##INFO=<ID=pop_AC_hom,Number=1,Type=String,Description="List of AC_hom for each population, population order: ['afr', 'ami', 'amr', 'asj', 'eas', 'fin', 'nfe', 'oth', 'sas', 'mid']">

    //Heteroplasmy: Cells each contain hundreds to thousands of mtDNA copies. Most variants are homoplasmic, meaning
    // they are present in all mtDNA molecules in a cell, and represent differences between the given individual and the
    // reference human mitochondrial genome. However, some variants are heteroplasmic, or present in only a fraction of
    // the cell’s mtDNA molecules. A key challenge is to call heteroplasmic variants (particularly at low fractions) and
    // to distinguish low heteroplasmy variants from technical artifacts or contamination.
    //
    // To make the mtDNA data more usable, we provide multiple variant annotations on the website and in the downloadable
    // callset. Some of these are listed below.
    //
    // We do not calculate an overall allele count and allele frequency. Instead, separate allele counts and frequencies
    // are provided for both homoplasmic and heteroplasmic variants as well as for each top-level haplogroup. For
    // example, the “AF_hom” annotation provides the overall allele frequency taking into account only variants that were
    // called homoplasmic in samples with that variant (heteroplasmy 95-100%).

    //['afr', 'ami', 'amr', 'asj', 'eas', 'fin', 'nfe', 'oth', 'sas', 'mid']
    private static final FrequencySource[] POPULATIONS = new FrequencySource[]{
            GNOMAD_G_AFR, GNOMAD_G_AMI, GNOMAD_G_AMR, GNOMAD_G_ASJ, GNOMAD_G_EAS, GNOMAD_G_FIN, GNOMAD_G_NFE, GNOMAD_G_OTH, GNOMAD_G_SAS, GNOMAD_G_MID
    };

    @Override
    List<Allele> parseInfoField(List<Allele> alleles, String info) {
        if (alleles.size() > 1) {
            throw new IllegalStateException("Unexpected multi-allelic site: " + alleles);
        }
        Allele allele = alleles.get(0);

        String[] popAn = findAndSplitField("pop_AN", info);
        String[] popAc = findAndSplitField("pop_AC_hom", info);

        if (popAn.length != popAc.length) {
            logger.error("Length mismatch for pop_AN and pop_AC_hom fields for allele {}", allele);
        }
        if (popAn.length != POPULATIONS.length) {
            logger.error("Incorrect number of tokens expected {} but found {} for allele {}", POPULATIONS.length, popAn.length, allele);

        }
        for (int i = 0; i < POPULATIONS.length; i++) {
            if (!"0".equals(popAc[i])) {
                int ac = Integer.parseInt(popAc[i]);
                int an = Integer.parseInt(popAn[i]);
                allele.addFrequency(AlleleData.frequencyOf(POPULATIONS[i], ac, an));
            }
        }
        return alleles;
    }

    private String [] findAndSplitField(String infoKey, String line) {
        String value = findInfoFieldValue(infoKey, line);
        return value.split("\\|");
    }

    public String findInfoFieldValue(String infoKey, String line) {
        int start = line.indexOf(infoKey);
        if (start != -1) {
            // e.g. 'AC_eur=' add 1 for the '=' character
            int beginIndex = start + infoKey.length() + 1;
            return line.substring(beginIndex, line.indexOf(";", start));
        }
        return "";
    }

    public Map<String, String> mapInfoFields(Set<String> keys, String infoField) {
        if (infoField == null || infoField.equals(".")) {
            return Map.of();
        }
        Map<String, String> result = new HashMap<>();
        int start = 0;
        for (int end; (end = infoField.indexOf(';', start)) != -1;) {
            String key = infoField.substring(start, infoField.indexOf("=", start));
            if (keys.contains(key)) {
                result.put(key, infoField.substring(start + key.length() + 1, end));
            }
            start = end + 1;
        }
        return Map.copyOf(result);
    }

}
