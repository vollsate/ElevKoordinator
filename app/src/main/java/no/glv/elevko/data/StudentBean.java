package no.glv.elevko.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import no.glv.elevko.intrfc.Parent;
import no.glv.elevko.intrfc.Student;

/**
 * @author GleVoll
 */
public class StudentBean implements Student {

    public String mIdent;

    public String fName;
    public String lName;
    public String bYear;
    public Date birth;

    public String adress;
    public String postalCode;
    public String grade;
    public int strength;

    public String mPhone;

    private String studentClass;

    private List<Parent> parents;

    private StudentBean() {
        parents = new ArrayList<Parent>( 2 );
    }

    public StudentBean( String stdClass ) {
        this();
        this.studentClass = stdClass;
    }

    @Override
    public String getFirstName() {
        return fName;
    }

    public void setFirstName( String nn ) {
        this.fName = nn;
    }

    public void setLastName( String nn ) {
        this.lName = nn;
    }

    public void setIdent( String nn ) {
        this.mIdent = nn;
    }

    @Override
    public String getBirthYear() {
        return bYear;
    }

    @Override
    public String getLastName() {
        return lName;
    }

    @Override
    public Date getBirth() {
        return birth;
    }

    @Override
    public String getAdress() {
        return adress;
    }

    @Override
    public String getPostalCode() {
        return postalCode;
    }

    public String getGrade() {
        return grade;
    }

    public void setFullName( String fullName ) {
        String[] name = fullName.split( "," );

        lName = name[0].trim();
        fName = name[1].trim();
    }

    @Override
    public String getGroupName() {
        return studentClass;
    }

    @Override
    public String getIdent() {
        return mIdent;
    }

    public String toString() {
        return "Student IDENT: " + mIdent;
    }

    @Override
    public void setBirth( Date val ) {
        this.birth = val;
    }

    @Override
    public void setAdress( String val ) {
        adress = val;
    }

    @Override
    public void setPostalCode( String val ) {
        postalCode = val;
    }

    @Override
    public void setGrade( String val ) {
        grade = val;
    }

    @Override
    public void setStrength( int val ) {
        strength = val;
    }

    @Override
    public int getStrength() {
        return strength;
    }

    @Override
    public void setGroupName( String val ) {
        studentClass = val;
    }

    @Override
    public List<Parent> getParents() {
        return parents;
    }

    @Override
    public void addParent( Parent parent ) {
        if ( parent == null )
            return;

        parents.add( parent );
    }

    @Override
    public void addParents( List<Parent> parents ) {
        this.parents.addAll( parents );
    }

    @Override
    public String getPhone() {
        return mPhone;
    }

    @Override
    public void setPhone( String val ) {
        this.mPhone = val;
    }
}
