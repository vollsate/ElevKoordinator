package no.glv.elevko;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import no.glv.elevko.core.DataHandler;
import no.glv.elevko.base.DialogFragmentBase;
import no.glv.elevko.intrfc.Student;
import no.glv.elevko.intrfc.StudentClass;
import no.glv.elevko.intrfc.StudentTask;
import no.glv.elevko.intrfc.Task;

/**
 * Used by the TaskActivity to add students to a task.
 * 
 * <p>
 * Students can only be added if they are already deleted - no need to add
 * students if every one is already a participant in the task.
 * 
 * @author GleVoll
 *
 */
public class AddStudentsToTaskFragment extends DialogFragmentBase {

    /** The task to add students to */
    Task mTask;
    ListView listView;

    /**
     * Will load the task from either a saved instance bundle or from arguments
     * sent when the fragment was started.
     * 
     * @return
     */
    Task getTask( Bundle savedInstanceState ) {
        if ( mTask != null )
            return mTask;

        String name = null; // Name of the task
        if ( savedInstanceState != null ) {
            name = savedInstanceState.getString( Task.EXTRA_TASKNAME );
        } else {
            name = getArguments().getString( Task.EXTRA_TASKNAME );
        }

        mTask = DataHandler.GetInstance().getTask( name );
        return mTask;
    }

    @Override
    protected int getRootViewID() {
        return R.layout.fragment_students_newtask;
    }

    @Override
    protected int getTitle() {
        return R.string.newTask_addStudents_msg;
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        getTask( savedInstanceState );
    }

    @Override
    public void onSaveInstanceState( Bundle outState ) {
        super.onSaveInstanceState( outState );
        outState.putString( Task.EXTRA_TASKNAME, mTask.getName() );
    }

    @Override
    public void buildView( View rootView ) {
        buildAdapter( rootView );
        buildButton( rootView );
    }

    /**
     * Creates the button view. When clicked, will show a Toast informing all
     * students added to the task.
     */
    protected void buildButton( View rootView ) {
        final AddStudentsToTaskFragment fr = this;

        Button btn = (Button) rootView.findViewById( R.id.BTN_newTask_verifyStudents );
        btn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View v ) {
                List<StudentTask> stdTasks = mTask.getAddedStudents();
                Iterator<StudentTask> it = stdTasks.iterator();
                StringBuffer sb = new StringBuffer();
                String msg = fr.getActivity().getResources().getString( R.string.task_student_added );

                while ( it.hasNext() ) {
                    Student std = it.next().getStudent();
                    String stdName = std.getLastName() + ", " + std.getFirstName();

                    sb.append( stdName ).append( "\n" );
                }
                msg = msg.replace( "{std}", sb.toString() );

                Toast t = Toast.makeText( fr.getActivity(), msg, Toast.LENGTH_LONG );
                DataHandler.GetInstance().commitStudentsTasks( mTask );
                t.show();

                fr.finish();
            }
        } );

        // Cancel button - will simply kill the fragment
        btn = (Button) rootView.findViewById( R.id.BTN_newTask_cancelStudents );
        btn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View v ) {
                getFragmentManager().beginTransaction().remove( fr ).commit();
            }
        } );
    }

    /**
     * 
     */
    protected void buildAdapter( View rootView ) {
        if ( listView != null )
            return;

        List<Student> students = createStudentList();

        listView = (ListView) rootView.findViewById( R.id.LV_newTask_addedStudents );
        AddedStudentsAdapter adapter = new AddedStudentsAdapter( getActivity(), R.id.LV_newTask_addedStudents,
                students );
        adapter.setTask( mTask );
        listView.setAdapter( adapter );
    }

    /**
     * 
     * @return
     */
    protected List<Student> createStudentList() {
        List<String> mClasses = mTask.getClasses();
        List<Student> students = new ArrayList<Student>();

        Iterator<String> it = mClasses.iterator();
        while ( it.hasNext() ) {
            String className = it.next();
            StudentClass stdClass = DataHandler.GetInstance().getStudentClass( className );

            Iterator<Student> itStd = stdClass.getStudents().iterator();
            while ( itStd.hasNext() ) {
                Student std = itStd.next();
                if ( mTask.hasStudent( std.getIdent() ) )
                    continue;

                students.add( std );
            }
        }

        return students;
    }

    /**
     * 
     * @author GleVoll
     *
     */
    public static class AddedStudentsAdapter extends ArrayAdapter<Student> implements OnCheckedChangeListener {

        private Task task;

        public AddedStudentsAdapter( Context context, int resource, List<Student> objects ) {
            super( context, resource, objects );
        }

        void setTask( Task task ) {
            this.task = task;
        }

        /**
         * 
         */
        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {
            Student std = getItem( position );
            ViewHolder holder = null;

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            if ( convertView == null ) {
                convertView = inflater.inflate( R.layout.row_newtask_students, parent, false );
                holder = new ViewHolder();
                holder.studentIdent = (TextView) convertView.findViewById( R.id.TV_newTask_studentIdent );
                holder.cBox = (CheckBox) convertView.findViewById( R.id.CB_newTask_addStudent );
                holder.cBox.setOnCheckedChangeListener( this );

                convertView.setTag( holder );
            }

            holder = (ViewHolder) convertView.getTag();

            String text = DataHandler.GetInstance().getSettingsManager().getStdInfoWhenNewTask( std );

            holder.studentIdent.setTag( std );
            holder.studentIdent.setText( text );

            holder.cBox.setTag( std );

            return convertView;
        }

        @Override
        public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {
            Student std = (Student ) buttonView.getTag();
            if ( !isChecked )
                task.removeStudent( std.getIdent() );
            else
                task.addStudent( std );
        }
    }

    static class ViewHolder {
        TextView studentIdent;
        CheckBox cBox;
    }

    /**
     * 
     * @param args
     * @param manager
     */
    static void StartFragment( Bundle args, FragmentManager manager ) {
        AddStudentsToTaskFragment fragment = new AddStudentsToTaskFragment();

        fragment.setArguments( args );

        FragmentTransaction ft = manager.beginTransaction();
        fragment.show( ft, AddStudentsToTaskFragment.class.getSimpleName() );

    }

}
