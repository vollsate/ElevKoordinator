package no.glv.elevko.app;

import java.util.Comparator;

import no.glv.elevko.intrfc.Assignment;
import no.glv.elevko.intrfc.Group;
import no.glv.elevko.intrfc.Student;
import no.glv.elevko.intrfc.Task;

/**
 * A utility class that will sort {@link Task}, {@link Assignment} and
 * {@link Group} in various ways.
 * 
 * @author glevoll
 *
 */
public class DataComparator {

	public static final int SORT_FIRSTNAME_ASC = 0;
	public static final int SORT_LASTNAME_ASC = 1;

	public static final int SORT_IDENT_ASC = 2;
	public static final int SORT_IDENT_DSC = 3;

	public static final int SORT_TASKDATE_ASC = 100;
	public static final int SORT_TASKDATE_DSC = 101;

	public static final int SORT_TASKNAME_ASC = 102;
	public static final int SORT_TASKNAME_DSC = 103;

	private DataComparator() {
	}

	/**
	 * 
	 * @author GleVoll
	 *
	 */
	public static class StudentComparator implements Comparator<Student> {

		private int mode;

		public StudentComparator() {
			this( SORT_FIRSTNAME_ASC );
		}

		public StudentComparator( int mode ) {
			this.mode = mode;
		}

		@Override
		public int compare( Student lhs, Student rhs ) {
			switch ( mode ) {
				case SORT_FIRSTNAME_ASC:
					return lhs.getFirstName().compareToIgnoreCase( rhs.getFirstName() );

				case SORT_LASTNAME_ASC:
					return lhs.getLastName().compareToIgnoreCase( rhs.getLastName() );

				case SORT_IDENT_ASC:
					return lhs.getIdent().compareToIgnoreCase( rhs.getIdent() );

				case SORT_IDENT_DSC:
					return rhs.getIdent().compareToIgnoreCase( lhs.getIdent() );

			}

			return 0;
		}

	}

	/**
	 * 
	 * @author GleVoll
	 *
	 */
	public static class StudentTaskComparator implements Comparator<Assignment> {

		private int mode;

		public StudentTaskComparator() {
			this( SORT_IDENT_ASC );
		}

		public StudentTaskComparator( int mode ) {
			this.mode = mode;
		}

		@Override
		public int compare( Assignment lhs, Assignment rhs ) {
			switch ( mode ) {
				case SORT_IDENT_ASC:
					return lhs.getIdent().compareToIgnoreCase( rhs.getIdent() );

				case SORT_IDENT_DSC:
					return rhs.getIdent().compareToIgnoreCase( lhs.getIdent() );

			}

			return 0;
		}

	}

	/**
	 * 
	 * @author GleVoll
	 *
	 */
	public static class TaskComparator implements Comparator<Task> {

		private int mode;

		public TaskComparator() {
			this( SORT_TASKDATE_ASC );
		}

		public TaskComparator( int mode ) {
			this.mode = mode;
		}

		@Override
		public int compare( Task lhs, Task rhs ) {
			switch ( mode ) {
				case SORT_TASKDATE_ASC:
					return lhs.getDate().compareTo( rhs.getDate() );

				case SORT_TASKDATE_DSC:
					return rhs.getDate().compareTo( lhs.getDate() );

				case SORT_TASKNAME_ASC:
					return lhs.getName().compareTo( rhs.getName() );

				case SORT_TASKNAME_DSC:
					return rhs.getName().compareTo( lhs.getName() );

			}

			return 0;
		}

	}
}
