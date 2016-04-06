package no.glv.elevko.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import no.glv.elevko.intrfc.Assignment;
import no.glv.elevko.intrfc.Group;
import no.glv.elevko.intrfc.Student;
import no.glv.elevko.intrfc.Task;

/**
 *
 */
public class TaskImpl implements Task {

    /** TaskImpl.java */
    private static final long serialVersionUID = -4612900329709063169L;
    private int _id;
    private String mName;
    private String mDesc;
    private Date mExpirationDate;
    private int mState;
    private int mSubject;
    private int mType;

    /** Flag to keep track of modification */
    private boolean mModified;

    /** Map of any modified student assignments */
    private TreeMap<String, Assignment> modifiedAssignments;

    /** Number of students in this task */
    private int mStdCount;

    /**
     * The map containing the students who has completed the task
     */
    private TreeMap<String, Assignment> assignmentsCompleted;

    /**
     * List of students who has not yet handed in their assignment
     */
    private TreeMap<String, Assignment> assignmentsPending;

    /** List of all groups in this task */
    private List<String> mGroups;

    /** List of listeners for change in this task */
    private List<OnAssignmentChangeListener> taskChangeListeners;

    /** List of students removed before commit to database */
    private List<Assignment> removedStudents;

    /** List of students added before commit to database */
    private List<Assignment> addedStudents;

    /** List of groups added before commit to database */
    private List<String> addedGroups;

    /** List of removed groups before commit to database */
    private List<String> removedGroups;

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
        mGroups = new ArrayList<>();
        assignmentsCompleted = new TreeMap<>();
        assignmentsPending = new TreeMap<>();

        mModified = false;
        modifiedAssignments = new TreeMap<>();
        removedStudents = new LinkedList<>();
        addedGroups = new LinkedList<>();
        removedGroups = new LinkedList<>();

        taskChangeListeners = new LinkedList<>();

