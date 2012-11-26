package ch.epfl.lasec.ciphercalendar.calendarchooser;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import ch.epfl.lasec.ciphercalendar.Constants;
import ch.epfl.lasec.ciphercalendar.R;
import ch.epfl.lasec.ciphercalendar.utils.CalendarContent;

public class CalendarListActivity extends FragmentActivity implements
	CalendarListFragment.Callbacks {

    private boolean mTwoPane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_calendar_list);

	if (findViewById(R.id.calendar_detail_container) != null) {
	    mTwoPane = true;
	    ((CalendarListFragment) getSupportFragmentManager()
		    .findFragmentById(R.id.calendar_list))
		    .setActivateOnItemClick(true);
	}
    }

    @Override
    public void onResume() {
	super.onResume();
	AccountManager accountManager = AccountManager.get(this);
	Account[] accounts = accountManager
		.getAccountsByType(Constants.ACCOUNT_TYPE);
	if (accounts.length == 0) {
	    accountManager.addAccount(Constants.ACCOUNT_TYPE, null, null, null,
		    this, null, null);
	} else {
	    CalendarContent.init(getApplicationContext(), accounts[0]);
	}
    }

    public void onItemSelected(String id) {
	if (mTwoPane) {
	    Bundle arguments = new Bundle();
	    arguments.putString(CalendarDetailFragment.ARG_ITEM_ID, id);
	    CalendarDetailFragment fragment = new CalendarDetailFragment();
	    fragment.setArguments(arguments);
	    getSupportFragmentManager().beginTransaction()
		    .replace(R.id.calendar_detail_container, fragment).commit();

	} else {
	    Intent detailIntent = new Intent(this, CalendarDetailActivity.class);
	    detailIntent.putExtra(CalendarDetailFragment.ARG_ITEM_ID, id);
	    startActivity(detailIntent);
	}
    }
}
