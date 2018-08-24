/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.cli.config;

import org.apache.commons.cli.Options;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for the command line options.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@SpringJUnitConfig(classes = CommandLineOptionsConfig.class)
public class CommandLineOptionsConfigTest {
    
    @Autowired
    private Options options;

    @Test
    public void testHasHelpOption() {
        assertThat(options.hasOption("help"), is(true));
    }

    @Test
    public void testHasAnalysisOption() {
        assertThat(options.hasOption("analysis"), is(true));
    }

    @Test
    public void testHasAnalysisBatchOption() {
        assertThat(options.hasOption("analysis-batch"), is(true));
    }
}
