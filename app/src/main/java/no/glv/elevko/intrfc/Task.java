package no.glv.elevko.intrfc;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import no.glv.elevko.app.DataHandler;

/**
 * This interface supports different types of tasks. A task must have a name and
 * an expiration date. Any classes that is connected to a task, will
 * automatically implement the task for every student in that task. A student
 * MUST be assigned to a task through a class, but certain students may be
 * removed individually from a task later.
 * <p/>
 * A student may be removed individually from a task calling the
 * <tt>removeStudent</tt> method.
 * <p/>
 * <p/>
 * Any changes to the task will result in calling any listeners registered in
 * the task. A listener will be called when the following events occur:
 * <blockquote>
 * <ul compact>
 * <li>A student is added
 * <li>A student is updated
 * <li>A student is removed
 * </ul>
 * <p/>
 * <ul>
 * <li>A class is added
 * <li>A class is removed
 * <li>A class is updated
 * </ul>
 * </blockquote>
 * <p/>
 * <p/>
 * A listener will also be called when the task itself is updated: <blockquote>
 * <ul>
 * <li>The date is changed
 * <li>The name is changed
 * </ul>
 * </blockquote>
 * <p/>
 * <p/>
 * A task is handed in (meaning the student is finished with the assignment hand
 * delivers the work to the teacher) using a <tt>mode</tt> parameter.
 * <p/>
 * <p/>
 * The hand in date (expiration date) for the task, is only concerned with the
 * day, not minutes or seconds. If a student hands in a task on the same day as
 * the expiration date, that is fine. Any hand ins later than that, will set a
 * flag in the students hand in date telling that the delivery was late.
 * <p/>
 * <p/>
 * The task has several states. The <tt>STATE_OPEN</tt> is the default
 * state, where it is accessible for students handing in work. A task can be in
 * the state of <tt>STATE_EXPIRED</tt>. In this state it is still
 * accessible for hand ins, but the student who hands in late will be flagged by
 * the task. The state <tt>STATE_EXPIRED</tt> will be set automatically by
 * the system, and will be set when the todays date is
 * <code>expirationDate + 1</code>.
 *
 * @author GleVoll
 */
public interface Task extends Serializable {

    /**
     * The normal flag for completing a task in due time.
     */
    int HANDIN_PROPER = 0;
    /**
     * The flag for students who hands in after the tasks expiration date.
     */
    int HANDIN_LATE = 1;
    /**
     * The student has been sick, and therefore hands in late
     */
    int HANDIN_SICK = 128;
    /**
     * The student has been away for some reason, and therefore hands in late
     */
    int HANDIN_AWAY = 256;
    /**
     * The hand in is canceled. May be done by the teacher later on.
     */
    int HANDIN_CANCEL = 512;

    /**
     * The task is open for handing in. This is the default state
     */
    int STATE_OPEN = 1;
    /**
     * The task is closed for hand in
     */
    int STATE_CLOSED = 2;
    /**
     * The task has expired, but still accepts hand ins
     */
    int STATE_EXPIRED = 4;

    /**
     * The name of the extra parameter when stored on instance save
     */
    String EXTRA_TASKNAME = BaseValues.EXTRA_BASEPARAM + "task";

    /**
     * @return The task ID
     */
    int getID();

    /**
     * @return The name of the task
     */
    String getName();

    /**
     * Sets the name of the task
     */
    void setName( String name );

    /**
     * @return The hand in date when the assignment is due
     */
    Date getDate();

    /**
     * Sets the new expiration date for this task.
     */
    void setDate( Date date );

    /**
     * @return The current state of the task
     */
    int getState();

    /**
     * @return The assigment date formatted as a String
     */
    String getStateAsString();

    /**
     * @param newState The new state of the task
     */
    void setState( int newState );

    /**
     * @return The tasks description. May be null
     */
    String getDesciption();

    /**
     * @param desc The tasks description. May be null
     */
    void setDescription( String desc );

    /**
     * Get the subject this class is related to
     */
    int getSubject();

    void setSubject( int subject );

    int getType();

    void setType( int type );

    /**
     * @return The number of Student who has handed in this task
     */
    int getStudentsHandedInCount();

