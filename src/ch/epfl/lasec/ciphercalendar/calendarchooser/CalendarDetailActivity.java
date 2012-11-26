package ch.epfl.lasec.ciphercalendar.calendarchooser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import ch.epfl.lasec.ciphercalendar.R;

public class CalendarDetailActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_calendar_detail);

	getActionBar().setDisplayHomeAsUpEnabled(true);

	if (savedInstanceState == null) {
	    Bundle arguments = new Bundle();
	    arguments.putString(CalendarDetailFragment.ARG_ITEM_ID, getIntent()
		    .getStringExtra(CalendarDetailFragment.ARG_ITEM_ID));
	    CalendarDetailFragment fragment = new CalendarDetailFragment();
	    fragment.setArguments(arguments);
	    getSupportFragmentManager().beginTransaction()
		    .add(R.id.calendar_detail_container, fragment).commit();
	}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	if (item.getItemId() == android.R.id.home) {
	    NavUtils.navigateUpTo(this, new Intent(this,
		    CalendarListActivity.class));
	    return true;
	}

	return super.onOptionsItemSelected(item);
    }
}
