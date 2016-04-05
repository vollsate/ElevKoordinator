package no.glv.elevko.core;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A base for creating custom simple dialogs.
 *
 * <p>
 *     An implementation must provide the following data: <code>getRootViewID</code> to the XML file
 * </p>
 * 
 * @author GleVoll
 *
 */
public abstract class DialogFragmentBase extends DialogFragment {

	private View rootView;

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
	}

	protected abstract int getRootViewID();

	protected abstract int getTitle();
	
	protected View getRootView() {
		return rootView;
	}

	@Override
	public final View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		if ( rootView == null )
			rootView = inflater.inflate( getRootViewID(), container, false );

		buildView( rootView );

		getDialog().setTitle( getTitle() );

		return rootView;
	}

	protected abstract void buildView( View rootView );

	protected void finish() {
		getFragmentManager().beginTransaction().remove( this ).commit();
	}

	/**
	 * 
	 * @return the datahandler
	 */
	protected DataHandler getDataHander() {
		return DataHandler.GetInstance();
	}
}
