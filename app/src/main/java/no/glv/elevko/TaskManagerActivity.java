/**
 *
 */
package no.glv.elevko;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import no.glv.elevko.TaskListFragment.TaskViewConfig;
import no.glv.elevko.android.BaseSwipeActivity;
import no.glv.elevko.app.DataComparator;
import no.glv.elevko.app.DataHandler;
import no.glv.elevko.intrfc.Task;

/**
 * An activity that shows all known tasks in the system, both open and closed.
 * <p/>
 * <p>Provides functionality to open, close or remove any task registered.</p>
 *
 * @author glevoll
 */
@SuppressWarnings("deprecation")
public class TaskManagerActivity extends BaseSwipeActivity {

    /**
     * @see no.glv.elevko.android.BaseSwipeActivity#getLayoutID()
     */
    @Override
    public int getLayoutID() {
        return R.layout.activity_task_manager;
    }

    /**
     * @see no.glv.elevko.android.BaseSwipeActivity#getViewpagerID()
     */
    @Override
    public int getViewpagerID() {
        return R.id.viewpager;
    }

    /**
     * @see no.glv.elevko.android.BaseSwipeActivity#getFragments()
     */
    @Override
    public BaseSwipeFragment[] getFragments() {
        return new BaseSwipeFragment[]{ new OpenTasksFragment(), new ClosedTasksFragment(), new AllTasksFragment() };
    }

    /**
     * @see no.glv.elevko.android.BaseSwipeActivity#getTabTitles()
     */
    @Override
    public String[] getTabTitles() {
        return new String[]{ getString( R.string.taskview_tab_open_title ),
                getString( R.string.taskview_tab_close_title ), "Alle" };
    }

    /**
     * Callback from an onClick event on the image telling weather or not a task is open. The <code>Task</code> is
     * stored as a tag in the view.
     *
     * @param v The <code>ImageView</code> sending the event
     */
    public void openTask( View v ) {
        ImageView iv = ( ImageView ) v;
        Task t = ( Task ) iv.getTag();

        if ( t.getState() == Task.STATE_CLOSED ) {
            t.setState( Task.STATE_OPEN );
            iv.setImageDrawable( getResources().getDrawable( R.drawable.ic_task_on ) );
        } else if ( t.getState() == Task.STATE_OPEN ) {
            t.setState( Task.STATE_CLOSED );
            iv.setImageDrawable( getResources().getDrawable( R.drawable.ic_task_off ) );
        }

        DataHandler.GetInstance().updateTask( t, null ).notifyTaskUpdate( t );

    }

    /**
     * @param state Get a default configuration state object.
     */
    public static TaskViewConfig GetConfig( int state ) {
        TaskViewConfig config = new TaskViewConfig();
        config.showCounterComplete = true;
        config.showCounterPending = true;
        config.showDescription = true;
        config.sortBy = DataComparator.SORT_TASKNAME_ASC;
        config.taskState = state;
        config.showCount = Integer.MAX_VALUE;
        config.showOnOffButton = true;

        return config;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    //
    //
    //
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * @author glevoll
     */
    public static class OpenTasksFragment extends BaseSwipeFragment {
        @Override
        protected View doCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
            TaskListFragment.StartFragment( getFragmentManager(), GetConfig( Task.STATE_OPEN ),
                    R.id.FR_openTasks_container );

            // View v = container.findViewById( R.id.FR_openTasks_container );

            return null;
        }

        @Override
        protected int getLayoutID() {
            return R.layout.fr_task_manager_open;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * @author glevoll
     */
    public static class ClosedTasksFragment extends BaseSwipeFragment {
        @Override
        protected View doCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
            TaskListFragment.StartFragment( getFragmentManager(), GetConfig( Task.STATE_CLOSED ),
                    R.id.FR_closedTasks_container );
            return null;
        }

        @Override
        protected int getLayoutID() {
            return R.layout.fr_task_manager_closed;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * @author glevoll
     */
    public static class AllTasksFragment extends BaseSwipeFragment {
        @Override
        protected View doCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
            TaskViewConfig config = GetConfig( Task.STATE_OPEN | Task.STATE_CLOSED );

            TaskListFragment.StartFragment( getFragmentManager(), config, R.id.FR_allTasks_container );
            return null;
        }

        @Override
        protected int getLayoutID() {
            return R.layout.fr_task_manager_all;
        }
    }
}
