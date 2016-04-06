package no.glv.elevko.intrfc;

import java.util.Date;

/**
 * Represents the bridge between the {@link Task} and the {@link Student}. A
 * Assignment will keep track of weather the student has executed the task, and
 * at what date the task was executed.
 * <p>
 * <p>
 * A comment may be useful if there are a specific message to remember about a
 * certain hand in.
 *
 * @author GleVoll
 */
public interface Assignment {

    /**
     * Hand in of a Task
     */
    int MODE_HANDIN = 0;
    /**
     * The task is not yet finished
     */
    int MODE_PENDING = 2;
    /**
     * The task has expired
     */
    int MODE_EXPIRED = 4;
    /**
     * The student handed in the task later than the expiration date
     */
    int MODE_LATE = 8;

    /**
     * Gets the comment related to this students task.
     */
    String getComment();

    /**
     * @param comment The comment to set on this students task.
     */
    void setComment( String comment );

    /**
     * PK in database
     */
    int getID();

    /**
     * Get the Student ID. This is the link to the {@link Student} instance that
     * is a member of this a task.
     */
    String getIdent();

    /**
     * The name of the task this Studenttask is a member of.
     */
    String getTaskName();

    Date getHandInDate();

    boolean isHandedIn();

    void handIn();

    void handIn( int mode );

    int getMode();

    String getModeAsString();

    Student getStudent();

    void setStudent( Student std );

    void setTaskName( String name );

    int getTaskID();

    void setTaskID( int id );

    String toSimpleString();

}
