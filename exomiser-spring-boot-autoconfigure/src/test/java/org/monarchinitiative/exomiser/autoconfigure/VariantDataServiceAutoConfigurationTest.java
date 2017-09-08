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

package org.monarchinitiative.exomiser.autoconfigure;

import org.junit.Test;
import org.monarchinitiative.exomiser.core.genome.dao.ErrorThrowingTabixDataSource;
import org.monarchinitiative.exomiser.core.genome.dao.TabixDataSource;
import org.springframework.beans.factory.BeanCreationException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class VariantDataServiceAutoConfigurationTest extends AbstractAutoConfigurationTest {


    @Test(expected = BeanCreationException.class)
    public void loadTabixFileThrowsBeanCreationExceptionWhenFileNotFound() {
        String testTabixFilePath = TEST_DATA.resolve("wibble.tsv.gz").toAbsolutePath().toString();
        load(VariantDataServiceAutoConfiguration.class, TEST_DATA_ENV, "exomiser.caddSnvPath=" + testTabixFilePath);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("caddSnvTabixDataSource");
    }

    @Test
    public void loadCaddSnvTabixFromPlaceholderWhenNotDefined() {
        load(VariantDataServiceAutoConfiguration.class, TEST_DATA_ENV);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("caddSnvTabixDataSource");
        assertThat(tabixDataSource, instanceOf(ErrorThrowingTabixDataSource.class));
    }

    @Test
    public void loadCaddSnvTabixFileFromFullPathWhenDefined() {
        String testTabixFilePath = TEST_DATA.resolve("whole_genome_SNVs.tsv.gz").toAbsolutePath().toString();
        load(VariantDataServiceAutoConfiguration.class, TEST_DATA_ENV, "exomiser.caddSnvPath=" + testTabixFilePath);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("caddSnvTabixDataSource");
        assertThat(tabixDataSource.getSource(), equalTo(testTabixFilePath));
    }

    @Test
    public void loadCaddIndelTabixFromPlaceholderWhenNotDefined() {
        load(VariantDataServiceAutoConfiguration.class, TEST_DATA_ENV);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("caddInDelTabixDataSource");
        assertThat(tabixDataSource, instanceOf(ErrorThrowingTabixDataSource.class));
    }

    @Test
    public void loadCaddIndelTabixFileFromFullPathWhenDefined() {
        String testTabixFilePath = TEST_DATA.resolve("InDels.tsv.gz").toAbsolutePath().toString();
        load(VariantDataServiceAutoConfiguration.class, TEST_DATA_ENV, "exomiser.caddInDelPath=" + testTabixFilePath);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("caddInDelTabixDataSource");
        assertThat(tabixDataSource.getSource(), equalTo(testTabixFilePath));
    }

    @Test
    public void loadRemmTabixFromPlaceholderWhenNotDefined() {
        load(VariantDataServiceAutoConfiguration.class, TEST_DATA_ENV);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("remmTabixDataSource");
        assertThat(tabixDataSource, instanceOf(ErrorThrowingTabixDataSource.class));
    }

    @Test
    public void loadRemmTabixFileFromFullPathWhenDefined() {
        String testTabixFilePath = TEST_DATA.resolve("remmData.tsv.gz").toAbsolutePath().toString();
        load(VariantDataServiceAutoConfiguration.class, TEST_DATA_ENV, "exomiser.remmPath=" + testTabixFilePath);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("remmTabixDataSource");
        assertThat(tabixDataSource.getSource(), equalTo(testTabixFilePath));
    }

    @Test
    public void loadLocalFrequencyTabixFromPlaceholderWhenNotDefined() {
        load(VariantDataServiceAutoConfiguration.class, TEST_DATA_ENV);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("localFrequencyTabixDataSource");
        assertThat(tabixDataSource, instanceOf(ErrorThrowingTabixDataSource.class));
    }

    @Test
    public void loadLocalFrequencyTabixFileFromFullPathWhenDefined() {
        String testTabixFilePath = TEST_DATA.resolve("local_freq.tsv.gz").toAbsolutePath().toString();
        load(VariantDataServiceAutoConfiguration.class, TEST_DATA_ENV, "exomiser.local-frequency-path=" + testTabixFilePath);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("localFrequencyTabixDataSource");
        assertThat(tabixDataSource.getSource(), equalTo(testTabixFilePath));
    }
}