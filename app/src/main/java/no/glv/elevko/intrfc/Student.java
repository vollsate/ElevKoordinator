package no.glv.elevko.intrfc;

import java.util.Date;
import java.util.List;

/**
 * Represents a student. A student is a member of a class, and involved in
 * one or more {@link Task}s.
 * <p>
 * <p>
 *
 * @author glevoll
 */
public interface Student {

    String EXTRA_IDENT = BaseValues.EXTRA_BASEPARAM + "Ident";
    String EXTRA_STUDENTNAME = BaseValues.EXTRA_BASEPARAM + "StudentName";

    String getFirstName();

    void setFirstName( String fName );

    String getBirthYear();

    String getLastName();

    void setLastName( String lName );

    Date getBirth();

    void setBirth( Date val );

    String getAdress();

    void setAdress( String val );

    String getPostalCode();

    void setPostalCode( String val );

    String getPhone();

    void setPhone( String val );

    String getGrade();

    void setGrade( String val );

    void setStrength( int val );

    int getStrength();

    String getGroupName();

    void setGroupName( String val );

    /**
     * @return The unique identity used on It's Learning and Google
     */
    String getIdent();

    void setIdent( String val );

    List<Parent> getParents();

    void addParent( Parent parent );

    void addParents( List<Parent> parents );

}
