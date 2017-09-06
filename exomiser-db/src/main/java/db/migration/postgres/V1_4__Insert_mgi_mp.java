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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package db.migration.postgres;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.FileReader;
import java.sql.Connection;

/**
 * Flyway java migration for importing data into the exomiser PostgreSQL instance.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class V1_4__Insert_mgi_mp implements JdbcMigration {
    
    @Override
    public void migrate(Connection connection) throws Exception {
        CopyManager copyManager = new CopyManager((BaseConnection) connection);
        try (FileReader fileReader = new FileReader("data/mouseMp.pg")) {
            copyManager.copyIn("COPY mgi_mp from STDIN WITH DELIMITER '|';", fileReader, 1024);
        }
    }
}
