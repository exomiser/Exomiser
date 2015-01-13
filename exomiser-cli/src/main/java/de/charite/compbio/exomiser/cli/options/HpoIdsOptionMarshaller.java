/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import static de.charite.compbio.exomiser.core.ExomiserSettings.HPO_IDS_OPTION;
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
public class HpoIdsOptionMarshaller extends AbstractOptionMarshaller {

    private static final Logger logger = LoggerFactory.getLogger(HpoIdsOptionMarshaller.class);

    public HpoIdsOptionMarshaller() {
        option = OptionBuilder
                .hasArgs()
                .withArgName("HPO ID")
                .withValueSeparator(',')
                .withDescription("Comma separated list of HPO IDs for the sample being sequenced e.g. HP:0000407,HP:0009830,HP:0002858")
                .withLongOpt(HPO_IDS_OPTION)
                .create();
    }
    
    @Override
    public void applyValuesToSettingsBuilder(String[] values, ExomiserSettings.SettingsBuilder settingsBuilder) {
        settingsBuilder.hpoIdList(parseHpoStringList(values));
    }

    private List<String> parseHpoStringList(String[] values) {
        logger.debug("Parsing HPO values from: {}", values);

        List<String> hpoList = new ArrayList<>();

        if (values.length == 0) {
            return hpoList;
        }

        Pattern hpoPattern = Pattern.compile("HP:[0-9]{7}");
        //I've gone for a more verbose splitting and individual token parsing 
        //instead of doing while hpoMatcher.matches(); hpoList.add(hpoMatcher.group()) 
        //on the whole input string so that the user has a warning about any invalid HPO ids
        for (String token : values) {
            token = token.trim();
            if (token.isEmpty()) {
                continue;
            }
            Matcher hpoMatcher = hpoPattern.matcher(token);
            if (hpoMatcher.matches()) { /* A well formed HPO term starts with "HP:" and has ten characters. */

                //ideally we need an HPO class as the second half of the ID is an integer.
                //TODO: add Hpo class to exomiser.core - Phenodigm.core already has one.

                hpoList.add(token);
            } else {
                logger.error("Malformed HPO input string \"{}\". Term \"{}\" does not match the HPO identifier pattern: {}", values, token, hpoPattern);
            }
        }

        return hpoList;
    }

}
