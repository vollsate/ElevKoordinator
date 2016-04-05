package no.glv.elevko.core;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import no.glv.elevko.R;
import no.glv.elevko.intrfc.BaseValues;
import no.glv.elevko.intrfc.Parent;
import no.glv.elevko.intrfc.Phone;
import no.glv.elevko.intrfc.Student;
import no.glv.elevko.intrfc.StudentClass;
import no.glv.elevko.intrfc.StudentTask;
import no.glv.elevko.intrfc.SubjectType;
import no.glv.elevko.intrfc.Task;
import no.glv.elevko.intrfc.Task.OnTaskChangeListener;
import no.glv.elevko.sql.Database;
import no.glv.elevko.sql.StudentTaskImpl;
import no.glv.elevko.sql.SubjectTypeBean;

/**
 * This class is the hub of the application. Every operation on any date MUST be
 * done through this class.
 * <p>
 * <p>
 * The SQL package is very important. The {@link Database} is the hub for
 * communicating with the database. The SQL package also implements the
 * interface package <code>(.intrfc)</code>.
 * <p>
 * <p>
 * Any changes to the data structure is logged and maintained by this class.
 * That means that listeners are the way to keep up with any changes. Take note
 * that the {@link Task} class also provide a listener interface:
 * {@link OnTaskChangeListener}.
 * <p>
 * <p>
 *
 * @author GleVoll
 */
public class DataHandler {

    private static final String TAG = DataHandler.class.getSimpleName();

    public static final int MODE_RESETDB = Integer.MAX_VALUE;

    private static final String PREF_SUBJECTTYPE = BaseValues.EXTRA_BASEPARAM + "subjectType";

    private static final String STUDENT_IN_TASK_FILENAME = "stdntsk.glv";

    private static String STDCLASS_FILE_SUFFIX = ".csv";

    /**
     * A map of all the installedTasks the students are involved in
     */
    private static HashMap<String, StudentTaskImpl> studentInTasks;

    private final Database db;
    private final SettingsManager sManager;

    private Application mApp;

    /**
     * All the loaded classes from the database
     */
    private TreeMap<String, StudentClass> installedClasses;

    /**
     * All the loaded installedTasks from the database
     */
    private TreeMap<Integer, Task> installedTasks;

    /**
     * A map of all the SUBJECT SubjectType installed on the system
     */
    private TreeMap<String, SubjectType> installedSubjects;
    /**
     * A map of all the THEME SubjectType installed on the system
     */
    private TreeMap<String, SubjectType> installedThemes;

    /**
     * Singleton instance
     */
    private static DataHandler instance;
    private static boolean isInitiated = false;

    // Listeners
    private HashMap<String, OnTasksChangedListener> taskChangeListeners;
    private HashMap<String, OnStudentClassChangeListener> stdClassChangeListeners;
    private HashMap<String, OnSubjectTypesListener> subjectTypeListeners;

    /**
     * @return
     * @throws IllegalStateException if {@link #Init(Application)} has not been
     *             called first!
     */
    public static final DataHandler GetInstance() {
        if ( !isInitiated )
            throw new IllegalStateException( "DataHandler not inititated" );

        return instance;
    }

    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    //
    // INIT
    //
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------

    /**
     * Will initiate the DataHandler. If already initiated, the method will
     * return quietly.
     * <p>
     * The Context parameter is used to initialize the database. The
     * {@link Database} will only allow one instance, so this needs to work
     * properly or an {@link IllegalStateException} is thrown.
     * <p>
     * <p>
     * Any open installedTasks will be loaded, and every known
     * {@link StudentClass} will
     * be loaded.
     *
     * @param app
     * @return The singleton instance.
     */
    public static final DataHandler Init( Application app ) {
        if ( isInitiated )
            return instance;

        if ( instance == null )
            instance = new DataHandler( new Database( app ), app );

        instance.loadStudentClasses();
        instance.loadTasks();
        instance.loadSubjectTypes();

        isInitiated = true;
        return instance;
    }

    /**
     * This constructor is private in order to keep from being instantiated by
     * other ways then through the {@link DataHandler#Init(Application)} method
     * call.
     * <p>
     * <p>
     * The <tt>db</tt> parameter is the {@link Database} instance to use to
     * access the SQLite data. The {@link SettingsManager} will be initiated and
     * the preferences used by this app will be loaded.
     *
     * @param db
     * @param app
     */
    private DataHandler( Database db, Application app ) {
        this.db = db;

        mApp = app;
        sManager = new SettingsManager( app );
        initiateMaps();
        initiateListeners();

        // Check to see if the SubjectTypes are installed, and if
        // not - install them
        boolean loadSubTypes = sManager.getBoolPref( PREF_SUBJECTTYPE, false );
        if ( !loadSubTypes ) {
            initSubjectTypes();
            sManager.setBoolPref( PREF_SUBJECTTYPE, true );
        }

        // cleanupDB();
    }

