package org.monarchinitiative.exomiser.data.genome;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.BufferedLineReader;
import htsjdk.tribble.index.IndexFactory;
import htsjdk.tribble.index.tabix.TabixIndex;
import htsjdk.variant.vcf.VCFCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Re-codes the data in the INFO field into a compressed format for better compression and data access.
 */
public class GnomadInfoRecoder {

    private static final Logger logger = LoggerFactory.getLogger(GnomadInfoRecoder.class);

    private static final List<String> POPULATIONS = List.of("afr", "amr", "asj", "eas", "eas_kor", "eas_jpn", "eas_oea", "fin", "nfe", "nfe_bgr", "nfe_est", "nfe_onf", "nfe_nwe", "nfe_seu", "nfe_swe", "oth", "sas");
    private static final List<String> SUBSETS = List.of("all", "controls", "non_cancer", "non_neuro", "non_topmed");

    public GnomadInfoRecoder() {
    }

    public void recodeGnomadFile(Path infFile, Path outFile) {
//       try (BufferedLineReader bufferedLineReader = new BufferedLineReader(new BlockCompressedInputStream(Files.newInputStream(infFile)));
//            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new BlockCompressedOutputStream(outFile.toFile(), 6)))) {
//            long lineCount = 0;
//           for (String line; (line = bufferedLineReader.readLine()) != null;) {
//                lineCount++;
//               if (line.startsWith("#")) {
//                   bufferedWriter.write(line);
//                   bufferedWriter.newLine();
//                   bufferedWriter.flush();
//               } else {
//                   if (lineCount % 1_000_000 == 0) {
//                       logger.info("Processed {} lines. Current: {}", lineCount, line);
//                   }
//                   String[] fields = line.split("\t");
//                   String recodedInfo = recodeInfo(fields[7]);
//                   fields[7] = recodedInfo;
//                   bufferedWriter.write(String.join("\t", List.of(fields)));
//                   bufferedWriter.newLine();
//                   bufferedWriter.flush();
//               }
//           }
//       } catch (IOException e) {
//            logger.error("Unable to write whitelist to bgzip.", e);
//        }

        // use HTSJDK to create tabix index...
        Path whiteListIndexPath = outFile.getParent().resolve(outFile.getFileName() + ".tbi");
        try {
            TabixIndex index = IndexFactory.createTabixIndex(outFile.toFile(), new VCFCodec(), null);
            index.write(whiteListIndexPath);
        } catch (IOException e) {
            logger.error("Unable to write whitelist tabix index.", e);
        }
    }

    public static String recodeInfo(String info) {
        String[] tokens = info.split(";");

        Map<String, String> infoFields = new HashMap<>();
        for (String token : tokens) {
            String[] kv = token.split("=");
            if (kv.length == 2) {
                // segdup isn't a k=v token
                infoFields.put(kv[0], kv[1]);
            }
        }

        Map<String, Subset> occurringSubsetPopulationFreqs = makeOccurringSubsets(infoFields);

        for (var entry : infoFields.entrySet()) {
            String key = entry.getKey();
            // 'all'
            if (key.startsWith("AN_")) {
                occurringSubsetPopulationFreqs.get("all").handleData(key, entry.getValue());
            } else if (key.startsWith("AC_")) {
                occurringSubsetPopulationFreqs.get("all").handleData(key, entry.getValue());
            } else if (key.startsWith("AF_")) {
                occurringSubsetPopulationFreqs.get("all").handleData(key, entry.getValue());
            } else if (key.startsWith("nhomalt_")) {
                occurringSubsetPopulationFreqs.get("all").handleData(key, entry.getValue());
                // "controls", "non_cancer", "non_neuro", "non_topmed"
            } else if (key.startsWith("controls")) {
                occurringSubsetPopulationFreqs.get("controls").handleData(key, entry.getValue());
            } else if (key.startsWith("non_cancer")) {
                occurringSubsetPopulationFreqs.get("non_cancer").handleData(key, entry.getValue());
            } else if (key.startsWith("non_neuro")) {
                occurringSubsetPopulationFreqs.get("non_neuro").handleData(key, entry.getValue());
            } else if (key.startsWith("non_topmed")) {
                occurringSubsetPopulationFreqs.get("non_topmed").handleData(key, entry.getValue());
            }
        }

        StringBuilder recoded = new StringBuilder();
        for (String subsetName : SUBSETS) {
            if (occurringSubsetPopulationFreqs.containsKey(subsetName)) {
                var subset = occurringSubsetPopulationFreqs.get(subsetName);
                var popFreq = subset.printPopulationFreqs();
                recoded.append(popFreq);
//                System.out.println(popFreq);
            }
        }

        int startFreqs = info.indexOf("AC_nfe_seu=");
        int endFreqs = info.indexOf(";age_hist_het_bin_freq=");
        return info.substring(0, startFreqs) + recoded + info.substring(endFreqs);
    }

