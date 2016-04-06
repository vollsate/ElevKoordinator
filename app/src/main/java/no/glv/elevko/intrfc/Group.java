package no.glv.elevko.intrfc;

import java.util.Iterator;
import java.util.List;

/**
 * This class holds all the students in a group (school class).
 * 
 * @author GleVoll
 *
 */
public interface Group {

	String EXTRA_GROUP = BaseValues.EXTRA_BASEPARAM + Group.class.getSimpleName();

	/**
	 * @return The name of the group
	 */
	String getName();

	/**
	 * @return The size, number of students, in this group.
	 */
	int getSize();

	/**
	 * @param name The unique student ID
	 * @return The student corresponding to the ID. May return null if not found.
	 */
	Student getStudentByID( String name );

	/**
	 * Add a student to the group.
	 */
	void add( Student std );

	/**
	 * Add all students in the list to the group
	 */
	void addAll( List<Student> list );

	Iterator<Student> iterator();

	List<Student> getStudents();

    /**
     * @author GleVoll
     */
    public interface OnGroupChangeListener extends OnChange {

        void onGroupChange( Group group, int mode );
    }


}
