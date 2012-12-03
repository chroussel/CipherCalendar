package ch.epfl.lasec.ciphercalendar.calendartools;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import ch.epfl.lasec.ciphercalendar.Constants;
import ch.epfl.lasec.ciphercalendar.utils.CipherTool;

public class CalendarContentResolver {
    private static final String TAG = "CalendarContentResolver";
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_CALENDAR_COLOR_INDEX = 4;
    private static final int PROJECTION_ACCOUNT_TYPE_INDEX = 5;
    public static final String[] CALENDAR_PROJECTION = new String[] {
	    Calendars._ID, // 0
	    Calendars.ACCOUNT_NAME, // 1
	    Calendars.CALENDAR_DISPLAY_NAME, // 2
	    Calendars.OWNER_ACCOUNT, // 3
	    Calendars.CALENDAR_COLOR, // 4
	    Calendars.ACCOUNT_TYPE, // 5
    };
    ContentResolver contentResolver;

    public CalendarContentResolver(Context ctx) {
	contentResolver = ctx.getContentResolver();
    }

    public List<CalendarItem> getCalendar(String accountName, String accountType) {
	List<CalendarItem> calendars = new ArrayList<CalendarItem>();
	Log.v(TAG, "getCalendar()");
	Cursor cur = null;
	Uri uri = Calendars.CONTENT_URI;
	String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
		+ Calendars.ACCOUNT_TYPE + " = ?))";
	String[] selectionArgs = new String[] { accountName, accountType };
	// Submit the query and get a Cursor object back.

	cur = contentResolver.query(uri, CALENDAR_PROJECTION, selection,
		selectionArgs, null);
	while (cur.moveToNext()) {
	    long calID = 0;
	    String displayName = null;
	    int color = 0;
	    // Get the field values
	    calID = cur.getLong(PROJECTION_ID_INDEX);
	    displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
	    color = cur.getInt(PROJECTION_CALENDAR_COLOR_INDEX);
	    calendars.add(new CalendarItem(Long.toString(calID), displayName,
		    accountName, accountType, color));
	}
	cur.close();
	Log.v(TAG, "getCalendar Success");
	return calendars;
    }

    public static final String[] EVENT_PROJECTION = new String[] { Events._ID,
	    Events.TITLE, Events.EVENT_LOCATION, Events.DESCRIPTION,
	    Events.EVENT_COLOR, Events.DTSTART, Events.DTEND, Events.DURATION,
	    Events.ALL_DAY, Events.RRULE, Events.EXRULE, Events.ACCESS_LEVEL,
	    Events.AVAILABILITY };

    public Set<String> getEvents(String CalendarId) {
	Set<String> EventList = new HashSet<String>();
	Log.v(TAG, "getEvents()");
	Cursor cursor = null;
	Uri uri = Events.CONTENT_URI;
	String selection = "((" + Events.CALENDAR_ID + " = ?))";
	String[] selectionArgs = { CalendarId };

	cursor = contentResolver.query(uri, EVENT_PROJECTION, selection,
		selectionArgs, null);
	while (cursor.moveToNext()) {
	    String title = null;

	    title = cursor.getString(0);
	    EventList.add(title);
	}
	cursor.close();
	Log.v(TAG, "getEvents Success");
	return EventList;
    }

