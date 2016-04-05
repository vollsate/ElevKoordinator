package no.glv.elevko;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.FragmentTransaction;
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

import no.glv.elevko.core.BaseActivity;
import no.glv.elevko.core.BaseTabActivity_old;
import no.glv.elevko.core.DataComparator;
import no.glv.elevko.core.DataHandler;
import no.glv.elevko.core.DatePickerDialogHelper;
import no.glv.elevko.core.Utils;
import no.glv.elevko.intrfc.Student;
import no.glv.elevko.intrfc.StudentTask;
import no.glv.elevko.intrfc.SubjectType;
import no.glv.elevko.intrfc.Task;
import no.glv.elevko.intrfc.Task.OnTaskChangeListener;
import no.glv.elevko.intrfc.TaskController;
import no.glv.elevko.sql.DBUtils;

/**
 * Shows detailed information about a specific task.
 * <p>
 * The {@link Task} have information about itself and when it's due, and
 * information about all the students currently involved in the Task: pending,
 * cancelled or handed in (finished).
 *
 * @author GleVoll
 */
public class TaskActivity extends BaseTabActivity_old implements TaskController {

    private BaseTabFragment[] fragments;
    TaskStudentsFragment classesFragment;
    TaskInfoFragment infoFragment;

    /** The task this activity shows */
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
    protected String getTabTitle() {
        return getString( R.string.task_title );
    }

    @Override
    public BaseTabFragment[] getFragments() {
        if ( fragments == null ) {
            classesFragment = new TaskStudentsFragment();
            infoFragment = new TaskInfoFragment();

            fragments = new BaseTabFragment[]{ infoFragment, classesFragment };
        }

        return fragments;
    }

    @Override
    public String[] getTabTitles() {
        return new String[]{ getString( R.string.task_tab_section1 ), getString( R.string.task_tab_section2 ) };
    }

    @Override
    public int getLayoutID() {
        return R.layout.activity_task;
    }

    @Override
    public int getViewpagerID() {
        return R.id.VP_task_pager;
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

        AddClassesToTaskFragment.StartFragment( args, getFragmentManager() );
    }

