package no.glv.elevko.app;

import android.app.Application;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import no.glv.elevko.R;
import no.glv.elevko.data.Database;
import no.glv.elevko.data.SubjectTypeBean;
import no.glv.elevko.intrfc.Assignment;
import no.glv.elevko.intrfc.BaseValues;
import no.glv.elevko.intrfc.Group;
import no.glv.elevko.intrfc.OnChange;
import no.glv.elevko.intrfc.Parent;
import no.glv.elevko.intrfc.Phone;
import no.glv.elevko.intrfc.Student;
import no.glv.elevko.intrfc.SubjectType;
import no.glv.elevko.intrfc.Task;
import no.glv.elevko.intrfc.Task.OnAssignmentChangeListener;

/**
 * This class is the hub of the application. Every operation on any date MUST be
 * done through this class.
 * <p/>
 * <p/>
 * The SQL package is very important. The {@link Database} is the hub for
 * communicating with the database. The SQL package also implements the
 * interface package <code>(.intrfc)</code>.
 * <p/>
 * <p/>
 * Any changes to the data structure is logged and maintained by this class.
 * That means that listeners are the way to keep up with any changes. Take note
 * that the {@link Task} class also provide a listener interface:
 * {@link OnAssignmentChangeListener}.
 * <p/>
 * <p/>
 *
 * @author GleVoll
 */
public class DataHandler {

    /** Used for logging */
    private static final String TAG = DataHandler.class.getSimpleName();

    //public static final int MODE_RESETDB = Integer.MAX_VALUE;

    private static final String PREF_SUBJECTTYPE = BaseValues.EXTRA_BASEPARAM + "subjectType";

    /** Filetype for loading CSV files from the download directory */
    private static final String GROUP_FILE_SUFFIX = ".csv";

    /** The database */
    private final Database db;
    private final SettingsManager sManager;

    private Application mApp;

    /**
     * All the loaded groups from the database
     */
    private TreeMap<String, Group> installedGroups;

    /**
     * All the loaded tasks from the database
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

    /** Singleton instance */
    private static DataHandler instance;

    /** Flag to ensure initiation */
    private static boolean isInitiated = false;

    /** Listeners for change in data */
    private List<OnDataChangeListener> onDataChangeListeners;

