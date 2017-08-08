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