    public void cleanupDB() {
        // db.cleanupDB();
    }

    /**
     * This will reset (delete and recreate) the database. MUST be called with
     * caution.
     */
    public void resetDB() {
        Log.d( TAG, "resetting database" );

        db.runCreate();
        initiateMaps();
        initSubjectTypes();
        loadSubjectTypes();

        notifyStudentClassChange( null, OnStudentClassChangeListener.MODE_DEL );
        notifyTaskChange( null, OnTasksChangedListener.MODE_DEL );
    }

    /**
     * Will initiate all the listener maps.
     */
    private void initiateListeners() {
        stdClassChangeListeners = new HashMap<String, DataHandler.OnStudentClassChangeListener>( 2 );
        taskChangeListeners = new HashMap<String, DataHandler.OnTasksChangedListener>( 2 );
        subjectTypeListeners = new HashMap<String, DataHandler.OnSubjectTypesListener>( 2 );
    }

    /**
     * Will initiate the Maps used to contain the DB. Called initially from the
     * constructor.
     * <p>
     * If {@link Database#cleanupDB()} is called, all the Maps will be reset by
     * an invocation
     * of this method.
     */
    private void initiateMaps() {
        installedClasses = new TreeMap<String, StudentClass>();
        installedTasks = new TreeMap<Integer, Task>();

        installedSubjects = new TreeMap<String, SubjectType>();
        installedThemes = new TreeMap<String, SubjectType>();
    }

    /**
     * Loads the installedTasks from the DB and and initialize every task with
     * it's
     * corresponding {@link StudentTask} instance.
     * <p>
     * The loadStudentClasses() metod MUST be called first.
     * <p>
     * <blockquote>
     * <ul>
     * <li>Load every task
     * <li>Load every corresponding StudentTask
     * <li>Fill the StudentTask with the Student instance
     * </ul>
     * </blockquote>
     */
    private List<Task> loadTasks() {
        List<Task> list = db.loadTasks();

        Iterator<Task> it = list.iterator();
        while ( it.hasNext() ) {
            Task task = it.next();
            List<StudentTask> stdTasks = db.loadStudentsInTask( task );
            // Make sure the StudentTask is properly set up.
            setUpStudentTask( task, stdTasks );

            task.addStudentTasks( stdTasks );
            task.markAsCommitted();
            installedTasks.put( Integer.valueOf( task.getID() ), task );
        }

        return list;
    }

    /**
     * Fills the {@link StudentTask} with the corresponding {@link Student}
     * instance and the corresponding {@link Task} data.
     * <p>
     * The complete list will be sorted by the default listing: ident ascending.
     *
     * @param task The task the StudentTask instance is connected to.
     * @param stdTasks
     */
    private void setUpStudentTask( Task task, List<StudentTask> stdTasks ) {
        for ( StudentTask stdTask : stdTasks ) {
            stdTask.setStudent( getStudentById( stdTask.getIdent() ) );
            stdTask.setTaskName( task.getName() );

            String stdClass = stdTask.getStudent().getStudentClass();
            task.addClassName( stdClass );
        }

        // Sort the list
        int sortType = getSettingsManager().getStudentClassSortType();
        Collections.sort( stdTasks, new DataComparator.StudentTaskComparator( sortType ) );
    }

    /**
     * Loads all the {@link StudentClass} found in the DB. All the instances is
     * filled with the corresponding {@link Student} instance.
     */
    private List<StudentClass> loadStudentClasses() {
        List<StudentClass> list = db.loadStudentClasses();

        Iterator<StudentClass> it = list.iterator();
        while ( it.hasNext() ) {
            StudentClass stdClass = it.next();
            populateStudentClass( stdClass );
            installedClasses.put( stdClass.getName(), stdClass );
        }

        return list;
    }

    /**
     * Fills the {@link StudentClass} instance with the students, and populate
     * the the student with the corresponding {@link Parent} instances and
     * {@link Phone} instance.
     * <p>
     * <p>
     *
     * @param stdClass
     */
    private void populateStudentClass( StudentClass stdClass ) {
        List<Student> stList = db.loadStudentsFromClass( stdClass.getName() );
        Collections.sort( stList, new DataComparator.StudentComparator() );

        stdClass.addAll( stList );

        Iterator<Student> stds = stdClass.getStudents().iterator();
        while ( stds.hasNext() ) {
            Student student = stds.next();
            populateStudent( student );
        }
    }

