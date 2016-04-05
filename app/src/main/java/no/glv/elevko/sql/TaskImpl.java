package no.glv.elevko.sql;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import no.glv.elevko.intrfc.Student;
import no.glv.elevko.intrfc.StudentClass;
import no.glv.elevko.intrfc.StudentTask;
import no.glv.elevko.intrfc.Task;

public class TaskImpl implements Task {

    /**
     * TaskImpl.java
     */
    private static final long serialVersionUID = -4612900329709063169L;
    private int _id;
    private String mName;
    private String mDesc;
    private Date mExpirationDate;
    private int mState;
    private int mSubject;
    private int mType;

    private boolean mModified;
    private TreeMap<String, StudentTask> mModifiedStudents;

    private int mStdCount;

    /**
     * The map containing the students who has finished the task
     */
    private TreeMap<String, StudentTask> studentsMapHandin;

    /**
     * List of students who has not yet handed in their assignment
     */
    private TreeMap<String, StudentTask> studentsMapPending;

    private List<String> mClasses;

    private LinkedList<OnTaskChangeListener> mTaskChangeListeners;

    private List<StudentTask> removedStudents;

    private List<StudentTask> addedStudents;

    private List<String> addedClasses;

    private List<String> removedClasses;

    /**
     * Package protected constructor.
     */
    TaskImpl() {
        this( -1 );
    }

    /**
     * @param id The unique ID of this task
     */
    TaskImpl( int id ) {
        this._id = id;
        mClasses = new ArrayList<String>();
        studentsMapHandin = new TreeMap<String, StudentTask>();
        studentsMapPending = new TreeMap<String, StudentTask>();

        mModified = false;
        mModifiedStudents = new TreeMap<String, StudentTask>();
        removedStudents = new LinkedList<StudentTask>();
        addedClasses = new LinkedList<String>();
        removedClasses = new LinkedList<String>();

        mTaskChangeListeners = new LinkedList<Task.OnTaskChangeListener>();

        mState = TASK_STATE_OPEN;
    }

    @Override
    public void addOnTaskChangeListener( OnTaskChangeListener listener ) {
        if ( mTaskChangeListeners.contains( listener ) )
            return;

        mTaskChangeListeners.add( listener );
    }

    @Override
    public void removeOnTaskChangeListener( OnTaskChangeListener listener ) {
        if ( !mTaskChangeListeners.contains( listener ) )
            return;

        mTaskChangeListeners.remove( listener );
    }

    /**
     * Will try to find out what kind of change:
     * <ul>
     * <li>Students removed</li>
     * <li>Students added</li>
     * <li>Students modified</li>
     * </ul>
     * <p>
     * <p>If there is a change to all these properties, all will be handled in the order above, by caling
     * Task#notifyChange(int)</p>
     */
    public void notifyChange() {
        if ( removedStudents.size() > 0 ) {
            notifyChange( OnTaskChangeListener.MODE_STD_DEL );
        }

        if ( addedStudents.size() > 0 ) {
            notifyChange( OnTaskChangeListener.MODE_STD_ADD );
        }

        if ( mModifiedStudents.size() > 0 ) {
            notifyChange( OnTaskChangeListener.MODE_STD_UPD );
        }
    }

    /**
     *
     */
    public void notifyChange( int mode ) {
        Iterator<OnTaskChangeListener> it = mTaskChangeListeners.iterator();
        while ( it.hasNext() )
            it.next().onTaskChange( this, mode );

    }

    @Override
    public int getID() {
        return _id;
    }

