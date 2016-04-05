package no.glv.elevko.sql;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

import no.glv.elevko.core.DBException;
import no.glv.elevko.intrfc.Parent;
import no.glv.elevko.intrfc.Phone;
import no.glv.elevko.intrfc.Student;
import no.glv.elevko.intrfc.StudentClass;
import no.glv.elevko.intrfc.StudentTask;
import no.glv.elevko.intrfc.SubjectType;
import no.glv.elevko.intrfc.Task;

/**
 * The main entry point into the database. This will keep only one instance, and
 * throw an exception is somebody tries to instantiate the database more than
 * once.
 *
 * @author glevoll
 */
public class Database extends SQLiteOpenHelper {

    private static final String TAG = Database.class.getSimpleName();

    /**
     * The current version of the database used.
     */
    public static final int DB_VERSION = 1;

    /**
     * The name of the database
     */
    public static final String DB_NAME = "stdwrkflw";

    /**
     * The singleton instance used to prevent more than one instance
     */
    private static Database instance;

    /**
     * @param ctx The application context. Used to access the Database
     * @return The singleton instance
     */
    public static Database GetInstance( Context ctx ) {
        if ( instance == null )
            instance = new Database( ctx );

        return instance;
    }

    /**
     * Creates the database content provider.
     *
     * @param context The context used to create the database.
     * @throws IllegalStateException is database already instantiated
     */
    public Database( Context context ) {
        super( context, DB_NAME, null, DB_VERSION, null );

        if ( instance != null )
            throw new IllegalStateException();
    }

    /**
     * Drops and creates
     */
    void deleteSubjectTypes() {
        SubjectTypeTbl.DropTable( getWritableDatabase() );
        SubjectTypeTbl.CreateTable( getWritableDatabase() );
    }

    /**
     * Drops and creates the entire database
     */
    public void runCreate() {
        SQLiteDatabase db = getWritableDatabase();

        onUpgrade( db, 1, 1 );
        onCreate( db );

        db.close();
    }

    @Override
    public void onCreate( SQLiteDatabase db ) {
        Log.d( TAG, "Creating tables .. " );
        StudentTbl.CreateTableSQL( db );
        StudentClassTbl.CreateTable( db );
        TaskTbl.CreateTable( db );
        StudentTaskTbl.CreateTable( db );
        PhoneTbl.CreateTable( db );
        ParentTbl.CreateTableSQL( db );
        SubjectTypeTbl.CreateTable( db );
    }

    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
        Log.d( TAG, "Dropping tables .." );
        StudentTbl.DropTable( db );
        StudentClassTbl.DropTable( db );
        TaskTbl.DropTable( db );
        StudentTaskTbl.DropTable( db );
        PhoneTbl.DropTable( db );
        ParentTbl.DropTable( db );
        SubjectTypeTbl.DropTable( db );
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

    /**
     * Loads the parents for a single student.
     *
     * @param id The student id
     * @return A list of all parents connected to this student
     */
    public List<Parent> loadParents( String id ) {
        return ParentTbl.LoadParent( id, getReadableDatabase() );
    }

    /**
     * Loads all registered phone numbers for a single parent belonging to a single student
     *
     * @param stdID    The student ID
     * @param parentID The parent ID
     * @return A list of all phone numberss
     */
    public List<Phone> loadPhone( String stdID, String parentID ) {
        return PhoneTbl.LoadParentPhone( stdID, parentID, getReadableDatabase() );
    }

    /**
     * @param stdClass Loads all student registered to a class
     * @return List of students
     */
    public List<Student> loadStudentsFromClass( String stdClass ) {
        return StudentTbl.LoadStudentFromClass( stdClass, getReadableDatabase() );
    }

    /**
     * Will insert the {@link Student} in the StudentTbl and every
     * {@link Parent} and {@link Phone} related to the student.
     *
     * @param student The student to insert
     */
    public boolean insertStudent( Student student ) {
        Log.d( TAG, "Inserting student: " + student.getIdent() );

        long rows = StudentTbl.Insert( student, getWritableDatabase() );
        if ( rows == 0 ) {
            return false; // No inserts .... :(
        }

        // Insert every parent (and phones)
        for ( Parent parent : student.getParents() ) {
            insertParent( parent );
        }

        return true;
    }

