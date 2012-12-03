package ch.epfl.lasec.ciphercalendar.calendartools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;
import ch.epfl.lasec.ciphercalendar.Constants;

public class CalendarContent {
    private static final String TAG = "CalendarContent";

    private static CalendarContentResolver contentResolver;
    private static Account account;
    public static final List<CalendarItem> ITEMS = new ArrayList<CalendarItem>();
    public static final Map<String, CalendarItem> ITEM_MAP = new HashMap<String, CalendarItem>();

    public static final Set<String> CIPHER_ITEMS = new HashSet<String>();

    public static void init(Context ctx, Account anAccount) {
	contentResolver = new CalendarContentResolver(ctx);
	account = anAccount;
    }

    public static void refresh() {
	Log.v(TAG, "refresh()");
	ITEMS.clear();
	ITEM_MAP.clear();
	CIPHER_ITEMS.clear();
	if (contentResolver != null)
	    addAllItem(contentResolver.getAllCalendar());
    }

    private static void addItem(CalendarItem item) {

	if (!(item.accountType.equals(Constants.ACCOUNT_TYPE))) {
	    if (ITEM_MAP.put(item.id, item) == null)

		ITEMS.add(item);
	} else {
	    String[] separated = item.calendarName.split("::");
	    Log.v(TAG, "addItem :" + Arrays.toString(separated));
	    if (separated.length > 0)
		CIPHER_ITEMS.add(separated[2]);
	}
    }

    private static void addAllItem(List<CalendarItem> itemList) {
	for (CalendarItem item : itemList) {
	    addItem(item);
	}
    }

    public static void localCopy(CalendarItem mItem) {
	contentResolver.copyLocalCalendar(mItem, account);
	refresh();
    }

    public static void localDelete(CalendarItem mItem) {
	contentResolver.deleteLocalCalendar(account, mItem);
	refresh();
    }
}
