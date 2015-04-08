
package db.migration.postgres;

import com.googlecode.flyway.core.api.migration.jdbc.JdbcMigration;
import java.io.FileReader;
import java.sql.Connection;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

/**
 * Flyway java migration for importing omim2gene data into the exomiser PostgreSQL instance.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class V2_7__Insert_entrez2sym implements JdbcMigration {
    
    @Override
    public void migrate(Connection connection) throws Exception {
        CopyManager copyManager = new CopyManager((BaseConnection) connection);
        try (FileReader fileReader = new FileReader("data/entrez2sym.pg")) {
            copyManager.copyIn("COPY entrez2sym from STDIN WITH DELIMITER '|';", fileReader, 1024);
        }
    }
}
