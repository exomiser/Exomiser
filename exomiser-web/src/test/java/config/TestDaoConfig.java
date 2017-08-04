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
package config;

import org.mockito.Mockito;
import org.monarchinitiative.exomiser.web.dao.ExomiserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
public class TestDaoConfig {
    
    Logger logger = LoggerFactory.getLogger(TestDaoConfig.class);

    @Bean
    public DataSource dataSource() {
        return Mockito.mock(DataSource.class);
    }
    
    @Bean 
    public ExomiserDao exomiserDao() {
        ExomiserDao mockExomiserDao = Mockito.mock(ExomiserDao.class);
        Map<String, String> diseases = new HashMap<>();
        diseases.put("OMIM:101200", "Mouse syndrome");
        diseases.put("OMIM:101600", "Gruffalo syndrome");
        when(mockExomiserDao.getDiseases()).thenReturn(diseases);
        
        Map<String, String> hpoTerms = new HashMap<>();
        hpoTerms.put("HP:0001234", "Purple prickles");
        hpoTerms.put("HP:5678000", "Knobbly knees");
        when(mockExomiserDao.getHpoTerms()).thenReturn(hpoTerms);
        
        Map<String, String> genes = new HashMap<>();
        genes.put("2260", "FGFR1");
        genes.put("2263", "FGFR2");
        genes.put("124", "ADH1A");
        when(mockExomiserDao.getGenes()).thenReturn(genes);
        
        return mockExomiserDao;
    }
}
