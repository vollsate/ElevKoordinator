package no.glv.paco.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import no.glv.paco.beans.StudentBean;
import no.glv.paco.intrfc.Student;

/**
 * Handles all SQL query about <code>Student</code>.
 */
class StudentTbl implements BaseColumns {

    public static final String TBL_NAME = "student";

    public static final String COL_IDENT = "_ID";
    public static final int COL_IDENT_ID = 0;

    public static final String COL_CLASS = "class";
    public static final int COL_CLASS_ID = 1;

    public static final String COL_GRADE = "grade";
    public static final int COL_GRADE_ID = 2;

    public static final String COL_FNAME = "fname";
    public static final int COL_FNAME_ID = 3;

    public static final String COL_LNAME = "lname";
    public static final int COL_LNAME_ID = 4;

    public static final String COL_BIRTH = "birth";
    public static final int COL_BIRTH_ID = 5;

    public static final String COL_ADR = "adr";
    public static final int COL_ADR_ID = 6;

    public static final String COL_POSTALCODE = "pcode";
    public static final int COL_POSTALCODE_ID = 7;

    public static final String COL_PHONE = "phone";
    public static final int COL_PHONE_ID = 8;

    public static final String COL_STRENGTH = "strength";
    public static final int COL_STRENGTH_ID = 9;

    private StudentTbl() {
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
                + COL_IDENT + " TEXT PRIMARY KEY UNIQUE, "
                + COL_CLASS + " TEXT, "
                + COL_GRADE + " TEXT, "
                + COL_FNAME + " TEXT, "
                + COL_LNAME + " TEXT, "
                + COL_BIRTH + " TEXT, "
                + COL_ADR + " TEXT, "
                + COL_POSTALCODE + " TEXT, "
                + COL_PHONE + " TEXT, "
                + COL_STRENGTH + " TEXT)";

        DBUtils.ExecuteSQL( sql, db );
    }

    static void DropTable( SQLiteDatabase db ) {
        String sql = "DROP TABLE IF EXISTS " + TBL_NAME;

        DBUtils.ExecuteSQL( sql, db );
    }

    /**
     * @param stdClass Loads every student registered to a particular class.
     * @param db       The database to query
     * @return List of every student registered
     */
    static List<Student> LoadStudentFromClass( String stdClass, SQLiteDatabase db ) {
        List<Student> list = new ArrayList<Student>();

        String sql = "SELECT * FROM " + TBL_NAME + " WHERE " + COL_CLASS + " = ?";
        Cursor cursor = db.rawQuery( sql, new String[]{ stdClass } );
        cursor.moveToFirst();
        while ( !cursor.isAfterLast() ) {
            list.add( CreateFromCursor( cursor ) );
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        return list;
    }

    private static Student CreateFromCursor( Cursor cursor ) {
        StudentBean bean = new StudentBean( null );

        bean.setIdent( cursor.getString( COL_IDENT_ID ) );
        bean.setGroupName( cursor.getString( COL_CLASS_ID ) );
        bean.setGrade( cursor.getString( COL_GRADE_ID ) );
        bean.setFirstName( cursor.getString( COL_FNAME_ID ) );
        bean.setLastName( cursor.getString( COL_LNAME_ID ) );
        bean.setBirth( DBUtils.ConvertStringToDate( cursor.getString( COL_BIRTH_ID ), null ) );
        bean.setAdress( cursor.getString( COL_ADR_ID ) );
        bean.setPostalCode( cursor.getString( COL_POSTALCODE_ID ) );
        bean.setPhone( cursor.getString( COL_PHONE_ID ) );
        bean.setStrength( cursor.getInt( COL_STRENGTH_ID ) );

        return bean;
    }

    /**
     * @param student The new student to insert
     * @param db      The database to update
     * @return The number of rows inserted
     */
    public static long Insert( Student student, SQLiteDatabase db ) {
        ContentValues stdValues = StudentValues( student );

        long retVal = db.insert( TBL_NAME, null, stdValues );
        db.close();

        return retVal;
    }

    /**
     * @param student The student to update
     * @param db      Is closed after use
     * @return 1 if successful, 0 otherwise
     */
    public static int Update( Student student, SQLiteDatabase db ) {
        String sqlFiler = COL_IDENT + " = ?";
        ContentValues cv = StudentValues( student );

        int retVal = db.update( TBL_NAME, cv, sqlFiler, new String[]{ student.getIdent() } );
        db.close();

        return retVal;
    }

    /**
     * @param stdID The student ID to delete
     * @param db    The database to query
     * @return The number of rows deleted
     */
    public static int Delete( String stdID, SQLiteDatabase db ) {
        String sqlFilter = COL_IDENT + " = ?";
        int retVal = db.delete( TBL_NAME, sqlFilter, new String[]{ stdID } );
        db.close();

        return retVal;
    }

    /**
     * @param student The student to convert
     * @return A <code>Student</code> converted to key/value pairs
     */
    private static ContentValues StudentValues( Student student ) {
        ContentValues cv = new ContentValues();

        cv.put( COL_IDENT, student.getIdent() );
        cv.put( COL_CLASS, student.getGroupName() );
        cv.put( COL_GRADE, student.getGrade() );
        cv.put( COL_FNAME, student.getFirstName() );
        cv.put( COL_LNAME, student.getLastName() );
        cv.put( COL_BIRTH, DBUtils.ConvertToString( student.getBirth() ) );
        cv.put( COL_ADR, student.getAdress() );
        cv.put( COL_POSTALCODE, student.getPostalCode() );
        cv.put( COL_PHONE, student.getPhone() );
        cv.put( COL_STRENGTH, student.getStrength() );

        return cv;
    }
}
