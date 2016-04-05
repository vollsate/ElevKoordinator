package no.glv.elevko;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import no.glv.elevko.core.BaseActivity;
import no.glv.elevko.core.DataComparator;
import no.glv.elevko.core.DataHandler;
import no.glv.elevko.core.DataHandler.OnTasksChangedListener;
import no.glv.elevko.intrfc.BaseValues;
import no.glv.elevko.intrfc.StudentTask;
import no.glv.elevko.intrfc.Task;
import no.glv.elevko.intrfc.Task.OnTaskChangeListener;

/**
 * This class will show any installed tasks in the system. You may specify
 * certain arguments in order to control the output from this fragment.
 * <p>
 * <p>
 * The following arguments are legal: <blockquote>
 * <table>
 * <tr>
 * <td><tt>Task.TASK_STATE_OPEN</tt></td>
 * <td>Shows any open tasks in the system
 * </tr>
 * <tr>
 * <td><tt>Task.TASK_STATE_CLOSED</tt></td>
 * <td>Shows any closed tasks in the system
 * </tr>
 * <tr>
 * <td><tt>Task.TASK_STATE_EXPIRED</tt></td>
 * <td>Shows any expired in the system
 * </tr>
 * </table>
 * </blockquote>
 * <p>
 * <p>
 * If wanted, the display view may also show a counter displaying how many
 * students are pending, how many students are handed in and the description of
 * the task. Use the {@link TaskViewConfig} class to configure the layout of the
 * view.
 *
 * @author GleVoll
 */
