package db.migration.postgres;

import com.googlecode.flyway.core.api.migration.jdbc.JdbcMigration;
import java.io.FileReader;
import java.sql.Connection;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Flyway java migration for importing data into the exomiser PostgreSQL instance.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class V2_0__Insert_frequency implements JdbcMigration {
    
    @Override
    public void migrate(Connection connection) throws Exception {
        CopyManager copyManager = new CopyManager((BaseConnection) connection);
        try (FileReader fileReader = new FileReader("data/frequency.pg")) {
            copyManager.copyIn("COPY frequency from STDIN WITH DELIMITER '|';", fileReader, 1024);
        }
    }
}
