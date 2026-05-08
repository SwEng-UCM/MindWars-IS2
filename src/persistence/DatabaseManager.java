/*
 *  @author Ashley Umeghalu
 * AI-assisted: yes (ChatGPT)
 */
package persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import util.AppPaths;

public class DatabaseManager {
    private static final String DB_URL =
            "jdbc:sqlite:" + AppPaths.databaseFile().toAbsolutePath();

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
