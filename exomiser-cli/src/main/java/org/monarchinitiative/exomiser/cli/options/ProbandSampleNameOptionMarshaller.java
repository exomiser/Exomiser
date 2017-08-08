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
public class ProbandSampleNameOptionMarshaller extends AbstractOptionMarshaller {

    public static final String PROBAND_SAMPLE_NAME_OPTION = "proband";

    public ProbandSampleNameOptionMarshaller() {
        option = Option.builder()
                .hasArg()
                .desc("Sample name of the proband. This should be present in both the ped and vcf files. Required if the vcf file is for a family.")
                .longOpt(PROBAND_SAMPLE_NAME_OPTION)
                .build();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, Settings.SettingsBuilder settingsBuilder) {
        settingsBuilder.probandSampleName(values[0]);
    }

}
