/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package db.migration.postgres;

import com.googlecode.flyway.core.api.migration.jdbc.JdbcMigration;
import java.io.FileReader;
import java.sql.Connection;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

/**
 * Flyway java migration for importing data into the exomiser PostgreSQL instance.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class V1_13__Insert_zp implements JdbcMigration {

    @Override
    public void migrate(Connection connection) throws Exception {
        CopyManager copyManager = new CopyManager((BaseConnection) connection);
        try (FileReader fileReader = new FileReader("data/zp.pg")) {
            copyManager.copyIn("COPY zp from STDIN WITH DELIMITER '|';", fileReader, 1024);
        }
    }
    
}
