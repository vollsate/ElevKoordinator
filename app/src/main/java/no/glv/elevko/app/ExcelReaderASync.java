package no.glv.elevko.app;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import no.glv.elevko.intrfc.Group;

/**
 *
 * @author glevoll
 *
 */
public class ExcelReaderAsync extends AsyncTask<Void, Integer, List<String>> {

    public static final String EXCEL_FILENAME = "harestuaskole.xls";

    Context context;
    private String fileName;
    private OnExcelWorkbookLoadedListener listener;

    private ExcelReader reader;

    private List<String> availClasses;

    /**
     *
     * @param ctx
     */
    public ExcelReaderAsync( Context ctx, String fName, OnExcelWorkbookLoadedListener l ) {
        context = ctx;
        listener = l;

        if ( fName == null ) this.fileName = EXCEL_FILENAME;
        else this.fileName = fName;
    }

    /**
     *
     */
    public List<String> doInBackground( Void... voids ) {
        try {
            InputStream s = context.getAssets().open( fileName );
            reader = new ExcelReader( s );

            return reader.loadWorkbook().getAvailableGroups();
        } catch ( IOException e ) {
            Log.e( getClass().getSimpleName(), "Cannot load Excel file: " + fileName, e );
        }

        return null;
    }

    @Override
    protected void onPostExecute( List<String> result ) {
        listener.onWorkbookLoaded( result );
    }

    /**
     * @return List of all available groups in the Excel workbook
     */
    public List<String> getAvailableGroups() {
        return reader.getAvailableGroups();
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public List<Group> loadGoups() throws IOException {
        return reader.loadGroups();
    }

    /**
     *
     * @param name
     * @return
     */
    public Group loadGroup( String name ) {
        return reader.loadGroup( name );
    }



    public interface OnExcelWorkbookLoadedListener {

        void onWorkbookLoaded( List<String> fileNames );
    }
}
