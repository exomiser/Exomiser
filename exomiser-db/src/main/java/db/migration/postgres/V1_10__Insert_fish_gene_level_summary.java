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
public class V1_10__Insert_fish_gene_level_summary  implements JdbcMigration {
    
    @Override
    public void migrate(Connection connection) throws Exception {
        CopyManager copyManager = new CopyManager((BaseConnection) connection);
        try (FileReader fileReader = new FileReader("data/fishGeneLevelSummary.pg")) {
            copyManager.copyIn("COPY fish_gene_level_summary from STDIN WITH DELIMITER '|';", fileReader, 1024);
            //System.out.println("Not yet implemented due to differences in schema and data file. Check it.");
        }
    }
}
