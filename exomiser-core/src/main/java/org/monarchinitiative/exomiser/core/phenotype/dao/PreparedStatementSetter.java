package org.monarchinitiative.exomiser.core.phenotype.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Non-spring dependent version of PreparedStatementSetter  {@see http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/jdbc/core/PreparedStatementSetter.html}
 * Code borrowed from {@see http://stackoverflow.com/a/33706394}
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@FunctionalInterface
public interface PreparedStatementSetter {

    void setValues(PreparedStatement ps) throws SQLException;

    static PreparedStatement prepareStatement(Connection connection, String sql, PreparedStatementSetter setter) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        setter.setValues(ps);
        return ps;
    }
}