    private static Map<String, Integer> makePopulationIndex(List<String> populations) {
        Map<String, Integer> index = new HashMap<>();
        // not really a population, but its there as a total before genotype filtering was applied
        int pos = 0;
        index.put("raw", pos);
        index.put("male", pos++);
        index.put("female", pos++);
        for (String population : populations) {
            index.put(population, pos++);
            index.put(population + "_male", pos++);
            index.put(population + "_female", pos++);
        }
        return Map.copyOf(index);
    }

    private static Map<String, Subset> makeOccurringSubsets(Map<String, String> infoFields) {
        Map<String, Integer> populationFrequencyindex = makePopulationIndex(POPULATIONS);

        var occurring = new HashMap<String, Subset>();
        // adding 'raw' as the first element
        occurring.put("all", new Subset("all", new PopulationFrequency((POPULATIONS.size() * 3) + 3, populationFrequencyindex), new FilteringAlleleFrequency()));
        for (String subset : SUBSETS) {
            for (String key : infoFields.keySet()) {
                if (key.startsWith(subset)) {
//                    System.out.println("Adding subset " + subset);
                    occurring.put(subset, new Subset(subset, new PopulationFrequency((POPULATIONS.size() * 3) + 3, populationFrequencyindex), new FilteringAlleleFrequency()));
                    break;
                }
            }
        }

        return occurring;
    }

    private record Subset(String subset, PopulationFrequency populationFrequency,
                          FilteringAlleleFrequency filteringAlleleFrequency) {
        public void handleData(String key, String value) {
            //non_topmed_AC_eas_male=0;non_topmed_AN_eas_male=0;non_topmed_nhomalt_eas_male=0
            // 'all' subset
//            System.out.println("Handling " + key + "=" + value);
//            System.out.print("Adding "  + subset + " ");
            if (key.startsWith("AC_")) {
                populationFrequency.addData(key, value);
            } else if (key.startsWith("AN_")) {
                populationFrequency.addData(key, value);
            } else if (key.startsWith("AF_")) {
                populationFrequency.addData(key, value);
            } else if (key.startsWith("nhomalt_")) {
                populationFrequency.addData(key, value);
            } else if (key.startsWith(subset + "_AC_")) {
                populationFrequency.addData(key.substring(subset.length() + 1), value);
            } else if (key.startsWith(subset + "_AN_")) {
                populationFrequency.addData(key.substring(subset.length() + 1), value);
            } else if (key.startsWith(subset + "_AF_")) {
                populationFrequency.addData(key.substring(subset.length() + 1), value);
            } else if (key.startsWith(subset + "_nhomalt_")) {
                populationFrequency.addData(key.substring(subset.length() + 1), value);
            }
        }

        public String printPopulationFreqs() {
            String[] ac = replaceNullWithMissing(populationFrequency.ac);
            String[] an = replaceNullWithMissing(populationFrequency.an);
            String[] af = replaceNullWithMissing(populationFrequency.af);
            String[] homs = replaceNullWithMissing(populationFrequency.nhomalt);
            return subset + "_AC=" + String.join("|", List.of(ac)) + ";" +
                    subset + "_AN=" + String.join("|", List.of(an)) + ";" +
                    subset + "_AF=" + String.join("|", List.of(af)) + ";" +
                    subset + "_nhomalt=" + String.join("|", List.of(homs)) + ";";
        }

        private static String[] replaceNullWithMissing(String[] ac) {
            for (int i = 0; i < ac.length; i++) {
                if (ac[i] == null) {
                    ac[i] = ".";
                }
            }
            return ac;
        }
    }

