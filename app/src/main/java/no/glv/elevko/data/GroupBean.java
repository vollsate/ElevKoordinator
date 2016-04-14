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

    private String name;

    private int _id;

    private String year;

    private ArrayList<Student> students;

    /**
     *
     */
    public GroupBean( int id ) {
        students = new ArrayList<Student>();

        this._id = id;
    }

    /*
     * (non-Javadoc)
     *
     * @see no.glv.elevko.core.Group#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * (non-Javadoc)
     *
     * @see no.glv.elevko.intrfc.Group#getSize()
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

    public int get_id() {
        return _id;
    }

    public String getYear() {
        return year;
    }

    public void setYear( String year ) {
        this.year = year;
    }

    public void setName( String name ) {
        this.name = name;
    }
}
