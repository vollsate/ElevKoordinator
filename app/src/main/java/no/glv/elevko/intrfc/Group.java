package no.glv.elevko.intrfc;

import java.util.Iterator;
import java.util.List;

/**
 * This class holds all the students in a group (school class).
 *
 * <p>
 *     The most important fields:
 *     <ul>
 *         <li><b>id</b>: Row in database. Unique</li>
 *         <li><b>name</b>: Name of the class. This can be altered by the user</li>
 *         <li><b>year</b>: What year the students are born i default.
 *         The user may choose to alter this and use any other kind of marking.</li>
 *     </ul>
 * </p>
 *
 * @author GleVoll
 */
public interface Group {

    String EXTRA_GROUP = BaseValues.EXTRA_BASEPARAM + Group.class.getSimpleName();

    /**
     * @return The name of the group
     */
    String getName();

    void setName( String name );

    String getYear();

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

    /**
     * @return An iterator for easy running through the registered students.
     */
    Iterator<Student> iterator();

    /**
     * @return A list of students registered to this group.
     */
    List<Student> getStudents();

    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------
    //
    // Listener
    //
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    /**
     * @author GleVoll
     */
    public interface OnGroupChangeListener extends OnChange {

        void onGroupChange( Group group, int mode );
    }


}
