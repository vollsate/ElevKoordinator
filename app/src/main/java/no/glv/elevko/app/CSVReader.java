package no.glv.elevko.app;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import no.glv.elevko.intrfc.Parent;
import no.glv.elevko.intrfc.Phone;
import no.glv.elevko.intrfc.Student;
import no.glv.elevko.intrfc.Group;
import no.glv.elevko.data.DBUtils;
import no.glv.elevko.data.ParentBean;
import no.glv.elevko.data.PhoneBean;
import no.glv.elevko.data.StudentBean;
import no.glv.elevko.data.GroupBean;

public class CSVReader extends AsyncTask<String, Void, Group> {
	
	private static final String TAG =CSVReader.class.getSimpleName();

	private static final String STUDENT_PROPERTY_SEP = ";";
	
	private OnDataLoadedListener listener;
	
	public CSVReader(OnDataLoadedListener l) {
		this.listener = l;
	}

	@Override
	protected Group doInBackground( String... params )  {
		String fileName = params[0];
		FileInputStream fis;
		BufferedReader buff = null;

		File externalDir = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS );
		String fName = externalDir.getAbsolutePath() + "/" + fileName;
		String stdClassName = fileName.substring( 0, fileName.length() - 4 );
		Group stdClass = new GroupBean( stdClassName );

		try {
			fis = new FileInputStream( new File( fName ) );
		}
		catch ( FileNotFoundException fnfEx ) {
			Log.e( TAG, "LoadStudentClass(): File not found: " + fName, fnfEx );
			listener.onError( fnfEx );
			throw new RuntimeException( fnfEx );
		}
		catch ( RuntimeException e ) {
			Log.e( TAG, "LoadStudentClass(): Unknown error: " + fName, e );
			listener.onError( e );
			throw e;
		}

		ArrayList<Student> list = new ArrayList<Student>();

		try {
			buff = new BufferedReader( new InputStreamReader( fis, "CP1252" ) );
			// Read the first header
			buff.readLine();

			String stdLine;
			while ( ( stdLine = buff.readLine() ) != null ) {
				Student newStudent = CreateStudentFromString( stdLine, stdClassName, "dd.MM.yyyy" );
				list.add( newStudent );
			}
		}
		catch ( IOException ioe ) {
			Log.e( TAG, "LoadStudentClass(): Error loading file: " + fName, ioe );
			listener.onError( ioe );
			throw new RuntimeException( ioe );
		}

		try {
			buff.close();
			fis.close();
		}
		catch ( IOException e ) {
			Log.e( TAG, "LoadStudentClass(): Error closing file: " + fName, e );
		}

		Log.v( TAG, list.toString() );
		stdClass.addAll( list );
		return stdClass;

	}

	/**
	 * 
	 * @param stdString A new Student implementation from a semicolon separated
	 *            String.
	 * @return
	 */
	private static Student CreateStudentFromString( String stdString, String className, String datePattern ) {
		StudentBean bean = new StudentBean( className );

		String[] params = stdString.split( STUDENT_PROPERTY_SEP );
		if ( params.length == 11 )
			return CreateOldStudentFromstring( bean, params, datePattern );

		int index = 0;

		bean.lName = params[index++];
		bean.fName = params[index++];
		bean.birth = DBUtils.ConvertStringToDate( params[index++], datePattern );
		bean.adress = params[index++];
		bean.mIdent = DataHandler.CreateStudentID( bean );

		bean.addParent( CreateParent( params, index, bean.getIdent(), Parent.PRIMARY ) );

		index = 10;
		bean.addParent( CreateParent( params, index, bean.getIdent(), Parent.SECUNDARY ) );

		return bean;

	}

	/**
	 * 
	 * @param params
	 * @param start
	 * @param id
	 * @param type
	 * @return
	 */
	private static Parent CreateParent( String[] params, int start, String id, int type ) {
		Parent parent = new ParentBean( null, type );
		parent.setStudentID( id );
		parent.setLastName( params[start++] );
		parent.setFirstName( params[start++] );

		if ( params.length >= 13 )
			parent.addPhone( CreatePhone( params[start++], Phone.MOBIL, id ) );
		if ( params.length >= 14 )
			parent.addPhone( CreatePhone( params[start++], Phone.WORK, id ) );
		if ( params.length >= 15 )
			parent.addPhone( CreatePhone( params[start++], Phone.HOME, id ) );

		if ( params.length >= 16 )
			parent.setMail( params[start++] );

		return parent;
	}

	/**
	 * 
	 * @param param
	 * @param type
	 * @param id
	 * @return
	 */
	private static Phone CreatePhone( String param, int type, String id ) {
		if ( param == null )
			return null;
		if ( !( param.length() > 0 ) )
			return null;

		PhoneBean phone = new PhoneBean( type );
		phone.setNumber( Long.parseLong( param ) );
		phone.setStudentID( id );

		return phone;
	}

	/**
	 * 
	 * @param bean
	 * @param params
	 * @param datePattern
	 * @return
	 */
	private static Student CreateOldStudentFromstring( StudentBean bean, String[] params, String datePattern ) {
		Parent parent = null;
		Phone phone = null;
		String[] subParams;
		int index = 0;

		bean.grade = params[index++];
		bean.birth = DBUtils.ConvertStringToDate( params[index++], datePattern );
		bean.setFullName( params[index++] );
		bean.adress = params[index++];
		bean.postalCode = params[index++];

		bean.mIdent = DataHandler.CreateStudentID( bean );

		parent = new ParentBean( null, Parent.PRIMARY );
		parent.setStudentID( bean.getIdent() );
		subParams = params[index++].split( "," );
		parent.setLastName( subParams[0].trim() );
		parent.setFirstName( subParams[1].trim() );

		phone = new PhoneBean( Phone.MOBIL );
		phone.setStudentID( bean.getIdent() );
		phone.setParentID( parent.getID() );
		phone.setNumber( Long.parseLong( params[index++] ) );
		parent.addPhone( phone );
		parent.setMail( params[index++] );
		bean.addParent( parent );

		parent = new ParentBean( bean.getIdent(), Parent.SECUNDARY );
		subParams = params[index++].split( "," );
		parent.setLastName( subParams[0].trim() );
		parent.setFirstName( subParams[1].trim() );

		phone = new PhoneBean( Phone.MOBIL );
		phone.setParentID( parent.getID() );
		phone.setStudentID( bean.getIdent() );
		try {
			phone.setNumber( Long.parseLong( params[index++] ) );
		}
		catch ( Exception e ) {
			phone.setNumber( 0 );
		}
		parent.addPhone( phone );
		parent.setMail( params[index++] );

		bean.addParent( parent );

		return bean;
	}

	
	
	@Override
	protected void onPostExecute( Group result ) {
		listener.onDataLoaded( result );
	}
	
	/**
	 * 
	 * @author glevoll
	 *
	 */
	public interface OnDataLoadedListener {
		
		void onDataLoaded( Group stdClass );
		
		void onError( Exception e );
	}
}