    /**
     * Will insert the <code>Parent</code> and any phone numbers associated with the parent
     *
     * @param parent The parent to insert
     */
    public void insertParent( Parent parent ) {
        ParentTbl.InsertParent( parent, getWritableDatabase() );

        // Break if no phone numbers
        if ( parent.getPhoneNumbers() == null )
            return;

        for ( Phone p : parent.getPhoneNumbers() ) {
            p.setParentID( parent.getID() );
            PhoneTbl.Insert( p, getWritableDatabase() );
        }
    }

    /**
     * @param student  the student to update
     * @param oldIdent The old ID if the student ID is to be updated.
     * @return 1 of successfull 0 otherwise
     */
    public int updateStudent( Student student, String oldIdent ) {
        if ( oldIdent!= null && !student.getIdent().equals( oldIdent ) )
            removeStudent( oldIdent );

        Log.d( TAG, "Updating student: " + student.getIdent() );
        return StudentTbl.Update( student, getWritableDatabase() );
    }

    /**
     * @param studentID the student ID to remove
     */
    public int removeStudent( String studentID ) {
        Log.d( TAG, "Deleting student: " + studentID );
        return StudentTbl.Delete( studentID, getWritableDatabase() );
    }

    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    //
    // TASK
    //
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------

    public SubjectType createSubjectType() {
        return new SubjectTypeBean();
    }

    public List<SubjectType> loadSubjectTypes() {
        return SubjectTypeTbl.LoadAll( getReadableDatabase() );
    }

    /**
     * @param st The subjecttype to insert
     * @return true if successful
     * @throws SQLException if the {@link SubjectType} cannot be inserted
     */
    public boolean insertSubjectType( SubjectType st ) {
        long retVal = SubjectTypeTbl.Insert( st, getWritableDatabase() );

        if ( retVal == -1 )
            throw new SQLException( "Error inserting SubjectType: " + st.toString() );

        return retVal >= 1;
    }


    /**
     * Deletes a subjectType from the database
     *
     * @return true if successful
     */
    public boolean deleteSubjectType( SubjectType st ) {
        int retVal = SubjectTypeTbl.Delete( st.getID(), getWritableDatabase() );

        return retVal >= 1;
    }

    /**
     * @param list A list of <code>SubjectType</code> instances to insert
     * @return true if successful
     * @throws SQLException if not all rows could be insert
     */
    public boolean insertSubjectTypes( List<SubjectType> list ) {
        int rows = ( int ) SubjectTypeTbl.Insert( list, getWritableDatabase() );

        if ( rows < list.size() - 1 )
            throw new SQLException( "Error inserting all rows. Data unstable!" );

        return true;
    }

    /**
     * @return a new Task instance
     */
    public Task createNewTask() {
        return new TaskImpl();
    }

    /**
     * @param task The task to insert
     */
    public boolean insertTask( Task task ) {
        Log.d( TAG, "Inserting new task: " + task.getName() );
        boolean retVal = true;
        try {
            int id = TaskTbl.InsertTask( task, getWritableDatabase() );
            ( ( TaskImpl ) task ).setID( id );

            StudentTaskTbl.InsertAll( task, getWritableDatabase() );
        } catch ( Exception e ) {
            Log.e( TAG, "Failure in adding task: " + task.getName(), e );
            retVal = false;
        }

        return retVal;
    }

    /**
     * @param stdTask The studenttask instance to update in the database
     * @return true if successful
     */
    public boolean updateStudentTask( StudentTask stdTask ) {
        return StudentTaskTbl.Update( stdTask, getWritableDatabase() ) == 1;

    }

    /**
     * Loads all the tasks in the database. Does not differentiate between
     * states.
     *
     * @return A list of all tasks loaded
     */
    public List<Task> loadTasks() {
        return TaskTbl.loadAllTasks( getReadableDatabase() );
    }

