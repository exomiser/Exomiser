/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import static de.charite.compbio.exomiser.core.ExomiserSettings.SEED_GENES_OPTION;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.OptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SeedGenesOptionMarshaller extends AbstractOptionMarshaller {

    private static final Logger logger = LoggerFactory.getLogger(SeedGenesOptionMarshaller.class);

    public SeedGenesOptionMarshaller() {
        option = OptionBuilder
                .hasArgs()
                .withArgName("Entrez geneId")
                .withValueSeparator(',')
                .withDescription("Comma separated list of seed genes (Entrez gene IDs) for random walk")
                .withLongOpt(SEED_GENES_OPTION)
                .create("S");
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
