package no.glv.elevko.data;

import java.util.ArrayList;
import java.util.List;

import no.glv.elevko.intrfc.Parent;
import no.glv.elevko.intrfc.Phone;

public class ParentBean implements Parent {

	private String fName;
	private String lName;

	private String id;
	private String studentID;
	private String mail;

	private List<Phone> phones;
	private int type;

	public ParentBean( String id, int type ) {
		this.id = id;
		this.type = type;
	}

	@Override
	public String getFirstName() {
		return fName;
	}

	@Override
	public String getLastName() {
		return lName;
	}

	@Override
	public String getMail() {
		return mail;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public List<Phone> getPhoneNumbers() {
		return phones;
	}

	@Override
	public void setFirstName( String name ) {
		fName = name;
	}

	@Override
	public void setLastName( String name ) {
		lName = name;
	}

	@Override
	public void setMail( String mail ) {
		this.mail = mail;
	}

	@Override
	public void addPhone( Phone phone ) {
		if ( phone == null )
			return;
		if ( phones == null )
			phones = new ArrayList<Phone>( 2 );

		phones.add( phone );
	}

	@Override
	public void addPhones( List<Phone> phones ) {
		for ( int i = 0; i < phones.size(); i++ )
			addPhone( phones.get( i ) );
	}

	@Override
	public Phone getPhone( int type ) {
		if ( phones == null )
			return null;
		for ( int i = 0; i < phones.size(); i++ ) {
			if ( phones.get( i ).getType() == type )
				return phones.get( i );
		}

		return null;
	}

	@Override
	public long getPhoneNumber( int type ) {
		if ( phones == null )
			return 0;

		for ( int i = 0; i < phones.size(); i++ ) {
			if ( phones.get( i ).getType() == type )
				return phones.get( i ).getNumber();
		}

		return 0;
	}

	@Override
	public String getStudentID() {
		return studentID;
	}

	@Override
	public void setStudentID( String stdID ) {
		this.studentID = stdID;
	}

	@Override
	public void setID( String id ) {
		this.id = id;
	}

	public String toString() {
		return "parentID=" + id + ", studentID=" + studentID;
	}
}
