package no.glv.elevko.app;

public class DBException extends RuntimeException {

	/** DBException.java */
	private static final long serialVersionUID = -5331569245421077680L;

	public DBException() {
	}

	public DBException( String detailMessage ) {
		super( detailMessage );
	}

	public DBException( Throwable throwable ) {
		super( throwable );
	}

	public DBException( String detailMessage, Throwable throwable ) {
		super( detailMessage, throwable );
	}

}
