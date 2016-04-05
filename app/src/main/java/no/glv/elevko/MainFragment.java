package no.glv.elevko;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;

import no.glv.elevko.base.BaseFragment;
import no.glv.elevko.intrfc.Task;

/**
 * Main fragment that shows the first page. This fragment will list all the
 * classes installed on the system and any open tasks.
 * 
 * <p>
 * 
 * - The user may look at a class or a task - A new task may be loaded - A new
 * class may be installed
 * 
 * Uses fragments to show the installed classes and installed tasks.
 * 
 * @author GleVoll
 *
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

		startInstalledClassesFR( savedInstanceState );
		startInstalledTasksFR( savedInstanceState );

		return rootView;
	}

	/**
	 * Will start the fragment that displays all the currently open tasks.
	 * 
	 * @param inState The saved instance bundle
	 */
	private void startInstalledTasksFR( Bundle inState ) {
		InstalledTasksFragment.TaskViewConfig config = new InstalledTasksFragment.TaskViewConfig();
		config.showCounterPending = true;
		config.showCounterHandin = true;
		config.showDescription = true;
		config.showExpiredDate = dataHandler.getSettingsManager().showExpiredDate();
		config.taskState = Task.TASK_STATE_OPEN;
		config.sortBy = dataHandler.getSettingsManager().getTaskSortBy();
		config.showCount = Integer.MAX_VALUE;

		InstalledTasksFragment.StartFragment( getFragmentManager(), config );
	}

	/**
     * Will start the fragment showing the currently installed classes.
	 * 
	 * @param inState The saved instance bundle
	 */
	private void startInstalledClassesFR( Bundle inState ) {
		InstalledClassesFragment.ClassViewConfig config = new InstalledClassesFragment.ClassViewConfig();
		config.showStudentCount = true;
		
		InstalledClassesFragment.StartFragment( getFragmentManager(), config );
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
				File file = dataHandler.listDB();
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file),"application/vnd.ms-excel");
				
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
	 * 
	 * <p>
	 * This action is not reversible!
	 */
	private void resetDB() {
		AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
		String msg = getResources().getString( R.string.action_resetDB_msg );

		builder.setMessage( msg ).setTitle( R.string.action_resetDB );
		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick( DialogInterface dialog, int which ) {
				dataHandler.resetDB();
				Toast.makeText( MainFragment.this.getActivity(), R.string.action_resetDB_done, Toast.LENGTH_LONG )
						.show();
			}
		} );

		builder.setNegativeButton( R.string.cancel, null );

		AlertDialog dialog = builder.create();
		dialog.show();

	}

}
