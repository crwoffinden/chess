package dataAccess.DAO;

import dataAccess.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Responsible for creating connections to the database. Connections should be closed after use, either by calling
 * {@link #closeConnection(Connection)} on the Database instance or directly on the connection.
 */
public class Database {

    // FIXME: Change these fields, if necessary, to match your database configuration
    public static final String DB_NAME = "chess";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "Edhe@dof2018";

    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/" + DB_NAME;

    /**
     * Gets a connection to the database.
     *
     * @return Connection the connection.
     * @throws DataAccessException if a data access error occurs.
     */
    public Connection getConnection() throws DataAccessException {
        try {
            return DriverManager.getConnection(CONNECTION_URL, DB_USERNAME, DB_PASSWORD);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Closes the specified connection.
     *
     * @param connection the connection to be closed.
     * @throws DataAccessException if a data access error occurs while closing the connection.
     */
    public void closeConnection(Connection connection) throws DataAccessException {
        if(connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new DataAccessException(e.getMessage());
            }
        }
    }

    public void configureDatabase() throws SQLException {
        try (var conn = getConnection()) {
            var createDbStatement = conn.prepareStatement("CREATE DATABASE IF NOT EXISTS chess");
            createDbStatement.executeUpdate();

            conn.setCatalog("chess");

            var createUserTable = """
            CREATE TABLE  IF NOT EXISTS User (
                Username VARCHAR(255) NOT NULL,
                Password VARCHAR(255) NOT NULL,
                Email VARCHAR(255) NOT NULL,
                PRIMARY KEY (Username)
            )""";


            try (var createTableStatement = conn.prepareStatement(createUserTable)) {
                createTableStatement.executeUpdate();
            }

            var createGameTable = """
            CREATE TABLE  IF NOT EXISTS Game (
                GameID INT NOT NULL,
                WhiteUsername VARCHAR(255),
                BlackUsername VARCHAR(255),
                GameName VARCHAR(255) NOT NULL,
                Game longtext NOT NULL,
                PRIMARY KEY (GameID)
            )""";


            try (var createTableStatement = conn.prepareStatement(createGameTable)) {
                createTableStatement.executeUpdate();
            }

            var createAuthTokenTable = """
            CREATE TABLE  IF NOT EXISTS AuthToken (
                AuthToken VARCHAR(255) NOT NULL,
                Username VARCHAR(255) NOT NULL,
                PRIMARY KEY (AuthToken)
            )""";


            try (var createTableStatement = conn.prepareStatement(createAuthTokenTable)) {
                createTableStatement.executeUpdate();
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }
}
