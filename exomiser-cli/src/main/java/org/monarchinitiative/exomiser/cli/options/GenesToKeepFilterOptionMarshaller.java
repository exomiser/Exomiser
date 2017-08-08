/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class GenesToKeepFilterOptionMarshaller extends AbstractOptionMarshaller {

    private static final Logger logger = LoggerFactory.getLogger(SeedGenesOptionMarshaller.class);
    
    public static final String GENES_TO_KEEP_OPTION = "genes-to-keep";

    public GenesToKeepFilterOptionMarshaller() {
        option = Option.builder()
                .hasArgs()
                .argName("Entrez geneId")
                .valueSeparator(',')
                .desc("Comma separated list of seed genes (Entrez gene IDs) for filtering")
                .longOpt(GENES_TO_KEEP_OPTION)
                .build();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        settingsBuilder.genesToKeep(parseGenesToKeepList(values));
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