    /**
     * @param task The task to update
     * @return true if successful
     */
    public boolean updateTask( Task task ) {
        int rows = 0;

        try {
            rows = TaskTbl.updateTask( task, getWritableDatabase() );
        } catch ( RuntimeException e ) {
            Log.e( TAG, "Cannot update task: " + task.getName(), e );
            e.printStackTrace();
        }

        Log.d( TAG, "Updated " + rows + " rows" );

        return rows > 0;
    }

    /**
     * @param stdTasks Update all <code>StudentTask</code> instances in the list
     */
    public void updateStudentTasks( List<StudentTask> stdTasks ) {

        for ( StudentTask stdTask : stdTasks ) {
            StudentTaskTbl.Update( stdTask, getWritableDatabase() );
        }
    }

    /**
     * @param stdTasks Deletes all the <code>StudentTask</code> instances in the list
     * @throws DBException if an error occurs
     */
    public void deleteStudentTasks( List<StudentTask> stdTasks ) {

        for ( StudentTask stdTask : stdTasks ) {
            int rows = StudentTaskTbl.Delete( stdTask, getWritableDatabase() );
            if ( rows == 0 || rows > 1 ) {
                if ( rows == 0 )
                    throw new DBException( "Unable to delete StudentTask: " + stdTask.toSimpleString() );

                if ( rows > 1 )
                    throw new DBException( "More than one row deleted when deleting StudentTask: "
                            + stdTask.toSimpleString() );
            }
        }
    }

    /**
     * @param stdTasks Inserts all instances of StudentTask in the list.
     * @throws DBException if an error occurs
     */
    public void insertStudentTasks( List<StudentTask> stdTasks ) {

        for ( StudentTask stdTask : stdTasks ) {
            int rows = StudentTaskTbl.Insert( stdTask, getWritableDatabase() );
            if ( rows == 0 ) {
                throw new DBException( "Unable to insert StudentTask: " + stdTask.toSimpleString() );
            }
        }
    }

    /**
     * @param task The task to delete
     * @return true if successful
     */
    public boolean deleteTask( Task task ) {
        int rows;

        try {
            rows = TaskTbl.DeleteTask( task, getWritableDatabase() );
        } catch ( RuntimeException e ) {
            Log.e( TAG, "Cannot update task: " + task.getName(), e );
            e.printStackTrace();
            throw new DBException( "Cannot update task: " + task.getName(), e  );
        }

        Log.d( TAG, "Deleted " + rows + " rows" );

        return rows > 0;
    }

    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    //
    // STUDENTCLASS
    //
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------

    /**
     * @return List of all registered <code>StudentClass</code>
     */
    public List<StudentClass> loadStudentClasses() {
        try {
            SQLiteDatabase db = getReadableDatabase();
            return StudentClassTbl.LoadStudentClasses( db );
        } catch ( RuntimeException e ) {
            Log.e( TAG, "Cannot load studentClasses", e );
            throw e;
        }

    }

    /**
     * @param stdClass The studentClass to insert
     */
    public void insertStudentClass( StudentClass stdClass ) {
        StudentClassTbl.InsertStudentClass( stdClass, getWritableDatabase() );

        List<Student> list = stdClass.getStudents();

        for ( Student std : list ) {
            std.setStudentClass( stdClass.getName() );
            insertStudent( std );
        }
    }

    /**
     * @param stdClass The studentClass to delete
     * @return The number of rows deleted
     */
    public long deleteStdClass( StudentClass stdClass ) {
        List<Student> list = stdClass.getStudents();
        for ( int i = 0; i < list.size(); i++ ) {
            Student std = list.get( i );
            StudentTbl.Delete( std.getIdent(), getWritableDatabase() );
        }

        return StudentClassTbl.Delete( stdClass.getName(), getWritableDatabase() );
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
     * @return List of all students connected to the specified task
     */
    public List<StudentTask> loadStudentsInTask( Task task ) {
        return StudentTaskTbl.LoadAllInTask( getReadableDatabase(), task );
    }

    /**
     * @return A list of all known studentTask instances
     */
    public List<StudentTask> loadAllStudentTask() {
        return StudentTaskTbl.LoadAll( getReadableDatabase() );
    }

}