    /**
     * Populates the {@link Student} instance with the parents and the phone
     * data
     *
     * @param student The student to populate
     */
    private void populateStudent( Student student ) {
        student.addParents( db.loadParents( student.getIdent() ) );

        Iterator<Parent> parIt = student.getParents().iterator();
        while ( parIt.hasNext() ) {
            Parent parent = parIt.next();
            parent.addPhones( db.loadPhone( parent.getStudentID(), parent.getID() ) );
        }
    }

    /**
     * Creates a new empty {@link Task} instance.
     *
     * @return
     */
    public Task createTask() {
        return db.createNewTask();
    }

    /**
     * @return
     */
    public SettingsManager getSettingsManager() {
        return sManager;
    }

    /**
     * Lists the entire Database to the logCat.
     * <p>
     * TODO: Should export the entire DB to an Excel workbook
     */
    public File listDB() {
        try {
            ExcelWriter writer = new ExcelWriter();

            // Add all the classes
            writer.addStudentClasses( loadStudentClasses() );

            // Add all installedTasks
            List<Task> tasks = loadTasks();
            writer.addTasks( tasks );

            // Add all students in task
            List<StudentTask> sts = new LinkedList<StudentTask>();
            for ( Task t : tasks ) {
                List<StudentTask> list = db.loadStudentsInTask( t );
                setUpStudentTask( t, list );

                sts.addAll( list );
            }
            writer.addStudentTasks( db.loadAllStudentTask() );

            return writer.writeToFile( "stdwrkflw.xls" );
        } catch ( Exception e ) {
            Log.e( TAG, "Error writin to Excel file", e );
        }

        Log.v( TAG, db.loadAllStudentTask().toString() );
        return null;
    }

