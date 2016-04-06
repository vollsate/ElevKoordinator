package no.glv.elevko;

import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import no.glv.elevko.android.BaseActivity;
import no.glv.elevko.android.BaseTabActivity;
import no.glv.elevko.android.DatePickerDialogHelper;
import no.glv.elevko.app.DataComparator;
import no.glv.elevko.app.DataHandler;
import no.glv.elevko.app.Utils;
import no.glv.elevko.data.DBUtils;
import no.glv.elevko.intrfc.Assignment;
import no.glv.elevko.intrfc.Student;
import no.glv.elevko.intrfc.SubjectType;
import no.glv.elevko.intrfc.Task;
import no.glv.elevko.intrfc.Task.OnAssignmentChangeListener;
import no.glv.elevko.intrfc.TaskController;

/**
 * Shows detailed information about a specific task.
 * <p/>
 * The {@link Task} have information about itself and when it's due, and
 * information about all the students currently involved in the Task: pending,
 * cancelled or handed in (finished).
 *
 * @author GleVoll
 */
@SuppressWarnings("deprecation")
public class TaskViewActivity extends BaseTabActivity implements TaskController {

    private BaseSwipeFragment[] fragments;
    TaskStudentsFragment classesFragment;
    TaskInfoFragment infoFragment;

    /**
     * The task this activity shows
     */
    Task mTask;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        getTask();
        setTitle( mTask.getName() );
    }

    @Override
    protected void onPause() {
        super.onPause();

        updateTask();
    }

    @Override
    public Task getTask() {
        if ( mTask == null )
            mTask = getDataHandler().getTask( getTaskName() );

        return mTask;
    }

    @Override
    public BaseSwipeFragment[] getFragments() {
        if ( fragments == null ) {
            classesFragment = new TaskStudentsFragment();
            infoFragment = new TaskInfoFragment();

            fragments = new BaseSwipeFragment[]{ infoFragment, classesFragment };
        }

        return fragments;
    }

    public String[] getTabTitles() {
        return new String[]{ getString( R.string.task_tab_section1 ), getString( R.string.task_tab_section2 ) };
    }

    @Override
    public int getLayoutID() {
        return R.layout.activity_task_view;
    }

    @Override
    public int getViewpagerID() {
        return R.id.container;
    }

    @Override
    protected int getTabLayoutID() {
        return R.id.tabs;
    }

    @Override
    public Integer getTaskName() {
        return GetTaskNameExtra( getIntent() );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.task, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch ( item.getItemId() ) {
            case R.id.task_action_addStudent:
                addStudent();
                return true;

            case R.id.task_action_addClass:
                addClass();
                return true;

            case R.id.task_action_Delete:
                deleteTask();
                return true;

            case R.id.task_action_close:
                getDataHandler().closeTask( mTask );
                finish();
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected( item );
    }

    /**
     * Adds a class to the {@link Task}. The fragment started will only show the
     * available classes, the ones the system has installed.
     */
    private void addClass() {
        Bundle args = new Bundle();
        args.putString( Task.EXTRA_TASKNAME, mTask.getName() );

        AddGroupsToTaskFragment.StartFragment( args, getFragmentManager() );
    }

    /**
     * Adds a student to the Task.
     * <p/>
     * <p/>
     * Uses the {@link AddStudentsToTaskFragment} to add any students that the
     * task has either deleted, or not added.
     */
    private void addStudent() {
        Bundle args = new Bundle();
        args.putString( Task.EXTRA_TASKNAME, mTask.getName() );

        AddStudentsToTaskFragment.StartFragment( args, getFragmentManager() );
    }

    /**
     * Will attempt to delete the Task and then finish the activity.
     * <p/>
     * Will first show an {@link AlertDialog} that will ask to confirm the
     * deletion. If so, the task is removed and the activity is finished.
     */
    private void deleteTask() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );

        builder.setTitle( getResources().getString( R.string.task_delete_title ) );
        builder.setMessage( mTask.getName() );

        builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick( DialogInterface dialog, int which ) {
                // Delete and finish
                if ( getDataHandler().deleteTask( mTask ) ) {
                    finish();
                }
            }
        } );

        builder.setNegativeButton( R.string.cancel, null );

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Updates the task in the database. This will collect all the information known about the task,
     * and update its signature in the database.
     * <p/>
     * The current students involved will not be affected.
     */
    private void updateTask() {
        // Get the basic string data
        String newName = ( ( TextView ) findViewById( R.id.ET_task_name ) ).getText().toString();
        String newDesc = ( ( TextView ) findViewById( R.id.ET_task_desc ) ).getText().toString();
        String newDate = ( ( TextView ) findViewById( R.id.ET_task_date ) ).getText().toString();

        // Get the subject types
        String subject = ( ( Spinner ) findViewById( R.id.SP_task_subject ) ).getSelectedItem().toString();
        String type = ( ( Spinner ) findViewById( R.id.SP_task_type ) ).getSelectedItem().toString();
        int iSub = getDataHandler().convertSubjectToID( subject );
        int iTyp = getDataHandler().convertTypeToID( type );

        Date date = getDateFromString( newDate );
        int oldID = mTask.getID();

        mTask.setName( newName );
        mTask.setDescription( newDesc );
        mTask.setDate( date );
        mTask.setSubject( iSub );
        mTask.setType( iTyp );

        getDataHandler().updateTask( mTask, oldID ).notifyTaskUpdate( mTask );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if ( mTask.isModified() ) {
            getDataHandler().commitStudentsTasks( mTask );
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    //
    // TaskInfoFragment
    //
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Shows the task info view.
     * <p/>
     * Depends on
     */
    public static class TaskInfoFragment extends BaseSwipeFragment implements OnDateSetListener,
            OnAssignmentChangeListener {

        private Task task;
        private ArrayList<String> mSubjectNames;
        private ArrayList<String> mTypesNames;

        @Override
        public void onDestroy() {
            super.onDestroy();
            task.unregisterOnAssignmentChangeListener( this );
        }

        @Override
        public View doCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
            task = getTask();
            task.registerOnAssignmentChangeListener( this );

            getTextView( R.id.TV_task_header ).setText( getString( R.string.task_header ) );

            getEditText( R.id.ET_task_name ).setText( task.getName() );
            InputMethodManager imm = ( InputMethodManager ) getActivity().getSystemService( Context.INPUT_METHOD_SERVICE );
            imm.hideSoftInputFromWindow( getEditText( R.id.ET_task_name ).getWindowToken(), 0 );

            getEditText( R.id.ET_task_desc ).setText( task.getDesciption() );
            getEditText( R.id.ET_task_date ).setText( BaseActivity.GetDateAsString( task.getDate() ) );

            Button btn = getButton( R.id.BTN_task_date );
            btn.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick( View v ) {
                    DatePickerDialogHelper.OpenDatePickerDialog( task.getDate(), getActivity(), TaskInfoFragment.this,
                            false, true );

                }
            } );

            Spinner sp = getSinner( R.id.SP_task_subject );
            SubjectType st = DataHandler.GetInstance().getSubjectType( task.getSubject() );
            Utils.SetupSpinner( sp, getSubjectNames(), st.getName(), getActivity() );
            sp = getSinner( R.id.SP_task_type );
            st = DataHandler.GetInstance().getSubjectType( task.getType() );
            Utils.SetupSpinner( sp, getTypesNames(), st.getName(), getActivity() );

            setCounters();
            InstalledGroupsInTaskFragment.NewInstance( task, getFragmentManager() );

            return rootView;
        }

        public ArrayList<String> getTypesNames() {
            if ( mTypesNames == null ) {
                mTypesNames = new ArrayList<>( DataHandler.GetInstance().getTypeNames() );
            }

            return mTypesNames;
        }

        private ArrayList<String> getSubjectNames() {
            if ( mSubjectNames == null ) {
                mSubjectNames = new ArrayList<>( DataHandler.GetInstance().getSubjectNames() );
            }

            return mSubjectNames;
        }

        /**
         *
         */
        private void setCounters() {
            getTextView( R.id.TV_task_studentCount ).setText( String.valueOf( task.getStudentCount() ) );
            getTextView( R.id.TV_task_pendingCount ).setText( String.valueOf( task.getAssignmentsPendingCount() ) );
            getTextView( R.id.TV_task_handinCount ).setText( String.valueOf( task.getStudentsHandedInCount() ) );
        }

        @Override
        public void onAssignmentChange( Task task, int mode ) {
            setCounters();
        }

        /**
         *
         */
        private Task getTask() {
            return ( ( TaskController ) getActivity() ).getTask();
        }

        @Override
        public void onDateSet( DatePicker view, int year, int monthOfYear, int dayOfMonth ) {
            Calendar cal = Calendar.getInstance();
            cal.set( year, monthOfYear, dayOfMonth );
            task.setDate( cal.getTime() );

            getEditText( R.id.ET_task_date ).setText( BaseActivity.GetDateAsString( task.getDate() ) );
            task.notifyChange( OnAssignmentChangeListener.DATE_CHANGE );
        }

        @Override
        protected int getLayoutID() {
            return R.layout.fragment_task_info;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    //
    // TaskStudentsFragment
    //
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * Shows all the students in this task. Will allow for adding, deletion and
     * hand-in of students.
     */
    public static class TaskStudentsFragment extends BaseSwipeFragment {

        StudentListAdapter adapter;
        Task mTask;

        @Override
        public View doCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
            // ListView listView = getListView( R.id.LV_task_students );
            ExpandableListView exListView = ( ExpandableListView ) rootView.findViewById( R.id.LV_task_students );
            mTask = ( ( TaskController ) getActivity() ).getTask();

            if ( adapter == null ) {
                adapter = new StudentListAdapter( getActivity(), BuildStudentList( mTask ) );
                mTask.registerOnAssignmentChangeListener( adapter );
            }

            adapter.setTask( mTask );
            // listView.setAdapter( adapter );
            exListView.setAdapter( adapter );
            exListView.setDescendantFocusability( ViewGroup.FOCUS_AFTER_DESCENDANTS );
            return rootView;
        }

        @Override
        public void onDestroy() {
            mTask.unregisterOnAssignmentChangeListener( adapter );
            super.onDestroy();
        }

        @Override
        public void onResume() {
            super.onResume();
            adapter.setTask( mTask );
        }

        @Override
        public void onSaveInstanceState( Bundle outState ) {
            super.onSaveInstanceState( outState );
            outState.putString( Task.EXTRA_TASKNAME, mTask.getName() );
        }

        @Override
        protected int getLayoutID() {
            return R.layout.fr_task_view_students;
        }

        @Override
        public void onCreate( Bundle savedInstanceState ) {
            super.onCreate( savedInstanceState );
        }

    }

    /**
     * The {@link ExpandableListAdapter} that shows the {@link Assignment}s
     * that are involved in this {@link Task}.
     *
     * @author GleVoll
     */
    static class StudentListAdapter extends BaseExpandableListAdapter implements
            OnAssignmentChangeListener, OnDateSetListener {

        private Task mTask;
        private List<Assignment> assignmentList;
        private Context mContext;

        private ViewGroup parent;

        /**
         * @param assignments All known assignments in this task, both completed and pending
         */
        public StudentListAdapter( Context ctx, List<Assignment> assignments ) {
            this.assignmentList = assignments;
            mContext = ctx;
        }

        /**
         * @param task Sets the task this adapter controls
         */
        public void setTask( Task task ) {
            this.mTask = task;
        }

        @Override
        public void onAssignmentChange( Task task, int mode ) {
            update();
        }

        /**
         * @return The specified assignment at current position
         */
        public Assignment getItem( int pos ) {
            return assignmentList.get( pos );
        }

        /**
         * @return The context this adapter adheres
         */
        public Context getContext() {
            return mContext;
        }

        /**
         * @param position    current position
         * @param convertView Previous stored view
         * @param parent      Owner of this view
         *
         * @return New updated view
         */
        public View getView( int position, View convertView, ViewGroup parent ) {
            Assignment stdTask = getItem( position );
            Student std = stdTask.getStudent();

            if ( convertView == null ) {
                convertView = createConvertView( getContext(), parent, stdTask );
            }

            if ( position % 2 == 0 )
                convertView.setBackgroundColor( getContext().getResources().getColor(
                        R.color.task_stdlist_dark ) );
            else
                convertView.setBackgroundColor( getContext().getResources().getColor( R.color.task_stdlist_light ) );

            ViewHolder holder = ( ViewHolder ) convertView.getTag();
            holder.nameTV.setTag( stdTask );
            holder.nameTV.requestFocus();
            holder.identTV.setTag( stdTask );
            holder.classTV.setTag( stdTask );
            holder.handinDateTV.setTag( stdTask );

            holder.imgDeleteView.setTag( stdTask );
            if ( stdTask.isHandedIn() )
                holder.imgDeleteView.setVisibility( View.INVISIBLE );
            else
                holder.imgDeleteView.setVisibility( View.VISIBLE );

            holder.chkBox.setTag( stdTask );

            holder.id = position;

            holder.nameTV.setText( std.getFirstName() );
            holder.identTV.setText( std.getIdent() );
            holder.classTV.setText( std.getGroupName() );
            holder.chkBox.setChecked( stdTask.isHandedIn() );

            boolean isExpired = mTask.isExpired();
            String handinDate = getContext().getResources().getString( R.string.task_handin );

            if ( stdTask.isHandedIn() )
                handinDate += DBUtils.ConvertToString( stdTask.getHandInDate() );
            else if ( !isExpired )
                handinDate = getContext().getResources().getString( R.string.task_pending );
            else
                handinDate = getContext().getResources().getString( R.string.task_expired );

            holder.handinDateTV.setText( handinDate );

            setColors( holder, stdTask );

            return convertView;
        }

        /**
         * Sets the proper color on text views in the task. Depending on weather task is complede/pending or weather
         * the date is due/not due.
         *
         * @param holder Keeper of text views to change color on
         */
        private void setColors( ViewHolder holder, Assignment assignment ) {
            if ( assignment.isHandedIn() )
                holder.handinDateTV.setTextColor( Color.BLACK );

            else if ( mTask.isExpired() )
                holder.handinDateTV.setTextColor( Color.RED );
            else
                holder.handinDateTV.setTextColor( Color.BLUE );
        }

        /**
         *
         * @param context
         * @param parent
         * @param assignment
         * @return
         */
        private View createConvertView( Context context, ViewGroup parent, Assignment assignment ) {
            LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            View myView = inflater.inflate( R.layout.row_task_stdlist, parent, false );
            ViewHolder holder = new ViewHolder();

            // Functionality for removing a student from this task
            holder.imgDeleteView = BaseActivity.GetImageView( myView, R.id.delete );
            holder.imgDeleteView.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick( View v ) {
                    final Assignment stdTask = ( Assignment ) v.getTag();

                    AlertDialog.Builder builder = new AlertDialog.Builder( getContext() );
                    String msg = getContext().getResources().getString( R.string.task_std_delete_msg );
                    msg = msg.replace( "{name}", stdTask.getStudent().getLastName() + ", "
                            + stdTask.getStudent().getFirstName() );

                    builder.setMessage( msg ).setTitle( R.string.task_std_delete_title );
                    builder.setPositiveButton( "OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick( DialogInterface dialog, int which ) {
                            Task task = mTask;
                            task.removeStudent( stdTask.getIdent() );
                            DataHandler.GetInstance().commitStudentsTasks( task );

                            String stdName = stdTask.getStudent().getLastName() + ", "
                                    + stdTask.getStudent().getFirstName();
                            String msg = getContext().getResources().getString( R.string.task_student_deleted );
                            msg = msg.replace( "{std}", stdName );
                            Toast t = Toast.makeText( getContext(), msg, Toast.LENGTH_LONG );
                            t.show();
                        }
                    } );

                    builder.setNegativeButton( "Avbryt", null );

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            } );

            // Functionality for student checking in assignments
            holder.chkBox = BaseActivity.GetCheckBox( myView, R.id.CB_task_stdlist );
            holder.chkBox.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged( CompoundButton v, boolean isChecked ) {
                    Assignment stdTask = ( Assignment ) v.getTag();

                    if ( stdTask.isHandedIn() == isChecked )
                        return;

                    DataHandler.GetInstance().handInTask( mTask, stdTask );
                }
            } );

            holder.nameTV = ( TextView ) myView.findViewById( R.id.TV_task_stdlist_name );
            holder.identTV = ( TextView ) myView.findViewById( R.id.TV_task_stdlist_ident );
            holder.classTV = ( TextView ) myView.findViewById( R.id.TV_task_stdlist_class );
            holder.handinDateTV = ( TextView ) myView.findViewById( R.id.TV_task_stdlist_handinDate );

            myView.setTag( holder );

            return myView;
        }

        /**
         *
         */
        public void update() {
            assignmentList.clear();
            assignmentList.addAll( BuildStudentList( mTask ) );
            notifyDataSetChanged();
        }

        @Override
        public int getGroupCount() {
            return assignmentList.size();
        }

        @Override
        public int getChildrenCount( int groupPosition ) {
            return 1;
        }

        @Override
        public Object getGroup( int groupPosition ) {
            return getItem( groupPosition );
        }

        @Override
        public Object getChild( int groupPosition, int childPosition ) {
            return getItem( groupPosition );
        }

        @Override
        public long getGroupId( int groupPosition ) {
            return groupPosition;
        }

        @Override
        public long getChildId( int groupPosition, int childPosition ) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView( int groupPosition, boolean isExpanded, View convertView, ViewGroup parent ) {
            return getView( groupPosition, convertView, parent );
        }

        @Override
        public View getChildView( int groupPosition, int childPosition, boolean isLastChild, View convertView,
                                  ViewGroup parent ) {

            Assignment st = getItem( groupPosition );
            this.parent = parent;
            final Task task = DataHandler.GetInstance().getTask( st.getTaskID() );

            if ( convertView == null ) {
                final LayoutInflater inflater = ( LayoutInflater ) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE );
                convertView = inflater.inflate( R.layout.row_task_stditem, parent, false );

                Button btn = ( Button ) convertView.findViewById( R.id.BTN_task_date );
                btn.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick( View v ) {
                        DatePickerDialogHelper.OpenDatePickerDialog( task.getDate(), getContext(),
                                StudentListAdapter.this,
                                false, true );

                    }
                } );

                ViewItemHolder itemHolder = new ViewItemHolder();
                convertView.setTag( itemHolder );
            }

            ViewItemHolder holder = ( ViewItemHolder ) convertView.getTag();
            holder.studentTask = st;

            // Set the comment, if any
            EditText etComment = ( EditText ) convertView.findViewById( R.id.ET_task_stditem_comment );
            if ( st.getComment() != null && st.getComment().length() > 0 ) {
                etComment.setText( st.getComment() );
            } else {
                etComment.setText( "" );
            }
            holder.etComment = etComment;

            // Set the date, if handed in
            EditText etDate = ( EditText ) convertView.findViewById( R.id.ET_task_stditem_date );
            if ( st.isHandedIn() ) {
                etDate.setText( Utils.GetDateAsString( st.getHandInDate() ) );
            }
            holder.etDate = etDate;

            ImageView imgSave = ( ImageView ) convertView.findViewById( R.id.IV_task_stditem_save );
            imgSave.setTag( holder );
            imgSave.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick( View v ) {
                    ViewItemHolder holder = ( ViewItemHolder ) v.getTag();
                    Assignment st = holder.studentTask;

                    st.setComment( holder.etComment.getText().toString() );
                    task.markAsUpdated( st );
                    task.notifyChange( OnAssignmentChangeListener.STD_UPDATE );
                    DataHandler.GetInstance().commitStudentsTasks( task );

                    Toast.makeText( getContext(), R.string.task_stditem_saved, Toast.LENGTH_SHORT ).show();
                }
            } );

            return convertView;
        }

        /**
         *
         */
        public void onDateSet( DatePicker view, int year, int monthOfYear, int dayOfMonth ) {
            Calendar cal = Calendar.getInstance();
            cal.set( year, monthOfYear, dayOfMonth );
            mTask.setDate( cal.getTime() );

            EditText et = ( EditText ) parent.findViewById( R.id.ET_task_stditem_date );
            et.setText( BaseActivity.GetDateAsString( mTask.getDate() ) );
            mTask.notifyChange( OnAssignmentChangeListener.DATE_CHANGE );
        }

        @Override
        public boolean isChildSelectable( int groupPosition, int childPosition ) {
            return false;
        }

    }

    /**
     * @author GleVoll
     */
    static class ViewHolder {
        int id;

        TextView nameTV;
        TextView identTV;
        TextView classTV;
        TextView handinDateTV;
        // ImageView imgInfoView;
        ImageView imgDeleteView;
        CheckBox chkBox;

    }

    /**
     * @author glevoll
     */
    static class ViewItemHolder {
        int id;

        EditText etComment;
        EditText etDate;
        Assignment studentTask;
    }

    /**
     * @param task The task to get students from
     *
     * @return List of all students assigned to the task
     */
    static List<Assignment> BuildStudentList( Task task ) {
        List<Assignment> stdTasks = task.getAssignmentList();
        Collections.sort( stdTasks, new DataComparator.StudentTaskComparator( DataComparator.SORT_IDENT_ASC ) );

        return stdTasks;
    }

    /**
     * Creates an <code>Intent</code> for starting this activity
     *
     * @param taskID The ID of the task to be viewed by the manager. The task will be stored as an extra parameter
     *               with the Intent.
     *
     * @return An intent able to start this activity.
     */
    public static Intent CreateActivityIntent( Integer taskID, Context ctx ) {
        Intent intent = new Intent( ctx, TaskViewActivity.class );
        PutTaskNameExtra( taskID, intent );

        return intent;
    }
}
