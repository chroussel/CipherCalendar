package ch.epfl.lasec.ciphercalendar.calendarchooser;

import android.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import ch.epfl.lasec.ciphercalendar.calendartools.CalendarContent;
import ch.epfl.lasec.ciphercalendar.calendartools.CalendarItem;

public class CalendarListFragment extends ListFragment {

    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private Callbacks mCallbacks = sDummyCallbacks;
    private int mActivatedPosition = ListView.INVALID_POSITION;

    public interface Callbacks {

	public void onItemSelected(String id);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {

	public void onItemSelected(String id) {
	}
    };

    public CalendarListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setListAdapter(new ArrayAdapter<CalendarItem>(getActivity(),
		R.layout.simple_list_item_activated_1, R.id.text1,
		CalendarContent.ITEMS));
    }

    @Override
    public void onResume() {
	super.onResume();
	CalendarContent.refresh();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
	super.onViewCreated(view, savedInstanceState);
	if (savedInstanceState != null
		&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
	    setActivatedPosition(savedInstanceState
		    .getInt(STATE_ACTIVATED_POSITION));
	}
    }

    @Override
    public void onAttach(Activity activity) {
	super.onAttach(activity);
	if (!(activity instanceof Callbacks)) {
	    throw new IllegalStateException(
		    "Activity must implement fragment's callbacks.");
	}

	mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
	super.onDetach();
	mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position,
	    long id) {
	super.onListItemClick(listView, view, position, id);
	mCallbacks.onItemSelected(CalendarContent.ITEMS.get(position).id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	if (mActivatedPosition != AdapterView.INVALID_POSITION) {
	    outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
	}
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
	getListView().setChoiceMode(
		activateOnItemClick ? AbsListView.CHOICE_MODE_SINGLE
			: AbsListView.CHOICE_MODE_NONE);
    }

    public void setActivatedPosition(int position) {
	if (position == AdapterView.INVALID_POSITION) {
	    getListView().setItemChecked(mActivatedPosition, false);
	} else {
	    getListView().setItemChecked(position, true);
	}

	mActivatedPosition = position;
    }
}
