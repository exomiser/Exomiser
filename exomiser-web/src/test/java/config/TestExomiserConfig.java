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
package config;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.exomiser.autoconfigure.EnableExomiser;
import de.charite.compbio.exomiser.core.analysis.AnalysisFactory;
import de.charite.compbio.exomiser.core.analysis.SettingsParser;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;


/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@EnableExomiser
public class TestExomiserConfig {
    
    Logger logger = LoggerFactory.getLogger(TestExomiserConfig.class);
    
    @Bean
    public SettingsParser mockSettingsParser() {
        return Mockito.mock(SettingsParser.class);
    }
    
    @Bean
    public AnalysisFactory mockAnalysisFactory() {
        return Mockito.mock(AnalysisFactory.class);
    }

    @Bean
    public JannovarData jannovarData() {
        return new JannovarData(HG19RefDictBuilder.build(), ImmutableList.<TranscriptModel>of());
    }

}
