package no.glv.paco;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import no.glv.paco.SendSMSDialog.OnVerifySendSMSListener;
import no.glv.paco.android.BaseActivity;
import no.glv.paco.app.DataHandler;
import no.glv.paco.app.Utils;
import no.glv.paco.intrfc.Group;
import no.glv.paco.intrfc.Parent;
import no.glv.paco.intrfc.Phone;
import no.glv.paco.intrfc.Student;

/**
 * This activity will show all the students in a certain group.
 * <p/>
 * TODO: The activity should be able to: - Update the name of the class
 * TODO: Should be able to display a certain subclass (A, B, ...)
 *
 * @author GleVoll
 */
public class GroupViewActivity extends BaseActivity implements OnClickListener,
        AdapterView.OnItemClickListener, OnVerifySendSMSListener, OnLongClickListener {

    private static final String TAG = GroupViewActivity.class.getSimpleName();

    /**
     * Used to replace text from an EditText
     **/
    private static final String CLASS_REPLACE = "{klasse}";

    /**  */
    private Group stdClass;

    StudentListAdapter adapter;

    DataHandler dataHandler;

    /**
     *
     */
    public GroupViewActivity() {
        dataHandler = DataHandler.GetInstance();
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_std_classlist );

        stdClass = GetStudentClassExtra( this.getIntent() );

        String title = getResources().getString( R.string.activity_stdlist_title );
        title = title.replace( CLASS_REPLACE, stdClass.getName() );
        setTitle( title );

        ExpandableListView listView = ( ExpandableListView ) findViewById( R.id.student_listview );
        adapter = new StudentListAdapter( getApplicationContext(), stdClass.getStudents() );
        adapter.setBaseActivity( getBaseActivity() );
        listView.setDescendantFocusability( ViewGroup.FOCUS_AFTER_DESCENDANTS );
        listView.setAdapter( adapter );
    }

    /**
     * Callback from the <code>StudentListAdapter</code> when a specific row (Student) is clicked.
     *
     * @param position The number of the student in the list
     * @param id       The id in the row
     */
    @Override
    public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
        Student std = stdClass.getStudents().get( position );
        StudentViewActivity.StartActivity( this, std );
    }

    /**
     *
     */
    private void update() {
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        update();
    }

    /**
     *
     */
    public void onClick( View v ) {
        Log.d( TAG, "onClick: " + v );
    }

    @Override
    public boolean onLongClick( View v ) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.menu_stdclass_list, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int id = item.getItemId();

        switch ( id ) {
            case R.id.menu_stdlist_delete:
                deleteClass();
                break;

            case R.id.menu_stdList_sort_firstNameAsc:
                dataHandler.getSettingsManager().sortByFirstNameAsc( stdClass.getName() );
                update();
                return true;

            case R.id.menu_stdList_sort_lastNameAsc:
                dataHandler.getSettingsManager().sortByLastNameAsc( stdClass.getName() );
                update();
                return true;

            case R.id.menu_stdlist_mail:
                sendMail();
                return true;

            case R.id.menu_stdlist_sms:
                sendSMS();
                return true;

        }

        return super.onOptionsItemSelected( item );
    }

    /**
     * @param pList List of phones to send SMS to
     * @param msg   The message to send
     */
    public void verifySendSMS( List<Phone> pList, String msg ) {
        Toast.makeText( this, "Sendt SMS til " + pList.size() + " foresatt(e)", Toast.LENGTH_LONG ).show();
    }

    @Override
    public void verifySendSMS( Phone p, String msg ) {
    }

    /**
     * Sends the SMS message
     */
    public void sendSMS() {
        SendSMSDialog.StartFragment( stdClass, this, getFragmentManager() );
    }

    /**
     * Sends mail
     */
    private void sendMail() {
        ArrayList<String> list = new ArrayList<String>();

        for ( Student std : stdClass.getStudents() ) {
            for ( int i = 0; i < std.getParents().size(); i++ ) {
                Parent par = std.getParents().get( i );
                if ( par.getMail() != null )
                    list.add( par.getMail() );
            }
        }

        String[] mails = new String[list.size()];
        int i = 0;
        for ( String name : list ) {
            mails[i++] = name;
        }

        Intent intent = Utils.CreateMailIntent( mails, this );

        try {
            startActivity( Intent.createChooser( intent, "Send mail..." ) );
        } catch ( android.content.ActivityNotFoundException ex ) {
            Toast.makeText( this, "There are no email clients installed.", Toast.LENGTH_SHORT ).show();
        }
    }

    /**
     *
     */
    private void deleteClass() {
        boolean canDel = dataHandler.isGroupRemovable( stdClass );
        Builder builder = new Builder( this );
        String msg = null, title = null;

        if ( !canDel ) {
            msg = getResources().getString( R.string.stdlist_del_isInvolved_msg )
                    .replace( "{class}", stdClass.getName() )
                    .replace( "{task}", dataHandler.getGroupInvolvedInTask( stdClass ).toString() );
            title = getResources().getString( R.string.stdlist_del_isInvolved_title );

            builder.setIcon( R.drawable.ic_action_error );
            builder.setNeutralButton( R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick( DialogInterface dialog, int which ) {
                }
            } );
        } else {
            msg = getResources().getString( R.string.stdlist_del_msg ).replace( "{class}", stdClass.getName() );
            title = getResources().getString( R.string.stdlist_del_title );

            builder.setIcon( R.drawable.ic_action_alert );
            builder.setNegativeButton( getResources().getString( R.string.cancel ),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick( DialogInterface dialog, int which ) {
                            // Left empty on purpose
                        }
                    } );

            builder.setPositiveButton( getResources().getString( R.string.ok ), new DialogInterface.OnClickListener() {

                @Override
                public void onClick( DialogInterface dialog, int which ) {
                    String msg = getResources().getString( R.string.stdlist_del_done_msg ).replace( "{class}",
                            stdClass.getName() );
                    dataHandler.removeGroup( stdClass.getName() ).notifyGroupRemove( stdClass );
                    Toast.makeText( GroupViewActivity.this, msg, Toast.LENGTH_LONG ).show();
                    finish();
                }
            } );
        }

        builder.setMessage( msg ).setTitle( title );
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    /**
     * @param stdClassName Name of the Student class
     * @param ctx          Current appication context
     *
     * @return The new Intent
     */
    static final Intent CreateActivityIntent( String stdClassName, Context ctx ) {
        Intent intent = new Intent( ctx, GroupViewActivity.class );
        PutStudentClassExtra( stdClassName, intent );

        return intent;
    }
}
