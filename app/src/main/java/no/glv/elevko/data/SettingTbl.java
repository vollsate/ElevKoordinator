package no.glv.elevko.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import no.glv.elevko.intrfc.Setting;

class SettingTbl implements BaseColumns {

	private static final String TAG = SettingTbl.class.getSimpleName();

	public static final String TBL_NAME = "setting";

	// String (student ident)
	public static final String COL_ID = "_id";
	public static final int COL_ID_ID = 0;

	public static final String COL_NAME = "name";
	public static final int COL_NAME_ID = 1;

	public static final String COL_VALUE = "lname";
	public static final int COL_VALUE_ID = 2;

	private SettingTbl() {
	}

	/**
	 * Called as part of initiation of the entire DATABASE.
	 * 
	 * DO NOT CLOSE THE SQLiteDatabase
	 * 
	 * @param db
	 *            Do not close!
	 */
	static void CreateTableSQL( SQLiteDatabase db ) {
		String sql = "CREATE TABLE " + TBL_NAME + "("
				+ COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ COL_NAME + " TEXT, "
				+ COL_VALUE + " TEXT)";

		DBUtils.ExecuteSQL( sql, db );
	}

	public static void DropTable( SQLiteDatabase db ) {
		String sql = "DROP TABLE IF EXISTS " + TBL_NAME;

		Log.v( TAG, "Executing SQL: " + sql );
		db.execSQL( sql );
	}

	/**
	 * 
	 * @param stdClass
	 * @param db
	 * @return
	 */
	public static List<Setting> Load( int id, SQLiteDatabase db ) {
		List<Setting> list = new ArrayList<Setting>();

		String sql = "SELECT * FROM " + TBL_NAME + " WHERE " + COL_ID + " = ?";
		Cursor cursor = db.rawQuery( sql, new String[] { String.valueOf( id ) } );
		cursor.moveToFirst();
		while ( !cursor.isAfterLast() ) {
			list.add( CreateFromCursor( cursor ) );
			cursor.moveToNext();
		}

		cursor.close();
		db.close();

		return list;
	}

	private static Setting CreateFromCursor( Cursor cursor ) {
		Setting setting = null;

		return setting;
	}

	/**
	 * 
	 * @param std
	 * @param db
	 */
	public static long Insert( Setting setting, SQLiteDatabase db ) {
		ContentValues parentValues = SettingValues( setting );

		long retVal = db.insert( TBL_NAME, null, parentValues );

		// setting.setID( String.valueOf( retVal ) );
		db.close();

		return retVal;
	}

	/**
	 * 
	 * @param setting
	 * @param db
	 *            Is closed after use
	 * 
	 * @return 1 if successful, 0 otherwise
	 */
	public static int Update( Setting setting, SQLiteDatabase db ) {
		String sqlFiler = COL_ID + " = ?";
		ContentValues cv = SettingValues( setting );

		int retVal = db.update( TBL_NAME, cv, sqlFiler, new String[] { String.valueOf( setting.getID() ) } );
		db.close();

		return retVal;
	}

	/**
	 * 
	 * @param id
	 * @param db
	 */
	public static int Delete( int id, SQLiteDatabase db ) {
		String sqlFilter = COL_ID + " = ?";
		int retVal = db.delete( TBL_NAME, sqlFilter, new String[] { String.valueOf( id ) } );
		db.close();

		return retVal;
	}

	/**
	 * 
	 * @param setting
	 * @return
	 */
	private static ContentValues SettingValues( Setting setting ) {
		ContentValues cv = new ContentValues();

		cv.put( COL_ID, setting.getID() );
		cv.put( COL_NAME, setting.getName() );
		cv.put( COL_VALUE, setting.getValue() );

		return cv;
	}
}
