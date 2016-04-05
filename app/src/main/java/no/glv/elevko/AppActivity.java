package no.glv.elevko;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import no.glv.elevko.android.BaseActivity;
import no.glv.elevko.app.DataHandler;

/**
 * The main activity. Responsible for initiating the {@link DataHandler}.
 */
@SuppressWarnings("deprecation")
public class AppActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    /** Layout resource ID */
    public static final int LAYOUT_ID = R.layout.activity_main;

    /** The navigation drawer resource ID */
    public static final int DRAWER_ID = R.id.drawer_layout;

    /** Toolbar resource ID */
    public static final int TOOLBAR_ID = R.id.toolbar;

    /** Floating action button resource ID */
    public static final int FLOAT_ACTION_BUTTON = R.id.fab;

    /**
     * Will start the entire application.
     *
     * @param savedInstanceState In case we need to take care of anything
     */
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        DataHandler.Init( getApplication() );
        setContentView( LAYOUT_ID );

        Toolbar toolbar = ( Toolbar ) findViewById( TOOLBAR_ID );
        setSupportActionBar( toolbar );

        FloatingActionButton fab = ( FloatingActionButton ) findViewById( FLOAT_ACTION_BUTTON );
        assert fab != null;
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                /*
                Snackbar.make( view, "Replace with your own action", Snackbar.LENGTH_LONG )
                        .setAction( "Action", null ).show();
                */
                Intent intent = new Intent( AppActivity.this, NewTaskActivity.class );
                startActivity( intent );
            }
        } );

        DrawerLayout drawer = ( DrawerLayout ) findViewById( DRAWER_ID );
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
        assert drawer != null;
        drawer.setDrawerListener( toggle );
        toggle.syncState();

        NavigationView navigationView = ( NavigationView ) findViewById( R.id.nav_view );
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener( this );

        displayView( R.id.nav_home );
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = ( DrawerLayout ) findViewById( DRAWER_ID );
        assert drawer != null;
        if ( drawer.isDrawerOpen( GravityCompat.START ) ) {
            drawer.closeDrawer( GravityCompat.START );
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected( MenuItem item ) {
        return displayView( item.getItemId() );
    }

    /**
     * @param id The menu item ID to display
     *
     * @return true
     */
    private boolean displayView( int id ) {
        // Handle navigation view item clicks here.
        Intent intent = null;
        Fragment fragment = null;

        switch ( id ) {
            case R.id.nav_home:
                fragment = new MainFragment();
                break;

            case R.id.nav_classes:
                fragment = new NewGroupFragment();
                break;

            case R.id.nav_tasks:
                intent = new Intent( this, TaskManagerActivity.class );
                break;
        }

        if ( fragment != null ) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace( R.id.frame_container, fragment );
            if ( id != R.id.nav_home ) ft.addToBackStack( null );
            ft.commit();
        }
        if ( intent != null ) {
            startActivity( intent );
        }

        DrawerLayout drawer = ( DrawerLayout ) findViewById( DRAWER_ID );
        assert drawer != null;
        drawer.closeDrawer( GravityCompat.START );
        return true;
    }
}
