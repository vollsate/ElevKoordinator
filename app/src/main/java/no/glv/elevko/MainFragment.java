package no.glv.elevko;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import no.glv.elevko.android.BaseFragment;
import no.glv.elevko.intrfc.Task;

/**
 * Main fragment that shows the first page. This fragment will list all the
 * classes installed on the system and any open tasks.
 * <p/>
 * <p/>
 * <p/>
 * - The user may look at a class or a task - A new task may be loaded - A new
 * class may be installed
 * <p/>
 * Uses fragments to show the installed classes and installed tasks.
 * 
 * <p>Handles the main menu options.</p>
 *
 * @author GleVoll
 */
public class MainFragment extends BaseFragment {

    // private static final String TAG = MainFragment.class.getSimpleName();

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setHasOptionsMenu( true );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        View rootView = inflater.inflate( R.layout.content_main, container, false );

        getActivity().setTitle( getResources().getString( R.string.app_name ) );

        startInstalledGroupsFR();
        startInstalledTasksFR();

        return rootView;
    }

    /**
     * Will start the fragment that displays all the currently open tasks.
     */
    private void startInstalledTasksFR() {
        InstalledTasksFragment.TaskViewConfig config = new InstalledTasksFragment.TaskViewConfig();
        config.showCounterPending = true;
        config.showCounterHandin = true;
        config.showDescription = true;
        config.showExpiredDate = getDataHandler().getSettingsManager().showExpiredDate();
        config.taskState = Task.TASK_STATE_OPEN;
        config.sortBy = getDataHandler().getSettingsManager().getTaskSortBy();
        config.showCount = Integer.MAX_VALUE;

        InstalledTasksFragment.StartFragment( getFragmentManager(), config );
    }

    /**
     * Will start the fragment showing the currently installed classes.
     */
    private void startInstalledGroupsFR() {
        InstalledGroupsFragment.ClassViewConfig config = new InstalledGroupsFragment.ClassViewConfig();
        config.showStudentCount = true;

        InstalledGroupsFragment.StartFragment( getFragmentManager(), config );
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater ) {
        inflater.inflate( R.menu.menu_main, menu );
    }

    /**
     *
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int id = item.getItemId();
        Intent intent = null;

        switch ( id ) {
            case R.id.menu_resetDB:
                resetDB();
                break;

            case R.id.menu_newTask:
                intent = new Intent( getActivity(), NewTaskActivity.class );
                break;

            case R.id.menu_listDB:
                File file = getDataHandler().listDB();
                intent = new Intent( Intent.ACTION_VIEW );
                intent.setDataAndType( Uri.fromFile( file ), "application/vnd.ms-excel" );

                break;

            default:
                return super.onOptionsItemSelected( item );
        }

        if ( intent != null )
            startActivity( intent );

        return true;
    }

    /**
     * Resets the database. Will start up an dialog to confirm such a reset.
     * <p/>
     * This action is not reversible!
     */
    private void resetDB() {
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        String msg = getResources().getString( R.string.action_resetDB_msg );

        builder.setMessage( msg ).setTitle( R.string.action_resetDB );
        builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick( final DialogInterface dialog, int which ) {
                getDataHandler().resetDB();
                Snackbar.make( getView(), R.string.action_resetDB_done, Snackbar.LENGTH_LONG )
                        .show();
            }
        } );

        builder.setNegativeButton( R.string.cancel, null );

        AlertDialog dialog = builder.create();
        dialog.show();

    }

}
