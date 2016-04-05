package no.glv.elevko.intrfc;

import java.util.Iterator;
import java.util.List;

/**
 * This class holds all the student classes in the system.
 * 
 * @author GleVoll
 *
 */
public interface StudentClass {

	String EXTRA_STUDENTCLASS = BaseValues.EXTRA_BASEPARAM + StudentClass.class.getSimpleName();

	String getName();

	int getSize();

	Student getStudentByFirstName( String name );

	Student getStudentByIdent( String name );

	void add( Student std );

	void addAll( List<Student> list );

	void addAll( Student[] stds );

	Student[] toArray();

	Iterator<Student> iterator();

	List<Student> getStudents();

}