    /**
     * Adds a student to the Task.
     * <p>
     * <p>
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
     * <p>
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
     * <p>
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
    public void onTabSelected( @SuppressWarnings("deprecation") ActionBar.Tab tab,
                               FragmentTransaction fragmentTransaction ) {
        super.onTabSelected( tab, fragmentTransaction );
    }

    @Override
    public void onTabUnselected( @SuppressWarnings("deprecation") ActionBar.Tab tab,
                                 FragmentTransaction fragmentTransaction ) {
    }

    @Override
    public void onTabReselected( @SuppressWarnings("deprecation") ActionBar.Tab tab,
                                 FragmentTransaction fragmentTransaction ) {
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
     * <p>
     * Depends on
     */
    public static class TaskInfoFragment extends BaseTabFragment implements OnDateSetListener,
            Task.OnTaskChangeListener {

        private Task task;
        private ArrayList<String> mSubjectNames;
        private ArrayList<String> mTypesNames;

        @Override
        public void onDestroy() {
            super.onDestroy();
            task.removeOnTaskChangeListener( this );
        }

        @Override
        public View doCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
            task = getTask();
            task.addOnTaskChangeListener( this );

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
            InstalledClassesInTaskFragment.NewInstance( task, getFragmentManager() );

            return rootView;
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
        private void setCounters() {
            getTextView( R.id.TV_task_studentCount ).setText( String.valueOf( task.getStudentCount() ) );
            getTextView( R.id.TV_task_pendingCount ).setText( String.valueOf( task.getStudentsPendingCount() ) );
            getTextView( R.id.TV_task_handinCount ).setText( String.valueOf( task.getStudentsHandedInCount() ) );
        }

        @Override
        public void onTaskChange( Task task, int mode ) {
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
            task.notifyChange( OnTaskChangeListener.MODE_DATE_CHANGE );
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
    public static class TaskStudentsFragment extends BaseTabFragment {

        StudentListAdapter adapter;
        Task mTask;

        @Override
        public View doCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
            // ListView listView = getListView( R.id.LV_task_students );
            ExpandableListView exListView = ( ExpandableListView ) rootView.findViewById( R.id.LV_task_students );
            mTask = ( ( TaskController ) getActivity() ).getTask();

            if ( adapter == null ) {
                adapter = new StudentListAdapter( getActivity(), BuildStudentList( mTask ) );
                mTask.addOnTaskChangeListener( adapter );
            }

            adapter.setTask( mTask );
            // listView.setAdapter( adapter );
            exListView.setAdapter( adapter );
            exListView.setDescendantFocusability( ViewGroup.FOCUS_AFTER_DESCENDANTS );
            return rootView;
        }

        @Override
        public void onDestroy() {
            mTask.removeOnTaskChangeListener( adapter );
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
            return R.layout.fragment_task_students;
        }

        @Override
        public void onCreate( Bundle savedInstanceState ) {
            super.onCreate( savedInstanceState );
        }

    }

    /**
     * The {@link ExpandableListAdapter} that shows the {@link StudentTask}s
     * that are involved in this {@link Task}.
     *
     * @author GleVoll
     */
    static class StudentListAdapter extends BaseExpandableListAdapter implements
            Task.OnTaskChangeListener, OnDateSetListener {

        private Task mTask;
        private List<StudentTask> stList;
        private Context mContext;

        private ViewGroup parent;

        /**
         * @param ctx
         * @param stdList
         */
        public StudentListAdapter( Context ctx, List<StudentTask> stdList ) {
            this.stList = stdList;
            mContext = ctx;
        }

        /**
         * @param task
         */
        public void setTask( Task task ) {
            this.mTask = task;
        }

        @Override
        public void onTaskChange( Task task, int mode ) {
            update();
        }

        /**
         * @param pos
         * @return
         */
        public StudentTask getItem( int pos ) {
            return stList.get( pos );
        }

        /**
         * @return
         */
        public Context getContext() {
            return mContext;
        }

        /**
         * @param position
         * @param convertView
         * @param parent
         * @return
         */
        public View getView( int position, View convertView, ViewGroup parent ) {
            StudentTask stdTask = getItem( position );
            Student std = stdTask.getStudent();

            if ( convertView == null ) {
                convertView = createView( getContext(), parent, stdTask );
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
            holder.classTV.setText( std.getStudentClass() );
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
         * @param holder
         * @param stdTask
         */
        private void setColors( ViewHolder holder, StudentTask stdTask ) {
            if ( stdTask.isHandedIn() )
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
         * @param stdTask
         * @return
         */
        private View createView( Context context, ViewGroup parent, StudentTask stdTask ) {
            LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            View myView = inflater.inflate( R.layout.row_task_stdlist, parent, false );
            ViewHolder holder = new ViewHolder();

            /*
            holder.imgInfoView = BaseActivity.GetImageView( myView, R.id.info );
            holder.imgInfoView.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick( View v ) {
                    StudentTask stdTask = ( StudentTask ) v.getTag();
                    StdInfoActivity.StartActivity( getContext(), stdTask.getStudent() );
                }
            } );
            */

            holder.imgDeleteView = BaseActivity.GetImageView( myView, R.id.delete );
            holder.imgDeleteView.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick( View v ) {
                    final StudentTask stdTask = ( StudentTask ) v.getTag();

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

            holder.chkBox = BaseActivity.GetCheckBox( myView, R.id.CB_task_stdlist );
            holder.chkBox.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged( CompoundButton v, boolean isChecked ) {
                    StudentTask stdTask = ( StudentTask ) v.getTag();

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
            stList.clear();
            stList.addAll( BuildStudentList( mTask ) );
            notifyDataSetChanged();
        }

        @Override
        public int getGroupCount() {
            return stList.size();
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
            View view = getView( groupPosition, convertView, parent );
            return view;
        }

        @Override
        public View getChildView( int groupPosition, int childPosition, boolean isLastChild, View convertView,
                                  ViewGroup parent ) {

            StudentTask st = getItem( groupPosition );
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
                    StudentTask st = holder.studentTask;

                    st.setComment( holder.etComment.getText().toString() );
                    task.markAsUpdated( st );
                    task.notifyChange( OnTaskChangeListener.MODE_STD_UPD );
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
            mTask.notifyChange( OnTaskChangeListener.MODE_DATE_CHANGE );
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
        StudentTask studentTask;
    }

    /**
     * @param task
     * @return
     */
    static List<StudentTask> BuildStudentList( Task task ) {
        List<StudentTask> stdTasks = task.getStudentsInTask();
        Collections.sort( stdTasks, new DataComparator.StudentTaskComparator( DataComparator.SORT_IDENT_ASC ) );

        return stdTasks;
    }

    static final Intent CreateActivityIntent( Integer taskName, Context ctx ) {
        Intent intent = new Intent( ctx, TaskActivity.class );
        PutTaskNameExtra( taskName, intent );

        return intent;
    }
}
