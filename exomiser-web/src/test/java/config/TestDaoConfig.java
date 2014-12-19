/*
 * Copyright (C) 2014 jj8
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package config;

import de.charite.compbio.exomiser.web.dao.ExomiserDao;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
public class TestDaoConfig {
    
    Logger logger = LoggerFactory.getLogger(TestDaoConfig.class);

    @Bean
    public DataSource mockDataSource() {
        return Mockito.mock(DataSource.class);
    }
    
    @Bean 
    public ExomiserDao mockExomiserDao() {
        ExomiserDao mockExomiserDao = Mockito.mock(ExomiserDao.class);
        Map<String, String> diseases = new HashMap<>();
        diseases.put("OMIM:101200", "Mouse syndrome");
        diseases.put("OMIM:101600", "Gruffalo syndrome");
        when(mockExomiserDao.getDiseases()).thenReturn(diseases);
        
        Map<String, String> hpoTerms = new HashMap<>();
        hpoTerms.put("HP:0001234", "Purple prickles");
        hpoTerms.put("HP:5678000", "Knobbly knees");
        when(mockExomiserDao.getHpoTerms()).thenReturn(hpoTerms);
        
        return mockExomiserDao;
    }
}
