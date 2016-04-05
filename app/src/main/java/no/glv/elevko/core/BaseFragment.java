/**
 * 
 */
package no.glv.elevko.core;


import android.support.v4.app.Fragment;

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
