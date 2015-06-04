/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.options;

import static de.charite.compbio.exomiser.core.ExomiserSettings.PRIORITISER_OPTION;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.OptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PrioritiserOptionMarshaller extends AbstractOptionMarshaller {

    private static final Logger logger = LoggerFactory.getLogger(PrioritiserOptionMarshaller.class);

    public PrioritiserOptionMarshaller() {
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
        String value = values[0];
        settingsBuilder.usePrioritiser(PriorityType.valueOfCommandLine(value));
        if (PriorityType.valueOfCommandLine(value) == PriorityType.NOT_SET) {
            logger.error("Invalid prioritiser option: {} ", value);
            logger.error("Please choose one of:");
            for (PriorityType priorityType : PriorityType.values()) {
                logger.error("\t{}", priorityType.getCommandLineValue());
            }
        }
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
        List<PriorityType> inValidPriorityTypes = new ArrayList<>();
        inValidPriorityTypes.add(PriorityType.NOT_SET);
        inValidPriorityTypes.add(PriorityType.OMIM_PRIORITY);

        List<PriorityType> validPriorityTypes = new ArrayList<>();
        //The last PriorityType is PriorityType.NOT_SET which has no command-line option so we ned to create a list of PriorityTypes without this one in.
        for (PriorityType priorityType : PriorityType.values()) {
            if (inValidPriorityTypes.contains(priorityType)) {
                //we're not interested in this option
            } else if (priorityType.getCommandLineValue().isEmpty()) {
                //we're not interested in this option either
            } else {
                //This is the option we're looking for!
                validPriorityTypes.add(priorityType);
            }
        }
        //now we've got the valid list of types, build up the description
        //this should look like this:
        //"Name of the PRIORITISER_OPTION used to score the genes.
        // Can be one of: inheritance-mode, phenomizer or dynamic-phenodigm. 
        // e.g. --PRIORITISER_OPTION=dynamic-phenodigm"

        StringBuilder priorityOptionDescriptionBuilder = new StringBuilder("Name of the prioritiser used to score the genes. Can be one of: ");

        int numPriorityTypes = validPriorityTypes.size();
        int lastType = numPriorityTypes - 1;
        int secondLastType = numPriorityTypes - 2;
        for (int i = 0; i < numPriorityTypes; i++) {
            PriorityType priorityType = validPriorityTypes.get(i);
            if (i == lastType) {
                priorityOptionDescriptionBuilder.append(priorityType.getCommandLineValue())
                        .append(". e.g. --").append(prioritiserLongOpt)
                        .append("=")
                        .append(priorityType.getCommandLineValue());
            } else if (i == secondLastType) {
                priorityOptionDescriptionBuilder.append(priorityType.getCommandLineValue()).append(" or ");
            } else {
                priorityOptionDescriptionBuilder.append(priorityType.getCommandLineValue()).append(", ");
            }
        }

        return priorityOptionDescriptionBuilder.toString();
    }

}
