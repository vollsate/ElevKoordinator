package no.glv.elevko.base;

import android.os.AsyncTask;

import no.glv.elevko.core.DataHandler;
import no.glv.elevko.core.ExcelReader;
import no.glv.elevko.intrfc.StudentClass;

public class LoadAndStoreASyncTask extends AsyncTask<String, Void, StudentClass> {

	private ExcelReader reader;
	private OnStudentClassStoredListener listener;

	public LoadAndStoreASyncTask( ExcelReader reader, OnStudentClassStoredListener l ) {
		this.reader = reader;
		this.listener = l;
	}

	@Override
	protected StudentClass doInBackground( String... params ) {
		String fileName = params[0];
		StudentClass stdClass = reader.loadClass( fileName );
		DataHandler.GetInstance().addStudentClass( stdClass );

		return stdClass;
	}

	@Override
	protected void onPostExecute( StudentClass result ) {
		listener.onStudentClassStore( result );
	}

	/**
	 * 
	 * @author glevoll
	 *
	 */
	public interface OnStudentClassStoredListener {

		void onStudentClassStore( StudentClass stdClass );
	}

}
