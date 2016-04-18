package no.glv.paco;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import no.glv.paco.android.LoadAndStoreASyncTask;
import no.glv.paco.android.LoadAndStoreASyncTask.OnStudentClassStoredListener;
import no.glv.paco.app.DataHandler;
import no.glv.paco.app.ExcelReaderAsync;
import no.glv.paco.app.ExcelReaderAsync.OnExcelWorkbookLoadedListener;
import no.glv.paco.intrfc.BaseValues;
import no.glv.paco.intrfc.Group;

public class LoadableExcelClassesFragment extends LoadClassFromFileFragment implements OnExcelWorkbookLoadedListener,
        OnStudentClassStoredListener {

    public static final String PARAM_FILENAME = BaseValues.EXTRA_BASEPARAM + "fileName";

    OnDataLoadedListener listener;
    ExcelReaderAsync reader;


    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
    }

    @Override
    protected int getTitle() {
        return R.string.loadData_excel_title;
    }

    @Override
    public void onStudentClassStore( Group stdClass ) {
        if ( listener != null )
            listener.onDataLoaded( stdClass );

        DataHandler.GetInstance().notifyGroupAdd( stdClass );
        String msg = getResources().getString( R.string.loadData_added_toast );
        msg = msg.replace( "{class}", stdClass.getName() );
        Toast.makeText( getActivity(), msg, Toast.LENGTH_LONG ).show();

        finish();
    }

    void startStoreStudentClass( String fileName ) {
        showProgressBar();

        String msg = getResources().getString( R.string.loadData_installing_msg ).replace( "{class}", fileName );
        getDialog().setTitle( msg );

        LoadAndStoreASyncTask las = new LoadAndStoreASyncTask( reader, this );
        las.execute( fileName );
    }

    /**
     *
     */
    @Override
    public void onWorkbookLoaded( List<String> fileNames ) {
        buildButton( getRootView() );
        buildAdapter( getRootView() );

        hideProgressBar();
    }

    @Override
    protected void buildView( View rootView ) {
        showProgressBar();

        Bundle args = getArguments();
        String fileName;
        if ( args != null ) {
            fileName = args.getString( PARAM_FILENAME );
        } else {
            fileName = null;
        }

        reader = new ExcelReaderAsync( getActivity(), fileName, this );
        reader.execute( new Void[]{null} );
    }

    /**
     *
     * @param rootView
     */
    private void buildButton( View rootView ) {
        Button btn = getButton();
        btn.setVisibility( View.VISIBLE );
        btn.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick( View v ) {
                LoadableExcelClassesFragment.this.finish();
            }
        } );
    }

    /**
     *
     * @param rootView
     */
    private void buildAdapter( View rootView ) {
        List<String> list = createFileList();

        ListView listView = getListView();
        LoadableFilesAdapter adapter = new LoadableFilesAdapter( getActivity(), R.id.LV_loadData_filesList, list );
        adapter.fragment = this;
        listView.setAdapter( adapter );
    }

    /**
     *
     * @return
     */
    private List<String> createFileList() {
        List<String> list = new LinkedList<String>();

        for ( String name : reader.getAvailableGroups() ) {
            if ( !DataHandler.GetInstance().getInstalledGroupNames().contains( name ) )
                list.add( name );
        }

        return list;
    }

    /**
     *
     * @author GleVoll
     *
     */
    public static class LoadableFilesAdapter extends ArrayAdapter<String> implements OnClickListener {

        LoadableExcelClassesFragment fragment;

        public LoadableFilesAdapter( Context context, int resource, List<String> objects ) {
            super( context, resource, objects );
        }

        /**
         *
         */
        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {
            LayoutInflater inflater = ( LayoutInflater ) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            if ( convertView == null ) {
                convertView = inflater.inflate( R.layout.row_loaddata_files, parent, false );
            }

            String file = getItem( position );

            TextView textView = ( TextView ) convertView.findViewById( R.id.TV_loadData_fileName );
            textView.setTag( file );
            textView.setText( file );
            textView.setOnClickListener( this );

            return convertView;
        }

        @Override
        public void onClick( View v ) {
            fragment.startStoreStudentClass( v.getTag().toString() );
        }
    }

}
