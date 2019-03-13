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

package org.monarchinitiative.exomiser.test;

import de.charite.compbio.jannovar.data.JannovarData;
import htsjdk.tribble.readers.TabixReader;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.phenotype.dao.HumanPhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.dao.MousePhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.dao.ZebraFishPhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@SpringJUnitConfig(ExomiserStubDataConfig.class)
public class ExomiserStubDataConfigTest {

    @Autowired
    private HumanPhenotypeOntologyDao hpoDao;

    @Autowired
    private MousePhenotypeOntologyDao mpoDao;

    @Autowired
    private ZebraFishPhenotypeOntologyDao zpoDao;

    @Autowired
    private JannovarData jannovarData;

    @Autowired
    private TabixReader inDelTabixReader;

    @Autowired
    private TabixReader snvTabixReader;

    @Autowired
    private TabixReader remmTabixReader;

    @Autowired
    private TabixReader localFrequencyTabixReader;

    @Autowired
    private DataMatrix dataMatrix;

    @Autowired
    private Path phenixDataDirectory;

    @Test
    void testHpoDao() {
        assertThat(hpoDao, instanceOf(HumanPhenotypeOntologyDao.class));
        assertThat(hpoDao.getAllTerms(), equalTo(Collections.emptySet()));
        assertThat(hpoDao.getPhenotypeMatchesForHpoTerm(PhenotypeTerm.of("HP:0000000", "Wibble")), equalTo(Collections.emptySet()));
        assertThat(hpoDao.getIdToPhenotypeTerms(), equalTo(Collections.emptyMap()));
    }

    @Test
    void testMpoDao() {
        assertThat(mpoDao, instanceOf(MousePhenotypeOntologyDao.class));
        assertThat(mpoDao.getAllTerms(), equalTo(Collections.emptySet()));
        assertThat(mpoDao.getPhenotypeMatchesForHpoTerm(PhenotypeTerm.of("MP:0000000", "Wibble")), equalTo(Collections.emptySet()));

    }

    @Test
    void testzpoDao() {
        assertThat(zpoDao, instanceOf(ZebraFishPhenotypeOntologyDao.class));
        assertThat(zpoDao.getAllTerms(), equalTo(Collections.emptySet()));
        assertThat(zpoDao.getPhenotypeMatchesForHpoTerm(PhenotypeTerm.of("ZP:0000000", "Wibble")), equalTo(Collections.emptySet()));

    }

    @Test
    public void testJannovarData() {
        assertThat(jannovarData.getChromosomes().size(), equalTo(25));
    }

    @Test
    public void testInDelTabixReader() {
        assertThat(inDelTabixReader, instanceOf(TabixReader.class));
    }

    @Test
    public void testSnvTabixReader() {
        assertThat(snvTabixReader, instanceOf(TabixReader.class));
    }

    @Test
    public void testRemmTabixReader() {
        assertThat(remmTabixReader, instanceOf(TabixReader.class));
    }

    @Test
    public void testLocalFrequencyTabixReader() {
        assertThat(localFrequencyTabixReader, instanceOf(TabixReader.class));
    }

    @Test
    public void testDataMatrix() {
        assertThat(dataMatrix, instanceOf(DataMatrix.class));
    }

    @Test
    public void phenixDataDirectory() {
        assertThat(phenixDataDirectory, equalTo(Paths.get("phenix")));
    }
}