    void setID( int id ) {
        this._id = id;
        mModified = true;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Date getDate() {
        return mExpirationDate;
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public void setState( int type ) {
        this.mState = type;
        mModified = true;
    }

    @Override
    public String getStateAsString() {
        switch ( mState ) {
            case TASK_STATE_CLOSED:
                return "closed";

            case TASK_STATE_OPEN:
                return "open";

            case TASK_STATE_EXPIRED:
                return "expired";

            default:
                return "";
        }
    }

    @Override
    public int getSubject() {
        return mSubject;
    }

    @Override
    public int getType() {
        return mType;
    }

    public void setType( int type ) {
        this.mType = type;
    }

    public void setSubject( int subject ) {
        this.mSubject = subject;
    }

    @Override
    public List<StudentTask> getStudentsHandedIn() {
        return new ArrayList<StudentTask>( studentsMapHandin.values() );
    }

    @Override
    public int getStudentsHandedInCount() {
        return studentsMapHandin.size();
    }

    @Override
    public boolean handIn( String ident ) {
        return handIn( ident, HANDIN_DATE );
    }

    @Override
    public boolean handIn( String ident, int mode ) {
        StudentTask stdTask = null;

        switch ( mode ) {
            case HANDIN_DATE:
                stdTask = studentsMapPending.remove( ident );
                if ( stdTask == null )
                    return false;

                stdTask.handIn();
                studentsMapHandin.put( ident, stdTask );
                break;

            case HANDIN_CANCEL:
                stdTask = studentsMapHandin.remove( ident );
                if ( stdTask == null )
                    return false;

                stdTask.handIn( StudentTask.MODE_PENDING );
                studentsMapPending.put( ident, stdTask );
                break;

            default:
                return false;
        }

        markAsUpdated( stdTask );
        notifyChange( OnTaskChangeListener.MODE_STD_HANDIN );
        return true;
    }

    /**
     * @param st
     */
    public void markAsUpdated( StudentTask st ) {
        mModifiedStudents.put( st.getIdent(), st );
        mModified = true;
    }

    @Override
    public List<String> getClasses() {
        return new ArrayList<String>( mClasses );
    }

    @Override
    public void addClass( StudentClass stdClass ) {
        addClassName( stdClass.getName() );
        Iterator<Student> it = stdClass.iterator();
        while ( it.hasNext() ) {
            addStudent( it.next() );
        }
    }

    @Override
    public void addClassName( String stdClass ) {
        if ( mClasses.contains( stdClass ) )
            return;

        mClasses.add( stdClass );
        addedClasses.add( stdClass );

        mModified = true;
    }

    @Override
    public void setName( String name ) {
        mName = name;
        mModified = true;
    }

    /**
     * Removes both the class and every student linked to this class.
     * <p>
     * Will not remove students who has already handed in their assignment
     */
    @Override
    public void removeClass( StudentClass stdClass ) {
        if ( !mClasses.contains( stdClass.getName() ) )
            return;

        mClasses.remove( stdClass.getName() );

        Iterator<Student> it = stdClass.getStudents().iterator();
        while ( it.hasNext() ) {
            removeStudent( it.next().getIdent() );
        }
    }

    @Override
    public boolean removeStudent( String ident ) {
        if ( !studentsMapPending.containsKey( ident ) )
            return false;

        if ( removedStudents == null )
            removedStudents = new LinkedList<StudentTask>();
        removedStudents.add( studentsMapPending.remove( ident ) );

        mStdCount--;
        mModified = true;
        return true;
    }

    @Override
    public List<StudentTask> getRemovedStudents() {
        return removedStudents;
    }

    @Override
    public List<String> getAddedClasses() {
        return addedClasses;
    }

    @Override
    public List<String> getRemovedClasses() {
        return removedClasses;
    }

    /**
     * Will only add to students pending
     *
     * @return true if the students gets added successfully, false if not added
     * correctly or if the student has already handed in the assignment
     */
    @Override
    public boolean addStudent( Student std ) {
        if ( std == null )
            return false;
        if ( hasStudent( std.getIdent() ) )
            return false;

        StudentTask stdTask = new StudentTaskImpl( std, mName, null /*
                                                                     * no
																	 * handinDate
																	 */ );
        stdTask.setTaskID( this._id );

        // Make sure to update the removed students, if the user makes a mistake
        // ..
        if ( removedStudents.contains( std.getIdent() ) )
            removedStudents.remove( std.getIdent() );

        return addStudentTask( stdTask );
    }

    /**
     * @param stdTask
     * @return
     */
    public boolean addStudentTask( StudentTask stdTask ) {
        if ( stdTask == null )
            return false;
        if ( hasStudent( stdTask.getIdent() ) )
            return false;

        if ( stdTask.getMode() == HANDIN_DATE )
            studentsMapHandin.put( stdTask.getIdent(), stdTask );
        else
            studentsMapPending.put( stdTask.getIdent(), stdTask );

        if ( addedStudents == null )
            addedStudents = new LinkedList<StudentTask>();
        addedStudents.add( stdTask );

//		notifyChange();

        mModified = true;
        mStdCount++;
        return true;
    }

    @Override
    public List<StudentTask> getAddedStudents() {
        return addedStudents;
    }

    @Override
    public boolean hasClass( String className ) {
        return mClasses.contains( className );
    }

    /**
     * @param list
     * @param ident
     * @return true if
     */
    boolean hasStudent( List<StudentTask> list, String ident ) {
        Iterator<StudentTask> it = list.iterator();
        while ( it.hasNext() ) {
            if ( it.next().getIdent().equals( ident ) )
                return true;
        }

        return false;
    }

    @Override
    public boolean hasStudent( String ident ) {
        if ( studentsMapHandin.containsKey( ident ) )
            return true;
        return studentsMapPending.containsKey( ident );

    }

    @Override
    public List<String> getStudentNames() {
        List<String> stds = new LinkedList<String>();

        stds.addAll( studentsMapHandin.keySet() );
        stds.addAll( studentsMapPending.keySet() );

        return stds;
    }

    @Override
    public String getDesciption() {
        return mDesc;
    }

    @Override
    public void setDescription( String desc ) {
        mDesc = desc;
        mModified = true;
    }

    @Override
    public List<StudentTask> getStudentsPending() {
        return new ArrayList<StudentTask>( studentsMapPending.values() );
    }

    public int getStudentsPendingCount() {
        return studentsMapPending.size();
    }

    @Override
    public void setDate( Date date ) {
        this.mExpirationDate = date;
        mModified = true;
    }

    @Override
    public List<StudentTask> getStudentsInTask() {
        List<StudentTask> list = new ArrayList<StudentTask>();

        list.addAll( studentsMapHandin.values() );
        list.addAll( studentsMapPending.values() );

        return list;
    }

    @Override
    public void addStudents( List<Student> students ) {
        Iterator<Student> it = students.iterator();
        while ( it.hasNext() ) {
            addStudent( it.next() );
        }
    }

    @Override
    public void addStudentTasks( List<StudentTask> students ) {
        Iterator<StudentTask> it = students.iterator();
        while ( it.hasNext() ) {
            addStudentTask( it.next() );
        }
    }

    @Override
    public boolean isModified() {
        return mModified;
    }

    @Override
    public void markAsCommitted() {
        if ( mModifiedStudents != null )
            mModifiedStudents.clear();
        if ( removedStudents != null )
            removedStudents.clear();
        if ( addedStudents != null )
            addedStudents.clear();

        addedClasses.clear();
        removedClasses.clear();

        mModified = false;
    }

    @Override
    public List<StudentTask> getUpdatedStudents() {
        return new LinkedList<StudentTask>( mModifiedStudents.values() );
    }

    @Override
    public boolean isExpired() {
        Calendar cal = Calendar.getInstance( Locale.getDefault() );

        int year = cal.get( Calendar.YEAR );
        int month = cal.get( Calendar.MONDAY );
        int day = cal.get( Calendar.DAY_OF_MONTH );

        cal.set( year, month, day - 1 );
        Date todayPlussOne = cal.getTime();

        // return mExpirationDate.getTime() <= todayPlussOne.getTime();
        return mExpirationDate.before( todayPlussOne );
    }

    @Override
    public int getStudentCount() {
        return mStdCount;
    }

}
