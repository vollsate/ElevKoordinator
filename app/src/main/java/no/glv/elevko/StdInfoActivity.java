package no.glv.elevko;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import no.glv.elevko.SendSMSDialog.OnVerifySendSMSListener;
import no.glv.elevko.base.BaseActivity;
import no.glv.elevko.core.DataHandler;
import no.glv.elevko.core.SettingsManager;
import no.glv.elevko.core.Utils;
import no.glv.elevko.intrfc.Parent;
import no.glv.elevko.intrfc.Phone;
import no.glv.elevko.intrfc.Student;

@SuppressWarnings("deprecation")
public class StdInfoActivity extends BaseActivity implements ActionBar.TabListener, OnVerifySendSMSListener {

	private static final String TAG = StdInfoActivity.class.getSimpleName();

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	Student bean;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_std_info );

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		Log.d( TAG, "Actionbar: " + actionBar );
		actionBar.setNavigationMode( ActionBar.NAVIGATION_MODE_TABS );

		bean = getStudentByIdentExtra();
		Log.d( TAG, "Student: " + bean );
		setTitle( bean.getFirstName() + " " + bean.getLastName() );

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter( getSupportFragmentManager() );
		mSectionsPagerAdapter.setStudent( bean );

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager ) findViewById( R.id.VP_stdInfo_pager );
		mViewPager.setAdapter( mSectionsPagerAdapter );

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener( new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected( int position ) {
				actionBar.setSelectedNavigationItem( position );
			}
		} );

		// For each of the sections in the app, add a tab to the action bar.
		for ( int i = 0; i < mSectionsPagerAdapter.getCount(); i++ ) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab( actionBar.newTab().setText( mSectionsPagerAdapter.getPageTitle( i ) )
					.setTabListener( this ) );
		}
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.menu_std_info, menu );
		return true;
	}

	/**
	 * 
	 * @param v
	 */
	public void makeCall( View v ) {
		Phone p = null;
		Parent parent = (Parent ) v.getTag();

		switch ( v.getId() ) {
			case R.id.IV_info_home_p1call:
				p = parent.getPhone( Phone.HOME );
				break;

			case R.id.IV_info_mob_p1call:
				p = parent.getPhone( Phone.MOBIL );
				break;

			case R.id.IV_info_work_p1call:
				p = parent.getPhone( Phone.WORK );
				break;

			case R.id.IV_info_home_p2call:
				p = parent.getPhone( Phone.HOME );
				break;

			case R.id.IV_info_mob_p2call:
				p = parent.getPhone( Phone.MOBIL );
				break;

			case R.id.IV_info_work_p2call:
				p = parent.getPhone( Phone.WORK );
				break;

			default:
				break;
		}

		if ( p == null )
			return;

		Intent intent = Utils.CreateCallIntent( p );
		startActivity( intent );
	}

	/**
	 * onClick method defined in the XML layout file. Will start the
	 * SendSMSDialog fragment.
	 * 
	 * @param v
	 */
	public void sendSMS( View v ) {
		Parent p = (Parent ) v.getTag();
		SendSMSDialog.StartFragment( p.getPhone( Phone.MOBIL ), this, getFragmentManager() );
	}

	@Override
	public void verifySendSMS( List<Phone> p, String msg ) {
	}

	@Override
	public void verifySendSMS( Phone p, String msg ) {
	}

	/**
	 * 
	 * @param v
	 */
	public void sendMail( View v ) {
		Parent p = (Parent ) v.getTag();

		Intent i = Utils.CreateMailIntent( new String[]{ p.getMail() }, this );
		startActivity( i );
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		int id = item.getItemId();
		if ( id == R.id.action_settings ) {
			return true;
		}
		return super.onOptionsItemSelected( item );
	}

	@Override
	public void onTabSelected( ActionBar.Tab tab, android.app.FragmentTransaction ft ) {
		mViewPager.setCurrentItem( tab.getPosition() );
	}

	@Override
	public void onTabUnselected( ActionBar.Tab tab, android.app.FragmentTransaction ft ) {

	}

	@Override
	public void onTabReselected( ActionBar.Tab tab, android.app.FragmentTransaction ft ) {

	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		Fragment[] fragments = new Fragment[3];
		Student bean;

		public SectionsPagerAdapter( FragmentManager fm ) {
			super( fm );
		}

		public void setStudent( Student std ) {
			bean = std;
		}

		@Override
		public Fragment getItem( int position ) {
			Fragment fr = fragments[position];

			switch ( position ) {
				case 0:
					if ( fr == null ) {
						fr = new StdInfoFragment();
					}
					break;

				case 1:
					if ( fr == null ) {
						fr = new StdParentPrimaryFragment();
					}
					break;

				case 2:
					if ( fr == null ) {
						fr = new StdParentSecundaryFragment();
					}
					break;

				default:
					break;
			}

			fragments[position] = fr;
			return fr;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle( int position ) {
			Locale l = Locale.getDefault();
			switch ( position ) {
				case 0:
					return getString( R.string.std_info_title ).toUpperCase( l );
				case 1:
					return getString( R.string.std_parent1_title ).toUpperCase( l );
				case 2:
					return getString( R.string.std_parent2_title ).toUpperCase( l );
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class StdParentPrimaryFragment extends Fragment {

		private Student bean;

		@Override
		public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
			bean = ( (StdInfoActivity) getActivity() ).bean;
			View rootView = inflater.inflate( R.layout.fragment_std_parent1, container, false );
			List<Parent> parents = bean.getParents();

			if ( !( parents.size() > 0 ) )
				return rootView;

			Parent parent = parents.get( 0 );

			EditText editText = (EditText) rootView.findViewById( R.id.ET_info_p1Name );
			editText.setText( parent.getFirstName() + " " + parent.getLastName() );

			editText = (EditText) rootView.findViewById( R.id.ET_info_p1Mail );
			editText.setText( parent.getMail() );

			createPhoneView( parent, rootView );

			return rootView;
		}

		/**
		 * 
		 * @param parent
		 * @param rootView
		 */
		private void createPhoneView( Parent parent, View rootView ) {
			EditText editText = null;
			Phone phone = parent.getPhone( Phone.MOBIL );
			String number = phone == null ? "" : String.valueOf( phone.getNumber() );

			if ( phone != null ) {
				editText = (EditText) rootView.findViewById( R.id.ET_info_p1Phone_mob );
				editText.setText( number );
			}

			phone = parent.getPhone( Phone.WORK );
			number = phone == null ? "" : String.valueOf( phone.getNumber() );

			if ( phone != null ) {
				editText = (EditText) rootView.findViewById( R.id.ET_info_p1Phone_work );
				editText.setText( number );
			}

			phone = parent.getPhone( Phone.HOME );
			number = phone == null ? "" : String.valueOf( phone.getNumber() );

			if ( phone != null ) {
				editText = (EditText) rootView.findViewById( R.id.ET_info_p1Phone_home );
				editText.setText( number );
			}

			setTagOnImages( parent, rootView );
		}

		/**
		 * 
		 * @param parent
		 * @param rootView
		 */
		private void setTagOnImages( Parent parent, View rootView ) {
			ImageView img = (ImageView) rootView.findViewById( R.id.IV_info_home_p1call );
			img.setTag( parent );

			img = (ImageView) rootView.findViewById( R.id.IV_info_mob_p1call );
			img.setTag( parent );

			img = (ImageView) rootView.findViewById( R.id.IV_info_work_p1call );
			img.setTag( parent );

			img = (ImageView) rootView.findViewById( R.id.IV_info_mob_p1msg );
			img.setTag( parent );

			img = (ImageView) rootView.findViewById( R.id.IV_info_p1Mail );
			img.setTag( parent );

		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class StdParentSecundaryFragment extends Fragment {

		private Student bean;

		@Override
		public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
			bean = ( (StdInfoActivity) getActivity() ).bean;
			View rootView = inflater.inflate( R.layout.fragment_std_parent2, container, false );
			List<Parent> parents = bean.getParents();

			if ( !( parents.size() > 1 ) )
				return rootView;
			Parent parent = parents.get( 1 );

			EditText editText = (EditText) rootView.findViewById( R.id.ET_info_p2Name );
			editText.setText( parent.getFirstName() + " " + parent.getLastName() );

			editText = (EditText) rootView.findViewById( R.id.ET_info_p2Mail );
			editText.setText( parent.getMail() );

			createPhoneView( parent, rootView );

			return rootView;
		}

		/**
		 * 
		 * @param parent
		 * @param rootView
		 */
		private void createPhoneView( Parent parent, View rootView ) {
			EditText editText = null;
			Phone phone = parent.getPhone( Phone.MOBIL );
			String number = phone == null ? "" : String.valueOf( phone.getNumber() );

			if ( phone != null ) {
				editText = (EditText) rootView.findViewById( R.id.ET_info_p2Phone_mob );
				editText.setText( number );
			}

			phone = parent.getPhone( Phone.WORK );
			number = phone == null ? "" : String.valueOf( phone.getNumber() );

			if ( phone != null ) {
				editText = (EditText) rootView.findViewById( R.id.ET_info_p2Phone_work );
				editText.setText( number );
			}

			phone = parent.getPhone( Phone.HOME );
			number = phone == null ? "" : String.valueOf( phone.getNumber() );

			if ( phone != null ) {
				editText = (EditText) rootView.findViewById( R.id.ET_info_p2Phone_home );
				editText.setText( number );
			}

			setTagOnImages( parent, rootView );
		}

		/**
		 * 
		 * @param parent
		 * @param rootView
		 */
		private void setTagOnImages( Parent parent, View rootView ) {
			ImageView img = (ImageView) rootView.findViewById( R.id.IV_info_home_p2call );
			img.setTag( parent );

			img = (ImageView) rootView.findViewById( R.id.IV_info_mob_p2call );
			img.setTag( parent );

			img = (ImageView) rootView.findViewById( R.id.IV_info_work_p2call );
			img.setTag( parent );

			img = (ImageView) rootView.findViewById( R.id.IV_info_mob_p2msg );
			img.setTag( parent );

			img = (ImageView) rootView.findViewById( R.id.IV_info_p2Mail );
			img.setTag( parent );
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class StdInfoFragment extends Fragment {

		private Student bean;

		@Override
		public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
			bean = ( (StdInfoActivity) getActivity() ).bean;
			View rootView = inflater.inflate( R.layout.fragment_std_info, container, false );
			SettingsManager manager = DataHandler.GetInstance().getSettingsManager();

			TextView tv = (TextView) rootView.findViewById( R.id.TV_info_header );
			// tv.setText( bean.getIdent() );

			EditText editText = (EditText) rootView.findViewById( R.id.ET_info_firstName );
			editText.setText( bean.getFirstName() );

			editText = (EditText) rootView.findViewById( R.id.ET_info_LastName );
			editText.setText( bean.getLastName() );

			editText = (EditText) rootView.findViewById( R.id.ET_info_ident );
			editText.setText( bean.getIdent() );

			editText = (EditText) rootView.findViewById( R.id.ET_info_birth );
			editText.setText( Utils.GetDateAsString( bean.getBirth() ) );

			editText = (EditText) rootView.findViewById( R.id.ET_info_pc );
			editText.setText( bean.getPostalCode() );

			editText = (EditText) rootView.findViewById( R.id.ET_info_adr );
			editText.setText( bean.getAdress() );

			tv = (TextView) rootView.findViewById( R.id.TV_info_google );

			String msg = getResources().getString( R.string.stdInfo_google );
			msg = msg.replace( "{google}", bean.getIdent() + "@" + manager.getGoogleAccount() );
			tv.setPaintFlags( tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG );
			tv.setText( msg );

			return rootView;
		}
	}

	/**
	 * 
	 * @param ctx
	 * @param std
	 */
	public static void StartActivity( Context ctx, Student std ) {
		Intent intent = new Intent( ctx, StdInfoActivity.class );
		PutIdentExtra( std, intent );

		ctx.startActivity( intent );

	}
}