    /**
     * @return The singleton instance
     *
     * @throws IllegalStateException if {@link #Init(Application)} has not been called first!
     */
    public static DataHandler GetInstance() {
        if ( !isInitiated )
            throw new IllegalStateException( "DataHandler not initiated" );

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
     * <p/>
     * The Context parameter is used to initialize the database. The
     * {@link Database} will only allow one instance, so this needs to work
     * properly or an {@link IllegalStateException} is thrown.
     * <p/>
     * <p/>
     * Any open installedTasks will be loaded, and every known
     * {@link Group} will
     * be loaded.
     *
     * @param app The application. Used to initiate the database
     *
     * @return The singleton instance.
     */
    public static DataHandler Init( Application app ) {
        if ( isInitiated )
            return instance;

        if ( instance == null )
            instance = new DataHandler( new Database( app ), app );

        instance.loadGroups();
        instance.loadTasks();
        instance.loadSubjectTypes();

        isInitiated = true;
        return instance;
    }

    /**
     * This constructor is private in order to keep from being instantiated by
     * other ways then through the {@link DataHandler#Init(Application)} method
     * call.
     * <p/>
     * <p/>
     * The <tt>db</tt> parameter is the {@link Database} instance to use to
     * access the SQLite data. The {@link SettingsManager} will be initiated and
     * the preferences used by this app will be loaded.
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

        // resetDB();
    }

    /**
     * This will reset (delete and recreate) the database. MUST be called with
     * caution.
     */
    public void resetDB() {
        Log.d( TAG, "Resetting database" );

        db.runCreate();
        initiateMaps();
        initSubjectTypes();
        loadSubjectTypes();

        notifyTaskChange( null, OnDataChangeListener.MODE_DEL );
    }

    /**
     * Will initiate all the listener maps.
     */
    private void initiateListeners() {
        onDataChangeListeners = new ArrayList<>( 2 );
    }

    /**
     * Will initiate the Maps used to contain the DB. Called initially from the
     * constructor.
     * <p/>
     * If {@link Database#runCreate()} ()} is called, all the Maps will be reset by
     * an invocation of this method.
     */
    private void initiateMaps() {
        installedGroups = new TreeMap<>();
        installedTasks = new TreeMap<>();

        installedSubjects = new TreeMap<>();
        installedThemes = new TreeMap<>();
    }

    /**
     * Loads the installedTasks from the DB and and initialize every task with
     * it's
     * corresponding {@link Assignment} instance.
     * <p/>
     * The loadAllGroups() metod MUST be called first.
     * <p/>
     * <blockquote>
     * <ul>
     * <li>Load every task
     * <li>Load every corresponding Assignment
     * <li>Fill the Assignment with the Student instance
     * </ul>
     * </blockquote>
     */
    private List<Task> loadTasks() {
        List<Task> list = db.loadTasks();

        for ( Task task : list ) {
            List<Assignment> stdTasks = db.loadStudentsInTask( task );
            // Make sure the Assignment is properly set up.
            setupAssignmentsForTask( task, stdTasks );

            task.addAssignments( stdTasks );
            task.markAsCommitted();
            installedTasks.put( task.getID(), task );
        }

        return list;
    }

    /**
     * Fills the {@link Assignment} with the corresponding {@link Student}
     * instance and the corresponding {@link Task} data.
     * <p/>
     * The complete list will be sorted by the default listing: ident ascending.
     *
     * @param task The task the Assignment instance is connected to.
     */
    private void setupAssignmentsForTask( Task task, List<Assignment> assignmentList ) {
        for ( Assignment assignment : assignmentList ) {
            assignment.setStudent( getStudentById( assignment.getIdent() ) );
            assignment.setTaskName( task.getName() );

            String groupName = assignment.getStudent().getGroupName();
            task.addGroupName( groupName );
        }

        // Sort the list
        int sortType = getSettingsManager().getStudentClassSortType();
        Collections.sort( assignmentList, new DataComparator.StudentTaskComparator( sortType ) );
    }

    /**
     * Loads all the {@link Group} found in the DB. All the instances is
     * filled with the corresponding {@link Student} instance.
     */
    private List<Group> loadGroups() {
        List<Group> list = db.loadAllGroups();

        for ( Group group : list ) {
            populateStudentClass( group );
            installedGroups.put( group.getName(), group );
        }

        return list;
    }

    /**
     * Fills the {@link Group} instance with the students, and populate
     * the the student with the corresponding {@link Parent} instances and
     * {@link Phone} instance.
     * <p/>
     * <p/>
     */
    private void populateStudentClass( Group group ) {
        List<Student> stList = db.loadStudentsFromClass( group.getName() );
        Collections.sort( stList, new DataComparator.StudentComparator() );

        group.addAll( stList );

        for ( Student student : group.getStudents() ) {
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

        for ( Parent parent : student.getParents() ) {
            parent.addPhones( db.loadPhone( parent.getStudentID(), parent.getID() ) );
        }
    }

    /**
     * Creates a new empty {@link Task} instance.
     */
    public Task createTask() {
        return db.createNewTask();
    }

    /**
     * @return The {@link SettingsManager}
     */
    public SettingsManager getSettingsManager() {
        return sManager;
    }

    /**
     * TODO: This method must be rewritten!
     * Lists the entire Database to an Excel workbook. The {@link ExcelWriter} is writing the
     * workbook to a file.
     * <p/>
     *
     * @return A handle to the file where the database is stored in an Excel workbook.
     */
    public File listDB() {
        try {
            ExcelWriter writer = new ExcelWriter();

            // Add all the classes
            writer.addStudentClasses( loadGroups() );

            // Add all installedTasks
            List<Task> tasks = loadTasks();
            writer.addTasks( tasks );

            // Add all students in task
            /*
            List<Assignment> assignments = new LinkedList<>();
            for ( Task t : tasks ) {
                List<Assignment> list = db.loadStudentsInTask( t );
                setupAssignmentsForTask( t, list );

                assignments.addAll( list );
            }
            */
            writer.addStudentTasks( db.loadAllStudentTask() );

            return writer.writeToFile( "stdwrkflw.xls" );
        } catch ( Exception e ) {
            Log.e( TAG, "Error writing to Excel file", e );
        }

        return null;
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
     * <p/>
     * <p/>
     * This function will iterate through every class installed to look for the
     * requested Student.
     *
     * @param ident The Student ID. See {@link Student} for more info.
     *
     * @return The requested {@link Student} or NULL.
     */
    public Student getStudentById( String ident ) {
        Student std = null;

        for ( String s : installedGroups.keySet() ) {
            std = getStudentById( s, ident );
            if ( std != null )
                break;
        }

        return std;
    }

    /**
     * Find a {@link Student} instances with the specified ID in a certain
     * class. If the student is not found in the class, NULL will be returned.
     * <p/>
     * <p/>
     * If any of the parameters is null, null will be returned.
     *
     * @param groupName The group to look for a student in.
     * @param ident     The unique ID of the student.
     *
     * @return The {@link Student} instance, or NULL.
     */
    public Student getStudentById( String groupName, String ident ) {
        if ( groupName == null || ident == null )
            return null;

        // if ( !installedGroups.containsKey( groupName )) return null;

        Group group = installedGroups.get( groupName );
        assert group != null;
        return group.getStudentByID( ident );
    }

    /**
     * Updates a student. The <tt>oldIdent</tt> parameter MUST be the original
     * ID of the student, otherwise this function will fail. This only applies
     * if the student ID itself is modified. If not, this parameter may be null.
     * <p/>
     * <p/>
     *
     * @param std      The {@link Student} instance to update.
     * @param oldIdent The original ID of the student.
     *
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
        LinkedList<SubjectType> list = new LinkedList<>();

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
     * Gets a reference to all installed {@link SubjectType#TYPE_SUBJECT}
     */
    public Collection<SubjectType> getSubjects() {
        return installedSubjects.values();
    }

    /**
     * @return A collection of all known installed {@link SubjectType#TYPE_THEME}
     */
    public Collection<SubjectType> getTypes() {
        return installedThemes.values();
    }

    /**
     * @return A collection of all known subject names
     */
    public Collection<String> getSubjectNames() {
        return installedSubjects.keySet();
    }

    /**
     * @return A collection of all known subject themes
     */
    public Collection<String> getTypeNames() {
        return installedThemes.keySet();
    }

    /**
     * Get the {@link SubjectType} instance with the specified ID. If the ID is
     * not found, an {@link IllegalStateException} is thrown.
     *
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
     * <p/>
     * TODO: Remove?
     */
    public SubjectType createSubjectType() {
        return new SubjectTypeBean();
    }

    /**
     * Adds a new SubjectType to the system. The new {@link SubjectType} will be
     * installed in the DataBase and any listeners for the change in
     * subjecttypes will be called.
     * <p/>
     * TODO: Remove?
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
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

            assert subType != null;
            subType.put( st.getName(), st );
        }

        return success;
    }

    /**
     * @param st Verify that the subject type
     *
     * @return false
     * <p/>
     * TODO: 06.04.2016 Remove?
     */
    public boolean checkSubjectType( SubjectType st ) {
        String name = st.getName();

        if ( name == null || name.length() == 0 )
            return false;

        return !installedSubjects.containsKey( name ) && !installedThemes.containsKey( name );

    }

    /**
     * Deletes a specified <code>SubjectType</code>
     *
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
     *
     * @return -1 if the subject is not found.
     */
    public int convertSubjectToID( String name ) {
        return convertSubjectTypeToID( installedSubjects, name );
    }

    /**
     * Converts the name of a SubjectType to its corresponding ID in the database.
     *
     * @param name Name of the subject type
     *
     * @return The ID of the named subject type
     */
    public int convertTypeToID( String name ) {
        return convertSubjectTypeToID( installedThemes, name );
    }

    /**
     * Get the ID of a specified {@link SubjectType} name. The ID is used in the
     * database.
     *
     * @param map  The map to look for the <code>SubjectType</code> in.
     * @param name Name of the <code>SubjectType</code>
     *
     * @return The ID or -1 if not found
     */
    private int convertSubjectTypeToID( Map<String, SubjectType> map, String name ) {
        if ( !map.containsKey( name ) )
            return -1;

        SubjectType st = map.get( name );
        return st.getID();
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
     * @return A List of names of all the Tasks loaded
     */
    public List<String> getTaskNames() {
        ArrayList<String> list = new ArrayList<>( installedTasks.size() );
        for ( Task t : installedTasks.values() ) {
            list.add( t.getName() );
        }

        return list;
    }

    /**
     * Finds any installedTasks that matches the flag. The flag must be one of
     * the states a {@link Task} may be in:
     * <tt>STATE_OPEN</tt>,
     * <tt>STATE_CLOSED</tt> or
     * <tt>STATE_EXPIRED</tt> or any combination of them.
     * <p/>
     * TODO: Needed?
     *
     * @return A List of every task in the system where the installedTasks state matches the flag
     */
    public List<String> getTaskNames( int flag ) {
        List<String> tasks = new ArrayList<>();

        for ( Task t : this.installedTasks.values() ) {
            int state = t.getState();
            if ( ( state & flag ) == Task.STATE_OPEN ) {
                tasks.add( t.getName() );
            }
            if ( ( state & flag ) == Task.STATE_CLOSED ) {
                tasks.add( t.getName() );
            }
            if ( ( state & flag ) == Task.STATE_EXPIRED ) {
                tasks.add( t.getName() );
            }
        }

        return tasks;
    }

    /**
     * TODO: Needed?
     *
     * @return Any {@link Task} loaded by the system.
     */
    public List<Task> getInstalledTasks() {
        return new ArrayList<>( installedTasks.values() );
    }

    /**
     * Finds any installedTasks that matches the flag. The flag must be one of
     * the states a {@link Task} may be in:
     * <tt>STATE_OPEN</tt>,
     * <tt>STATE_CLOSED</tt> or
     * <tt>STATE_EXPIRED</tt> or any combination of them.
     */
    public List<Task> getTasks( int flag ) {
        List<Task> ts = new LinkedList<>();

        for ( Task t : installedTasks.values() ) {
            int state = t.getState();
            boolean add = false;
            if ( ( flag & state ) == Task.STATE_OPEN )
                add = true;
            if ( ( flag & state ) == Task.STATE_CLOSED )
                add = true;
            if ( ( flag & state ) == Task.STATE_EXPIRED )
                add = true;

            if ( add )
                ts.add( t );
        }

        return ts;
    }

    /**
     * @param name Name of {@link Task} to find.
     *
     * @return The task, or NULL if not found
     */
    public Task getTask( String name ) {
        for ( Task t : installedTasks.values() ) {
            if ( t.getName().equalsIgnoreCase( name ) ) {
                return t;
            }
        }

        return null;
    }

    public Task getTask( Integer id ) {
        return installedTasks.get( id );
    }

    /**
     * Adds the task to the database. The task will be filled with instances of
     * {@link Assignment} objects linking the {@link Student} to the
     * {@link Task}.
     * <p/>
     * <p/>
     * Remember to call the {@link DataHandler#updateTask(Task, Integer)} after
     * calling this!
     *
     * @param task The task to add
     *
     * @throws NullPointerException if name of task is null TODO: Necessary?
     */
    public DataHandler addTask( @NonNull Task task ) {
        if ( task.getName() == null )
            throw new NullPointerException( "Name of task cannot be NULL!" );

        if ( db.insertTask( task ) ) {
            List<Assignment> stds = db.loadStudentsInTask( task );
            setupAssignmentsForTask( task, stds );
            task.addAssignments( stds );

            installedTasks.put( task.getID(), task );
        }

        return this;
    }

    /**
     * TODO: Should this be done somewhere else?
     *
     * @return How to display the name of the task.
     */
    public String getTaskDisplayName( Task task ) {
        String name = task.getName();
        if ( name == null || name.length() == 0 ) {
            name = getSubjectType( task.getSubject() ).getName();
        }

        return name;
    }

    /**
     * A student is handing in an assignment.
     * <p/>
     * TODO: Is this handled correct?
     *
     * @param t          The task handling the student
     * @param assignment The assignment handed in
     */
    public void handInTask( Task t, Assignment assignment ) {
        if ( !assignment.isHandedIn() )
            t.handIn( assignment.getIdent() );
        else
            t.handIn( assignment.getIdent(), Task.HANDIN_CANCEL );

        notifyTaskChange( t, OnDataChangeListener.MODE_UPD );
    }

    /**
     * @param task  The task that is updated.
     * @param oldID The old task ID if any
     *
     * @throws DBException if an error occurs
     */
    public DataHandler updateTask( Task task, Integer oldID ) {
        Log.d( TAG, "Updating task: " + task.getName() );

        if ( !db.updateTask( task ) )
            throw new DBException( "Failed to update Task: " + task.getName() );

        if ( oldID != null ) {
            installedTasks.remove( oldID );
            installedTasks.put( task.getID(), task );
        }

        return this;
    }

    /**
     * Deletes a {@link Task} from the systems DB. After deletion, the system
     * will notify through the {@link OnDataChangeListener} with
     * the flag {@link OnDataChangeListener#MODE_DEL}.
     * <p/>
     * <p/>
     * Any students engaged in this task, will automatically be deleted.
     *
     * @param task The {@link Task} instance to delete.
     *
     * @return <tt>TRUE</tt> if successful.
     */
    public boolean deleteTask( Task task ) {
        if ( db.deleteTask( task ) ) {
            db.removeAssignments( task.getAssignmentList() );

            installedTasks.remove( task.getID() );
            notifyTaskDelete( task );
            return true;
        }

        return false;
    }

    /**
     * @param task The task to close. Not deleted
     *
     * @return true if successfully clossed
     */
    public boolean closeTask( Task task ) {
        task.setState( Task.STATE_CLOSED );

        boolean success = db.updateTask( task );
        notifyTaskChange( task, OnChange.MODE_CLS );

        return success;
    }

    /**
     * Commits a {@link Task} to the DB.
     */
    public void commitTask( Task task ) {
        db.insertTask( task );
    }

    /**
     * @param task The task containing Students to be written to the database
     */
    public void commitStudentsTasks( Task task ) {
        List<Assignment> list = task.getUpdatedStudents();
        if ( list != null && !list.isEmpty() ) {
            db.updateStudentTasks( list );
        }

        list = task.getRemovedAssignments();
        if ( list != null && !list.isEmpty() ) {
            db.removeAssignments( list );
        }

        list = task.getAddedStudents();
        if ( list != null && !list.isEmpty() ) {
            db.insertStudentTasks( list );
        }

        task.notifyChange();
        task.markAsCommitted();
    }

    /**
     * Commits all tasks loaded to the database.
     */
    public void commitTasks() {
        for ( Task task : installedTasks.values() ) {
            commitTask( task );
        }
    }

    /**
     * @param task The Task that has changes (add, remove)
     */
    private void notifyTaskChange( Task task, int mode ) {
        if ( onDataChangeListeners.isEmpty() )
            return;

        for ( OnDataChangeListener onDataChangeListener : onDataChangeListeners )
            onDataChangeListener.onDataChange( mode, OnDataChangeListener.DATA_TASK );
    }

    /**
     * @param newTask Notify that a new task is added to the system
     */
    public void notifyTaskAdd( Task newTask ) {
        notifyTaskChange( newTask, OnDataChangeListener.MODE_ADD );
    }

    /**
     * @param oldTask Notify that a task has been deleted
     */
    public void notifyTaskDelete( Task oldTask ) {
        notifyTaskChange( oldTask, OnDataChangeListener.MODE_DEL );
    }

    /**
     * @param task Notify that a task has been updated
     */
    public void notifyTaskUpdate( Task task ) {
        notifyTaskChange( task, OnDataChangeListener.MODE_UPD );
    }

    /**
     * @param listener to register
     */
    public void registerOnDataChangeListener( OnDataChangeListener listener ) {
        unregisterOnDataChangeListener( listener );
        onDataChangeListeners.add( listener );
    }

    /**
     * @param listener to unregister
     */
    public void unregisterOnDataChangeListener( OnDataChangeListener listener ) {
        if ( onDataChangeListeners.contains( listener ) )
            onDataChangeListeners.remove( listener );
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
     * Checks to see if a {@link Group} is deletable. If the <code>Group</code>
     * is involved in a specific, and open Task, the Group cannot be
     * deleted.
     *
     * @param group The {@link Group} to check.
     *
     * @return true if removable
     */
    public boolean isGroupRemovable( Group group ) {
        boolean deletable = true;

        for ( Task t : installedTasks.values() ) {
            if ( t.getGroups().contains( group.getName() ) ) {
                deletable = false;
            }
        }

        return deletable;
    }

    /**
     * Returns a list of strings containing the names of all the installedTasks
     * the {@link Group} is involved in.
     *
     * @return A list of names, or an empty list
     */
    public List<String> getGroupInvolvedInTask( Group group ) {
        LinkedList<String> list = new LinkedList<>();

        for ( Task t : installedTasks.values() ) {
            if ( t.getGroups().contains( group.getName() ) ) {
                list.add( t.getName() );
            }
        }

        return list;
    }

    /**
     * @return List of all known group names
     */
    public List<String> getInstalledGroupNames() {
        return new ArrayList<>( installedGroups.keySet() );
    }

    /**
     * @param name Name of <code>Group</code> to look for
     *
     * @return The <code>Group</code> or null if not found.
     */
    public Group getGroup( String name ) {
        Group g = installedGroups.get( name );

        if ( g == null ) Log.e( TAG, "getGroup( " + name + " ): was NULL" );

        return g;
    }

    /**
     * @param group The <code>Group</code> to add to the database.
     */
    public void addGroup( Group group ) {
        db.insertGroup( group );
        installedGroups.put( group.getName(), group );
    }

    /**
     * @param name Name of group to remove
     *
     * @return This instance
     */
    public DataHandler removeGroup( String name ) {
        if ( !installedGroups.containsKey( name ) || isGroupInTask( name ) )
            return this;

        Group group = installedGroups.remove( name );
        db.removeGroup( group );

        return this;
    }

    /**
     * @param name Name of group to check to see if it has any tasks pending TODO: Correct?
     */
    public boolean isGroupInTask( String name ) {
        for ( Task task : installedTasks.values() ) {
            if ( task.getGroups().contains( name ) )
                return true;
        }

        return false;
    }

    /**
     * @param group The group where a change has occurred.
     * @param mode  The type of change
     */
    private void notifyGroupChange( Group group, int mode ) {
        if ( onDataChangeListeners.isEmpty() )
            return;

        for ( OnDataChangeListener onDataChangeListener : onDataChangeListeners )
            onDataChangeListener.onDataChange( mode, OnDataChangeListener.DATA_GROUP );
    }

    /**
     * @param group The group that has been added
     */
    public void notifyGroupAdd( Group group ) {
        notifyGroupChange( group, OnDataChangeListener.MODE_ADD );
    }

    /**
     * @param group The group removed
     */
    public void notifyGroupRemove( Group group ) {
        notifyGroupChange( group, OnDataChangeListener.MODE_DEL );
    }

    /**
     * Registers a listener for any event when there is a change to a Group. A change may
     * be adding, updating or removing a <code>Group</code>.
     *
     * @param listener The listener to register
     */
    public void registerOnGroupChangeListener( @NonNull OnDataChangeListener listener ) {
        if ( onDataChangeListeners.contains( listener ) )
            onDataChangeListeners.remove( listener );

        onDataChangeListeners.add( listener );
    }

    /**
     * Unregisters a group change listener
     *
     * @param listener The listener to unregister
     */
    public void unregisterOnGroupChangeListener( @NonNull OnDataChangeListener listener ) {
        onDataChangeListeners.remove( listener );
    }

    /**
     * @return List of available files in the download directory
     */
    public List<String> getFilesFromDownloadDir() {
        List<String> list = new ArrayList<>();

        File externalDir = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS );
        File[] files = externalDir.listFiles();
        for ( File f : files ) {
            if ( f.isFile() && f.getName().endsWith( GROUP_FILE_SUFFIX ) )
                list.add( f.getName() );
        }

        return list;
    }

    /**
     * Creates a new student identity. The identity is created by using the to last digits in the birth year, the
     * first three letters in the first name and the last four letters in the surname.
     *
     * @return A new student ID
     */
    static String CreateStudentID( Student bean ) {
        String fn = bean.getFirstName();
        if ( fn.length() >= 3 )
            fn = fn.substring( 0, 3 );

        String ln = bean.getLastName();
        if ( ln.length() >= 4 )
            ln = ln.substring( 0, 4 );

        String year = Utils.GetDateAsString( bean.getBirth() );
        year = year.substring( year.length() - 2, year.length() );

        // Build ID
        String ident = year + fn + ln;
        ident = ident.replace( 'æ', 'e' );
        ident = ident.replace( 'ø', 'o' );
        ident = ident.replace( 'å', 'a' );

        //Log.d( TAG, "Creating student ID: " + ident );
        return ident.toLowerCase( Locale.getDefault() );
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
     * Used as a callback by the {@link DataHandler} when there is a change to
     * the set of loaded installedTasks. Use the
     * {@link OnAssignmentChangeListener} to get
     * a callback for an specific {@link Task} instance.
     * <p/>
     * <p/>
     * Called when a task is added, deleted og closed, or opened.
     *
     * @author GleVoll
     *         TODO: Should handle any datachange to the Datahandler: OnChange#mode
     */
    public interface OnDataChangeListener extends OnChange {

        int DATA_TASK = 0x100;
        int DATA_GROUP = 0x101;
        int DATA_SUBTYPE = 0x102;

        /**
         * @param mode
         */
        void onDataChange( int mode, int dataType );
    }

}
