package no.glv.paco;

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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import no.glv.paco.app.DataHandler;
import no.glv.paco.intrfc.Group;
import no.glv.paco.intrfc.Student;
import no.glv.paco.intrfc.Task;

/**
 * Used mainly by TaskActivity to add classes to the {@link Task} if needed.
 *
 * @author GleVoll
 */
public class AddGroupsToTaskFragment extends AddStudentsToTaskFragment implements AddedStudentsToTaskFragment.OnStudentsVerifiedListener {

    ListView listView;

    @Override
    protected int getTitle() {
        return R.string.task_class_title;
    }

    /**
     *
     * @param rootView
     */
    protected void buildButton( View rootView ) {
        final AddGroupsToTaskFragment fr = this;

        Button btn = ( Button ) rootView.findViewById( R.id.BTN_newTask_verifyStudents );
        btn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View v ) {
                AddedStudentsToTaskFragment.StartFragment( mTask, fr, fr.getFragmentManager() );
            }
        } );

        btn = ( Button ) rootView.findViewById( R.id.BTN_newTask_cancelStudents );
        btn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick( View v ) {
                getFragmentManager().beginTransaction().remove( fr ).commit();
            }
        } );
    }

    @Override
    public void onStudentsVerified( Task task ) {
        getFragmentManager().beginTransaction().remove( this ).commit();

        StringBuffer sb = new StringBuffer();
        Iterator<String> it = mTask.getAddedGroups().iterator();
        while ( it.hasNext() ) {
            sb.append( it.next() ).append( "\n" );
        }

        String msg = getActivity().getResources().getString( R.string.task_class_added );
        msg = msg.replace( "{class}", sb.toString() );

        Toast t = Toast.makeText( getActivity(), msg, Toast.LENGTH_LONG );
        DataHandler.GetInstance().commitStudentsTasks( mTask );
        t.show();
    }

    @Override
    public void addStudent( Student std ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeStudent( Student std ) {
        // TODO Auto-generated method stub

    }

    /**
     *
     * @param rootView
     */
    protected void buildAdapter( View rootView ) {
        if ( listView != null )
            return;

        List<Group> students = createClassList();

        listView = ( ListView ) rootView.findViewById( R.id.LV_newTask_addedStudents );
        AddClassesAdapter adapter = new AddClassesAdapter( getActivity(), R.id.LV_newTask_addedStudents, students );
        adapter.setTask( mTask );
        listView.setAdapter( adapter );
    }

    /**
     *
     * @return
     */
    protected List<Group> createClassList() {
        DataHandler dataHandler = DataHandler.GetInstance();
        List<String> classNames = dataHandler.getInstalledGroupNames();
        List<Group> stdClasses = new LinkedList<Group>();
        List<String> classesInTask = mTask.getGroups();

        Iterator<String> it = classNames.iterator();
        while ( it.hasNext() ) {
            String className = it.next();
            if ( !classesInTask.contains( className ) )
                stdClasses.add( dataHandler.getGroup( className ) );
        }

        return stdClasses;
    }

    /**
     * @author GleVoll
     */
    public static class AddClassesAdapter extends ArrayAdapter<Group> implements OnCheckedChangeListener {

        private Task mTask;

        public AddClassesAdapter( Context context, int resource, List<Group> objects ) {
            super( context, resource, objects );
        }

        void setTask( Task task ) {
            this.mTask = task;
        }

        /**
         *
         */
        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {
            Group stdClass = getItem( position );
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

            holder.studentIdent.setTag( stdClass );
            holder.studentIdent.setText( stdClass.getName() );

            holder.cBox.setTag( stdClass );
            holder.cBox.setOnCheckedChangeListener( this );

            return convertView;
        }

        @Override
        public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {
            Group stdClass = ( Group ) buttonView.getTag();
            if ( isChecked )
                mTask.addGroup( stdClass );
            else
                mTask.removeGroup( stdClass );
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
        AddGroupsToTaskFragment fragment = new AddGroupsToTaskFragment();

        fragment.setArguments( args );

        FragmentTransaction ft = manager.beginTransaction();
        fragment.show( ft, AddGroupsToTaskFragment.class.getSimpleName() );

    }

}
