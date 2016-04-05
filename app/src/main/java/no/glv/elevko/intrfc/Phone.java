package no.glv.elevko.intrfc;

/**
 * Represents a phone number. There are three diffent types of numbers: home
 * number, work and mobile.
 * 
 * @author glevoll
 *
 */
public interface Phone {

	int MOBIL = 1;

	int HOME = 2;

	int WORK = 3;

	long getNumber();

	int getType();

	String getStudentID();

	String getParentID();

	void setNumber( long num );

	void setStudentID( String id );

	void setParentID( String id );

}
