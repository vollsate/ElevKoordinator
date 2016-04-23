package no.glv.paco.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import no.glv.paco.beans.GroupBean;
import no.glv.paco.intrfc.Group;

/**
 * Handles all SQL query
 */
class GroupTbl {

    private static final String TAG = GroupTbl.class.getSimpleName();

    public static final String TBL_NAME = "group";

    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final String COL_YEAR = "year";

    private static final String SQL_CRATE_TABLE = "CREATE TABLE " + TBL_NAME + "(" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NAME + " TEXT NOT NULL, " +
            COL_YEAR + " TEXT)";

    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TBL_NAME;

    private GroupTbl() {
    }

    /**
     *
     */
    public static void CreateTable( SQLiteDatabase db ) {
        String sql = SQL_CRATE_TABLE;

        Log.v( TAG, "Executing SQL: " + sql );
        db.execSQL( sql );
    }

    /**
     *
     */
    public static boolean CreateTable( Statement db ) throws SQLException {
        String sql = SQL_CRATE_TABLE;

        Log.v( TAG, "Executing SQL: " + sql );
        boolean result = db.execute( sql );
        db.close();

        return result;
    }

    /**
     *
     */
    public static void DropTable( SQLiteDatabase db ) {
        String sql = "DROP TABLE IF EXISTS " + TBL_NAME;

        Log.v( TAG, "Executing SQL: " + sql );
        db.execSQL( sql );
    }

    /**
     *
     */
    public static boolean DropTable( Statement db ) throws SQLException {
        String sql = SQL_DROP_TABLE;

        Log.v( TAG, "Executing SQL: " + sql );
        boolean result = db.execute( sql );

        db.close();
        return result;
    }

    /**
     * @return List of all known Group instances
     */
    public static List<Group> LoadAllGroups( SQLiteDatabase db ) {
        List<Group> list = new ArrayList<Group>();

        String sql = "SELECT * FROM " + TBL_NAME;
        Log.v( TAG, "Executing SQL: " + sql );

        Cursor cursor = db.rawQuery( sql, null );
        Log.d( TAG, "Count groups: " + cursor.getCount() );

        if ( cursor.moveToFirst() ) {
            do {
                list.add( CreateFromCursor( cursor ) );
                cursor.moveToNext();
            }
            while ( !cursor.isAfterLast() );
        }

        cursor.close();
        db.close();
        return list;
    }

    /**
     * @return List of all known Group instances
     */
    public static List<Group> LoadAllGroups( Statement db ) throws SQLException {
        List<Group> list = new ArrayList<Group>();

        String sql = "SELECT * FROM " + TBL_NAME;
        Log.v( TAG, "Executing SQL: " + sql );

        ResultSet cursor = db.executeQuery( sql );
        Log.d( TAG, "Count groups: " + cursor.getFetchSize() );

        while ( cursor.next() ) {
            list.add( CreateFromResultSet( cursor ) );
        }

        cursor.close();
        db.close();
        return list;
    }

    /**
     * @return A new Group object from the database
     */
    private static Group CreateFromCursor( Cursor cursor ) {
        GroupBean bean = new GroupBean( cursor.getInt( 0 ) );
        bean.setName( cursor.getString( 1 ) );
        bean.setName( cursor.getString( 2 ) );

        return bean;
    }

    /**
     * @return A new Group object from the database
     */
    private static Group CreateFromResultSet( ResultSet cursor ) throws SQLException {
        GroupBean bean = new GroupBean( cursor.getInt( 0 ) );

        bean.setName( cursor.getString( 1 ) );
        bean.setName( cursor.getString( 2 ) );

        return bean;
    }

    /**
     *
     */
    public static void InsertGroup( Group stdClass, SQLiteDatabase db ) {
        ContentValues cv = StudentClassValues( stdClass );

        long retVal = db.insert( TBL_NAME, null, cv );
        Log.d( TAG, "Retval from InsertGroup: " + retVal );

        db.close();
    }

    /**
     *
     */
    public static void InsertGroup( Group group, Statement db ) throws  SQLException {
        String sql = "INSERT INTO " + TBL_NAME +
                "(" + COL_NAME + ", " + COL_YEAR + ") " +
                "VALUES ('" + group.getName() + "', " +
                "'" + group.getYear() + "')";


        int retVal = db.executeUpdate( TBL_NAME );
        Log.d( TAG, "Retval from InsertGroup: " + retVal );

        db.close();
    }

    /**
     * @return The number of rows deleted
     */
    public static int Delete( String name, SQLiteDatabase db ) {
        String sql = COL_NAME + "=?";

        return db.delete( TBL_NAME, sql, new String[]{name} );
    }

    private static ContentValues StudentClassValues( Group stdClass ) {
        ContentValues cv = new ContentValues();

        cv.put( COL_NAME, stdClass.getName() );

        return cv;
    }

}
