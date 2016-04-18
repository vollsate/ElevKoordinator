package no.glv.paco.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import no.glv.paco.intrfc.Task;

/**
 * A static class that works towards a SQLite database. Takes care of loading,
 * inserting, deleting and updating tasks.
 *
 * @author GleVoll
 */
class TaskTbl {

    /**
     * Name of the TABLE in the SQLite database
     */
    public static final String TBL_NAME = "ntasks";

    public static final String COL_ID = "_ID";
    public static final int COL_ID_ID = 0;

    public static final String COL_NAME = "name";
    public static final int COL_NAME_ID = 1;

    public static final String COL_DESC = "desc";
    public static final int COL_DESC_ID = 2;

    public static final String COL_DATE = "date";
    public static final int COL_DATE_ID = 3;

    public static final String COL_STATE = "state";
    public static final int COL_STATE_ID = 4;

    public static final String COL_SUBJECT = "sbjct";
    public static final int COL_SUBJECT_ID = 5;

    public static final String COL_TYPE = "type";
    public static final int COL_TYPE_ID = 6;

    private TaskTbl() {
    }

    /**
     * @param db Is NOT closed after use!
     */
    public static boolean CreateTable( SQLiteDatabase db ) {
        String sql = "CREATE TABLE " + TBL_NAME + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT, "
                + COL_DESC + " TEXT, "
                + COL_DATE + " LONG NOT NULL, "
                + COL_STATE + " INTEGER, "
                + COL_SUBJECT + " INTEGER, "
                + COL_TYPE + " INTEGER)";

        return DBUtils.ExecuteSQL( sql, db );
    }

    /**
     * @param db Is NOT closed after use!
     */
    public static boolean DropTable( SQLiteDatabase db ) {
        return DBUtils.ExecuteSQL( "DROP TABLE IF EXISTS " + TBL_NAME, db );
    }

    /**
     * @return List of all tasks in the system
     */
    public static List<Task> loadAllTasks( SQLiteDatabase db ) {
        String sql = "SELECT * FROM " + TBL_NAME;
        List<Task> list = new ArrayList<Task>();
        Cursor cursor = db.rawQuery( sql, null );
        cursor.moveToFirst();

        while ( !cursor.isAfterLast() ) {
            list.add( CreateFromCursor( cursor ) );
            cursor.moveToNext();
        }

        cursor.close();
        db.close();
        return list;
    }

    /**
     * @param name Name of task to load
     * @return Task loaded of null if nothing found
     */
    static Task Load( String name, SQLiteDatabase db ) {
        String sql = "SELECT * FROM " + TBL_NAME + " WHERE " + COL_NAME + "=?";
        Task t = null;

        Cursor c = db.rawQuery( sql, new String[]{ name } );
        c.moveToFirst();
        while ( !c.isAfterLast() ) {
            t = CreateFromCursor( c );
            c.moveToNext();
        }

        c.close();
        db.close();
        return t;
    }

    /**
     *
     */
    private static Task CreateFromCursor( Cursor cursor ) {
        TaskImpl task = new TaskImpl( cursor.getInt( COL_ID_ID ) );

        task.setID( cursor.getInt( COL_ID_ID ) );
        task.setName( cursor.getString( COL_NAME_ID ) );
        task.setDescription( cursor.getString( COL_DESC_ID ) );
        task.setDate( new Date( cursor.getLong( COL_DATE_ID ) ) );
        task.setState( cursor.getInt( COL_STATE_ID ) );
        task.setSubject( cursor.getInt( COL_SUBJECT_ID ) );
        task.setType( cursor.getInt( COL_TYPE_ID ) );

        return task;
    }

    /**
     * @param task The task to insert
     * @param db   Is closed after use
     * @return Number of tasks inserted (should be 1 or 0)
     */
    public static int InsertTask( Task task, SQLiteDatabase db ) {
        ContentValues cv = TaksValues( task );
        int retVal = ( int ) db.insertOrThrow( TBL_NAME, null, cv );

        db.close();
        return retVal;
    }

    /**
     *
     */
    private static ContentValues TaksValues( Task task ) {
        ContentValues cv = new ContentValues();

        cv.put( COL_NAME, task.getName() );
        cv.put( COL_DESC, task.getDesciption() );
        cv.put( COL_DATE, task.getDate().getTime() );
        cv.put( COL_STATE, task.getState() );
        cv.put( COL_SUBJECT, task.getSubject() );
        cv.put( COL_TYPE, task.getType() );

        return cv;
    }

    /**
     * Updates a specific task
     *
     * @param task The {@link Task} to update, with all the new values
     * @param db   Is closed after use
     * @return 0 if no update, otherwise 1
     */
    public static int updateTask( Task task, SQLiteDatabase db ) {
        ContentValues cv = TaksValues( task );
        String whereClause = COL_ID + "=?";
        int retVal = db.update( TBL_NAME, cv, whereClause, new String[]{ String.valueOf( task.getID() ) } );

        db.close();

        return retVal;
    }

    /**
     * @param task The task to delete
     * @param db   Is closed after use
     * @return 1 if task deleted, 0 otherwise
     */
    public static int DeleteTask( Task task, SQLiteDatabase db ) {
        String whereClause = COL_ID + "=?";
        int retVal = db.delete( TBL_NAME, whereClause, new String[]{ String.valueOf( task.getID() ) } );

        db.close();
        return retVal;
    }

}