    private Uri asSyncAdapter(Uri uri, String account, String accountType) {
	Log.v(TAG, "asSyncAdapter()");
	return uri
		.buildUpon()
		.appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,
			"true")
		.appendQueryParameter(Calendars.ACCOUNT_NAME, account)
		.appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType)
		.build();
    }

    public List<CalendarItem> getAllCalendar() {
	List<CalendarItem> calendars = new ArrayList<CalendarItem>();
	Log.v(TAG, "getAllCalendar()");
	Cursor cur = null;
	Uri uri = Calendars.CONTENT_URI;
	String[] selectionArgs = new String[] {};
	// Submit the query and get a Cursor object back.

	cur = contentResolver.query(uri, CALENDAR_PROJECTION, null,
		selectionArgs, null);
	while (cur.moveToNext()) {
	    long calID = 0;
	    String displayName = null;
	    String accountName = null;
	    String accountType = null;
	    int color = 0;
	    // Get the field values
	    calID = cur.getLong(PROJECTION_ID_INDEX);
	    displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
	    accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
	    accountType = cur.getString(PROJECTION_ACCOUNT_TYPE_INDEX);
	    color = cur.getInt(PROJECTION_CALENDAR_COLOR_INDEX);
	    calendars.add(new CalendarItem(Long.toString(calID), displayName,
		    accountName, accountType, color));
	}
	cur.close();
	Log.v(TAG, "getAllCalendar Success");
	return calendars;
    }

    public void copyLocalCalendar(CalendarItem source, Account account) {
	Log.v(TAG, "copyLocalCalendar()");

	createLocalCalendar(account, createCalendarName(source), source.color);
    }

    public String createLocalCalendar(Account account, String calendarName,
	    int color) {
	Log.v(TAG, "createLocalCalendar()");
	ContentValues values = new ContentValues();

	values.put(Calendars.ACCOUNT_NAME, account.name);
	values.put(Calendars.ACCOUNT_TYPE, account.type);
	values.put(Calendars.NAME, calendarName);
	values.put(Calendars.CALENDAR_DISPLAY_NAME, calendarName);
	values.put(Calendars.CALENDAR_COLOR, color);
	values.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
	values.put(Calendars.OWNER_ACCOUNT, account.name);

	values.put(Calendars.SYNC_EVENTS, 1);
	values.put(Calendars.VISIBLE, 1);

	Uri creationUri = asSyncAdapter(Calendars.CONTENT_URI, account.name,
		account.type);
	Uri created = contentResolver.insert(creationUri, values);

	String id = created.getLastPathSegment();

	Log.v(TAG, "Calendar created:" + id + "-" + created);
	return id;
    }

    public static String createCalendarName(CalendarItem mItem) {
	return mItem.accountType + "::" + mItem.accountName + "::"
		+ mItem.calendarName + "::" + mItem.id;
    }

    public void deleteLocalCalendar(Account account, CalendarItem mItem) {
	Log.v(TAG, "deleteLocalCalendar()");
	Uri deleteUri = asSyncAdapter(Calendars.CONTENT_URI, account.name,
		account.type);
	String selection = "((" + Calendars.NAME + " = ?) AND ("
		+ Calendars.ACCOUNT_NAME + " = ?) AND ("
		+ Calendars.ACCOUNT_TYPE + " = ?))";
	String[] selectionArgs = { createCalendarName(mItem), account.name,
		account.type };
	int deleteRow = contentResolver.delete(deleteUri, selection,
		selectionArgs);

	Log.v(TAG, "Calendar deleted:" + deleteUri + "-" + deleteRow);
    }

    public void syncCalendar(Context context, CalendarItem localCalendar,
	    SecretKey secretKey) throws Exception {
	Log.v(TAG, "syncCalendar()");

	EventIdDatabaseHelper dbHelper = new EventIdDatabaseHelper(context);
	String[] splitName = localCalendar.calendarName.split("::");
	String accountType = splitName[0];
	String accountName = splitName[1];
	String calendarName = splitName[2];
	String calendarId = splitName[3];

	CalendarItem distantCalendar = new CalendarItem(calendarId,
		calendarName, accountName, accountType, 0);

	LongSparseArray<ContentValues> localCalendarEvents = fetchCalendar(
		dbHelper, localCalendar, null);
	LongSparseArray<ContentValues> distantCalendarEvents = fetchCalendar(
		dbHelper, distantCalendar, secretKey);

	for (int i = 0; i < distantCalendarEvents.size(); i++) {
	    long distantId = distantCalendarEvents.keyAt(i);
	    long localId = dbHelper.getLocalId(distantId);
	    if (localId == -1) {
		Log.v(TAG, "syncCalendar : pull from distant");
		// Event on distant but no event on localCalendar
		// Pull data.
		ContentValues distantValues = distantCalendarEvents.valueAt(i);
		addEvent(dbHelper, distantValues, localCalendar, distantId,
			false);

	    } else {
		Log.v(TAG, "syncCalendar: (" + distantId + " ; " + localId
			+ ")");
		ContentValues localValues = localCalendarEvents.get(localId);
		if (localValues == null) {
		    Log.v(TAG, "syncCalendar : delete distant");
		    // Event on distant but was in local; delete on distant
		    deleteEvent(dbHelper, distantCalendar, distantId, true);
		} else {
		    Log.v(TAG, "syncCalendar : merge");
		    ContentValues distantValues = distantCalendarEvents
			    .valueAt(i);
		    if (!localValues.equals(distantValues)) {
			Log.v(TAG, "syncCalendar : ask for user");
			// Event on both ask user for merge
			if (askUser()) {
			    cipher(localValues, secretKey);
			    updateEvent(localValues, distantCalendar,
				    distantId, true);
			} else {
			    updateEvent(distantValues, localCalendar, localId,
				    false);
			}
			localCalendarEvents.remove(localId);
		    }

		}
	    }
	}

	for (int j = 0; j < localCalendarEvents.size(); j++) {
	    long localId = localCalendarEvents.keyAt(j);
	    long distantId = dbHelper.getDistantId(localId);

	    if (distantId == -1) {
		Log.v(TAG, "syncCalendar : push to distant");
		// Event to push to distant calendar
		ContentValues localValues = localCalendarEvents.valueAt(j);
		cipher(localValues, secretKey);
		addEvent(dbHelper, localValues, distantCalendar, localId, true);
	    } else {
		ContentValues distantValues = distantCalendarEvents
			.get(distantId);
		if (distantValues == null) {
		    deleteEvent(dbHelper, localCalendar, localId, false);
		    /*
		     * if (askUser()) { ContentValues localValues =
		     * localCalendarEvents .valueAt(j); cipher(localValues,
		     * secretKey); addEvent(dbHelper, localValues,
		     * distantCalendar, localId, true); }
		     */
		}
	    }
	}

    }

    /**
     * 
     * @return false if update from distant, true if update from local
     */
    private boolean askUser() {
	return true;
    }

    /*
     * private boolean isValuesEqual(ContentValues v1, ContentValues v2) {
     * Set<String> keySetv1 = v1.keySet(); Set<String> keySetv2 = v2.keySet();
     * 
     * if (keySetv1.size() != keySetv2.size()) { Log.v(TAG,
     * "isValuesEqual different size ( " + keySetv1.size() + " ; " +
     * keySetv2.size() + ")"); return false; }
     * 
     * if (!keySetv1.containsAll(keySetv2)) { Log.v(TAG,
     * "isValuesEqual: not the same keys"); return false; }
     * 
     * for (String key : keySetv1) { Object o1 = v1.get(key); Object o2 =
     * v2.get(key); if (o1 == null && o2 == null) { continue; } else if (o1 ==
     * null || o2 == null) { return false; } else if (!o1.equals(o2)) {
     * Log.v(TAG, "isValuesEqual: not equal values (" + key + ": " +
     * o1.toString() + ";" + o2.toString() + ")"); return false;
     * 
     * } } return true; }
     */
    private void cipher(ContentValues values, SecretKey secretKey)
	    throws InvalidKeyException, NoSuchAlgorithmException,
	    NoSuchPaddingException, IllegalBlockSizeException,
	    BadPaddingException, InvalidParameterSpecException,
	    UnsupportedEncodingException {

	String title = CipherTool.cipher(values.getAsString(Events.TITLE),
		secretKey);
	String location = CipherTool.cipher(
		values.getAsString(Events.EVENT_LOCATION), secretKey);
	String description = CipherTool.cipher(
		values.getAsString(Events.DESCRIPTION), secretKey);

	values.put(Events.TITLE, title);
	values.put(Events.EVENT_LOCATION, location);
	values.put(Events.DESCRIPTION, description);
    }

    private void addEvent(EventIdDatabaseHelper dbHelper, ContentValues values,
	    CalendarItem calendar, long sourceId, boolean distant) {
	Log.v(TAG, "addEvent( distant:" + distant + " )");
	Uri insertUri = Events.CONTENT_URI;
	if (!distant) {
	    insertUri = asSyncAdapter(insertUri, calendar.accountName,
		    calendar.accountType);
	}
	values.put(Events.EVENT_TIMEZONE, "GMT");
	values.put(Events.CALENDAR_ID, calendar.id);
	Uri insertedUri = contentResolver.insert(insertUri, values);
	long eventId = Long.valueOf(insertedUri.getLastPathSegment());
	Log.v(TAG, "addEvent: Id: " + eventId);
	if (distant)
	    dbHelper.addEvent(sourceId, eventId);
	else
	    dbHelper.addEvent(eventId, sourceId);

    }

    private int updateEvent(ContentValues values, CalendarItem calendar,
	    long eventId, boolean distant) {
	Log.v(TAG, "updateEvent()");
	Uri editsUri = Events.CONTENT_URI;
	if (!distant) {
	    editsUri = asSyncAdapter(editsUri, calendar.accountName,
		    calendar.accountType);
	}
	values.put(Events.CALENDAR_ID, calendar.id);
	Uri editUri = ContentUris.withAppendedId(editsUri, eventId);
	return contentResolver.update(editUri, values, null, null);
    }

    private int deleteEvent(EventIdDatabaseHelper dbHelper,
	    CalendarItem calendar, long eventId, boolean distant) {
	Log.v(TAG, "deleteEvent()");
	Uri deleteUri = Events.CONTENT_URI;
	if (!distant) {
	    deleteUri = asSyncAdapter(deleteUri, calendar.accountName,
		    calendar.accountType);
	}
	String[] selArgs = new String[] { Long.toString(eventId) };
	if (calendar.accountType.equals(Constants.ACCOUNT_TYPE)) {
	    dbHelper.deleteEventLocal(eventId);
	} else {
	    dbHelper.deleteEventDistant(eventId);
	}
	return contentResolver.delete(deleteUri, Events._ID + "=?", selArgs);
    }

    private LongSparseArray<ContentValues> fetchCalendar(
	    EventIdDatabaseHelper dbHelper, CalendarItem calendar,
	    SecretKey secretKey) {
	Log.v(TAG, "fetchCalendar()");
	Cursor cursor = null;
	Uri uri = asSyncAdapter(Events.CONTENT_URI, calendar.accountName,
		calendar.accountType);
	String selection = "((" + Events.CALENDAR_ID + " = ?) AND ("
		+ Events.CALENDAR_DISPLAY_NAME + " = ?))";

	String[] selectionArgs = { calendar.id, calendar.calendarName };

	cursor = contentResolver.query(uri, EVENT_PROJECTION, selection,
		selectionArgs, null);

	String[] columnNames = cursor.getColumnNames();

	LongSparseArray<ContentValues> eventList = new LongSparseArray<ContentValues>();

	while (cursor.moveToNext()) {
	    try {
		ContentValues values = new ContentValues();

		int eventId = cursor.getInt(0);
		for (int i = 1; i < columnNames.length; i++) {

		    String columnName = columnNames[i];
		    String data = null;
		    String rawdata = cursor.getString(i);
		    if (rawdata != null && !rawdata.isEmpty()) {
			if (secretKey != null
				&& (columnName.equals(Events.TITLE)
					|| columnName
						.equals(Events.DESCRIPTION) || columnName
					    .equals(Events.EVENT_LOCATION))) {

			    data = CipherTool.decipher(rawdata, secretKey);

			    Log.v(TAG, "fetchCalendar(): " + data);
			} else {
			    data = rawdata;
			}
		    }
		    values.put(columnNames[i], data);
		}
		eventList.put(eventId, values);
		Log.v(TAG, "fetchCalendar: Id: " + eventId);
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		HandleCipherException(e);
	    }

	}
	cursor.close();
	Log.v(TAG, "fetchCalendar Success");
	return eventList;

    }

    private void HandleCipherException(Exception e) {
	Log.v(TAG, "HandleCipherException()");
	Log.w(TAG, "Error while fetching event");
	e.printStackTrace();
    }

}
