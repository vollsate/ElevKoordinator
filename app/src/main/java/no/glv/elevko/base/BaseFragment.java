/**
 * 
 */
package no.glv.elevko.base;


import android.support.v4.app.Fragment;

import no.glv.elevko.core.DataHandler;

/**
 * @author GleVoll
 *
 */
public class BaseFragment extends Fragment {
	
	protected DataHandler dataHandler;

	/**
	 * 
	 */
	public BaseFragment(  ) {
		super();
		dataHandler = DataHandler.GetInstance();
	}

	/**
	 * 
	 * @return
	 */
	protected BaseActivity getBaseActivity() {
		return (BaseActivity) getActivity();
	}

}
