/**
 *
 */
package no.glv.paco.android;


import android.support.v4.app.Fragment;

import no.glv.paco.app.DataHandler;

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
