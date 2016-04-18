package no.glv.paco.android;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

public class DatePickerDialogHelper {

	public DatePickerDialogHelper() {
	}

	public static void OpenDatePickerDialog( Date date, Context ctx, OnDateSetListener listener, boolean showSpinner,
			boolean showCalendar ) {
		if ( date == null )
			date = new Date();

		Calendar cal = Calendar.getInstance();
		cal.setTime( date );

		int day = cal.get( Calendar.DAY_OF_MONTH );
		int month = cal.get( Calendar.MONTH );
		int year = cal.get( Calendar.YEAR );

		DatePickerDialog dpd = new DatePickerDialog( ctx, listener, year, month, day );

		DatePicker picker = dpd.getDatePicker();
		picker.setSpinnersShown( showSpinner );
		picker.setCalendarViewShown( showCalendar );

		dpd.show();
	}

}
