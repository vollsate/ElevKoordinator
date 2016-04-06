package no.glv.elevko.android;

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

import no.glv.elevko.app.DataHandler;

public abstract class BaseSwipeActivity extends BaseActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    protected SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    protected ViewPager mViewPager;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( getLayoutID() );

        // Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        // setSupportActionBar( toolbar );
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter( getSupportFragmentManager() );
        mSectionsPagerAdapter.baseSwipeActivity = this;
        mSectionsPagerAdapter.fragments = getFragments();
        mSectionsPagerAdapter.titles = getTabTitles();

        // Set up the ViewPager with the sections adapter.
        mViewPager = ( ViewPager ) findViewById( getViewpagerID() );
        assert mViewPager != null;
        mViewPager.setAdapter( mSectionsPagerAdapter );


/*
        FloatingActionButton fab = ( FloatingActionButton ) findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                Snackbar.make( view, "Replace with your own action", Snackbar.LENGTH_LONG )
                        .setAction( "Action", null ).show();
            }
        } );
*/
    }


    /**
     * @return The R.layout ID the corresponds to a ViewPager XML element
     */
    public abstract int getLayoutID();

    /**
     * @return The R.id ID to the ViewPager XML element
     */
    public abstract int getViewpagerID();

    /**
     * @return all fragments to swipe through
     */
    public abstract BaseSwipeFragment[] getFragments();

    public abstract String[] getTabTitles();


    /**
     * @author GleVoll
     */
    public abstract static class BaseSwipeFragment extends BaseFragment {

        protected View rootView;
        BaseSwipeActivity baseSwipeActivity;

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
         * @return The <code>DataHandler</code>
         */
        protected DataHandler getDataHandler() {
            return getBaseActivity().getDataHandler();
        }

        /**
         * @return The resource ID holding the fragment
         */
        protected abstract int getLayoutID();

        /**
         * Called after onCreate
         *
         * @param container Holding the fragment
         *
         * @return New view
         */
        protected abstract View doCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState );

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


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        BaseSwipeFragment[] fragments;
        String[] titles;
        BaseSwipeActivity baseSwipeActivity;

        public SectionsPagerAdapter( FragmentManager fm ) {
            super( fm );
        }

        protected BaseSwipeActivity getBaseSwipeActivity() {
            if ( baseSwipeActivity == null )
                baseSwipeActivity = BaseSwipeActivity.this;

            return baseSwipeActivity;
        }

        @Override
        public Fragment getItem( int position ) {
            fragments[position].baseSwipeActivity = getBaseSwipeActivity();
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
}
