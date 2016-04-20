/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import de.charite.compbio.exomiser.cli.CommandLineParseError;
import de.charite.compbio.exomiser.core.analysis.Settings.SettingsBuilder;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.OptionBuilder;

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
        
        option = OptionBuilder
                .hasArg()
                .withArgName("name")
                .withValueSeparator()
                .withDescription(buildPrioritiserDescription(PRIORITISER_OPTION))
                .withLongOpt(PRIORITISER_OPTION)
                .create();
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
    private String buildPrioritiserDescription(String prioritiserLongOpt) {
        //Build up the description this should look like this:
        //"Name of the PRIORITISER_OPTION used to score the genes.
        // Can be one of: inheritance-mode, phenomizer or dynamic-phenodigm. 
        // e.g. --PRIORITISER_OPTION=dynamic-phenodigm"

        StringBuilder priorityOptionDescriptionBuilder = new StringBuilder("Name of the prioritiser used to score the genes. Can be one of: ");

        List<String> commandLineValues = new ArrayList<>(prioritiserCliValues.keySet());
        
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
