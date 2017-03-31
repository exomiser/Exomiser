/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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

package org.monarchinitiative.exomiser.core.genome.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.monarchinitiative.exomiser.core.model.TopologicalDomain;
import org.monarchinitiative.exomiser.core.prioritisers.config.TestDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestDataSourceConfig.class, TadDao.class})
@Sql(scripts = {"file:src/test/resources/sql/create_tad.sql", "file:src/test/resources/sql/tadDaoTestData.sql"})
public class TadDaoTest {

    @Autowired
    private TadDao instance;

    @Test
    public void testGetAllTads() {
        //some real TADS (cut short)
        Map<String, Integer> genes1 = new LinkedHashMap<>();
        genes1.put("ISG15", 9636);
        genes1.put("TNFRSF4", 7293);
        TopologicalDomain tad1 = new TopologicalDomain(1, 770137, 1250137, genes1);

        Map<String, Integer> genes2 = new LinkedHashMap<>();
        genes2.put("MMP23B", 8510);
        TopologicalDomain tad2 = new TopologicalDomain(1, 1250137, 1850140, genes2);

        Map<String, Integer> genes3 = new LinkedHashMap<>();
        genes3.put("LBH", 81606);
        genes3.put("LCLAT1", 253558);
        genes3.put("YPEL5", 51646);
        TopologicalDomain tad3 = new TopologicalDomain(2, 30346496, 30906496, genes3);

        //fictitious overlapping domains - these might occur.
        Map<String, Integer> genes4 = new LinkedHashMap<>();
        genes4.put("GENE1", 11111);
        TopologicalDomain tad4 = new TopologicalDomain(2, 30346496, 30800000, genes4);

        Map<String, Integer> genes5 = new LinkedHashMap<>();
        genes5.put("GENE2", 22222);
        TopologicalDomain tad5 = new TopologicalDomain(2, 30200000, 30800000, genes5);

        Map<String, Integer> genes6 = new LinkedHashMap<>();
        genes6.put("GENE3", 33333);
        genes6.put("GENE4", 44444);
        TopologicalDomain tad6 = new TopologicalDomain(2, 30100000, 30600000, genes6);

        List<TopologicalDomain> expected = new ArrayList<>();
        expected.add(tad1);
        expected.add(tad2);
        expected.add(tad3);
        expected.add(tad4);
        expected.add(tad5);
        expected.add(tad6);

        assertThat(instance.getAllTads(), equalTo(expected));
    }

}