public class InstalledTasksFragment extends InstalledDataFragment<Integer>
        implements OnTasksChangedListener, Task.OnTaskChangeListener {

    /**  */
    public static final String INST_STATE_TASK_NAMES = BaseValues.EXTRA_BASEPARAM + "taskNames";

    public static final int CONTAINER_ID = R.id.FR_installedTasks_container;

    /**
     * Contains the configuration data for the fragment
     */
    private TaskViewConfig config;

    /**
     * A list of all the task names to display, stored by taskID
     */
    private ArrayList<Integer> mTasks;

    /**
     * A list of the counters
     */
    private List<TextView> mPendingCounters;
    private List<TextView> mHandinCounters;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        mPendingCounters = new LinkedList<TextView>();
        mHandinCounters = new LinkedList<TextView>();

        if ( savedInstanceState != null ) {
            config = (TaskViewConfig) savedInstanceState.getSerializable( PARAM_CONFIG );
            mTasks = savedInstanceState.getIntegerArrayList( INST_STATE_TASK_NAMES );
        } else {
            config = (TaskViewConfig) getArguments().getSerializable( PARAM_CONFIG );
            sortTaskNames();
        }

        // Add a listener to every task, so we get informed when students hand
        // in or is removed from the task itself.
        for ( Integer name : mTasks ) {
            Task t = dataHandler.getTask( name );
            t.addOnTaskChangeListener( this );
        }

        // Add a listener so we get informed when a Task is deleted or updated
        dataHandler.registerOnTaskChangeListener( this );
    }

    /**
     * Sorts the taskNames according to the preference in the {@link DataConfig}
     * instance.
     */
    private void sortTaskNames() {
        List<Task> tasks = dataHandler.getTasks( config.taskState );
        Collections.sort( tasks, new DataComparator.TaskComparator( config.sortBy ) );
        mTasks = new ArrayList<Integer>();
        for ( Task t : tasks )
            mTasks.add( t.getID() );
    }

    @Override
    protected DataConfig getConfig() {
        return config;
    }

    @Override
    public void onSaveInstanceState( Bundle outState ) {
        super.onSaveInstanceState( outState );

        outState.putSerializable( PARAM_CONFIG, config );
        outState.putIntegerArrayList( INST_STATE_TASK_NAMES, mTasks );
    }

    @Override
    public void onDestroy() {
        for ( Integer name : mTasks ) {
            Task t = dataHandler.getTask( name );
            t.removeOnTaskChangeListener( this );
        }

        mPendingCounters.clear();
        super.onDestroy();
    }

    @Override
    public int getViewGroupLayoutID() {
        return R.layout.fr_installedtasks;
    }

    @Override
    public List<Integer> getNames() {
        if ( isModified() ) {
            sortTaskNames();
        }

        return mTasks;
    }

    @Override
    public int getRowLayoutID() {
        return R.layout.row_installed_task;
    }

    @Override
    protected View buildRow( final Integer taskID, int pos ) {
        ViewGroup vg = inflateViewGroup( getRowLayoutID() );
        Task task = dataHandler.getTask( taskID );
        String name = dataHandler.getTaskDisplayName( task );

        LinearLayout ll = (LinearLayout) vg.findViewById( R.id.LL_task_rowData );
        ll.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View v ) {
                Intent intent = createIntent( taskID, getActivity() );
                if ( intent != null )
                    startActivity( intent );
            }
        } );

        // Weather or not to show the ON/OFF button.
        // this to allow for opening/closing a task.
        ImageView iv = (ImageView) vg.findViewById( R.id.IV_task_openOrClosed );
        if ( config.showOnOffButton ) {
            iv.setTag( task );
            if ( task.getState() == Task.TASK_STATE_OPEN )
                iv.setImageDrawable( getResources().getDrawable( R.drawable.ic_task_on ) );
            else if ( task.getState() == Task.TASK_STATE_CLOSED )
                iv.setImageDrawable( getResources().getDrawable( R.drawable.ic_task_off ) );
        } else {
            iv.setVisibility( View.GONE );
        }

        // Set the Student pending counter, if needed
        TextView tvCountPending = (TextView) vg.findViewById( R.id.TV_task_counterPending );
        if ( !config.showCounterPending ) {
            tvCountPending.setVisibility( View.GONE );
        } else {
            tvCountPending.setText( "" + task.getStudentsPendingCount() );
            tvCountPending.setTag( task );
            tvCountPending.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick( View v ) {
                    Task t = (Task ) v.getTag();
                    int pivot = t.getStudentsPendingCount();
                    int i = 0;

                    StringBuffer sb = new StringBuffer();
                    for ( StudentTask st : t.getStudentsPending() ) {
                        sb.append( st.getStudent().getFirstName() ).append( " " )
                                .append( st.getStudent().getLastName() );
                        if ( i++ < pivot - 1 )
                            sb.append( "\n" );
                    }

                    String msg = sb.toString();
                    Toast.makeText( getActivity(), msg, Toast.LENGTH_LONG ).show();
                }
            } );

            mPendingCounters.add( tvCountPending );
        }

        // Set the Student hand in counter, if needed
        TextView tvCountHandin = (TextView) vg.findViewById( R.id.TV_task_counterHandin );
        if ( !config.showCounterHandin ) {
            tvCountHandin.setVisibility( View.GONE );
        } else {
            tvCountHandin.setText( "" + task.getStudentsHandedInCount() );
            tvCountHandin.setTag( task );
            mHandinCounters.add( tvCountHandin );
        }

        // Set the name and add a click listener
        TextView tvName = (TextView) vg.findViewById( R.id.TV_task_name );
        tvName.setText( name );

        // Weather or not to show the expired date.
        TextView tvDate = (TextView) vg.findViewById( R.id.TV_task_date );
        if ( config.showExpiredDate ) {
            String dateStr = BaseActivity.GetDateAsString( task.getDate() );
            tvDate.setText( dateStr );

            if ( task.isExpired() ) {
                tvDate.setTextColor( Color.RED );
            }
        } else {
            tvDate.setVisibility( View.GONE );
        }

        // Weather or not to show the tasks description
        TextView tvDesc = (TextView) vg.findViewById( R.id.TV_task_desc );
        TextView tvSubjectType = (TextView) vg.findViewById( R.id.TV_task_subjecttype );
        if ( !config.showDescription ) {
            tvDesc.setVisibility( View.GONE );
            tvSubjectType.setVisibility( View.GONE );
        } else {
            String desc = task.getDesciption();
            if ( desc == null || desc.length() == 0 ) {
                tvDesc.setVisibility( View.GONE );
            }

            else {
                if ( desc.length() > 100 ) {
                    desc = desc.substring( 0, 30 ) + "...";
                }
                tvDesc.setText( desc );
            }

            // Set the subject and type
            String type = dataHandler.getSubjectType( task.getType() ).getName();
            String subject = dataHandler.getSubjectType( task.getSubject() ).getName();
            tvSubjectType.setText( "[" + subject + "/" + type + "]" );
        }

        return vg;
    }

    private void updateCounter() {
        for ( TextView tvCounter : mPendingCounters ) {
            Task task = (Task ) tvCounter.getTag();
            tvCounter.setText( "" + task.getStudentsPendingCount() );
        }

        for ( TextView tvCounter : mHandinCounters ) {
            Task task = (Task ) tvCounter.getTag();
            tvCounter.setText( "" + task.getStudentsHandedInCount() );
        }
    }

    @Override
    public Intent createIntent( Integer name, Context context ) {
        return TaskActivity.CreateActivityIntent( name, context );
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater ) {
        inflater.inflate( R.menu.menu_fr_installedclasses, menu );
    }

    @Override
    public void onTaskChange( Task newTask, int mode ) {
        switch ( mode ) {
        case OnTaskChangeListener.MODE_STD_HANDIN:
        case OnTaskChangeListener.MODE_STD_ADD:
        case OnTaskChangeListener.MODE_STD_DEL:
            updateCounter();
            // notifyDataSetChanged();
            break;

        default:
            onDataChange( mode );
            break;
        }
    }

    @Override
    public void onTasksChange( int mode ) {
        switch ( mode ) {
        case OnTaskChangeListener.MODE_TASK_SORT:
            invalidateView();
            break;

        default:
            onDataChange( mode );
            break;
        }
    }

    public static void StartFragment( FragmentManager manager, TaskViewConfig config, int containerID ) {
        InstalledTasksFragment tasksFragment = new InstalledTasksFragment();
        Bundle args = new Bundle();

        int showCount = config.showCount;
        if ( showCount < 0 ) {
            showCount = DataHandler.GetInstance().getSettingsManager().getShowCount();
        }

        args.putSerializable( InstalledTasksFragment.PARAM_CONFIG, config );
        tasksFragment.setArguments( args );

        FragmentTransaction tr = manager.beginTransaction();
        tr.replace( containerID, tasksFragment ).commit();
    }

    /**
     * @param manager
     * @param config
     */
    public static void StartFragment( FragmentManager manager, TaskViewConfig config ) {
        StartFragment( manager, config, CONTAINER_ID );
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    //
    // Configuration class
    //
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * The configuration class for
     *
     * @author glevoll
     */
    public static class TaskViewConfig extends DataConfig {

        /**
         * InstalledTasksFragment.java
         */
        private static final long serialVersionUID = 1L;

        public boolean showCounterPending;
        public boolean showCounterHandin;
        public boolean showOnOffButton;
        public boolean showExpiredDate;

        public boolean showDescription;

        public int taskState;

    }
}
