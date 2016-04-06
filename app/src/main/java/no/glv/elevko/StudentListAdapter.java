package no.glv.elevko;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import no.glv.elevko.android.BaseActivity;
import no.glv.elevko.app.DataHandler;
import no.glv.elevko.android.ExpandableListViewBase;
import no.glv.elevko.app.SettingsManager;
import no.glv.elevko.app.Utils;
import no.glv.elevko.intrfc.Student;

/**
 * This Adapter will list all the student in a given Group. This class
 * will load a XML layout row: row_student_list.
 * <p/>
 * This list MUST display the first name of every student, and the
 *
 * @author GleVoll
 */
public class StudentListAdapter extends ExpandableListViewBase<Student> implements OnClickListener {

    /**  */
    @SuppressWarnings("unused")
    private static final String TAG = StudentListAdapter.class.getSimpleName();

    private SettingsManager mSettingsManager;

    /**
     * A-F. Not used or stored anywhere as of yet.
     **/
    private String[] baseClassNames;

    private BaseActivity baseActivity;

    /**
     * @param students List of students to view
     */
    public StudentListAdapter( Context context, List<Student> students ) {
        super( context, students );

        mSettingsManager = DataHandler.GetInstance().getSettingsManager();
        baseClassNames = context.getResources().getStringArray( R.array.stdList_classes );
    }

    public void setBaseActivity( BaseActivity ba ) {
        this.baseActivity = ba;
    }

    /**
     * Gets the upper view (not the expanded). Will alter the background color of every other row
     */
    public View getView( int position, View convertView, ViewGroup parent, boolean isExpanded ) {
        Student student = getItem( position );

        if ( convertView == null )
            convertView = createConvertView( parent, student, position, isExpanded );

        if ( position % 2 == 0 )
            convertView.setBackgroundColor( getContext().getResources().getColor(
                    R.color.task_stdlist_dark ) );
        else
            convertView.setBackgroundColor( getContext().getResources().getColor( R.color.task_stdlist_light ) );

        ViewHolder holder = ( ViewHolder ) convertView.getTag();
        holder.imgTaskView.setTag( student );
        holder.id = position;

        if ( mSettingsManager.isShowFullname() )
            holder.textView.setText( student.getFirstName() + " "
                    + student.getLastName() );
        else
            holder.textView.setText( student.getFirstName() );

        holder.identText.setText( student.getIdent() );
        holder.birthText.setText( Utils.GetDateAsString( student.getBirth() ) );

        return convertView;
    }

