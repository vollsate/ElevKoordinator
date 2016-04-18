package no.glv.paco.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by GleVoll on 10.04.2016.
 */
public class CloudSQLWrapper {

    private static final String DB_IPADDRESS = "173.194.229.217:3306";

    private static final String DB_USER = "pacu";

    private static final String DB_PWD = "123456";

    private static boolean _IsConnected = false;

    private Connection _connection;

    static {
        try {
            Class.forName( "com.mysql.jdbc.Driver" );
            _IsConnected = true;
        } catch ( ClassNotFoundException clex ) {
            clex.printStackTrace();
            _IsConnected = false;
        }
    }

    public CloudSQLWrapper() {

    }


    public Connection getConnection() throws SQLException {
        if ( _connection != null ) return _connection;

        try {
            _connection = DriverManager.getConnection( "jdbc:mysql://" + DB_IPADDRESS + "/pacu",
                    DB_USER,
                    DB_PWD );
        } catch ( SQLException sqlEx ) {
            throw sqlEx;
        }

        return _connection;
    }
}