    @SuppressWarnings( "rawtypes" )
    public Map<String, List> getAllData() {
        HashMap<String, List> entireDB = new HashMap<String, List>();

        entireDB.put( "parents", db.loadParents( null ) );

        return entireDB;
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
     * Finds and returns the {@link Student} instance with the specified ID. If
     * the student is not found, NULL will be returned.
     * <p>
     * <p>
     * This function will iterate through every class installed to look for the
     * requested Student.
     *
     * @param ident The Student ID. See {@link Student} for more info.
     * @return The requested {@link Student} or NULL.
     */
    public Student getStudentById( String ident ) {
        Student std = null;

        Iterator<String> it = installedClasses.keySet().iterator();
        while ( it.hasNext() ) {
            std = getStudentById( it.next(), ident );
            if ( std != null )
                break;
        }

        return std;
    }

    /**
     * Find a {@link Student} instances with the specified ID in a certain
     * class. If the student is not found in the class, NULL will be returned.
     * <p>
     * <p>
     * If any of the parameters is null, null will be returned.
     *
     * @param stdClassName The class to look for a student in.
     * @param ident The unique ID of the student.
     * @return The {@link Student} instance, or NULL.
     */
    public Student getStudentById( String stdClassName, String ident ) {
        if ( stdClassName == null || ident == null )
            return null;

        Student std = null;

        StudentClass stdClass = installedClasses.get( stdClassName );
        std = stdClass.getStudentByIdent( ident );

        return std;
    }

    /**
     * Updates a student. The <tt>oldIdent</tt> parameter MUST be the original
     * ID of the student, otherwise this function will fail. This only applies
     * if the student ID itself is modified. If not, this parameter may be null.
     * <p>
     * <p>
     *
     * @param std The {@link Student} instance to update.
     * @param oldIdent The original ID of the student.
     * @return true if successful
     */
    public boolean updateStudent( Student std, String oldIdent ) {
        int retVal = 0;
        try {
            retVal = db.updateStudent( std, oldIdent );
            // notifyStudentUpdate( std );
        } catch ( Exception e ) {
            Log.e( TAG, "Failed to update student: " + std.getIdent(), e );
        }

        return retVal > 0;
    }

    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    //
    // SUBJECT TYPE
    //
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------

    /**
     * Will create the default {@link SubjectType} the system knows. These are
     * located in an XML file in the <tt>values</tt> folder.
     */
    private void initSubjectTypes() {
        // Get the default arrays
        String[] subjects = mApp.getResources().getStringArray( R.array.task_subjects );
        String[] types = mApp.getResources().getStringArray( R.array.task_types );
        String defDesc = mApp.getResources().getString( R.string.task_st_subjects_defaultDesc );
        LinkedList<SubjectType> list = new LinkedList<SubjectType>();

        for ( String s : subjects ) {
            SubjectType st = db.createSubjectType();
            st.setDescription( defDesc );
            st.setName( s );
            st.setType( SubjectType.TYPE_SUBJECT );

            list.add( st );
        }

        for ( String s : types ) {
            SubjectType st = db.createSubjectType();
            st.setDescription( defDesc );
            st.setName( s );
            st.setType( SubjectType.TYPE_THEME );

            list.add( st );
        }

        try {
            db.insertSubjectTypes( list );
        } catch ( Exception e ) {
            Log.e( TAG, "Error initiating SubjectTypes", e );
        }
    }

    /**
     * Loads all the {@link SubjectType} instances found in the database. These
     * types are stored in memory by the application.
     */
    private void loadSubjectTypes() {
        List<SubjectType> list = db.loadSubjectTypes();
        for ( SubjectType st : list ) {
            int type = st.getType();
            if ( ( type & SubjectType.TYPE_SUBJECT ) == SubjectType.TYPE_SUBJECT )
                installedSubjects.put( st.getName(), st );
            else
                installedThemes.put( st.getName(), st );
        }
    }

    /**
     * Gets a reference to all installed SubjectType.TYPE_SUBJECT
     *
     * @return
     */
    public Collection<SubjectType> getSubjects() {
        return installedSubjects.values();
    }

    /**
     * @return
     */
    public Collection<SubjectType> getTypes() {
        return installedThemes.values();
    }

    /**
     * @return
     */
    public Collection<String> getSubjectNames() {
        return installedSubjects.keySet();
    }

    /**
     * @return
     */
    public Collection<String> getTypeNames() {
        return installedThemes.keySet();
    }

    /**
     * Get the {@link SubjectType} instance with the specified ID. If the ID is
     * not found, an {@link IllegalStateException} is thrown.
     *
     * @param id
     * @return
     * @throws IllegalStateException if the {@link SubjectType} is not found.
     */
    public SubjectType getSubjectType( int id ) {
        for ( SubjectType st : installedSubjects.values() ) {
            if ( st.getID() == id )
                return st;
        }

        for ( SubjectType st : installedThemes.values() ) {
            if ( st.getID() == id )
                return st;
        }

        throw new IllegalStateException( "Error loading SubjectType with ID: " + id );
    }

    /**
     * Creates a new SubjectType bean. The new bean is not stored in any
     * registrey.
     * You need to register the new bean with <code>createSubjectType</code>
     *
     * @return
     */
    public SubjectType createSubjectType() {
        return new SubjectTypeBean();
    }

    /**
     * Adds a new SubjectType to the system. The new {@link SubjectType} will be
     * installed in the DataBase and any listeners for the change in
     * subjecttypes will be called.
     *
     * @param st
     * @return
     */
    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public boolean addSubjectType( SubjectType st ) {
        if ( st == null )
            return false;

        boolean success = db.insertSubjectType( st );
        if ( success ) {
            Map subType = null;
            int id = st.getType();

            if ( ( id & SubjectType.TYPE_SUBJECT ) == SubjectType.TYPE_SUBJECT ) {
                subType = installedSubjects;
            } else if ( ( id & SubjectType.TYPE_THEME ) == SubjectType.TYPE_THEME ) {
                subType = installedThemes;
            }

            subType.put( st.getName(), st );
        }

        return success;
    }

    /**
     * @param st
     * @return
     */
    public boolean checkSubjectType( SubjectType st ) {
        String name = st.getName();

        if ( name == null || name.length() == 0 )
            return false;
        if ( installedSubjects.containsKey( st.getName() ) )
            return false;
        return !installedThemes.containsKey( st.getName() );

    }

    /**
     * Deletes a specified <code>SubjectType</code>
     *
     * @param st
     * @return <code>true</code> if successful
     */
    public boolean deleteSubjectType( SubjectType st ) {
        boolean success = db.deleteSubjectType( st );

        if ( success ) {
            if ( installedSubjects.containsKey( st.getName() ) )
                installedSubjects.remove( st.getName() );
            else
                installedThemes.remove( st.getName() );
        }

        return success;
    }

    /**
     * Converts the name of an {@link SubjectType} to the ID it is stored with
     * in the Database.
     *
     * @param name Name of the {@link SubjectType} to look for.
     * @return -1 if the subject is not found.
     */
    public int convertSubjectToID( String name ) {
        return convertSubjectTypeToID( installedSubjects, name );
    }

    /**
     * @param name
     * @return
     */
    public int convertTypeToID( String name ) {
        return convertSubjectTypeToID( installedThemes, name );
    }

    /**
     * Get the ID of a specified {@link SubjectType} name. The ID is used in the
     * database
     *
     * @param map
     * @param name
     * @return
     */
    private int convertSubjectTypeToID( Map<String, SubjectType> map, String name ) {
        if ( !map.containsKey( name ) )
            return -1;

        SubjectType st = map.get( name );
        return st.getID();
    }

    /**
     * @param listener
     */
    public void registerSubjectTypeListener( OnSubjectTypesListener listener ) {
        unregisterOnSubjectTypeListener( listener );

        String name = listener.getClass().getName();
        subjectTypeListeners.put( name, listener );
    }

    /**
     * @param listener
     */
    public void unregisterOnSubjectTypeListener( OnSubjectTypesListener listener ) {
        String name = listener.getClass().getSimpleName();

        if ( subjectTypeListeners.containsKey( name ) )
            subjectTypeListeners.remove( name );
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

    /**
     * TODO: Simply use the {@link Collection}?
     *
     * @return A List of names of all the Tasks loaded
     */
    public List<String> getTaskNames() {
        ArrayList<String> list = new ArrayList<String>( installedTasks.size() );
        for ( Task t : installedTasks.values() ) {
            list.add( t.getName() );
        }

        return list;
    }

    /**
     * Finds any installedTasks that matches the flag. The flag must be one of
     * the states
     * a {@link Task} may be in: <tt>TASK_STATE_OPEN</tt>,
     * <tt>TASK_STATE_CLOSED</tt> or <tt>TASK_STATE_EXPIRED</tt> or any
     * combination of the them.
     *
     * @return A List of every task in the system where the installedTasks state
     *         matches
     *         the flag
     */
    public List<String> getTaskNames( int flag ) {
        List<String> tasks = new ArrayList<String>();

        for ( Task t : this.installedTasks.values() ) {
            int state = t.getState();
            if ( ( state & flag ) == Task.TASK_STATE_OPEN ) {
                tasks.add( t.getName() );
            }
            if ( ( state & flag ) == Task.TASK_STATE_CLOSED ) {
                tasks.add( t.getName() );
            }
            if ( ( state & flag ) == Task.TASK_STATE_EXPIRED ) {
                tasks.add( t.getName() );
            }
        }

        return tasks;
    }

    /**
     * @return Any {@link Task} loaded by the system.
     */
    public List<Task> getInstalledTasks() {
        return new ArrayList<Task>( installedTasks.values() );
    }

    /**
     * @param flag
     * @return
     */
    public List<Task> getTasks( int flag ) {
        List<Task> ts = new LinkedList<Task>();

        for ( Task t : installedTasks.values() ) {
            int state = t.getState();
            boolean add = false;
            if ( ( flag & state ) == Task.TASK_STATE_OPEN )
                add = true;
            if ( ( flag & state ) == Task.TASK_STATE_CLOSED )
                add = true;
            if ( ( flag & state ) == Task.TASK_STATE_EXPIRED )
                add = true;

            if ( add )
                ts.add( t );
        }

        return ts;
    }

    /**
     * @param name Name of {@link Task} to find.
     * @return The actual task, or NULL if not found
     */
    public Task getTask( String name ) {
        Task task = null;
        for ( Task t : installedTasks.values() ) {
            if ( t.getName().equalsIgnoreCase( name ) ) {
                task = t;
                break;
            }
        }

        return task;
    }

    public Task getTask( Integer id ) {
        return installedTasks.get( id );
    }

    /**
     * Adds the task to the database. The task will be filled with instances of
     * {@link StudentTask} objects linking the {@link Student} to the
     * {@link Task}.
     * <p>
     * <p>
     * Remember to call the {@link DataHandler#updateTask(Task, Integer)} after
     * calling this!
     *
     * @param task
     */
    public DataHandler addTask( Task task ) {
        if ( task == null )
            throw new NullPointerException( "Task to add cannot be NULL!" );

        if ( task.getName() == null )
            throw new NullPointerException( "Name of task cannot be NULL!" );

        if ( db.insertTask( task ) ) {
            List<StudentTask> stds = db.loadStudentsInTask( task );
            setUpStudentTask( task, stds );
            task.addStudentTasks( stds );

            installedTasks.put( Integer.valueOf( task.getID() ), task );
        }

        return this;
    }

    public String getTaskDisplayName( Task task ) {
        String name = task.getName();
        if ( name == null || name.length() == 0 ) {
            name = getSubjectType( task.getSubject() ).getName();
        }

        return name;
    }

    public void handInTask( Task t, StudentTask st ) {
        if ( !st.isHandedIn() )
            t.handIn( st.getIdent() );
        else
            t.handIn( st.getIdent(), Task.HANDIN_CANCEL );

        notifyTaskChange( t, OnTasksChangedListener.MODE_UPD );
    }

    /**
     * @param task
     * @param oldID
     */
    public DataHandler updateTask( Task task, Integer oldID ) {
        Log.d( TAG, "Updating task: " + task.getName() );

        if ( !db.updateTask( task ) )
            throw new IllegalStateException( "Failed to update Task: " + task.getName() );

        if ( oldID != null ) {
            installedTasks.remove( Integer.valueOf( oldID ) );
            installedTasks.put( Integer.valueOf( task.getID() ), task );
        }

        return this;
    }

    /**
     * Deletes a {@link Task} from the systems DB. After deletion, the system
     * will notify through the {@link DataHandler.OnTasksChangedListener} with
     * the flag {@link OnTasksChangedListener#MODE_DEL}.
     * <p>
     * <p>
     * Any students engaged in this task, will automatically be deleted.
     *
     * @param task The {@link Task} instance to delete.
     * @return <tt>TRUE</tt> if successful.
     */
    public boolean deleteTask( Task task ) {
        if ( db.deleteTask( task ) ) {
            db.deleteStudentTasks( task.getStudentsInTask() );

            installedTasks.remove( task.getID() );
            notifyTaskDelete( task );
            return true;
        }

        return false;
    }

    /**
     * @param name
     * @return
     */
    public boolean closeTask( String name ) {
        return closeTask( getTask( name ) );
    }

    /**
     * @param task
     * @return
     */
    public boolean closeTask( Task task ) {
        task.setState( Task.TASK_STATE_CLOSED );

        boolean succes = db.updateTask( task );
        notifyTaskChange( task, OnChangeListener.MODE_CLS );

        return succes;
    }

    /**
     * Commits a {@link Task} to the DB.
     *
     * @param task
     */
    public void commitTask( Task task ) {
        db.insertTask( task );
    }

    /**
     * @param task The task containing Students to be written to the database
     */
    public void commitStudentsTasks( Task task ) {
        List<StudentTask> list = task.getUpdatedStudents();
        if ( list != null && !list.isEmpty() ) {
            db.updateStudentTasks( list );
        }

        list = task.getRemovedStudents();
        if ( list != null && !list.isEmpty() ) {
            db.deleteStudentTasks( list );
        }

        list = task.getAddedStudents();
        if ( list != null && !list.isEmpty() ) {
            db.insertStudentTasks( list );
        }

        task.notifyChange();
        task.markAsCommitted();
    }

    /**
     *
     */
    public void commitTasks() {
        Iterator<Task> it = installedTasks.values().iterator();

        while ( it.hasNext() ) {
            Task task = it.next();
            commitTask( task );
        }
    }

    /**
     * Called when some changes are made to the installedTasks.
     */
    public void notifyTaskSettingsChange() {
        Iterator<OnTasksChangedListener> it = taskChangeListeners.values().iterator();
        while ( it.hasNext() ) {
            it.next().onTasksChange( OnTaskChangeListener.MODE_TASK_SORT );
        }
    }

    /**
     * @param newTask
     */
    private void notifyTaskChange( Task newTask, int mode ) {
        if ( taskChangeListeners.isEmpty() )
            return;

        Iterator<OnTasksChangedListener> it = taskChangeListeners.values().iterator();
        while ( it.hasNext() )
            it.next().onTasksChange( mode );
    }

    /**
     * @param newTask
     */
    public void notifyTaskAdd( Task newTask ) {
        notifyTaskChange( newTask, OnTasksChangedListener.MODE_ADD );
    }

    /**
     * @param oldTask
     */
    public void notifyTaskDelete( Task oldTask ) {
        notifyTaskChange( oldTask, OnTasksChangedListener.MODE_DEL );
    }

    /**
     * @param task
     */
    public void notifyTaskUpdate( Task task ) {
        notifyTaskChange( task, OnTasksChangedListener.MODE_UPD );
    }

    /**
     * @param listener
     */
    public void registerOnTaskChangeListener( OnTasksChangedListener listener ) {
        unregisterOnTaskChangeListener( listener );

        String name = listener.getClass().getName();
        taskChangeListeners.put( name, listener );
    }

    /**
     * @param listener
     */
    public void unregisterOnTaskChangeListener( OnTasksChangedListener listener ) {
        String name = listener.getClass().getName();

        if ( taskChangeListeners.containsKey( name ) )
            taskChangeListeners.remove( name );
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
     * Checks to see if a {@link StudentClass} is deletable. If the Studentclass
     * is involved in a specific, and open Task, the StudentClass cannot be
     * deleted.
     *
     * @param stdClass The {@link StudentClass} to check.
     * @return true if removable
     */
    public boolean isStudentClassRemovable( StudentClass stdClass ) {
        boolean deletable = true;

        Iterator<Task> it = installedTasks.values().iterator();
        while ( it.hasNext() ) {
            Task t = it.next();
            if ( t.getClasses().contains( stdClass.getName() ) ) {
                deletable = false;
            }
        }

        return deletable;
    }

    /**
     * Returns a list of strings containing the names of all the installedTasks
     * the
     * {@link StudentClass} is involved in.
     *
     * @param stdClass
     * @return A list of names, or an empty list
     */
    public List<String> getStudentClassInvolvedInTask( StudentClass stdClass ) {
        LinkedList<String> list = new LinkedList<String>();

        Iterator<Task> it = installedTasks.values().iterator();
        while ( it.hasNext() ) {
            Task t = it.next();
            if ( t.getClasses().contains( stdClass.getName() ) ) {
                list.add( t.getName() );
            }
        }

        return list;
    }

    /**
     * @return
     */
    public ArrayList<String> getInstalledClassNames() {
        return new ArrayList<String>( installedClasses.keySet() );
    }

    /**
     * @param name
     * @return
     */
    public StudentClass getStudentClass( String name ) {
        return installedClasses.get( name );
    }

    /**
     * @param stdClass
     */
    public void addStudentClass( StudentClass stdClass ) {
        db.insertStudentClass( stdClass );
        installedClasses.put( stdClass.getName(), stdClass );
    }

    /**
     * @param name
     * @return
     */
    public DataHandler deleteStudentClass( String name ) {
        if ( !installedClasses.containsKey( name ) || stdClassHasTasks( name ) )
            return this;

        StudentClass stdcClass = installedClasses.remove( name );
        db.deleteStdClass( stdcClass );

        return this;
    }

    /**
     * @param stdClassName
     * @return
     */
    public boolean stdClassHasTasks( String stdClassName ) {
        Iterator<Task> it = installedTasks.values().iterator();
        while ( it.hasNext() ) {
            Task task = it.next();
            if ( task.getClasses().contains( stdClassName ) )
                return true;
        }

        return false;
    }

    /**
     * @param stdClass
     */
    private void notifyStudentClassChange( StudentClass stdClass, int mode ) {
        if ( stdClassChangeListeners.isEmpty() )
            return;

        Iterator<OnStudentClassChangeListener> it = stdClassChangeListeners.values().iterator();
        while ( it.hasNext() )
            it.next().onStudentClassUpdate( stdClass, mode );
    }

    /**
     * @param stdClass
     */
    public void notifyStudentClassAdd( StudentClass stdClass ) {
        notifyStudentClassChange( stdClass, OnStudentClassChangeListener.MODE_ADD );
    }

    /**
     * @param stdClass
     */
    public void notifyStudentClassDel( StudentClass stdClass ) {
        notifyStudentClassChange( stdClass, OnStudentClassChangeListener.MODE_DEL );
    }

    /**
     * @param stdClass
     */
    public void notifyStudentClassUpdate( StudentClass stdClass ) {
        notifyStudentClassChange( stdClass, OnStudentClassChangeListener.MODE_UPD );
    }

    /**
     * @param listener
     */
    public void addOnStudentClassChangeListener( OnStudentClassChangeListener listener ) {
        String name = listener.getClass().getName();

        if ( stdClassChangeListeners.containsKey( name ) )
            stdClassChangeListeners.remove( name );

        stdClassChangeListeners.put( name, listener );
    }

    /**
     * @return
     */
    public List<String> getFilesFromDownloadDir() {
        List<String> list = new ArrayList<String>();

        File externalDir = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS );
        File[] files = externalDir.listFiles();
        for ( int i = 0; i < files.length; i++ ) {
            File f = files[i];
            if ( f.isFile() && f.getName().endsWith( STDCLASS_FILE_SUFFIX ) )
                list.add( f.getName() );
        }

        return list;
    }

    /**
     * Loads a CSV file with semicolon separated entries. The file MUST have an
     * header line, and the headers MUST be in this order: - Klasse - Født -
     * Fullt navn
     *
     * @return A ready formatted StudentClass instance
     * @throws IOException if any I/O error occurs
     */
    public static StudentClass LoadStudentClassFromDownloadDir( Context ctx, String fileName ) throws IOException {
        return null;
    }

    /**
     * @param bean
     * @return A new Ident
     */
    static String CreateStudentIdent( Student bean ) {
        String ident = null;

        String fn = bean.getFirstName();
        if ( fn.length() >= 3 )
            fn = fn.substring( 0, 3 );

        String ln = bean.getLastName();
        if ( ln.length() >= 4 )
            ln = ln.substring( 0, 4 );

        String year = Utils.GetDateAsString( bean.getBirth() );
        year = year.substring( year.length() - 2, year.length() );

        ident = year + fn + ln;
        ident = ident.replace( 'æ', 'e' );
        ident = ident.replace( 'ø', 'o' );
        ident = ident.replace( 'å', 'a' );

        Log.d( TAG, "Creating ident: " + ident );
        return ident.toLowerCase( Locale.getDefault() );
    }

    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    //
    // STUDENTS IN TASKS
    //
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------

    /**
     * @param ctx
     * @param stds
     * @param append
     */
    public static void WriteStudentInTasks( Context ctx, HashMap<String, StudentTaskImpl> stds, boolean append ) {
        if ( studentInTasks == null )
            return;

        FileOutputStream fos;
        BufferedWriter buff;

        try {
            fos = ctx.openFileOutput( STUDENT_IN_TASK_FILENAME, Context.MODE_PRIVATE );
            buff = new BufferedWriter( new OutputStreamWriter( fos ) );

            Iterator<String> it = studentInTasks.keySet().iterator();
            while ( it.hasNext() ) {
                StudentTaskImpl std = studentInTasks.get( it.next() );
                String data = CreateStringFromStudentsInTasks( std );

                buff.write( data );
                buff.newLine();
            }
        } catch ( FileNotFoundException e ) {
            Log.e( TAG, "Cannot write " + STUDENT_IN_TASK_FILENAME, e );
        } catch ( IOException ioe ) {
            Log.e( TAG, "Failure writing data to " + STUDENT_IN_TASK_FILENAME, ioe );
        }
    }

    /**
     * @param std
     * @return
     */
    private static String CreateStringFromStudentsInTasks( StudentTaskImpl std ) {
        StringBuffer sb = new StringBuffer();
        /*
         * sb.append( std.getIdent() ).append( STUDENT_IN_TASK_SEP ); int size =
         * std.getEngagedTasks().size() - 1; for ( int i = 0; i < size; i++ ) {
         * sb.append( std.getEngagedTasks().get( i ) ).append(
         * STUDENT_IN_TASK_DELIM ); } sb.append( std.getEngagedTasks().get( size
         * ) );
         */
        return sb.toString();
    }

    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    //
    // UTIL METHODS
    //
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------

    /**
     * Checks if external storage is available for read and write
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals( state );
    }

    /**
     * Checks if external storage is available to at least read
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals( state ) || Environment.MEDIA_MOUNTED_READ_ONLY.equals( state );
    }

    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    //
    // LISTENER
    //
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------------------

    /**
     * Listener that defines change to the DataHandler data set. The interfaces
     * will only be called when there is a change to the state of the
     * DataHandler, and not when there's a change to any intern state of an
     * instance.
     * <p>
     * <p>
     * The interfaces will be called when a new object is added or removed.
     *
     * @author glevoll
     */
    public interface OnChangeListener {
        int MODE_ADD = 1;
        int MODE_DEL = 2;
        int MODE_UPD = 4;

        int MODE_CLS = 8;
    }

    /**
     * Called by the system when a {@link SubjectType} is either added, updated
     * or deleted from the system.
     *
     * @author glevoll
     */
    public interface OnSubjectTypesListener extends OnChangeListener {

        /**
         * @param mode
         */
        void onSubjectTypeChange( int mode );
    }

    /**
     * Used as a callback by the {@link DataHandler} when there is a change to
     * the set of loaded installedTasks. Use the
     * {@link Task.OnTaskChangeListener} to get
     * a callback for an specific {@link Task} instance.
     * <p>
     * <p>
     * Called when a task is added, deleted og closed, or opened.
     *
     * @author GleVoll
     */
    public interface OnTasksChangedListener extends OnChangeListener {

        /**
         * @param mode
         */
        void onTasksChange( int mode );
    }

    /**
     * @author GleVoll
     */
    public interface OnStudentClassChangeListener extends OnChangeListener {

        void onStudentClassUpdate( StudentClass stdClass, int mode );
    }

}
