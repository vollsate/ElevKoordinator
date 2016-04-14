package no.glv.elevko.android;

import android.os.AsyncTask;

import no.glv.elevko.app.DataHandler;
import no.glv.elevko.app.ExcelReaderAsync;
import no.glv.elevko.intrfc.Group;

public class LoadAndStoreASyncTask extends AsyncTask<String, Void, Group> {

    private ExcelReaderAsync reader;
    private OnStudentClassStoredListener listener;

    public LoadAndStoreASyncTask( ExcelReaderAsync reader, OnStudentClassStoredListener l ) {
        this.reader = reader;
        this.listener = l;
    }

    @Override
    protected Group doInBackground( String... params ) {
        String fileName = params[ 0 ];
        Group stdClass = reader.loadGroup( fileName );
        DataHandler.GetInstance().addGroup( stdClass );

        return stdClass;
    }

    @Override
    protected void onPostExecute( Group result ) {
        listener.onStudentClassStore( result );
    }

    /**
     *
     * @author glevoll
     *
     */
    public interface OnStudentClassStoredListener {

        void onStudentClassStore( Group stdClass );
    }

}
