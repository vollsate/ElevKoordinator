package no.glv.elevko.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Locale;

import no.glv.elevko.R;
import no.glv.elevko.core.DataHandler;

public abstract class BaseTabActivity extends BaseActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( getLayoutID() );

        Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        //setSupportActionBar( toolbar );
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter( getSupportFragmentManager() );
        mSectionsPagerAdapter.baseTabActivity = this;
        mSectionsPagerAdapter.fragments = getFragments();
        mSectionsPagerAdapter.titles = getTabTitles();

        // Set up the ViewPager with the sections adapter.
        mViewPager = ( ViewPager ) findViewById( getViewpagerID() );
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


    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.menu_tabbed, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if ( id == R.id.action_settings ) {
            return true;
        }

        return super.onOptionsItemSelected( item );
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
     *
     * @return
     */
    public abstract BaseTabFragment[] getFragments();

    public abstract String[] getTabTitles();



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance( int sectionNumber ) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt( ARG_SECTION_NUMBER, sectionNumber );
            fragment.setArguments( args );
            return fragment;
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState ) {
            View rootView = inflater.inflate( R.layout.fragment_tabbed, container, false );
            TextView textView = ( TextView ) rootView.findViewById( R.id.section_label );
            textView.setText( getString( R.string.section_format, getArguments().getInt( ARG_SECTION_NUMBER ) ) );
            return rootView;
        }


    }


    /**
     * @author GleVoll
     */
    public abstract static class BaseTabFragment extends BaseFragment {

        protected View rootView;
        BaseTabActivity baseTabActivity;

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
        protected BaseTabActivity getBaseTabActivity() {
            return ( BaseTabActivity ) getActivity();
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


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        BaseTabFragment[] fragments;
        String[] titles;
        BaseTabActivity baseTabActivity;

        public SectionsPagerAdapter( FragmentManager fm ) {
            super( fm );
        }

        protected BaseTabActivity getBaseTabActivity() {
            if ( baseTabActivity == null )
                baseTabActivity = BaseTabActivity.this;

            return baseTabActivity;
        }
        public void setFragments( BaseTabFragment[] frs ) {
            this.fragments = frs;
        }

        @Override
        public Fragment getItem( int position ) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance( position + 1 );
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
}
