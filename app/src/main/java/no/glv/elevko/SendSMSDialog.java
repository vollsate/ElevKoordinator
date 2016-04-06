package no.glv.elevko;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.LinkedList;
import java.util.List;

import no.glv.elevko.android.DialogFragmentBase;
import no.glv.elevko.intrfc.Group;
import no.glv.elevko.intrfc.Parent;
import no.glv.elevko.intrfc.Phone;
import no.glv.elevko.intrfc.Student;

/**
 * A dialog that can either send SMS to one {@link Phone} object or to an entire
 * {@link Group}. The <tt>Phone.MOBIL</tt>.
 * 
 * <p>
 * The two checkboxes (if mail to an entire class is chosen), will send mail to
 * either primary parent, secondary parent or both.
 * 
 * <p>The listener callback will need to do the
 * 
 * @author glevoll
 *
 */
public class SendSMSDialog extends DialogFragmentBase implements View.OnClickListener {

	OnVerifySendSMSListener listener;
	Group stdClass;
	Phone p;

	private EditText etMsg;
	private CheckBox cbPar1;
	private CheckBox cbPar2;

	public SendSMSDialog() {

	}

	/*
	public SendSMSDialog( Phone phone ) {
		p = phone;
	}
*/
	@Override
	protected void buildView( View rootView ) {
		buildButton( rootView );
	}

	@Override
	public void onClick( View view ) {
		View v = view.getRootView();
		etMsg = (EditText) v.findViewById( R.id.ET_stdList_sms );
		cbPar1 = (CheckBox) v.findViewById( R.id.checkBox1 );
		cbPar2 = (CheckBox) v.findViewById( R.id.checkBox2 );

		if ( p == null ) {
			sentMultiSMS( view );
			return;
		}

		SmsManager manager = SmsManager.getDefault();
		String num = "+47" + p.getNumber();
		manager.sendTextMessage( num, null, etMsg.getText().toString(), null, null );

		getFragmentManager().beginTransaction().remove( this ).commit();

	}

	private void sentMultiSMS( View view ) {
		boolean p1, p2 = false;

		View v = view.getRootView();

		p1 = cbPar1.isChecked();
		p2 = cbPar2.isChecked();

		List<Phone> pList = new LinkedList<Phone>();

		for ( Student s : stdClass.getStudents() ) {
			int i = 0;
			if ( s.getParents() == null || s.getParents().size() == i )
				continue;

			if ( p1 && s.getParents().size() >= ++i ) {
				Parent par = s.getParents().get( i - 1 );
				long num = par.getPhoneNumber( Phone.MOBIL );
				if ( num != 0 ) {
					pList.add( par.getPhone( Phone.MOBIL ) );
				}
			}

			if ( p2 && s.getParents().size() >= ++i ) {
				Parent par = s.getParents().get( i - 1 );
				long num = par.getPhoneNumber( Phone.MOBIL );
				if ( num != 0 ) {
					pList.add( par.getPhone( Phone.MOBIL ) );
				}
			}
		}

		EditText et = (EditText) v.findViewById( R.id.ET_stdList_sms );
		SmsManager manager = SmsManager.getDefault();
		String msg = et.getText().toString();
		for ( Phone p : pList ) {
			String num = "+47" + p.getNumber();
			manager.sendTextMessage( num, null, msg, null, null );
		}

		listener.verifySendSMS( pList, et.getText().toString() );
		getFragmentManager().beginTransaction().remove( this ).commit();
	}

	/**
	 * 
	 * @param rootView
	 */
	private void buildButton( View rootView ) {
		final Fragment fr = this;

		if ( p != null ) {
			cbPar1 = (CheckBox) rootView.findViewById( R.id.checkBox1 );
			cbPar2 = (CheckBox) rootView.findViewById( R.id.checkBox2 );
			cbPar1.setVisibility( View.GONE );
			cbPar2.setVisibility( View.GONE );
		}

		Button btn = (Button) rootView.findViewById( R.id.BTN_stdlist_sendSMS );
		btn.setOnClickListener( this );

		btn = (Button) rootView.findViewById( R.id.BTN_stdList_cancelSMS );
		btn.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick( View v ) {
				fr.getFragmentManager().beginTransaction().remove( fr ).commit();
			}
		} );
	}

	@Override
	protected int getRootViewID() {
		return R.layout.dialog_sms;
	}

	@Override
	protected int getTitle() {
		return R.string.stdInfo_sms_send;
	}

	/**
	 * 
	 * @param stdClass
	 * @param listener
	 * @param manager
	 * @return
	 */
	public static SendSMSDialog StartFragment( Group stdClass, OnVerifySendSMSListener listener,
			FragmentManager manager ) {
		return StartFragment( stdClass, null, listener, manager );
	}

	/**
	 * 
	 * @param p
	 * @param listener
	 * @param manager
	 * @return
	 */
	public static SendSMSDialog StartFragment( Phone p, OnVerifySendSMSListener listener,
			FragmentManager manager ) {
		return StartFragment( null, p, listener, manager );
	}

	/**
	 * 
	 * @param stdClass
	 * @param p
	 * @param listener
	 * @param manager
	 * @return
	 */
	public static SendSMSDialog StartFragment( Group stdClass, Phone p, OnVerifySendSMSListener listener,
			FragmentManager manager ) {
		SendSMSDialog fragment = new SendSMSDialog(  ); // TODO: Fix phone using bundle

		fragment.listener = listener;
		fragment.stdClass = stdClass;

		FragmentTransaction ft = manager.beginTransaction();
		fragment.show( ft, fragment.getClass().getSimpleName() );

		return fragment;
	}

	public interface OnVerifySendSMSListener {
		void verifySendSMS( List<Phone> p, String msg );

		void verifySendSMS( Phone p, String msg );
	}

}
