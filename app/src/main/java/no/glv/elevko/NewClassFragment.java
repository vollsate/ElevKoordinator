package no.glv.elevko;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import no.glv.elevko.InstalledClassesFragment.ClassViewConfig;
import no.glv.elevko.LoadClassFromFileFragment.OnDataLoadedListener;
import no.glv.elevko.base.BaseActivity;
import no.glv.elevko.intrfc.StudentClass;

/**
 * This fragment will display the possibility to install new classes. The fragment may load classes in two different
 * ways: By an CSV file located in the download folder or an Excel file located in the download folder.
 *
 * @author glevoll
 */
public class NewClassFragment extends Fragment implements OnClickListener, OnDataLoadedListener {

    private StudentClass stdClass;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
    }

    /**
     *
     */
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View rootView = inflater.inflate( R.layout.fragment_newclass, container, false );

        // Start the Installed classes fragment - this will show the classes already installed
        ClassViewConfig config = new ClassViewConfig();
        config.showStudentCount = true;
        InstalledClassesFragment.StartFragment( getFragmentManager(), config );

        Button btn = ( Button ) rootView.findViewById( R.id.BTN_loadFile );
        btn.setOnClickListener( this );

        btn = ( Button ) rootView.findViewById( R.id.BTN_loadExcel );
        btn.setOnClickListener( this );

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater ) {
        inflater.inflate( R.menu.menu_load_data, menu );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        //int id = item.getItemId();
        return super.onOptionsItemSelected( item );
    }

    /**
     * Will attempt to load a new class from either an Excel file or an CSV file.
     */
    @Override
    public void onClick( View v ) {
        if ( v.getId() == R.id.BTN_loadFile ) {
            LoadClassFromFileFragment.StartFragment( this, getActivity().getFragmentManager() );
            return;
        }

        // Load data from Excel workbook
        else if ( v.getId() == R.id.BTN_loadExcel ) {
            try {
                LoadClassFromFileFragment fragment = new LoadableExcelClassesFragment();
                fragment.listener = this;
                android.app.FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                fragment.show( ft, getClass().getSimpleName() );
            } catch ( Exception e ) {
                Toast.makeText( getActivity(), e.toString(), Toast.LENGTH_LONG ).show();
            }
        }
    }

    @Override
    public void onDataLoaded( StudentClass stdClass ) {
        String msg = getResources().getString( R.string.loadData_added_toast );
        msg = msg.replace( "{class}", stdClass.getName() );

        Toast.makeText( getActivity(), msg, Toast.LENGTH_LONG ).show();
    }

    /**
     *
     */
    public void startStudentListActivity() {
        Intent intent = new Intent( getActivity(), StdClassListActivity.class );
        BaseActivity.PutStudentClassExtra( stdClass.getName(), intent );
        startActivity( intent );
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
            View rootView = inflater.inflate( R.layout.fragment_newclass, container, false );

            Button btn = ( Button ) rootView.findViewById( R.id.BTN_loadFile );
            btn.setOnClickListener( ( OnClickListener ) getActivity() );

            btn = ( Button ) rootView.findViewById( R.id.BTN_loadExcel );
            btn.setOnClickListener( ( OnClickListener ) getActivity() );

            return rootView;
        }
    }
}
