package no.glv.elevko;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import java.util.List;

import no.glv.elevko.intrfc.Task;

/**
 * A specific fragment that shows any classes connected to a task.
 * 
 * @author glevoll
 *
 */
public class InstalledGroupsInTaskFragment extends GroupListFragment {

	public static final String PARAM_TASK = "task";

	@Override
	public List<String> getNames() {
		String name = getArguments().getString( PARAM_TASK );
		Task tas = getDataHandler().getTask( name );
		return tas.getGroups();
	}

	@Override
	public Intent createIntent( String name, Context context ) {
		return GroupViewActivity.CreateActivityIntent( name, getActivity() );
	}

	/**
	 * 
	 * @param task
	 * @param manager
	 * @return
	 */
	public static void NewInstance( Task task, FragmentManager manager ) {
		ClassViewConfig config = new ClassViewConfig();
		config.showStudentCount = false;
		Bundle args = new Bundle();
		args.putString( InstalledGroupsInTaskFragment.PARAM_TASK, task.getName() );

		StartFragment( manager, config, args, new InstalledGroupsInTaskFragment() );
	}

}
