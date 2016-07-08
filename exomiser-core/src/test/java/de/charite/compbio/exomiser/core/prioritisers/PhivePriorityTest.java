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

package de.charite.compbio.exomiser.core.prioritisers;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.prioritisers.util.TestPriorityServiceFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhivePriorityTest {

    private Logger logger = LoggerFactory.getLogger(PhivePriorityTest.class);

    //going to run this off the live database for the time being.
    @Bean
    public DataSource dataSource() {
        return new HikariDataSource(h2Config());
    }

    @Bean
    public HikariConfig h2Config() {

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:file:C:/Users/jj8/Documents/exomiser-cli-4.0.0/data/exomiser;MODE=PostgreSQL;SCHEMA=EXOMISER;DATABASE_TO_UPPER=FALSE;IFEXISTS=TRUE;AUTO_RECONNECT=TRUE;ACCESS_MODE_DATA=r;");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(3);
        config.setPoolName("exomiser-H2");

        logger.info("DataSource using maximum of {} database connections", config.getMaximumPoolSize());
        logger.info("Returning a new {} DataSource pool to URL {} user: {}", config.getPoolName(), config.getJdbcUrl(), config.getUsername());

        return config;
    }

    private List<Gene> getGenes() {
        return Lists.newArrayList(
                new Gene("FGFR2", 2263),
                new Gene("ROR2", 4920),
                new Gene("FREM2", 341640),
                new Gene("ZNF738", 148203)
        );
    }

    //TODO: this should be the output of a Prioritiser: Genes + HPO -> PrioritiserResults
    private List<PhivePriorityResult> getPriorityResultsOrderedByScore(List<Gene> genes) {
        return genes.stream()
                .flatMap(gene -> gene.getPriorityResults().values()
                        .stream())
                .map(priorityResult -> (PhivePriorityResult) priorityResult)
                .sorted(Comparator.comparingDouble(PriorityResult::getScore).reversed())
                .collect(Collectors.toList());
    }

    @Test
    public void testGetPriorityType() {
        PhivePriority instance = new PhivePriority(Collections.emptyList(), TestPriorityServiceFactory.STUB_SERVICE);
        assertThat(instance.getPriorityType(), equalTo(PriorityType.PHIVE_PRIORITY));
    }

    @Test
    public void testPrioritizeGenes() {
        List<Gene> genes = getGenes();

        List<String> hpoIds= Lists.newArrayList("HP:0010055", "HP:0001363", "HP:0001156", "HP:0011304");
        PhivePriority phivePriority = new PhivePriority(hpoIds, TestPriorityServiceFactory.TEST_SERVICE);
        phivePriority.setDataSource(dataSource());
        phivePriority.prioritizeGenes(genes);
//        List<PriorityResult> results = instance.prioritizeGenes(genes);

        List<PhivePriorityResult> results = getPriorityResultsOrderedByScore(genes);
        assertThat(results.size(), equalTo(genes.size()));
        results.forEach(result -> {
            System.out.println(result);
        });

        List<PhivePriorityResult> expected = Lists.newArrayList(
                new PhivePriorityResult(2263, "FGFR2", 0.8278620846776202, "MGI:95523", "Fgfr2"),
                new PhivePriorityResult(4920, "ROR2", 0.6999089165402461, "MGI:1347521", "Ror2"),
                new PhivePriorityResult(341640, "FREM2", 0.6208762939500725, "MGI:2444465", "Frem2"),
                new PhivePriorityResult(148203, "ZNF738", 0.6000000238418579, null, null)
                );
        assertThat(results, equalTo(expected));
    }

}