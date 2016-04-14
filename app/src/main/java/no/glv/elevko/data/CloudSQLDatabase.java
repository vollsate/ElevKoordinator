package no.glv.elevko.data;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import no.glv.elevko.app.DBException;
import no.glv.elevko.intrfc.Assignment;
import no.glv.elevko.intrfc.Group;
import no.glv.elevko.intrfc.Student;
import no.glv.elevko.intrfc.Task;

/**
 * The main entry point into the database. This will keep only one instance, and
 * throw an exception is somebody tries to instantiate the database more than
 * once.
 *
 * @author glevoll
 */
public class CloudSQLDatabase {

    static {
        try {
            Class.forName( "com.mysql.jdbc.Driver" );
        } catch ( ClassNotFoundException ex ) {
            System.out.println( "Can not load JDBC Driver: " + ex.getMessage() );
        }
    }

    /** Used for logging */
    private static final String TAG = CloudSQLDatabase.class.getSimpleName();

    /**
     * The current version of the database used.
     */
    public static final int DB_VERSION = 1;

    /**
     * The name of the database
     */
    public static final String DB_NAME = "pacu";

    private String dbAddress;
    private String dbUser;
    private String dbPwd;

    /**
     * The singleton instance used to prevent more than one instance
     */
    private static CloudSQLDatabase instance;

    private Connection _con;

    /**
     * @return The singleton instance
     */
    public static CloudSQLDatabase GetInstance( String dbAddress, String dbUser, String
            dbPassword ) {
        if ( instance == null ) {
            try {
                Class.forName( "com.mysql.jdbc.Driver" );
            } catch ( ClassNotFoundException ex ) {
                System.out.println( "Can not load JDBC Driver: " + ex.getMessage() );
                throw new DBException( ex );
            }

            instance = new CloudSQLDatabase( dbAddress, dbUser, dbPassword );

        }

        return instance;
    }

    /**
     * Creates the database content provider.
     *
     * @throws DBException is database already instantiated
     */
    public CloudSQLDatabase( String dbAddress, String dbUser, String
            dbPassword ) throws DBException {
        if ( instance != null )
            throw new IllegalStateException();

        this.dbAddress = dbAddress;
        this.dbUser = dbUser;
        this.dbPwd = dbPassword;

        try {
            _con = DriverManager.getConnection( dbAddress, dbUser, dbPassword );
        }
        catch ( SQLException sqlEx ) {
            throw new DBException( "Unable to connect to database: " + dbAddress, sqlEx );
        }
    }




    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    //
    // STUDENT
    //
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------

    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    //
    // TASK
    //
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------

    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    //
    // GROUP
    //
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------

    /**
     * @return List of all registered <code>Group</code>
     */
    public List<Group> loadAllGroups() throws  DBException{
        try {
            return GroupTbl.LoadAllGroups( _con.createStatement() );
        } catch ( Exception e ) {
            Log.e( TAG, "Cannot load groups", e );
            throw new DBException( "Cannot load groups", e );
        }

    }

    /**
     * Will insert a Group and all students connected to it. If no students are connected (size=0) an error will be
     * thrown.
     *
     * @param group The group to insert
     *
     * @throws DBException if group is emty
     */
    public void insertGroup( Group group ) throws SQLException {
        if ( group.getSize() == 0 ) throw new DBException( "Cannot insert empty group!" );

        GroupTbl.InsertGroup( group, _con.createStatement() );

        List<Student> list = group.getStudents();
        for ( Student std : list ) {
            std.setGroupName( group.getName() );
            //insertStudent( std );
        }
    }


    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    //
    // STUDENT IN TASK
    //
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------

    /**
     * @param task The task to find students connected to
     *
     * @return List of all students connected to the specified task
     */
    public List<Assignment> loadStudentsInTask( Task task ) {
        return null;
    }

    /**
     * @return A list of all known studentTask instances
     */
    public List<Assignment> loadAllStudentTask() {
        return null;
    }

}