    private static class PopulationFrequency {
        // PopulationFrequency
        // for 2.1, 3.1 has another 2 populations - ami and mid.
        // populations = [afr, amr, asj, eas, eas_kor, eas_jpn, fin, nfe, nfe_bgr, nfe_seu, nfe_onf, nfe_swe, nfe_nwe, nfe_est, oth, sas]
        // all '_pop' arrays are 1 + (len(populations) * 3) = 1 + (16 * 3) = 49
        // category_offsets = {pop_total = 1, male = 2, female = 3}
        // pop_category = (pop_pos * 3) + category_offset
        // Keys per subset: subsets=[all, controls, non_cancer, non_neuro, non_topmed]
        // subset_AN=raw|pop0_total|pop0_male|pop0_female|pop1_total|pop1_male|pop1_female...]
        // subset_AC_pop=raw|pop0_total|pop0_male|pop0_female|pop1_total|pop1_male|pop1_female...]
        // subset_AF_pop=raw|pop0_total|pop0_male|pop0_female|pop1_total|pop1_male|pop1_female...]
        // subset_nhomalt_pop=raw|pop0_total|pop0_male|pop0_female|pop1_total|pop1_male|pop1_female...]
        //
        // PopMax
        // subset_popmax=pop|AC|AN|AF|nhomalt
        // e.g.
        // popmax=afr;AC_popmax=13;AN_popmax=7836;AF_popmax=1.65901e-03;nhomalt_popmax=0 (77 chars)
        // ->
        // all_popmax=afr|13|7836|1.65901e-03|0 (32 chars)
        private Map<String, Integer> populationFrequencyindex;
        private final String[] ac;
        private final String[] an;
        private final String[] af;
        private final String[] nhomalt;

        public PopulationFrequency(int populationSize, Map<String, Integer> populationFrequencyindex) {
            this.populationFrequencyindex = populationFrequencyindex;
            this.ac = new String[populationSize];
            this.an = new String[populationSize];
            this.af = new String[populationSize];
            this.nhomalt = new String[populationSize];
        }

        // expects data in the form of AN_eas, AN_eas_jpn,
        public void addData(String key, String value) {
            if (key.endsWith("popmax")) {
//                System.out.println("POPMAX! " + key);
                // TODO: AN_popmax,
                return;
            }
            int firstUnderscore = key.indexOf("_");
            String set = key.substring(0, firstUnderscore);
            int position = populationFrequencyindex.get(key.substring(firstUnderscore + 1));
            switch (set) {
                case "AC" -> ac[position] = value;
                case "AN" -> an[position] = value;
                case "AF" -> af[position] = value;
                case "nhomalt" -> nhomalt[position] = value;
            }
//            System.out.println(key + " " + value + " to " + set + " position " + position);
        }
    }

    // FilteringAlleleFrequency
    // subset_faf95=[all, afr, amr, eas, nfe]
    // subset_faf99=[all, afr, amr, eas, nfe]
    private static class FilteringAlleleFrequency {

        public FilteringAlleleFrequency() {
        }

    }


    // subsets: [all, controls, non_cancer, non_neuro, non_topmed]

    // all_AN_pop=raw|pop_total0|pop0_male|pop0_female|pop_total1|pop1_male|pop1_female
    // all_AC_pop=
    // all_AF_pop=
    // all_nhomalt_pop=
    // all_faf95=
    // all_faf99=
    // all_popmax=afr|13|7836|1.65901e-03|0

    // non_neuro_AN_pop=
    // non_neuro_AC_pop=
    // non_neuro_AF_pop=
    // non_neuro_nhomalt_pop=
    // non_neuro_faf95=
    // non_neuro_faf99=
    // non_neuro_popmax=sas;non_neuro_AC_popmax=32;non_neuro_AN_popmax=194;non_neuro_AF_popmax=1.64948e-01;non_neuro_nhomalt_popmax=0
    // non_neuro_popmax=sas|32|194|1.64948e-01|0

}
