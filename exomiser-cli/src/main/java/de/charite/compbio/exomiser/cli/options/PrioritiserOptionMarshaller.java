/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.cli.CommandLineParseError;
import de.charite.compbio.exomiser.core.analysis.Settings.SettingsBuilder;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import org.apache.commons.cli.Option;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PrioritiserOptionMarshaller extends AbstractOptionMarshaller {

    public static final String PRIORITISER_OPTION = "prioritiser"; //values for this are handled by PriorityType

    private final Map<String, PriorityType> prioritiserCliValues = new LinkedHashMap<>();

    public PrioritiserOptionMarshaller() {

        prioritiserCliValues.put("hiphive", PriorityType.HIPHIVE_PRIORITY);
        prioritiserCliValues.put("phenix", PriorityType.PHENIX_PRIORITY);
        prioritiserCliValues.put("phive", PriorityType.PHIVE_PRIORITY);
        prioritiserCliValues.put("exomewalker", PriorityType.EXOMEWALKER_PRIORITY);
        prioritiserCliValues.put("omim", PriorityType.OMIM_PRIORITY);
        prioritiserCliValues.put("uber-pheno", PriorityType.UBERPHENO_PRIORITY);
        //'none' is the default
        prioritiserCliValues.put("none", PriorityType.NONE);

        List<String> commandLineValues = new ArrayList<>(prioritiserCliValues.keySet());

        option = Option.builder()
                .hasArg()
                .argName("name")
                .valueSeparator()
                .desc(buildPrioritiserDescription(PRIORITISER_OPTION, commandLineValues))
                .longOpt(PRIORITISER_OPTION)
                .build();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, SettingsBuilder settingsBuilder) {
        String value = values[0].toLowerCase();
        PriorityType priorityType = prioritiserCliValues.get(value);
        if (priorityType == null) {
            throw new CommandLineParseError(buildErrorMessage(value));
        }
        settingsBuilder.usePrioritiser(priorityType);
    }

    /**
     * There is a lot of messing about needed to get the Prioritiser option
     * description sorted, but this will now automatically change to reflect
     * changes in any names or types which are added to the
     * {@link de.charite.compbio.exomiser.core.prioritisers.PriorityType}
     *
     * @param prioritiserLongOpt
     * @return
     */
    private String buildPrioritiserDescription(String prioritiserLongOpt, List<String> commandLineValues) {
        //Build up the description this should look like this:
        //"Name of the PRIORITISER_OPTION used to score the genes.
        // Can be one of: inheritance-mode, phenomizer or dynamic-phenodigm. 
        // e.g. --PRIORITISER_OPTION=dynamic-phenodigm"

        StringBuilder priorityOptionDescriptionBuilder = new StringBuilder("Name of the prioritiser used to score the genes. Can be one of: ");

        int numPriorityTypes = commandLineValues.size();
        int lastType = numPriorityTypes - 1;
        int secondLastType = numPriorityTypes - 2;
        
        for (int i = 0; i < numPriorityTypes; i++) {
            String cliValue = commandLineValues.get(i);
            if (i == lastType) {
                priorityOptionDescriptionBuilder.append(cliValue)
                        .append(". e.g. --").append(prioritiserLongOpt)
                        .append("=")
                        .append(cliValue);
            } else if (i == secondLastType) {
                priorityOptionDescriptionBuilder.append(cliValue).append(" or ");
            } else {
                priorityOptionDescriptionBuilder.append(cliValue).append(", ");
            }
        }

        return priorityOptionDescriptionBuilder.toString();
    }
    
    private String buildErrorMessage(String value) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(String.format("Invalid %s option: '%s'. ", PRIORITISER_OPTION, value));
        errorMessage.append("Valid options are: ");
        errorMessage.append(prioritiserCliValues.keySet());
        return errorMessage.toString();
    }
    
}
