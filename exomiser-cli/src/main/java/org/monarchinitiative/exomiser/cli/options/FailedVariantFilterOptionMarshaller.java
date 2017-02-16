package org.monarchinitiative.exomiser.cli.options;

import org.apache.commons.cli.Option;
import org.monarchinitiative.exomiser.core.analysis.Settings;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class FailedVariantFilterOptionMarshaller extends AbstractOptionMarshaller {

    public static final String FAILED_VARIANT_FILTER_OPTION = "remove-failed";

    public FailedVariantFilterOptionMarshaller() {
        option = Option.builder()
                .optionalArg(false)
                .desc("Calling this option will tell Exomiser to ignore any variants marked in the input VCF as having failed any previous filters from other upstream analyses. In other words, unless a variant has a 'PASS' or '.' in the FILTER field of the input VCF, it will be excluded from the analysis by the Exomiser.")
                .longOpt(FAILED_VARIANT_FILTER_OPTION)
                .build();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, Settings.SettingsBuilder settingsBuilder) {
        settingsBuilder.removeFailed(true);
    }
}
