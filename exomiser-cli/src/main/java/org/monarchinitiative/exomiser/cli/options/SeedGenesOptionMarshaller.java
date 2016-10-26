/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.cli.options;

import org.apache.commons.cli.Option;
import org.monarchinitiative.exomiser.core.analysis.Settings.SettingsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SeedGenesOptionMarshaller extends AbstractOptionMarshaller {

    private static final Logger logger = LoggerFactory.getLogger(SeedGenesOptionMarshaller.class);

    public static final String SEED_GENES_OPTION = "seed-genes";

    public SeedGenesOptionMarshaller() {
        option = Option.builder("S")
                .hasArgs()
                .argName("Entrez geneId")
                .valueSeparator(',')
                .desc("Comma separated list of seed genes (Entrez gene IDs) for random walk")
                .longOpt(SEED_GENES_OPTION)
                .build();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        settingsBuilder.seedGeneList(parseEntrezSeedGeneList(values));
    }

    private List<Integer> parseEntrezSeedGeneList(String[] values) {

        List<Integer> returnList = new ArrayList<>();

        if (values.length == 0) {
            return returnList;
        }

        Pattern entrezGeneIdPattern = Pattern.compile("[0-9]+");

        for (String string : values) {
            if (string.isEmpty()) {
                continue;
            }
            String trimmedString = string.trim();
            Matcher entrezGeneIdPatternMatcher = entrezGeneIdPattern.matcher(trimmedString);
            if (entrezGeneIdPatternMatcher.matches()) {
                Integer integer = Integer.parseInt(trimmedString);
                returnList.add(integer);
            } else {
                logger.error("Malformed Entrez gene ID input string \"{}\". Term \"{}\" does not match the Entrez gene ID identifier pattern: {}", values, string, entrezGeneIdPattern);
            }
        }

        return returnList;
    }

}
