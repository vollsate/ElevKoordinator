package no.glv.elevko;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import no.glv.elevko.android.InstalledDataFragment;
import no.glv.elevko.app.DataComparator;
import no.glv.elevko.app.DataHandler;
import no.glv.elevko.intrfc.Group;

/**
 * This will list any installed class in the system. It may be installed as a
 * fragment in any container with the ID: <tt>fr.installedClasses</tt>
 * <p/>
 * <p/>
 * <p/>
 * The fragment task a {@link DataConfig} parameter to be used to control the
 * layout and view of the class. The following parameters may be used:
 * <blockquote>
 * <ul>
 * <li>showCount - The number of classes to show.
 * <li>sortBy - The {@link DataComparator} sortBy int ID.
 * <li>showDesc - A boolean telling weather or not to display the number of
 * students in the class
 * </ul>
 * </blockquote>
 *
 * @author GleVoll
 */
public class GroupListFragment extends InstalledDataFragment<String> implements DataHandler.OnDataChangeListener {

    /** A config class that controls how the installed groups are listet */
    private ClassViewConfig config;

    /** All users of this fragment, MUST use this resource ID */
    public static final int CONTAINER_ID = R.id.FR_grouplist_container;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        getDataHandler().registerOnGroupChangeListener( this );

        config = ( ClassViewConfig ) getArguments().getSerializable( PARAM_CONFIG );
    }

    @Override
    public void onDestroy() {
        getDataHandler().unregisterOnGroupChangeListener( this );
        super.onDestroy();
    }

    @Override
    protected DataConfig getConfig() {
        return config;
    }

    @Override
    public int getViewGroupLayoutID() {
        return R.layout.grouplist_fragment;
    }

    @Override
    public List<String> getNames() {
        return getDataHandler().getInstalledGroupNames();
    }

    @Override
    public int getRowLayoutID() {
        return R.layout.grouplist_row;
    }

    /**
     * Builds one particular row with an installed group.
     *
     * @param name Name of the group to build a row for
     * @param pos  The position in the list of groups
     *
     * @return The newly built row
     */
    protected View buildRow( final String name, int pos ) {
        ViewGroup vg = ( ViewGroup ) inflateView( getRowLayoutID() );
        //ViewGroup vg = (ViewGroup) getBaseActivity().findViewById( CONTAINER_ID );
        Group group = getDataHandler().getGroup( name );

        // Start the Group view activity when the row is clicked
        LinearLayout ll = ( LinearLayout ) vg.findViewById( R.id.LL_groupList_rowData );
        ll.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View v ) {
                startActivity( createIntent( name, getActivity() ) );
            }
        } );

        TextView textView = ( TextView ) vg.findViewById( R.id.TV_groupList_name );
        textView.setText( name );
        textView.setTag( String.valueOf( pos ) );

        TextView tvStds = ( TextView ) vg.findViewById( R.id.TV_groupList_desc );
        TextView tvStdCount = ( TextView ) vg.findViewById( R.id.TV_groupList_counter );
        if ( config.showStudentCount ) {
            tvStdCount.setText( String.valueOf( group.getSize() ) );
            tvStdCount.setTag( String.valueOf( pos ) );
        } else {
            tvStdCount.setVisibility( View.GONE );
            tvStds.setVisibility( View.GONE );
        }

        return vg;
    }

    /**
     * Starts the <code>Activity</code> that displays the specific installed student class. Caused by a click on the
     * row.
     *
     * @param name    Name of class to display.
     * @param context The context used to start the activity
     *
     * @return The <code>Intent</code> created.
     */
    @Override
    public Intent createIntent( String name, Context context ) {
        return GroupViewActivity.CreateActivityIntent( name, context );
    }

    @Override
    public void onDataChange( int mode, int type ) {
        onDataChange( mode );
    }

    /**
     * Will start an GroupListFragment. This method-call will assume that the fragment will start in the
     * resourceID: <code>R.id.FR_installedClasses_container</code>.
     *
     * @param manager The manager to start the fragment
     * @param config  Any configuration data
     */
    public static void StartFragment( FragmentManager manager, @NonNull ClassViewConfig config ) {
        Bundle args = new Bundle();

        StartFragment( manager, config, args, new GroupListFragment() );
    }

    /**
     * @param manager FragmentManager to start the Fragment
     * @param config  Any configuration data
     */
    public static void StartFragment( FragmentManager manager, @NonNull ClassViewConfig config, Bundle args, Fragment
            fragment ) {
        if ( config.showCount < 0 ) {
            config.showCount = DataHandler.GetInstance().getSettingsManager()
                    .getShowCount();
        }

        args.putSerializable( PARAM_CONFIG, config );
        fragment.setArguments( args );

        FragmentTransaction tr = manager.beginTransaction();
        tr.replace( CONTAINER_ID, fragment ).commit();
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
    public static class ClassViewConfig extends DataConfig {

        /**
         * TaskListFragment.java
         */
        private static final long serialVersionUID = 1L;

        public boolean showStudentCount;
    }
}
