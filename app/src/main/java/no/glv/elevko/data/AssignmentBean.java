package no.glv.elevko.data;

import java.util.Date;

import no.glv.elevko.intrfc.Assignment;
import no.glv.elevko.intrfc.Student;

/**
 * Keeps track of every student registered and every task they are currently
 * engaged in.
 *
 * @author GleVoll
 */
public class AssignmentBean implements Assignment {

    private Date completionDate;
    private int mode;

    private int ID;
    private String mTask;
    private String mStudentID;
    private Student student;

    private String mComment;

    private int taskID;

    /**
     *
     */
    public AssignmentBean( String studentID, String task, Date date ) {
        this( studentID, task, MODE_PENDING, date );
    }

    /**
     *
     */
    public AssignmentBean( String studentID, String task, int mode, Date date ) {
        this( studentID, -1, mode, date );
        this.mTask = task;
    }

    /**
     *
     */
    public AssignmentBean( String studentID, int task, int mode, Date date ) {
        this.mStudentID = studentID;
        this.taskID = task;
        this.mode = mode;
        this.completionDate = date;
    }

    @Override
    public String getComment() {
        return mComment;
    }

    @Override
    public void setComment( String comment ) {
        mComment = comment;
    }

    @Override
    public int getTaskID() {
        return taskID;
    }

    @Override
    public void setTaskID( int id ) {
        this.taskID = id;
    }

    @Override
    public int getID() {
        return ID;
    }

    /**
     * Set the PK from the database
     */
    public void setID( int id ) {
        this.ID = id;
    }

    /**
     *
     * @param student
     * @param task
     * @param date
     */
    public AssignmentBean( Student student, String task, Date date ) {
        this( student.getIdent(), task, date );
        this.student = student;
    }

    /**
     *
     */
    public AssignmentBean( Student student, String task, int mode, Date date ) {
        this( student.getIdent(), task, mode, date );
        this.student = student;
    }

    public String getIdent() {
        return mStudentID;
    }

    @Override
    public String getTaskName() {
        return mTask;
    }

    @Override
    public Date getHandInDate() {
        return completionDate;
    }

    @Override
    public void handIn() {
        handIn( MODE_HANDIN );
    }

    @Override
    public void handIn( int mode ) {
        this.mode = mode;

        switch ( mode ) {
            case MODE_HANDIN:
                if ( completionDate == null )
                    completionDate = new Date();
                break;

            case MODE_PENDING:
                completionDate = null;
                break;

            default:
                break;
        }
    }

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public String getModeAsString() {
        switch ( mode ) {
            case MODE_EXPIRED:
                return "utgått";

            case MODE_HANDIN:
                return "levert";

            case MODE_LATE:
                return "for sen levering";

            case MODE_PENDING:
                return "venter ..";

            default:
                return "";
        }
    }

    @Override
    public boolean isHandedIn() {
        return ( completionDate != null );
    }

    @Override
    public Student getStudent() {
        return student;
    }

    @Override
    public void setStudent( Student std ) {
        this.student = std;
    }

    @Override
    public void setTaskName( String name ) {
        this.mTask = name;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append( "_ID=" ).append( this.ID ).append( ", " );
        sb.append( "task=" ).append( this.mTask ).append( ", " );
        sb.append( "ID=" ).append( this.mStudentID ).append( ", " );
        sb.append( "mode=" ).append( this.mode );

        return sb.toString();
    }

    public String toSimpleString() {
        return "_ID=" + ID + ", ID=" + mStudentID;
    }
}
