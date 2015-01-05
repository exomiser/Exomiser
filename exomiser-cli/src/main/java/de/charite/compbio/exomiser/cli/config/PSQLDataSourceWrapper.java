package de.charite.compbio.exomiser.cli.config;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import javax.sql.DataSource;

/**
 * Wrapper for DataSource that automatically sets the search path in case of Postgres.
 * 
 * Connection.setSchema("exomiser") did not work using PGPoolingDataSource.
 * So we use the prepared statement with set search_path. 
 * 
 * @author Max Schubach <max.schubach@charite.de>
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
class PSQLDataSourceWrapper implements DataSource {
	DataSource wrapped;
	
	PSQLDataSourceWrapper(DataSource wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public Connection getConnection() throws SQLException {
		Connection connection  = this.wrapped.getConnection();
        PreparedStatement ps = connection.prepareStatement("SET search_path=exomiser");
        ps.execute();
		return connection;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return wrapped.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		wrapped.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		wrapped.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return wrapped.getLoginTimeout();
	}

	@Override
	public java.util.logging.Logger getParentLogger()
			throws SQLFeatureNotSupportedException {
		return wrapped.getParentLogger();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return wrapped.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return wrapped.isWrapperFor(iface);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return wrapped.getConnection(username, password);
	}
}