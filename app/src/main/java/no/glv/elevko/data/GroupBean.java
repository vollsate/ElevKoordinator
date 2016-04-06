/**
 * 
 */
package no.glv.elevko.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import no.glv.elevko.intrfc.Group;
import no.glv.elevko.intrfc.Student;

/**
 * @author GleVoll
 *
 */
public class GroupBean implements Group {

	private String mName;

	private ArrayList<Student> students;

	/**
	 * 
	 */
	public GroupBean( String name ) {
		students = new ArrayList<Student>();

		mName = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.glv.elevko.core.Group#getName()
	 */
	@Override
	public String getName() {
		return mName;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see no.glv.elevko.app.StudentClass#getSize()
	 */
	@Override
	public int getSize() {
		return students.size();
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public Student getStudentByID( String name ) {
		Student bean = null;
		Iterator<Student> it = students.iterator();

		while ( it.hasNext() ) {
			bean = it.next();
			if ( bean.getIdent().equals( name ) )
				break;
			else
				bean = null;
		}

		return bean;
	}

	@Override
	public void add( Student std ) {
		students.add( std );
	}

	@Override
	public void addAll( List<Student> list ) {
		students.addAll( list );
	}

	@Override
	public Iterator<Student> iterator() {
		return students.iterator();
	}

	@Override
	public List<Student> getStudents() {
		return students;
	}

}
