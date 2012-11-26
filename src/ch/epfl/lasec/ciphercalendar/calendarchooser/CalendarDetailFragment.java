package ch.epfl.lasec.ciphercalendar.calendarchooser;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import ch.epfl.lasec.ciphercalendar.R;
import ch.epfl.lasec.ciphercalendar.utils.CalendarContent;
import ch.epfl.lasec.ciphercalendar.utils.CalendarItem;

public class CalendarDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";

    CalendarItem mItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	if (getArguments().containsKey(ARG_ITEM_ID)) {
	    mItem = CalendarContent.ITEM_MAP.get(getArguments().getString(
		    ARG_ITEM_ID));
	}
    }

    private final OnCheckedChangeListener checkChangeListener = new OnCheckedChangeListener() {

	public void onCheckedChanged(CompoundButton buttonView,
		boolean isChecked) {
	    if (isChecked) {
		CalendarContent.localCopy(mItem);
	    } else {
		CalendarContent.localDelete(mItem);
		;
	    }
	}
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	    Bundle savedInstanceState) {
	View rootView = inflater.inflate(R.layout.fragment_calendar_detail,
		container, false);
	if (mItem != null) {
	    ((TextView) rootView.findViewById(R.id.calendar_detail))
		    .setText(mItem.details());
	    Switch calendar_sync_switch = (Switch) rootView
		    .findViewById(R.id.calendar_choose_switch);

	    calendar_sync_switch.setChecked(mItem.isSync());
	    calendar_sync_switch
		    .setOnCheckedChangeListener(checkChangeListener);

	}
	return rootView;
    }

}
