package no.glv.elevko.core;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Locale;

/**
 * A base class for handling the creation and management of different TABS
 *
 * @author GleVoll
 */
@SuppressWarnings("deprecation")
public abstract class BaseTabActivity_old extends BaseActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
     * derivative, which will keep every loaded fragment in memory. If this
     * becomes too memory intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentPagerAdapter}.
     */
    PagerAdapterExt mAdapter;

    /**
     * @return The R.layout ID the corresponds to a ViewPager XML element
     */
    public abstract int getLayoutID();

    /**
     * @return The R.id ID to the ViewPager XML element
     */
    public abstract int getViewpagerID();

    /**
     *
     * @return
     */
    public abstract BaseTabFragment[] getFragments();

    /**
     *
     * @return
     */
    public abstract String[] getTabTitles();

    protected String getTabTitle() {
        return getTitle().toString();
    }

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( getLayoutID() );

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        //actionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_TABS );
        setTitle( getTabTitle() );

        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mAdapter = new PagerAdapterExt( getSupportFragmentManager() );
        mAdapter.baseTabActivity = this;
        mAdapter.fragments = getFragments();
        mAdapter.titles = getTabTitles();

        // Set up the ViewPager with the sections adapter.
        mViewPager = ( ViewPager ) findViewById( getViewpagerID() );
        mViewPager.setAdapter( mAdapter );

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener( new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected( int position ) {
                actionBar.setSelectedNavigationItem( position );
            }
        } );

        // For each of the sections in the app, add a tab to the action bar.
        for ( int i = 0; i < mAdapter.getCount(); i++ ) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab( actionBar.newTab().setText( mAdapter.getPageTitle( i ) ).setTabListener( this ) );
        }
    }

    @Override
    public void onTabSelected( ActionBar.Tab tab, FragmentTransaction fragmentTransaction ) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem( tab.getPosition() );
    }

    /**
     * @author GleVoll
     */
    public class PagerAdapterExt extends FragmentPagerAdapter {

        BaseTabFragment[] fragments;
        String[] titles;
        BaseTabActivity_old baseTabActivity;

        /**
         *
         * @param fm
         */
        public PagerAdapterExt( FragmentManager fm ) {
            super( fm );
        }

        protected BaseTabActivity_old getBaseTabActivity() {
            if ( baseTabActivity == null )
                baseTabActivity = BaseTabActivity_old.this;

            return baseTabActivity;
        }

        public void setFragments( BaseTabFragment[] frs ) {
            this.fragments = frs;
        }

        @Override
        public Fragment getItem( int position ) {
            fragments[position].baseTabActivity = getBaseTabActivity();
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle( int position ) {
            return titles[position].toUpperCase( Locale.getDefault() );

        }
    }

    /**
     * @author GleVoll
     */
    public abstract static class BaseTabFragment extends BaseFragment {

        protected View rootView;
        BaseTabActivity_old baseTabActivity;

        /**
         *
         */
        public final View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
            rootView = inflater.inflate( getLayoutID(), container, false );
            View view = doCreateView( inflater, container, savedInstanceState );

            if ( view == null ) view = rootView;
            return view;
        }

        /**
         *
         * @return
         */
        protected BaseTabActivity_old getBaseTabActivity() {
            return ( BaseTabActivity_old ) getActivity();
        }

        /**
         *
         * @return
         */
        protected DataHandler getDataHandler() {
            return getBaseTabActivity().getDataHandler();
        }

        /**
         *
         * @return
         */
        protected abstract int getLayoutID();

        /**
         *
         * @param inflater
         * @param container
         * @param savedInstanceState
         * @return
         */
        protected abstract View doCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState );

        /**
         *
         * @param id
         * @return
         */
        protected EditText getEditText( int id ) {
            return ( EditText ) rootView.findViewById( id );
        }

        protected TextView getTextView( int id ) {
            return ( TextView ) rootView.findViewById( id );
        }

        protected ListView getListView( int id ) {
            return ( ListView ) rootView.findViewById( id );
        }

        protected Button getButton( int id ) {
            return ( Button ) rootView.findViewById( id );
        }

        protected Spinner getSinner( int id ) {
            return ( Spinner ) rootView.findViewById( id );
        }
    }

}
