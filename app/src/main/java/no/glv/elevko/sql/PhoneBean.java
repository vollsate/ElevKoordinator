package no.glv.elevko.sql;

import no.glv.elevko.intrfc.Phone;

public class PhoneBean implements Phone {

	private String stdID;
	private String parentID;
	private long number;
	private int type;

	public PhoneBean( int type ) {
		this.type = type;
	}

	@Override
	public long getNumber() {
		return number;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public String getStudentID() {
		return stdID;
	}

	@Override
	public void setNumber( long num ) {
		this.number = num;
	}

	@Override
	public void setStudentID( String id ) {
		this.stdID = id;
	}

	@Override
	public String getParentID() {
		return parentID;
	}

	@Override
	public void setParentID( String id ) {
		this.parentID = id;
	}

}
