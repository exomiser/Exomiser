/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import static de.charite.compbio.exomiser.core.ExomiserSettings.GENES_TO_KEEP_OPTION;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.OptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GenesToKeepFilterOptionMarshaller extends AbstractOptionMarshaller {

    private static final Logger logger = LoggerFactory.getLogger(SeedGenesOptionMarshaller.class);
    
    public GenesToKeepFilterOptionMarshaller() {
        option = OptionBuilder
                .hasArgs()
                .withArgName("Entrez geneId")
                .withValueSeparator(',')
                .withDescription("Comma separated list of seed genes (Entrez gene IDs) for filtering")
                .withLongOpt(GENES_TO_KEEP_OPTION)
                .create();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, ExomiserSettings.SettingsBuilder settingsBuilder) {
        settingsBuilder.genesToKeepList(parseGenesToKeepList(values));
    }
    
    private Set<Integer> parseGenesToKeepList(String[] values) {

        Set<Integer> returnList = new LinkedHashSet<>();

        if (values.length == 0) {
            return returnList;
        }

        Pattern entrezGeneIdPattern = Pattern.compile("[0-9]+");

        for (String string : values) {
            Matcher entrezGeneIdPatternMatcher = entrezGeneIdPattern.matcher(string);
            if (entrezGeneIdPatternMatcher.matches()) {
                Integer integer = Integer.parseInt(string.trim());
                returnList.add(integer);
            } else {
                logger.error("Malformed Entrez gene ID input string \"{}\". Term \"{}\" does not match the Entrez gene ID identifier pattern: {}", values, string, entrezGeneIdPattern);
            }
        }

        return returnList;
    }

}
