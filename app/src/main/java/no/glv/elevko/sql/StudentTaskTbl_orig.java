package no.glv.elevko.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import no.glv.elevko.intrfc.StudentTask;
import no.glv.elevko.intrfc.Task;

class StudentTaskTbl_orig {

	private static final String TAG = StudentTaskTbl_orig.class.getSimpleName();

	public static final String TBL_NAME = "nstdntsk";

	public static final String COL_ID = "_ID";
	public static final int COL_ID_ID = 0;

	public static final String COL_TASK = "task";
	public static final int COL_TASK_ID = 1;

	public static final String COL_IDENT = "ident";
	public static final int COL_IDENT_ID = 2;

	public static final String COL_DATE = "date";
	public static final int COL_DATE_ID = 3;

	/** Weather or not student has delivered (IN | PENDING | EXPIRED | LATE ) */
	public static final String COL_MODE = "mode";
	public static final int COL_MODE_ID = 4;

	private StudentTaskTbl_orig() {
	}

	/**
	 * 
	 * @param db
	 */
	public static void CreateTable( SQLiteDatabase db ) {
		String sql = "CREATE TABLE " + TBL_NAME + "("
				+ COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COL_TASK + " INTEGER NOT NULL, "
				+ COL_IDENT + " TEXT NOT NULL, "
				+ COL_DATE + " LONG, "
				+ COL_MODE + " LONG NOT NULL)";

		Log.v( TAG, "Executing SQL: " + sql );
		db.execSQL( sql );
	}

	/**
	 * 
	 * @param db
	 */
	public static void DropTable( SQLiteDatabase db ) {
		String sql = "DROP TABLE IF EXISTS " + TBL_NAME;

		Log.v( TAG, "Executing SQL: " + sql );
		db.execSQL( sql );
	}

	/**
	 * 
	 * @param db
	 * @return
	 */
	public static List<String> FindAllTaskNames( SQLiteDatabase db ) {
		List<String> list = new LinkedList<String>();
		String sql = "SELECT distinct " + COL_TASK + " FROM " + TBL_NAME;

		Cursor cursor = db.rawQuery( sql, null );
		cursor.moveToFirst();

		while ( !cursor.isAfterLast() ) {
			list.add( cursor.getString( 0 ) );
			cursor.moveToNext();
		}

		cursor.close();
		db.close();
		return list;
	}

	/**
	 * 
	 * @param name
	 * @param db
	 * @return
	 */
	public static int RemoveStudentsInTask( int taskID, SQLiteDatabase db ) {
		String sql = COL_TASK + "=?";

		int row = db.delete( TBL_NAME, sql, new String[] { String.valueOf( taskID ) } );

		db.close();
		return row;
	}

	/**
	 * 
	 * @param db
	 * @return
	 */
	public static List<StudentTask> LoadAllInTask( SQLiteDatabase db, Task task ) {
		String sql = "SELECT * FROM " + TBL_NAME + " WHERE " + COL_TASK + "=?";

		Cursor cursor = db.rawQuery( sql, new String[] { String.valueOf( task.getID() ) } );
		List<StudentTask> list = new ArrayList<StudentTask>();
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
	 * 
	 * @param db
	 * @return
	 */
	public static List<StudentTask> LoadAll( SQLiteDatabase db ) {
		String sql = "SELECT * FROM " + TBL_NAME;

		Cursor cursor = db.rawQuery( sql, null );
		List<StudentTask> list = new ArrayList<StudentTask>();
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
	 * 
	 * @param ident
	 * @param task
	 * @param db
	 * @return
	 */
	public static StudentTask Load( String ident, String task, SQLiteDatabase db ) {
		String sql = "SELECT * FROM " + TBL_NAME + " WHERE " + COL_TASK + "=? AND " + COL_IDENT + "=?";
		Cursor cursor = db.rawQuery( sql, new String[] { task, ident } );

		if ( cursor.getCount() > 1 )
			throw new IllegalStateException( "Too many StudentTask: ident=" + ident
					+ ", task=" + task );

		cursor.moveToFirst();
		return CreateFromCursor( cursor );
	}

	/**
	 * 
	 * @param cursor
	 * @return
	 */
	private static StudentTask CreateFromCursor( Cursor cursor ) {
		int id = cursor.getInt( COL_ID_ID );
		int task = cursor.getInt( COL_TASK_ID );
		String ident = cursor.getString( COL_IDENT_ID );
		long dateL = cursor.getLong( COL_DATE_ID );
		int mode = cursor.getInt( COL_MODE_ID );
		Date date = null;
		if ( dateL > 0 )
			date = new Date( dateL );

		StudentTaskImpl impl = new StudentTaskImpl( ident, task, mode, date );
		impl.setID( id );

		return impl;
	}

	/**
	 * 
	 * @param task
	 * @param db
	 * @return
	 */
	public static int Insert( StudentTask task, SQLiteDatabase db ) {
		int retVal = InsertOneST( task, db );
		db.close();
		return retVal;
	}

	/**
	 * 
	 * @param task
	 * @param db Does NOT close the connection
	 * @return
	 */
	private static int InsertOneST( StudentTask task, SQLiteDatabase db ) {
		long rowNum = db.insert( TBL_NAME, null, StudentTaskValues( task ) );

		( (StudentTaskImpl)task).setID( (int) rowNum );
		return (int) rowNum;
	}

	/**
	 * 
	 * @param task
	 * @param db
	 * @return
	 */
	public static void InsertAll( Task task, SQLiteDatabase db ) {
		Iterator<StudentTask> it = task.getStudentsInTask().iterator();
		while ( it.hasNext() ) {
			StudentTask st = it.next();
			st.setTaskName( task.getName() );
			st.setTaskID( task.getID() );
			InsertOneST( st, db );
		}

		db.close();
	}

	/**
	 * 
	 * @param stdTask
	 * @param db
	 * @return
	 */
	public static long Update( StudentTask stdTask, SQLiteDatabase db ) {
		long retVal = 0;

		String whereClause = COL_ID + "=?";
		retVal = db.update( TBL_NAME, StudentTaskValues( stdTask ), whereClause,
				new String[] { String.valueOf( stdTask.getID() ) } );

		return retVal;
	}

	/**
	 * 
	 * @param stdTask
	 * @param db
	 * @return
	 */
	public static int Delete( StudentTask stdTask, SQLiteDatabase db ) {
		int retVal = 0;

		String whereClause = COL_ID + "=?";
		Log.d( TAG, whereClause );

		retVal = db.delete( TBL_NAME, whereClause, new String[] { String.valueOf( stdTask.getID() ) } );

		return retVal;
	}

	/**
	 * 
	 * @param task
	 * @return
	 */
	private static ContentValues StudentTaskValues( StudentTask task ) {
		ContentValues cv = new ContentValues();

		cv.put( COL_TASK, task.getTaskID() );
		cv.put( COL_IDENT, task.getIdent() );

		Date date = task.getHandInDate();
		long dateL = 0;
		if ( date != null )
			dateL = date.getTime();
		cv.put( COL_DATE, dateL );

		cv.put( COL_MODE, task.getMode() );

		return cv;
	}

}
