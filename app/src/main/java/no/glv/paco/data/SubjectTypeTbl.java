package no.glv.paco.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import no.glv.paco.intrfc.SubjectType;

/**
 * Represents a {@link SubjectType} instance in the database.
 * 
 * @author glevoll
 *
 */
class SubjectTypeTbl implements BaseColumns {

	private static final String TAG = SubjectTypeTbl.class.getSimpleName();

	public static final String TBL_NAME = "sbjcttp";

	public static final String COL_ID = "_id";
	public static final int COL_ID_ID = 0;

	/** String - name of subject/type */
	public static final String COL_NAME = "name";
	public static final int COL_NAME_ID = 1;

	/** String - description */
	public static final String COL_DESC = "desc";
	public static final int COL_DESC_ID = 2;

	/** INTEGER */
	public static final String COL_TYPE = "type";
	public static final int COL_TYPE_ID = 3;

	private SubjectTypeTbl() {
	}

	/**
	 * Called as part of initiation of the entire DATABASE.
	 * 
	 * DO NOT CLOSE THE SQLiteDatabase
	 * 
	 * @param db Do not close!
	 */
	static void CreateTable( SQLiteDatabase db ) {
		String sql = "CREATE TABLE " + TBL_NAME + "("
				+ COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COL_NAME + " TEXT NOT NULL, "
				+ COL_DESC + " TEXT, "
				+ COL_TYPE + " INTEGER)";

		Log.v( TAG, "Executing SQL: " + sql );
		db.execSQL( sql );
	}

	public static void DropTable( SQLiteDatabase db ) {
		String sql = "DROP TABLE IF EXISTS " + TBL_NAME;

		Log.v( TAG, "Executing SQL: " + sql );
		db.execSQL( sql );
	}

	/**
	 * Loads all known {@link SubjectType} found in the database.
	 * 
	 * @param db
	 * @return A list of every known {@link SubjectType} or an empty list if
	 *         something fails
	 */
	public static List<SubjectType> LoadAll( SQLiteDatabase db ) {
		List<SubjectType> list = new ArrayList<SubjectType>();

		String sql = "SELECT * FROM " + TBL_NAME;
		Log.d( TAG, "Executing SQL: " + sql );

		try {
			Cursor cursor = db.rawQuery( sql, null );
			cursor.moveToFirst();
			while ( !cursor.isAfterLast() ) {
				list.add( CreateFromCursor( cursor ) );
				cursor.moveToNext();
			}
			cursor.close();
		}
		catch ( Exception e ) {
			Log.e( TAG, "Error getting phonerecord", e );
		}

		db.close();

		return list;
	}

	/**
	 * Creates a {@link SubjectType} instance from an SQL cursor object.
	 * 
	 * <p>
	 * NB! The cursor will NOT be closed
	 * 
	 * @param cursor
	 * @return
	 */
	private static SubjectType CreateFromCursor( Cursor cursor ) {
		SubjectTypeBean st = new SubjectTypeBean( cursor.getInt( COL_ID_ID ) );

		st.setName( cursor.getString( COL_NAME_ID ) );
		st.setDescription( cursor.getString( COL_DESC_ID ) );
		st.setType( cursor.getInt( COL_TYPE_ID ) );

		return st;
	}

	/**
	 * Inserts one {@link SubjectType} instance in the database.
	 * 
	 * <p>
	 * The row number will be the ID, and may be retrieved through the
	 * {@link SubjectType#getID()}.
	 * 
	 * @param st
	 * @param db The SQL database will be closed
	 * 
	 * @return The row number of the newly created {@link SubjectType}.
	 */
	public static long Insert( SubjectType st, SQLiteDatabase db ) {
		return Insert( st, db, true );
	}

	/**
	 * Inserts a list of {@link SubjectType} instances.
	 * 
	 * @param list
	 * @param db
	 * @return The number of rows inserted in the database.
	 */
	public static long Insert( List<SubjectType> list, SQLiteDatabase db ) {
		int count = 0;
		for ( SubjectType st : list ) {
			long retVal = Insert( st, db, false );
			if ( retVal > 0 )
				count++;
		}

		db.close();
		return count;
	}

	/**
	 * Insert one {@link SubjectType} instance. The rownumber will be set as the
	 * ID of the {@link SubjectType}.
	 * 
	 * @param st
	 * @param db
	 * @param close Weather or not to close the DB connection
	 * 
	 * @return The row number in the database.
	 */
	private static long Insert( SubjectType st, SQLiteDatabase db, boolean close ) {
		ContentValues stValues = SQLValues( st );

		// retVal will be row number or -1 if error
		long retVal = db.insert( TBL_NAME, null, stValues );
		if ( retVal == -1 ) {
			Log.e( TAG, "Failed to insert SubjectType: " + st.getName() );
		}
		else
			( (SubjectTypeBean) st ).setId( (int) retVal );

		if ( close )
			db.close();

		return retVal;
	}

	/**
	 * 
	 * @param st
	 * @param db Is closed after use
	 * 
	 * @return 1 if successful, 0 otherwise
	 */
	public static int Update( SubjectType st, SQLiteDatabase db ) {
		String sqlFiler = COL_ID + " = ?";
		ContentValues cv = SQLValues( st );

		// retVal will be 1 if success, 0 otherwise
		int retVal = db.update( TBL_NAME, cv, sqlFiler, new String[] { String.valueOf( st.getID() ) } );
		if ( retVal == 0 )
			Log.e( TAG, "Error updating SubjecType#name = " + st.getName() );
		
		db.close();

		return retVal;
	}

	/**
	 * 
	 * @param id The row number of the SubjectType
	 * @param db
	 */
	public static int Delete( int id, SQLiteDatabase db ) {
		String sqlFilter = COL_ID + " = ?";
		
		// retVal will be rows deleted, or 0
		int retVal = db.delete( TBL_NAME, sqlFilter, new String[] { String.valueOf( id ) } );
		if ( retVal == 0 ) {
			Log.e( TAG, "Error deleting SubjectType#ID = " + id );
		}
		
		db.close();

		return retVal;
	}

	/**
	 * Creates a SQL insert object from a {@link SubjectType}.
	 * 
	 * @param st
	 * @return
	 */
	private static ContentValues SQLValues( SubjectType st ) {
		ContentValues cv = new ContentValues();

		cv.put( COL_NAME, st.getName() );
		cv.put( COL_DESC, st.getDescription() );
		cv.put( COL_TYPE, st.getType() );

		return cv;
	}
}