    /**
     * A student is handing in the task. This is the default hand in method. If
     * the student is on time, the hand in will be flagged as
     * <tt>HANDIN_PROPER</tt>. If the hand in is overdue, the flag
     * <tt>HANDIN_LATE</tt> is set on the student.
     *
     * @param ident The unique identifier of a {@link Student}
     *
     * @return <code>true</code> if handed in successfully.
     */
    boolean handIn( String ident );

    /**
     * Hand in a students task with a specific mode. Any listener on the task
     * will be called. The possible modes of hand in are:
     * <ul>
     * <li>Task{@link #HANDIN_PROPER}
     * <li>Task{@link #HANDIN_CANCEL}
     * </ul>
     * <p/>
     * <p/>
     * The system also has other hand in modes, but these are not yet
     * implemented.
     *
     * @param ident The student ID.
     * @param mode  The mode.
     *
     * @return <code>true</code> if handed in successfully.
     */
    boolean handIn( String ident, int mode );

    /**
     * Checks to see if there are any changes to the task. A modification occurs
     * when a student hands in, is added or is removed.
     *
     * @return <code>true</code> if any of the students linked to this task is
     * modified.
     */
    boolean isModified();

    /**
     * Resets the task after a DB commit.
     * <p/>
     * <p/>
     * Must only be called after the task has been committed to the database.
     * Any history the task contains for keeping track with adding, removal and
     * updating is lost when this is called.
     */
    void markAsCommitted();

    /**
     * Mark a certain {@link Assignment} instance as updated. This method will
     * force a store in the DB if commited through the {@link no.glv.elevko.app.DataHandler}.
     */
    void markAsUpdated( Assignment st );

    /**
     * This list will contain only students who has recently been added to the
     * task. Any students already registered to the task will not be contained
     * within this list.
     * <p/>
     * <p/>
     * Make sure to use the <tt>DataHandler.commitTask</tt> to commit the task
     * to the database.
     *
     * @return A list of {@link Assignment} instances that has been added to
     * the task after its latest commit.
     */
    List<Assignment> getAddedStudents();

    /**
     * This list will contain only the students who has been updated since the
     * last commit to the database.
     *
     * @return A list of newly updated students.
     */
    List<Assignment> getUpdatedStudents();

    /**
     * This list will only contain the newly added classes. Any classes
     * committed to the database will not be contained in this list.
     *
     * @return List of classes related to this task
     */
    List<String> getAddedGroups();

    /**
     * This list will only contain the classes that has recently been removed
     * from the task.
     * <p/>
     * TODO: Needed?
     *
     * @return List
     */
    List<String> getRemovedGroups();

    /**
     * This list will not differentiate in the hand in state of the student.
     * Both <tt>HANDIN_PROPER</tt> and <tt>HANDIN_LATE</tt> will be returned here.
     *
     * @return A list of student IDs who has handed in the assignment.
     */
    List<Assignment> getFinishedAssignments();

    /**
     * The list of students pending to hand in. This list will not differentiate
     * on the state of the student.
     *
     * @return List
     */
    List<Assignment> getPendingAssignments();

    /**
     * @return How many students has not yet handed in the task.
     */
    int getAssignmentsPendingCount();

    /**
     * This will return the current value, even if the task is not yet
     * committed.
     *
     * @return How many students are involved in this task
     */
    int getStudentCount();

    /**
     * @return A list of the current classes registered with this task.
     */
    List<String> getGroups();

    /**
     * Adds a {@link Group} to the task. Every {@link Student} in this
     * class will be automatically assigned to the task, unless some of them are
     * removed.
     * <p/>
     * <p/>
     * A reference to the {@link Group} is not held within the Task.
     *
     * @param group The class to add
     */
    void addGroup( Group group );

    /**
     * Adds a class by name
     *
     * @param group Name of class to add
     */
    void addGroupName( String group );

    /**
     * Removes an entire class from the task. Any student registered within this
     * class, will be automatically removed from the task. Any students that has
     * handed in the task, will also be removed.
     * <p/>
     * <p/>
     * This cannot be undone after a commit!
     *
     * @param group The class to remove
     */
    void removeGroup( Group group );

