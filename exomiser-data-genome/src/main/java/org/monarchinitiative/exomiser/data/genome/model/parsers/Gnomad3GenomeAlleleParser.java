package org.monarchinitiative.exomiser.data.genome.model.parsers;

import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Gnomad3GenomeAlleleParser extends VcfAlleleParser {

    private static final Logger logger = LoggerFactory.getLogger(Gnomad3GenomeAlleleParser.class);

    private final List<GnomadPopulationKey> populationKeys;
    private final Set<String> requiredKeys;

    public Gnomad3GenomeAlleleParser() {
        this(GnomadPopulationKey.GNOMAD_V3_1_GENOMES, Set.of(".", "PASS", "RF", "InbreedingCoeff", "LCR", "SEGDUP"));
    }

    public Gnomad3GenomeAlleleParser(List<GnomadPopulationKey> populationKeys, Set<String> allowedFilterValues) {
        this.populationKeys = populationKeys;
        this.requiredKeys = populationKeys.stream()
                .flatMap(popKey -> Stream.of(popKey.acPop(), popKey.anPop(), popKey.homPop()))
                .collect(Collectors.toSet());
        //  ##INFO=<ID=splice_ai_max_ds,Number=1,Type=Float,Description="Illumina's SpliceAI max delta score; interpreted as the probability of the variant being splice-altering.">
        requiredKeys.add("splice_ai_max_ds");
        this.allowedFilterValues = allowedFilterValues;
    }

    public List<GnomadPopulationKey> getPopulationKeys() {
        return populationKeys;
    }

    @Override
    List<Allele> parseInfoField(List<Allele> alleles, String info) {
        Map<String, String> alleleCounts = mapInfoFields(info);
        if (alleleCounts.isEmpty()) {
            throw new IllegalStateException("Is this a gnomAD file? Unable to find any expected keys " + requiredKeys);
        }
        for (GnomadPopulationKey population : populationKeys) {
            if (!alleleCounts.containsKey(population.anPop()) && alleleCounts.containsKey(population.anPop().toUpperCase())) {
                throw new IllegalArgumentException("Incorrect gnomAD format - looks like you're trying to parse a v2.0 format file. Expected keys of format '" + population.anPop() + "' but got '" + population.anPop().toUpperCase() + "'. Try providing a gnomAD v2.1 - v3.1 file.");
            }
        }

        for (int i = 0; i < alleles.size(); i++) {
            Allele allele = alleles.get(i);
            //AC = AlleleCount, AN = AlleleNumber, freq as percentage = (AC/AN) * 100
            var frequencies = parseAllelePopulationFrequencies(alleleCounts, i);
            allele.addAllFrequencies(frequencies);
            // gnomAD_3.1 special difference
            //  ##INFO=<ID=splice_ai_max_ds,Number=1,Type=Float,Description="Illumina's SpliceAI max delta score; interpreted as the probability of the variant being splice-altering.">
            var spliceAi = parseSpliceAiScore(alleleCounts, i);
            if (!spliceAi.equals(AlleleProto.PathogenicityScore.getDefaultInstance())) {
                allele.addPathogenicityScore(spliceAi);
            }
        }
        return alleles;
    }

    //this is the slowest part of this.
    private Map<String, String> mapInfoFields(String infoField) {
        Map<String, String> result = new HashMap<>(requiredKeys.size());
        int keysFound = 0;
        int start = 0;
        for (int end; (end = infoField.indexOf(';', start)) != -1;) {
            String key = infoField.substring(start, infoField.indexOf("=", start));
            if (requiredKeys.contains(key)) {
                result.put(key, infoField.substring(start + key.length() + 1, end));
            }
            start = end + 1;
            // stop looking if we have everything as string splitting is super-slow.
            if (keysFound == requiredKeys.size()) {
                return result;
            }
        }
        return result;
    }

    private List<AlleleProto.Frequency> parseAllelePopulationFrequencies(Map<String, String> alleleCounts, int i) {
        List<AlleleProto.Frequency> frequencies = new ArrayList<>();
        for (GnomadPopulationKey population : populationKeys) {
            int alleleCount = parseAlleleCount(alleleCounts.get(population.acPop()), i);
            if (alleleCount != 0) {
                int alleleNumber = Integer.parseInt(alleleCounts.get(population.anPop()));
                int homozygotes = parseAlleleCount(alleleCounts.get(population.homPop()), i);
                var frequency = AlleleData.frequencyOf(population.frequencySource(), alleleCount, alleleNumber, homozygotes);
                frequencies.add(frequency);
            }
        }
        return frequencies;
    }

    private int parseAlleleCount(String alleleCountValue, int altAllelePos) {
        if (alleleCountValue == null) {
            return 0;
        }
        String alleleCount = alleleCountValue.split(",")[altAllelePos];
        return Integer.parseInt(alleleCount);
    }

    // ##INFO=<ID=splice_ai_max_ds,Number=1,Type=Float,
    private AlleleProto.PathogenicityScore parseSpliceAiScore(Map<String, String> alleleCounts, int altAllelePos) {
        var alleleCountValue = alleleCounts.get("splice_ai_max_ds");
        if (alleleCountValue == null) {
            return AlleleProto.PathogenicityScore.getDefaultInstance();
        }
        String alleleCount = alleleCountValue.split(",")[altAllelePos];
        float score = Float.parseFloat(alleleCount);
        if (score == 0) {
            return AlleleProto.PathogenicityScore.getDefaultInstance();
        }
        return AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.SPLICE_AI, score);
    }
}
