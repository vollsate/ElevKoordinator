package no.glv.paco.android;

import android.os.Bundle;
import android.support.design.widget.TabLayout;

public abstract class BaseTabActivity extends BaseSwipeActivity {

    protected abstract int getTabLayoutID();

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        //Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        //setSupportActionBar( toolbar );
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        //mSectionsPagerAdapter = new BaseSwipeActivity.SectionsPagerAdapter( getSupportFragmentManager() );

        // Set up the ViewPager with the sections adapter.
        //mViewPager = ( ViewPager ) findViewById( getViewpagerID() );
        //mViewPager.setAdapter( mSectionsPagerAdapter );

        TabLayout tabLayout = ( TabLayout ) findViewById( getTabLayoutID() );
        assert tabLayout != null;
        tabLayout.setupWithViewPager( mViewPager );
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
}
