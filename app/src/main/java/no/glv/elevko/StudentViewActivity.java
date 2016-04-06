package no.glv.elevko;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import no.glv.elevko.SendSMSDialog.OnVerifySendSMSListener;
import no.glv.elevko.android.BaseSwipeActivity;
import no.glv.elevko.app.DataHandler;
import no.glv.elevko.app.SettingsManager;
import no.glv.elevko.app.Utils;
import no.glv.elevko.intrfc.Parent;
import no.glv.elevko.intrfc.Phone;
import no.glv.elevko.intrfc.Student;

@SuppressWarnings("deprecation")
public class StudentViewActivity extends BaseSwipeActivity implements OnVerifySendSMSListener {

    private static final String TAG = StudentViewActivity.class.getSimpleName();

    Student bean;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        // Set up the action bar.
        //final ActionBar actionBar = getActionBar();
        //Log.d( TAG, "Actionbar: " + actionBar );
        //actionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_TABS );

        bean = getStudentByIdentExtra();
        setTitle( bean.getFirstName() + " " + bean.getLastName() );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.menu_std_info, menu );
        return true;
    }

    /**
     * @param v The view that sent the OnClick event
     */
    public void makeCall( View v ) {
        Phone p = null;
        Parent parent = ( Parent ) v.getTag();

        switch ( v.getId() ) {
            case R.id.IV_info_home_p1call:
                p = parent.getPhone( Phone.HOME );
                break;

            case R.id.IV_info_mob_p1call:
                p = parent.getPhone( Phone.MOBIL );
                break;

            case R.id.IV_info_work_p1call:
                p = parent.getPhone( Phone.WORK );
                break;

            default:
                break;
        }

        if ( p == null )
            return;

        Intent intent = Utils.CreateCallIntent( p );
        startActivity( intent );
    }

    /**
     * onClick method defined in the XML layout file. Will start the
     * SendSMSDialog fragment.
     */
    public void sendSMS( View v ) {
        Parent p = ( Parent ) v.getTag();
        SendSMSDialog.StartFragment( p.getPhone( Phone.MOBIL ), this, getFragmentManager() );
    }

    @Override
    public void verifySendSMS( List<Phone> p, String msg ) {
    }

    @Override
    public void verifySendSMS( Phone p, String msg ) {
    }

    /**
     * @param v The view that sent the OnClick event (ImageView)
     */
    public void sendMail( View v ) {
        Parent p = ( Parent ) v.getTag();

        Intent i = Utils.CreateMailIntent( new String[]{ p.getMail() }, this );
        startActivity( i );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        return super.onOptionsItemSelected( item );
    }

    @Override
    public int getLayoutID() {
        return R.layout.activity_std_view;
    }

    @Override
    public int getViewpagerID() {
        return R.id.VP_stdInfo_pager;
    }

    @Override
    public BaseSwipeFragment[] getFragments() {
        return new BaseSwipeFragment[]{
                new StdInfoFragment(), new StdParentPrimaryFragment(), new StdParentSecondaryFragment()
        };
    }

    @Override
    public String[] getTabTitles() {
        Locale l = Locale.getDefault();
        return new String[]{
                getString( R.string.std_info_title ).toUpperCase( l ),
                getString( R.string.std_parent1_title ).toUpperCase( l ),
                getString( R.string.std_parent2_title ).toUpperCase( l )
        };
    }

    /**
     *
     */
    public static class StdParentPrimaryFragment extends StdParentFragment {

        @Override
        protected Parent getParent( Student student ) {
            List<Parent> parents = bean.getParents();

            if ( parents.size() == 0 )
                return null;

            return parents.get( 0 );
        }
    }

    /**
     *
     */
    public static class StdParentSecondaryFragment extends StdParentFragment {

        @Override
        protected Parent getParent( Student student ) {
            List<Parent> parents = bean.getParents();

            if ( parents.size() < 2 )
                return null;

            return parents.get( 1 );
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public abstract static class StdParentFragment extends BaseSwipeFragment {

        protected Student bean;

        protected abstract Parent getParent( Student student );

        @Override
        public View doCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
            bean = ( ( StudentViewActivity ) getActivity() ).bean;
            Parent parent = getParent( bean );
            if ( parent == null ) return rootView;

            EditText editText = getEditText( R.id.ET_info_p1Name );
            editText.setText( parent.getFirstName() + " " + parent.getLastName() );

            editText = getEditText( R.id.ET_info_p1Mail );
            editText.setText( parent.getMail() );

            createPhoneView( parent, rootView );

            return rootView;
        }

        @Override
        protected int getLayoutID() {
            return R.layout.fr_student_view_parent;
        }

        /**
         *
         * @param parent
         * @param rootView
         */
        private void createPhoneView( Parent parent, View rootView ) {
            EditText editText = null;
            Phone phone = parent.getPhone( Phone.MOBIL );
            String number = phone == null ? "" : String.valueOf( phone.getNumber() );

            if ( phone != null ) {
                editText = ( EditText ) rootView.findViewById( R.id.ET_info_p1Phone_mob );
                editText.setText( number );
            }

            phone = parent.getPhone( Phone.WORK );
            number = phone == null ? "" : String.valueOf( phone.getNumber() );

            if ( phone != null ) {
                editText = ( EditText ) rootView.findViewById( R.id.ET_info_p1Phone_work );
                editText.setText( number );
            }

            phone = parent.getPhone( Phone.HOME );
            number = phone == null ? "" : String.valueOf( phone.getNumber() );

            if ( phone != null ) {
                editText = ( EditText ) rootView.findViewById( R.id.ET_info_p1Phone_home );
                editText.setText( number );
            }

            setTagOnImages( parent, rootView );
        }

        /**
         *
         * @param parent
         * @param rootView
         */
        private void setTagOnImages( Parent parent, View rootView ) {
            ImageView img = ( ImageView ) rootView.findViewById( R.id.IV_info_home_p1call );
            img.setTag( parent );

            img = ( ImageView ) rootView.findViewById( R.id.IV_info_mob_p1call );
            img.setTag( parent );

            img = ( ImageView ) rootView.findViewById( R.id.IV_info_work_p1call );
            img.setTag( parent );

            img = ( ImageView ) rootView.findViewById( R.id.IV_info_mob_p1msg );
            img.setTag( parent );

            img = ( ImageView ) rootView.findViewById( R.id.IV_info_p1Mail );
            img.setTag( parent );

        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class StdInfoFragment extends BaseSwipeFragment {

        private Student bean;

        @Override
        protected int getLayoutID() {
            return R.layout.fr_std_view_info;
        }

        @Override
        public View doCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
            bean = ( ( StudentViewActivity ) getActivity() ).bean;
            SettingsManager manager = DataHandler.GetInstance().getSettingsManager();

            TextView tv = getTextView( R.id.TV_info_header );
            // tv.setText( bean.getIdent() );

            EditText editText = getEditText( R.id.ET_info_firstName );
            editText.setText( bean.getFirstName() );

            editText = getEditText( R.id.ET_info_LastName );
            editText.setText( bean.getLastName() );

            editText = getEditText( R.id.ET_info_ident );
            editText.setText( bean.getIdent() );

            editText = getEditText( R.id.ET_info_birth );
            editText.setText( Utils.GetDateAsString( bean.getBirth() ) );

            editText = getEditText( R.id.ET_info_pc );
            editText.setText( bean.getPostalCode() );

            editText = getEditText( R.id.ET_info_adr );
            editText.setText( bean.getAdress() );

            tv = getTextView( R.id.TV_info_google );

            String msg = getResources().getString( R.string.stdInfo_google );
            msg = msg.replace( "{google}", bean.getIdent() + "@" + manager.getGoogleAccount() );
            tv.setPaintFlags( tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG );
            tv.setText( msg );

            return rootView;
        }
    }

    /**
     *
     * @param ctx
     * @param std
     */
    public static void StartActivity( Context ctx, Student std ) {
        Intent intent = new Intent( ctx, StudentViewActivity.class );
        PutIdentExtra( std, intent );

        ctx.startActivity( intent );

    }
}