    /**
     * This method will only remove the student from the ones who have not
     * handed in the assignment (task).
     * <p/>
     * <p/>
     * The task will be in the state of <tt>modified</tt> and the removed
     * student will be moved to the tasks history as an removed student. Commit
     * the change to the database before calling <tt>markAsCommitted</tt>
     *
     * @param ident The identity of the Student
     *
     * @return true if the Student was removed
     */
    boolean removeStudent( String ident );

    /**
     * Adds a {@link Student} to the task. The student will be set to the state
     * of <tt>PENDING</tt>
     * <p/>
     * <p/>
     * The student will not be registered in the database. This must be done
     * through the {@link DataHandler}.
     *
     * @param ident The ID of the student.
     *
     * @return <code>true</code> if successfully added.
     */
    boolean addStudent( Student ident );

    /**
     * @param groupName Name of group to check for.
     *
     * @return <code>true</code> if Task has this group registered.
     */
    boolean hasGroup( String groupName );

    /**
     * Checks to see if the task has a certain student registered within the
     * task. The task will check every student, and not differentiate between
     * different classes.
     *
     * @param ident The ID of the student.
     *
     * @return <code>true</code> if student is registered in task.
     */
    boolean hasStudent( String ident );

    /**
     * @return A list containing all the Student IDs registered in the group.
     */
    List<String> getStudentNames();

    /**
     * A list of all the students registered in the task. Will not differentiate
     * between different states of the student.
     *
     * @return List of every assignment registered to this task.
     */
    List<Assignment> getAssignmentList();

    /**
     * Adds a list of students to the task. Every {@link Student} in this list
     * will have it's own {@link Assignment} instance created.
     * <p/>
     * <p/>
     * The task must be committed to the DB.
     *
     * @param students List of students to add
     */
    void addStudents( List<Student> students );

    /**
     * Adds a list of already prepared {@link Assignment} instances. Usually as
     * a result of a database query.
     */
    void addAssignments( List<Assignment> assignments );

    /**
     * Checks to see if the task has expired. Returns true, only if the todays
     * date is <code>expirationDate + 1</code>.
     *
     * @return <code>true</code> if the task has expired.
     */
    boolean isExpired();

    void registerOnAssignmentChangeListener( OnAssignmentChangeListener listener );

    void unregisterOnAssignmentChangeListener( OnAssignmentChangeListener listener );

    void notifyChange( int mode );

    /**
     * A generic notify method. The task will try to find out what kind of change that has happened and to the
     * proper procedure.
     * <p/>
     * <p/>
     * This method will be helpful if there are more than one update to the task.
     */
    void notifyChange();

    /**
     * Gets a list of all the students removed from the task. Remember to commit
     * the task through the {@link DataHandler}.
     *
     * @return List
     */
    List<Assignment> getRemovedAssignments();

    /**
     * A callback interface used when a the following updates happen to the
     * task:
     * <p/>
     * <blockquote>
     * <ul>
     * <li>Date is changed
     * <li>Name is changed
     * <li>Description has changed
     * <li>A student is added, deleted, updated or hands in
     * <li>A class is added, deleted or updated
     * </ul>
     * </blockquote>
     *
     * @author glevoll
     */
    interface OnAssignmentChangeListener extends OnChange {

        // TODO: Convert this static properties to enumerations


        int DATE_CHANGE = 101;
        int NAME_CHANGE = 102;
        int DESC_CHANGE = 103;

        int STD_ADD = 104;
        int STD_REMOVE = 105;
        int STD_UPDATE = 106;
        int STD_HANDIN = 107;

        int GROUP_ADD = 108;
        int GROUP_REMOVE = 109;
        int GROUP_UPDATE = 110;

        /** Flag to indicate the list of students has been sorted */
        int SORT = 111;

        /**
         * A callback method to inform that a task has changed in's state
         * somehow. Triggered by a call to the {@link
         * Task#notifyChange(int)}
         *
         * @param task The task changed
         * @param mode The type of change
         */
        void onAssignmentChange( Task task, int mode );
    }

}
