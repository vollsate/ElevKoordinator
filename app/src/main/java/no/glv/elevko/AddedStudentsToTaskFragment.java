package no.glv.elevko;

import android.app.Fragment;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import no.glv.elevko.core.DataHandler;
import no.glv.elevko.core.DialogFragmentBase;
import no.glv.elevko.intrfc.Student;
import no.glv.elevko.intrfc.StudentClass;
import no.glv.elevko.intrfc.Task;

/**
 * A dialog that will list every known student added to the task. The user may
 * choose to remove certain students from the list, if needed.
 *
 * @author GleVoll
 */
public class AddedStudentsToTaskFragment extends DialogFragmentBase {

    private Task task;
    private OnStudentsVerifiedListener listener;
    ListView listView;

    Task getTask() {
        if ( task == null ) {
            task = ( Task ) getArguments().getSerializable( Task.EXTRA_TASKNAME );
        }

        return task;
    }

    Task getTask( Bundle b ) {
        if ( b != null ) {
            String name = b.getString( Task.EXTRA_TASKNAME );
            task = DataHandler.GetInstance().getTask( name );
        } else
            task = getTask();

        return task;
    }

    public void setOnVerifiedListener( OnStudentsVerifiedListener listener ) {
        this.listener = listener;
    }

    @Override
    protected int getRootViewID() {
        return R.layout.fragment_students_newtask;
    }

    @Override
    protected int getTitle() {
        return R.string.newTask_addStudents_msg;
    }

    /**
     *
     */
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        getTask( savedInstanceState );
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState( Bundle outState ) {
        super.onSaveInstanceState( outState );

        outState.putString( Task.EXTRA_TASKNAME, task.getName() );
    }

    @Override
    public void buildView( View rootView ) {
        buildAdapter( rootView );
        buildButton( rootView );
    }

    /**
     *
     */
    private void buildButton( View rootView ) {
        final Fragment fr = this;

        Button btn = ( Button ) rootView.findViewById( R.id.BTN_newTask_verifyStudents );
        btn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View v ) {
                fr.getFragmentManager().beginTransaction().remove( fr ).commit();

                listener.onStudentsVerified( task );
            }
        } );

        btn = ( Button ) rootView.findViewById( R.id.BTN_newTask_cancelStudents );
        btn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View v ) {
                fr.getFragmentManager().beginTransaction().remove( fr ).commit();
            }
        } );
    }

    /**
     *
     */
    private void buildAdapter( View rootView ) {
        if ( listView != null )
            return;

        List<Student> students = createStudentList();

        listView = ( ListView ) rootView.findViewById( R.id.LV_newTask_addedStudents );
        AddedStudentsAdapter adapter = new AddedStudentsAdapter( getActivity(), R.id.LV_newTask_addedStudents, students );
        adapter.setTask( task );
        listView.setAdapter( adapter );
    }

    /**
     * @return
     */
    private List<Student> createStudentList() {
        List<String> mClasses = getTask().getClasses();
        List<Student> students = new ArrayList<Student>();

        Iterator<String> it = mClasses.iterator();
        while ( it.hasNext() ) {
            String className = it.next();
            StudentClass stdClass = DataHandler.GetInstance().getStudentClass( className );
            students.addAll( stdClass.getStudents() );
        }

        return students;
    }

    /**
     * @author GleVoll
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
            ViewHolder holder = null;

            LayoutInflater inflater = ( LayoutInflater ) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            if ( convertView == null ) {
                convertView = inflater.inflate( R.layout.row_newtask_students, parent, false );
                holder = new ViewHolder();
                holder.studentIdent = ( TextView ) convertView.findViewById( R.id.TV_newTask_studentIdent );
                holder.cBox = ( CheckBox ) convertView.findViewById( R.id.CB_newTask_addStudent );

                convertView.setTag( holder );
            }

            holder = ( ViewHolder ) convertView.getTag();

            Student std = getItem( position );
            String text = DataHandler.GetInstance().getSettingsManager().getStdInfoWhenNewTask( std );

            holder.studentIdent.setTag( std );
            holder.studentIdent.setText( text );

            holder.cBox.setTag( std );
            holder.cBox.setChecked( true );
            holder.cBox.setOnCheckedChangeListener( this );

            return convertView;
        }

        @Override
        public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {
            Student std = ( Student ) buttonView.getTag();
            if ( !isChecked )
                task.removeStudent( std.getIdent() );
            else
                task.addStudent( std );
        }
    }

    /**
     * @param task
     * @param listener
     * @param manager
     * @return
     */
    public static AddedStudentsToTaskFragment StartFragment( Task task, OnStudentsVerifiedListener listener,
                                                             FragmentManager manager ) {
        AddedStudentsToTaskFragment fragment = new AddedStudentsToTaskFragment();
        Bundle args = new Bundle();
        args.putSerializable( Task.EXTRA_TASKNAME, task );
        fragment.setArguments( args );

        fragment.setOnVerifiedListener( listener );

        FragmentTransaction ft = manager.beginTransaction();
        fragment.show( ft, fragment.getClass().getSimpleName() );

        return fragment;

    }

    static class ViewHolder {
        TextView studentIdent;
        CheckBox cBox;
    }

    /**
     * @author GleVoll
     */
    public interface OnStudentsVerifiedListener {

        String EXTRA_NAME = OnStudentsVerifiedListener.class.getSimpleName();

        void onStudentsVerified( Task task );

        void addStudent( Student std );

        void removeStudent( Student std );
    }

}
