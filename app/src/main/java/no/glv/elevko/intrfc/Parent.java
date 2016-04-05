package no.glv.elevko.intrfc;

import java.util.List;

/**
 * Represents a students parent. A parent has a name, mail and one or more
 * {@link Phone} numbers.
 * 
 * <p>
 * The app distinguishes between primary and secondary parent through the
 * {@link #getType()} function.
 * 
 * @author glevoll
 *
 */
public interface Parent {

	int PRIMARY = 1;
	int SECUNDARY = 2;

	String getFirstName();

	String getLastName();

	String getMail();

	int getType();

	String getID();

	void setID( String id );

	String getStudentID();

	void setStudentID( String stdID );

	List<Phone> getPhoneNumbers();

	void setFirstName( String name );

	void setLastName( String name );

	void setMail( String mail );

	void addPhone( Phone phone );

	void addPhones( List<Phone> phones );

	Phone getPhone( int type );

	long getPhoneNumber( int type );

}
