package no.glv.elevko;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import no.glv.elevko.app.CSVReader;
import no.glv.elevko.app.DataHandler;
import no.glv.elevko.android.DialogFragmentBase;
import no.glv.elevko.intrfc.BaseValues;
import no.glv.elevko.intrfc.Group;

/**
 * Will load a new class from a file. The file must contain only one class.
 */
public class LoadClassFromFileFragment extends DialogFragmentBase
{

	public static final String EXTRA_TASKNAME = BaseValues.EXTRA_BASEPARAM + "TaskName";
	OnDataLoadedListener listener;
	ProgressBar mProgressBar;
	Button mButton;
	ListView mListView;

	/**
	 * 
	 * @return
	 */
	private ProgressBar getProgressBar() {
		if ( mProgressBar == null ) {
			mProgressBar = (ProgressBar) getRootView().findViewById( R.id.PB_newclass_indeterminate );
		}

		return mProgressBar;
	}

	/**
	 * 
	 * @return
	 */
	protected Button getButton() {
		if ( mButton == null )
			mButton = (Button) getRootView().findViewById( R.id.BTN_loadData_cancel );

		return mButton;
	}

	/**
	 * 
	 * @return
	 */
	protected ListView getListView() {
		if ( mListView == null )
			mListView = (ListView) getRootView().findViewById( R.id.LV_loadData_filesList );

		return mListView;
	}

	/**
	 * 
	 */
	protected void hideProgressBar() {
		getProgressBar().setVisibility( View.INVISIBLE );

		getButton().setVisibility( View.VISIBLE );
		getListView().setVisibility( View.VISIBLE );
	}

	/**
	 * 
	 */
	protected void showProgressBar() {
		getProgressBar().setVisibility( View.VISIBLE );

		getButton().setVisibility( View.GONE );
		getListView().setVisibility( View.GONE );
	}

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
	}

	@Override
	protected int getRootViewID() {
		return R.layout.fragment_loaddata_files;
	}

	@Override
	protected int getTitle() {
		return R.string.loadData_csv_title;
	}

	@Override
	protected void buildView( View rootView ) {
		hideProgressBar();
		buildAdapter( rootView );
		buildButton( rootView );
	}

	/**
	 * 
	 * @param rootView
	 */
	private void buildButton( View rootView ) {
		Button btn = getButton();
		btn.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick( View v ) {
				LoadClassFromFileFragment.this.finish();
			}
		} );
	}

	/**
	 * 
	 * @param rootView
	 */
	private void buildAdapter( View rootView ) {
		List<String> list = createFileList();

		ListView listView = getListView();
		LoadableFilesAdapter adapter = new LoadableFilesAdapter( getActivity(), R.id.LV_loadData_filesList, list );
		adapter.fragment = this;
		listView.setAdapter( adapter );
	}

	/**
	 * 
	 * @return
	 */
	private List<String> createFileList() {
		return DataHandler.GetInstance().getFilesFromDownloadDir();
	}

	/**
	 * 
	 * @author GleVoll
	 *
	 */
	public static class LoadableFilesAdapter extends ArrayAdapter<String> implements OnClickListener,
			no.glv.elevko.app.CSVReader.OnDataLoadedListener {

		private List<String> files;
		LoadClassFromFileFragment fragment;

		public LoadableFilesAdapter( Context context, int resource, List<String> objects ) {
			super( context, resource, objects );

			this.files = objects;
		}

		@Override
		public void onError( Exception e ) {
			// TODO Auto-generated method stub

		}

		/**
		 * 
		 */
		@Override
		public View getView( int position, View convertView, ViewGroup parent ) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			if ( convertView == null ) {
				convertView = inflater.inflate( R.layout.row_loaddata_files, parent, false );
			}

			String file = files.get( position );

			TextView textView = (TextView) convertView.findViewById( R.id.TV_loadData_fileName );
			textView.setTag( file );
			textView.setText( file );
			textView.setOnClickListener( this );

			return convertView;
		}

		@Override
		public void onClick( View v ) {
			fragment.showProgressBar();
			String fileName = v.getTag().toString();
			
			String msg = fragment.getResources().getString( R.string.loadData_installing_msg ).replace( "{class}", fileName );			
			fragment.getDialog().setTitle( msg );
			try {
				CSVReader reader = new CSVReader( this );
				reader.execute( fileName );
			}
			catch ( Exception e ) {
				Log.d( "", e.toString() );
			}
		}

		@Override
		public void onDataLoaded( Group stdClass ) {
			fragment.hideProgressBar();
			if ( stdClass == null )
				return;

			DataHandler.GetInstance().addGroup( stdClass );
			DataHandler.GetInstance().notifyGroupAdd( stdClass );
			if ( fragment.listener != null )
				fragment.listener.onDataLoaded( stdClass );

			fragment.finish();
		}

	}

	/**
	 * 
	 * @param listener
	 * @param manager
	 */
	public static void StartFragment( OnDataLoadedListener listener, android.app.FragmentManager manager ) {
		LoadClassFromFileFragment fragment = new LoadClassFromFileFragment();

		fragment.listener = listener;
		android.app.FragmentTransaction ft = manager.beginTransaction();
		fragment.show( ft, fragment.getClass().getSimpleName() );

	}

	interface OnDataLoadedListener {
		void onDataLoaded( Group stdClass );
	}
}
