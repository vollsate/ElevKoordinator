/**
 *
 */
package no.glv.elevko.android;


import android.support.v4.app.Fragment;

import no.glv.elevko.app.DataHandler;

/**
 * @author GleVoll
 */
public class BaseFragment extends Fragment {

    protected DataHandler getDataHandler() {
        return DataHandler.GetInstance();
    }

    /**
     *
     * @return
     */
    protected BaseActivity getBaseActivity() {
        return ( BaseActivity ) getActivity();
    }

}
