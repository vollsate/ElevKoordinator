package no.glv.elevko.sql;

import java.util.Date;

import no.glv.elevko.intrfc.Student;
import no.glv.elevko.intrfc.StudentTask;

/**
 * Keeps track of every student registered and every task they are currently
 * engaged in.
 * 
 * @author GleVoll
 *
 */
public class StudentTaskImpl implements StudentTask {

	private Date handinDate;
	private int mode;

	private int ID;
	private String mTask;
	private String mIdent;
	private Student student;

	private String mComment;

	private int taskID;

	/**
	 * 
	 * @param mIdent
	 * @param taskName
	 * @param date
	 */
	public StudentTaskImpl( String student, String task, Date date ) {
		this( student, task, MODE_PENDING, date );
	}

	/**
	 * 
	 * @param mIdent
	 * @param taskName
	 * @param mode
	 * @param date
	 */
	public StudentTaskImpl( String ident, String task, int mode, Date date ) {
		this( ident, -1, mode, date );
		this.mTask = task;
	}

	/**
	 * 
	 * @param ident
	 * @param task
	 * @param mode
	 * @param date
	 */
	public StudentTaskImpl( String ident, int task, int mode, Date date ) {
		this.mIdent = ident;
		this.taskID = task;
		this.mode = mode;
		this.handinDate = date;
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
	 * 
	 * @param id
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
	public StudentTaskImpl( Student student, String task, Date date ) {
		this( student.getIdent(), task, date );
		this.student = student;
	}

	/**
	 * 
	 * @param student
	 * @param task
	 * @param mode
	 * @param date
	 */
	public StudentTaskImpl( Student student, String task, int mode, Date date ) {
		this( student.getIdent(), task, mode, date );
		this.student = student;
	}

	public String getIdent() {
		return mIdent;
	}

	@Override
	public String getTaskName() {
		return mTask;
	}

	@Override
	public Date getHandInDate() {
		return handinDate;
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
				if ( handinDate == null )
					handinDate = new Date();
				break;

			case MODE_PENDING:
				handinDate = null;
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
		return ( handinDate != null );
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
		sb.append( "ident=" ).append( this.mIdent ).append( ", " );
		sb.append( "mode=" ).append( this.mode );

		return sb.toString();
	}

	public String toSimpleString() {
		return "_ID=" + ID + ", ident=" + mIdent;
	}
}
