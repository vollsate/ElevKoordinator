package no.glv.elevko.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import no.glv.elevko.app.DBException;
import no.glv.elevko.intrfc.Parent;

/**
 * Handles all SQL queries about Parent
 */
class ParentTbl implements BaseColumns {

    private static final String TAG = ParentTbl.class.getSimpleName();

    public static final String TBL_NAME = "parent";

    public static final String COL_ID = "_id";
    public static final int COL_ID_ID = 0;

    // String (student ident)
    public static final String COL_STDID = "stdid";
    public static final int COL_STDID_ID = 1;

    public static final String COL_FNAME = "fname";
    public static final int COL_FNAME_ID = 2;

    public static final String COL_LNAME = "lname";
    public static final int COL_LNAME_ID = 3;

    public static final String COL_MAIL = "mail";
    public static final int COL_MAIL_ID = 4;

    public static final String COL_TYPE = "type";
    public static final int COL_TYPE_ID = 5;

    private ParentTbl() {
    }

    /**
     * Called as part of initiation of the entire DATABASE.
     * <p>
     * DO NOT CLOSE THE SQLiteDatabase
     *
     * @param db Do not close!
     */
    static void CreateTableSQL( SQLiteDatabase db ) {
        String sql = "CREATE TABLE " + TBL_NAME + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_STDID + " TEXT NOT NULL, "
                + COL_FNAME + " TEXT, "
                + COL_LNAME + " TEXT, "
                + COL_MAIL + " TEXT, "
                + COL_TYPE + " INTEGER)";

        DBUtils.ExecuteSQL( sql, db );
    }

    public static void DropTable( SQLiteDatabase db ) {
        String sql = "DROP TABLE IF EXISTS " + TBL_NAME;

        Log.v( TAG, "Executing SQL: " + sql );
        db.execSQL( sql );
    }

    /**
     * Load the parent to a specific student ID. If studentID is null, loads every
     * parent.
     *
     * @param studentID The student id to look for. May be null.
     * @param db        The database to query
     * @return List of all parents connected to the student ID, or every known parent.
     */
    public static List<Parent> LoadParent( String studentID, SQLiteDatabase db ) {
        List<Parent> list = new ArrayList<Parent>();

        String sql = "SELECT * FROM " + TBL_NAME;
        if ( studentID != null )
            sql += " WHERE " + COL_STDID + " = ?";

        Cursor cursor = db.rawQuery( sql, new String[]{ studentID } );
        cursor.moveToFirst();
        while ( !cursor.isAfterLast() ) {
            list.add( CreateFromCursor( cursor ) );
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        return list;
    }

    private static Parent CreateFromCursor( Cursor cursor ) {
        Parent parent = new ParentBean( cursor.getString( COL_ID_ID ), cursor.getInt( COL_TYPE_ID ) );

        parent.setFirstName( cursor.getString( COL_FNAME_ID ) );
        parent.setStudentID( cursor.getString( COL_STDID_ID ) );
        parent.setLastName( cursor.getString( COL_LNAME_ID ) );
        parent.setMail( cursor.getString( COL_MAIL_ID ) );

        return parent;
    }

    /**
     * Attempts to insert a parent into the database. If an error occurs, a <code>DatabaseException</code> will be
     * thrown.
     *
     * @param parent The parent to insert
     * @param db     The database to update
     * @return The number of rows updated
     */
    public static long InsertParent( Parent parent, SQLiteDatabase db ) {
        ContentValues parentValues = ParentValues( parent );

        long retVal = db.insert( TBL_NAME, null, parentValues );
        if ( retVal == -1 )
            throw new DBException( "Error inserting parent: " + parent.toString() );

        parent.setID( String.valueOf( retVal ) ); // Set the row number as the ID
        db.close();

        return retVal;
    }

    /**
     * @param parent The parent to update
     * @param db     Is closed after use
     * @return 1 if successful, 0 otherwise
     */
    public static int UpdateParent( Parent parent, SQLiteDatabase db ) {
        String sqlFiler = COL_STDID + " = ?";
        ContentValues cv = ParentValues( parent );

        int retVal = db.update( TBL_NAME, cv, sqlFiler, new String[]{ parent.getID() } );
        db.close();

        return retVal;
    }

    /**
     *
     */
    private static ContentValues ParentValues( Parent parent ) {
        ContentValues cv = new ContentValues();

        cv.put( COL_STDID, parent.getStudentID() );
        cv.put( COL_FNAME, parent.getFirstName() );
        cv.put( COL_LNAME, parent.getLastName() );
        cv.put( COL_MAIL, parent.getMail() );
        cv.put( COL_TYPE, parent.getType() );

        return cv;
    }
}
