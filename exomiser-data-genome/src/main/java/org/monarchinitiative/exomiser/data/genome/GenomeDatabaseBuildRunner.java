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

package org.monarchinitiative.exomiser.data.genome;

import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomeDatabaseBuildRunner {

    private static final Logger logger = LoggerFactory.getLogger(GenomeDatabaseBuildRunner.class);

    private final String buildString;
    private final Path dataPath;
    private final Path outputPath;

    public GenomeDatabaseBuildRunner(String buildString, Path dataPath, Path outputPath) {
    //provide this as a class?
        this.buildString = buildString;
        this.dataPath = dataPath;
        this.outputPath = outputPath;
//        build dir
//        output dir
    }

    public void run() {
        Path databasePath = outputPath.resolve(String.format("%s_genome", buildString));
        DataSource dataSource = createDataSource(databasePath);
        logger.info("Created database: {}", databasePath);
        //run the Fantom and Ensemble parsers here?

        migrateDatabase(dataSource);
        logger.info("Finished importing genome data");
    }

    private DataSource createDataSource(Path databasePath) {
        String initSql = "MODE=PostgreSQL;LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0;MV_STORE=FALSE;";
        String url = String.format("jdbc:h2:file:%s;%s", databasePath.toAbsolutePath(), initSql);
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(url)
                .username("sa")
                .build();
    }

    private void migrateDatabase(DataSource dataSource) {
        Map<String, String> propertyPlaceHolders = new HashMap<>();
        propertyPlaceHolders.put("import.path", dataPath.toString());

        logger.info("Migrating {} genome database...", buildString);
        Flyway h2Flyway = new Flyway();
        h2Flyway.setDataSource(dataSource);
        h2Flyway.setSchemas("EXOMISER");
        h2Flyway.setLocations("classpath:db/migration");
        h2Flyway.setPlaceholders(propertyPlaceHolders);
        h2Flyway.clean();
        h2Flyway.migrate();
    }
}
