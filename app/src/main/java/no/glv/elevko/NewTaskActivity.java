package no.glv.elevko;

import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import no.glv.elevko.core.BaseActivity;
import no.glv.elevko.core.DataHandler;
import no.glv.elevko.core.DatePickerDialogHelper;
import no.glv.elevko.core.Utils;
import no.glv.elevko.core.ViewGroupAdapter;
import no.glv.elevko.intrfc.Student;
import no.glv.elevko.intrfc.Task;

/**
 * Creates a new Task.
 * <p>
 * Uses the {@link AddClassesToTaskFragment} to collect the data.
 *
 * @author GleVoll
 */
public class NewTaskActivity extends BaseActivity implements OnClickListener, OnDateSetListener, AddedStudentsToTaskFragment
        .OnStudentsVerifiedListener {

    private static final String ST_SUBJECTS = "st.subjects";
    private static final String ST_TYPES = "st.types";

    private static final String TAG = NewTaskActivity.class.getSimpleName();
    private no.glv.elevko.intrfc.Task task;

    private ArrayList<String> mSubjectNames;
    private ArrayList<String> mTypesNames;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_new_task );

        // Get stored data
        if ( savedInstanceState != null ) {
            task = ( Task ) savedInstanceState.getSerializable( Task.EXTRA_TASKNAME );
            mSubjectNames = savedInstanceState.getStringArrayList( ST_SUBJECTS );
            mTypesNames = savedInstanceState.getStringArrayList( ST_TYPES );
        } else
            task = DataHandler.GetInstance().createTask();

        // Add button listeners
        Button btn = ( Button ) findViewById( R.id.BTN_newTask_create );
        btn.setOnClickListener( this );
        btn = ( Button ) findViewById( R.id.BTN_newTask_date );
        btn.setOnClickListener( this );

        // Set the todays date on the EditText
        EditText eText = ( EditText ) findViewById( R.id.ET_newTask_date );
        eText.setText( BaseActivity.GetDateAsString( Calendar.getInstance().getTime() ) );

        setupSpinners();

        // Load the classes that may be added to the task. Will be installed in a container.
        AddClassToTaskFragment fragment = new AddClassToTaskFragment();
        Bundle args = new Bundle();
        args.putSerializable( Task.EXTRA_TASKNAME, task );
        ViewGroupAdapter.BeginFragmentTransaction( getSupportFragmentManager(), fragment, args, R.id.LL_newTask_classes );
    }

    /**
     *
     */
    private void setupSpinners() {
        String[] subjects = getResources().getStringArray( R.array.task_subjects );
        String types[] = getResources().getStringArray( R.array.task_types );

        // Set the proper SubjectTypes to the spinners
        Utils.SetupSpinner( getSubjectSpinner(), getSubjectNames(), subjects[0], this );
        Utils.SetupSpinner( getTypeSpinner(), getTypesNames(), types[0], this );
    }

    public ArrayList<String> getTypesNames() {
        if ( mTypesNames == null ) {
            mTypesNames = new ArrayList<String>( DataHandler.GetInstance().getTypeNames() );
        }

        return mTypesNames;
    }

    private ArrayList<String> getSubjectNames() {
        if ( mSubjectNames == null ) {
            mSubjectNames = new ArrayList<String>( DataHandler.GetInstance().getSubjectNames() );
        }

        return mSubjectNames;
    }

    /**
     *
     */
    private Spinner getSubjectSpinner() {
        return ( Spinner ) findViewById( R.id.SP_newTask_subject );
    }

    private Spinner getTypeSpinner() {
        return ( Spinner ) findViewById( R.id.SP_newTask_type );
    }

    @Override
    protected void onSaveInstanceState( Bundle outState ) {
        super.onSaveInstanceState( outState );

        outState.putSerializable( Task.EXTRA_TASKNAME, task );
        outState.putStringArrayList( ST_SUBJECTS, mSubjectNames );
        outState.putStringArrayList( ST_TYPES, mTypesNames );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.new_task, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int id = item.getItemId();
        if ( id == R.id.action_settings ) {
            return true;
        }
        return super.onOptionsItemSelected( item );
    }

    /**
     * Callback from the button. Called to create the task or set the date
     *
     * @param v The view calling (create button or date button)
     *          <p>
     *          TODO: Should control the date input: What if name of something other is missing?
     */
    @Override
    public void onClick( View v ) {
        int id = v.getId();

        switch ( id ) {
            case R.id.BTN_newTask_create:
                createNewTask();
                break;

            case R.id.BTN_newTask_date:
                Log.d( TAG, v.toString() );

                DatePickerDialogHelper.OpenDatePickerDialog( new Date(), this, this, false, true );
            default:
                break;
        }

    }

    /**
     * @return true if task successfully created
     */
    private boolean createNewTask() {
        EditText editText = ( EditText ) findViewById( R.id.ET_newTask_name );
        String taskName = editText.getText().toString();
        if ( taskName == null || taskName.length() == 0 ) {
            Utils.DisplayToast( this, getString( R.string.newTask_missingname_toast ) );
            return false;
        }

        if ( task.getAddedClasses().size() == 0 ) {
            Utils.DisplayToast( this, getString( R.string.newTask_missingclass_toast ) );
            return false;
        }

        editText = ( EditText ) findViewById( R.id.ET_newTask_desc );
        String taskDesc = editText.getText().toString();

        editText = ( EditText ) findViewById( R.id.ET_newTask_date );
        String dateStr = editText.getText().toString();

        task.setName( taskName );
        task.setDescription( taskDesc );
        task.setDate( getDateFromString( dateStr ) );

        Spinner spSubject = getSubjectSpinner();
        //int subPos = spSubject.getSelectedItemPosition();
        String subStr = spSubject.getSelectedItem().toString();

        Spinner spType = getTypeSpinner();
        //int typePos = spType.getSelectedItemPosition();
        String typStr = spType.getSelectedItem().toString();

        task.setSubject( DataHandler.GetInstance().convertSubjectToID( subStr ) );
        task.setType( DataHandler.GetInstance().convertTypeToID( typStr ) );


        // Show FragmentDialog to confirm all the students in the task
        AddedStudentsToTaskFragment.StartFragment( task, this, getFragmentManager() );
        return true;
    }

    @Override
    public void onDateSet( DatePicker picker, int year, int monthOfYear, int dayOfMonth ) {
        EditText eText = ( EditText ) findViewById( R.id.ET_newTask_date );
        eText.setText( Utils.GetDateAsString( year, monthOfYear, dayOfMonth ) );
    }

    /**
     * Called by the {@link AddedStudentsToTaskFragment} fragment when the user
     * has chosen whom to include in the task.
     *
     * @param task
     */
    @Override
    public void onStudentsVerified( Task task ) {
        getDataHandler().addTask( task ).notifyTaskAdd( task );
        task.markAsCommitted();

        String msg = getResources().getString( R.string.newTask_added_toast );
        msg = msg.replace( "{task}", getDataHandler().getTaskDisplayName( task ) );
        Toast.makeText( this, msg, Toast.LENGTH_LONG ).show();

        finish();
    }

    @Override
    public void addStudent( Student std ) {
        task.addStudent( std );
    }

    @Override
    public void removeStudent( Student std ) {
        task.removeStudent( std.getIdent() );
    }

}