        mState = STATE_OPEN;
    }

    @Override
    public void registerOnAssignmentChangeListener( OnAssignmentChangeListener listener ) {
        if ( taskChangeListeners.contains( listener ) )
            return;

        taskChangeListeners.add( listener );
    }

    @Override
    public void unregisterOnAssignmentChangeListener( OnAssignmentChangeListener listener ) {
        if ( !taskChangeListeners.contains( listener ) )
            return;

        taskChangeListeners.remove( listener );
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
            notifyChange( OnAssignmentChangeListener.STD_REMOVE );
        }

        if ( addedStudents.size() > 0 ) {
            notifyChange( OnAssignmentChangeListener.STD_ADD );
        }

        if ( modifiedAssignments.size() > 0 ) {
            notifyChange( OnAssignmentChangeListener.STD_UPDATE );
        }
    }

    /**
     *
     */
    public void notifyChange( int mode ) {
        for ( OnAssignmentChangeListener mTaskChangeListener : taskChangeListeners )
            mTaskChangeListener.onAssignmentChange( this, mode );

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
            case STATE_CLOSED:
                return "closed";

            case STATE_OPEN:
                return "open";

            case STATE_EXPIRED:
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
    public List<Assignment> getFinishedAssignments() {
        return new ArrayList<>( assignmentsCompleted.values() );
    }

    @Override
    public int getStudentsHandedInCount() {
        return assignmentsCompleted.size();
    }

    /**
     * Will call {@link TaskImpl#handIn(String, int)} with <code>Task#HANDIN_PROPER</code> mode flag.
     */
    @Override
    public boolean handIn( String studentID ) {
        return handIn( studentID, HANDIN_PROPER );
    }

    @Override
    public boolean handIn( String studentID, int mode ) {
        Assignment assignment;

        switch ( mode ) {
            case HANDIN_PROPER:
                assignment = assignmentsPending.remove( studentID );
                if ( assignment == null )
                    return false;

                assignment.handIn();
                assignmentsCompleted.put( studentID, assignment );
                break;

            case HANDIN_CANCEL:
                assignment = assignmentsCompleted.remove( studentID );
                if ( assignment == null )
                    return false;

                assignment.handIn( Assignment.MODE_PENDING );
                assignmentsPending.put( studentID, assignment );
                break;

            default:
                return false;
        }

        markAsUpdated( assignment );
        notifyChange( OnAssignmentChangeListener.STD_HANDIN );
        return true;
    }

    /**
     * The updated assignment will be stored for later commit to database.
     *
     * @param assignment Mark the assignment as updated.
     */
    public void markAsUpdated( Assignment assignment ) {
        modifiedAssignments.put( assignment.getIdent(), assignment );
        mModified = true;
    }

    @Override
    public List<String> getGroups() {
        return new ArrayList<>( mGroups );
    }

    @Override
    public void addGroup( Group group ) {
        addGroupName( group.getName() );
        Iterator<Student> it = group.iterator();
        while ( it.hasNext() ) {
            addStudent( it.next() );
        }
    }

    @Override
    public void addGroupName( String group ) {
        if ( mGroups.contains( group ) )
            return;

        mGroups.add( group );
        addedGroups.add( group );

        mModified = true;
    }

    @Override
    public void setName( String name ) {
        mName = name;
        mModified = true;
    }

    /**
     * Removes both the class and every student linked to this class.
     * <p/>
     * Will not remove students who has already handed in their assignment
     */
    @Override
    public void removeGroup( Group group ) {
        if ( !mGroups.contains( group.getName() ) )
            return;

        mGroups.remove( group.getName() );

        for ( Student student : group.getStudents() ) {
            removeStudent( student.getIdent() );
        }
    }

    @Override
    public boolean removeStudent( String ident ) {
        if ( !assignmentsPending.containsKey( ident ) )
            return false;

        if ( removedStudents == null )
            removedStudents = new LinkedList<>();
        removedStudents.add( assignmentsPending.remove( ident ) );

        mStdCount--;
        mModified = true;
        return true;
    }

    @Override
    public List<Assignment> getRemovedAssignments() {
        return removedStudents;
    }

    @Override
    public List<String> getAddedGroups() {
        return addedGroups;
    }

    @Override
    public List<String> getRemovedGroups() {
        return removedGroups;
    }

    /**
     * Will only add to students pending completion of task.
     *
     * @return true if the students gets added successfully, false if not added correctly or if the student has
     * already handed in the assignment
     */
    @Override
    public boolean addStudent( Student std ) {
        if ( std == null )
            return false;
        if ( hasStudent( std.getIdent() ) )
            return false;

        Assignment stdTask = new AssignmentBean( std, mName, null /* no handinDate */ );
        stdTask.setTaskID( this._id );

        // Make sure to update the removed students, if the user makes a mistake
        // ..
        if ( removedStudents.contains( std.getIdent() ) )
            removedStudents.remove( std.getIdent() );

        return addAssignment( stdTask );
    }

    /**
     * @param assignment The assignment to add
     *
     * @return true if added
     */
    public boolean addAssignment( Assignment assignment ) {
        if ( assignment == null )
            return false;
        if ( hasStudent( assignment.getIdent() ) )
            return false;

        if ( assignment.getMode() == HANDIN_PROPER )
            assignmentsCompleted.put( assignment.getIdent(), assignment );
        else
            assignmentsPending.put( assignment.getIdent(), assignment );

        if ( addedStudents == null )
            addedStudents = new LinkedList<>();
        addedStudents.add( assignment );

//		notifyChange();

        mModified = true;
        mStdCount++;
        return true;
    }

    @Override
    public List<Assignment> getAddedStudents() {
        return addedStudents;
    }

    @Override
    public boolean hasGroup( String groupName ) {
        return mGroups.contains( groupName );
    }

    /**
     * @return true if a student is assigned this task
     */
    boolean hasStudent( List<Assignment> list, String studentID ) {
        for ( Assignment assignment : list ) {
            if ( assignment.getIdent().equals( studentID ) )
                return true;
        }

        return false;
    }

    @Override
    public boolean hasStudent( String id ) {
        if ( assignmentsCompleted.containsKey( id ) )
            return true;
        return assignmentsPending.containsKey( id );

    }

    @Override
    public List<String> getStudentNames() {
        List<String> stds = new LinkedList<>();

        stds.addAll( assignmentsCompleted.keySet() );
        stds.addAll( assignmentsPending.keySet() );

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
    public List<Assignment> getPendingAssignments() {
        return new ArrayList<>( assignmentsPending.values() );
    }

    public int getAssignmentsPendingCount() {
        return assignmentsPending.size();
    }

    @Override
    public void setDate( Date date ) {
        this.mExpirationDate = date;
        mModified = true;
    }

    @Override
    public List<Assignment> getAssignmentList() {
        List<Assignment> list = new ArrayList<>();

        list.addAll( assignmentsCompleted.values() );
        list.addAll( assignmentsPending.values() );

        return list;
    }

    @Override
    public void addStudents( List<Student> students ) {
        for ( Student student : students ) {
            addStudent( student );
        }
    }

    @Override
    public void addAssignments( List<Assignment> assignments ) {
        for ( Assignment assignment : assignments ) {
            addAssignment( assignment );
        }
    }

    @Override
    public boolean isModified() {
        return mModified;
    }

    @Override
    public void markAsCommitted() {
        if ( modifiedAssignments != null )
            modifiedAssignments.clear();
        if ( removedStudents != null )
            removedStudents.clear();
        if ( addedStudents != null )
            addedStudents.clear();

        addedGroups.clear();
        removedGroups.clear();

        mModified = false;
    }

    @Override
    public List<Assignment> getUpdatedStudents() {
        return new LinkedList<>( modifiedAssignments.values() );
    }

    @Override
    public boolean isExpired() {
        Calendar cal = Calendar.getInstance( Locale.getDefault() );

        int year = cal.get( Calendar.YEAR );
        int month = cal.get( Calendar.MONTH );
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