    /**
     * Creates the convert view if needed.
     */
    private View createConvertView( final ViewGroup parent, final Student student, final int groupPos, final boolean isExpanded ) {
        LayoutInflater inflater = ( LayoutInflater ) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View myView = inflater.inflate( R.layout.row_stdclass_list, parent, false );

        ViewHolder holder = new ViewHolder();

        ImageView imgTaskView = ( ImageView ) myView.findViewById( R.id.task );
        imgTaskView.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick( View v ) {
                Toast.makeText( getContext(), "Will implement individual Assignment soon..", Toast.LENGTH_LONG )
                        .show();
            }
        } );

        TextView textView = ( TextView ) myView.findViewById( R.id.TV_stdlist_name );
        textView.setTag( student );
        holder.textView = textView;

        textView = ( TextView ) myView.findViewById( R.id.TV_stdlist_ident );
        holder.identText = textView;

        textView = ( TextView ) myView.findViewById( R.id.TV_stdlist_birth );
        holder.birthText = textView;

        holder.imgTaskView = imgTaskView;

        myView.setTag( holder );

        return myView;
    }

    static class ViewHolder {
        int id;

        TextView textView;
        TextView identText;
        TextView birthText;
        ImageView imgTaskView;

    }

    @Override
    public View getGroupView( final int groupPosition, final boolean isExpanded, final View convertView, final ViewGroup parent ) {
        View view = getView( groupPosition, convertView, parent, isExpanded );

        RelativeLayout ll = ( RelativeLayout ) view.findViewById( R.id.LL_stdList_container );
        ll.setOnLongClickListener( new OnLongClickListener() {

            @Override
            public boolean onLongClick( View v ) {
                Student std = getItem( groupPosition );
                StudentViewActivity.StartActivity( baseActivity, std );

                return true;
            }
        } );

        ll.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick( View v ) {
                if ( !isExpanded )
                    ( ( ExpandableListView ) parent ).expandGroup( groupPosition );
                else
                    ( ( ExpandableListView ) parent ).collapseGroup( groupPosition );
            }
        } );

        return view;
    }

    /**
     * @param parent Parent view
     *
     * @return New view (The expanded view)
     */
    private View createChildView( ViewGroup parent ) {
        LayoutInflater inflater = ( LayoutInflater ) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE );
        View convertView = inflater.inflate( R.layout.row_stdlist_stditem, parent, false );

        StudentItemHolder holder = new StudentItemHolder();
        convertView.setTag( holder );

        return convertView;
    }


    @Override
    public View getChildView( int groupPosition, int childPosition, boolean isLastChild, View convertView,
                              ViewGroup parent ) {
        if ( convertView == null ) {
            convertView = createChildView( parent );
        }

        final Student st = getItem( groupPosition );
        final StudentItemHolder holder = ( StudentItemHolder ) convertView.getTag();
        holder.spClass = ( Spinner ) convertView.findViewById( R.id.SP_stdList_stditem_classes );
        holder.st = st;

        if ( st.getGrade() != null && st.getGrade().length() > 0 ) {
            int sel = ( int ) st.getGrade().charAt( 0 );
            holder.spClass.setSelection( sel - ( int ) 'A' );
        } else {
            holder.spClass.setSelection( baseClassNames.length - 1 );
        }
/*
        ImageView ivSave = ( ImageView ) convertView.findViewById( R.id.IV_stdList_stditem_save );
        ivSave.setTag( holder );
        ivSave.setOnClickListener( this );
*/
        holder.spClass.setOnItemSelectedListener( new OnItemSelectedListener() {
            @Override
            public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
                String grade = holder.spClass.getSelectedItem().toString();
                Student student = holder.st;
                if ( student.getGrade() == null ) {
                    student.setGrade( grade );
                } else if ( /* position > 0 && */ !st.getGrade().equals( grade ) ) {
                    st.setGrade( grade );
                    DataHandler.GetInstance().updateStudent( st, null );
                }
            }

            @Override
            public void onNothingSelected( AdapterView<?> parent ) {
            }

        } );

        SeekBar seekBar = ( SeekBar ) convertView.findViewById( R.id.SB_stdList_stditem_strength );
        holder.sbStrength = seekBar;
        seekBar.setProgress( holder.st.getStrength() );
        seekBar.setTag( holder );
        seekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser ) {
            }

            @Override
            public void onStartTrackingTouch( SeekBar seekBar ) {
            }

            @Override
            public void onStopTrackingTouch( SeekBar seekBar ) {
                StudentItemHolder holder = ( StudentItemHolder ) seekBar.getTag();
                int progress = seekBar.getProgress();

                if ( holder.st.getStrength() != progress ) {
                    DataHandler.GetInstance().updateStudent( holder.st, null );
                    holder.st.setStrength( progress );
                }
            }
        } );

        return convertView;
    }

    /**
     * Method served to execute when the imgage Save is clicked. Not in use as of now
     */
    @Override
    public void onClick( View v ) {
        StudentItemHolder holder = ( StudentItemHolder ) v.getTag();
        String g = holder.spClass.getSelectedItem().toString();
        holder.st.setGrade( g );

        updateStudent( holder.st );
        Toast.makeText( getContext(), "Saved", Toast.LENGTH_SHORT ).show();
    }

    private void updateStudent( Student student ) {
        DataHandler.GetInstance().updateStudent( student, null );
    }

    /**
     * @author glevoll
     */
    static class StudentItemHolder {

        Spinner spClass;
        SeekBar sbStrength;
        Student st;
    }

}
