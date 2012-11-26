package ch.epfl.lasec.ciphercalendar.calendartools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import android.util.Log;
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
	Log.v(TAG, "getCalendar Success");
	return calendars;
    }

    public static final String[] EVENT_PROJECTION = new String[] { Events._ID,
	    Events.ORGANIZER, Events.TITLE, Events.EVENT_LOCATION,
	    Events.DESCRIPTION, Events.EVENT_COLOR, Events.DTSTART,
	    Events.DTEND, Events.EVENT_TIMEZONE, Events.EVENT_END_TIMEZONE,
	    Events.DURATION, Events.ALL_DAY, Events.RRULE, Events.EXRULE,
	    Events.ACCESS_LEVEL, Events.AVAILABILITY };

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

    public void syncCalendar(Context context, CalendarItem calendar,
	    SecretKey secretKey, Account account) throws Exception {
	Log.v(TAG, "syncCalendar()");
	EventIdDatabaseHelper dbHelper = new EventIdDatabaseHelper(context);
	pullCalendar(dbHelper, calendar, secretKey, account);
	pushCalendar(dbHelper, calendar, secretKey, account);
    }

    private void pullCalendar(EventIdDatabaseHelper dbHelper,
	    CalendarItem calendar, SecretKey secretKey, Account account)
	    throws Exception {
	Log.v(TAG, "pullCalendar()" + calendar.calendarName);
	String[] splitName = calendar.calendarName.split("::");
	if (splitName.length != 4)
	    throw new Exception("Can't decipher Calendar, Bad Name");
	String accountType = splitName[0];
	String accountName = splitName[1];
	String calendarName = splitName[2];
	String calendarId = splitName[3];
	Cursor cursor = null;
	Uri uri = asSyncAdapter(Events.CONTENT_URI, accountName, accountType);
	String selection = "((" + Events.CALENDAR_ID + " = ?) AND ("
		+ Events.CALENDAR_DISPLAY_NAME + " = ?))";
	String[] selectionArgs = { calendarId, calendarName };

	cursor = contentResolver.query(uri, EVENT_PROJECTION, selection,
		selectionArgs, null);
	String[] columnNames = cursor.getColumnNames();

	while (cursor.moveToNext()) {
	    ContentValues values = new ContentValues();
	    values.put(Events.CALENDAR_ID, calendar.id);
	    int googleId = cursor.getInt(0);
	    for (int i = 1; i < columnNames.length; i++) {
		String columnName = columnNames[i];
		String data = null;
		String rawdata = cursor.getString(i);
		if (rawdata != null && !rawdata.isEmpty()) {
		    if (columnName.equals(Events.TITLE)
			    || columnName.equals(Events.DESCRIPTION)
			    || columnName.equals(Events.EVENT_LOCATION)) {
			data = CipherTool.decipher(rawdata, secretKey);
			Log.v(TAG, "pullCalendar(): " + data);
		    } else {
			data = rawdata;
		    }
		}
		values.put(columnNames[i], data);
	    }
	    int eventId = dbHelper.getCipherId(googleId);
	    Log.v(TAG, "pullCalendar(): " + eventId);
	    if (eventId == -1) {
		Uri insertUri = asSyncAdapter(Events.CONTENT_URI, account.name,
			account.type);
		Uri insertedUri = contentResolver.insert(insertUri, values);
		eventId = Integer.valueOf(insertedUri.getLastPathSegment());
		dbHelper.addEvent(eventId, googleId);
	    } else {
		Uri editsUri = asSyncAdapter(Events.CONTENT_URI, account.name,
			account.type);
		Uri editUri = ContentUris.withAppendedId(editsUri, eventId);
		int nbRow = contentResolver.update(editUri, values, null, null);
		if (nbRow == 0) {
		    dbHelper.deleteEvent(eventId, googleId);
		    Uri insertUri = asSyncAdapter(Events.CONTENT_URI,
			    account.name, account.type);
		    Uri insertedUri = contentResolver.insert(insertUri, values);
		    eventId = Integer.valueOf(insertedUri.getLastPathSegment());
		    dbHelper.addEvent(eventId, googleId);
		}
		Log.v(TAG, "pullCalendar(): nbRow edited: " + nbRow);
	    }
	}
	Log.v(TAG, "pullCalendar Success");

    }

    private void pushCalendar(EventIdDatabaseHelper dbHelper,
	    CalendarItem calendar, SecretKey secretKey, Account account)
	    throws Exception {
	Log.v(TAG, "pushCalendar()");
	String[] splitName = calendar.calendarName.split("::");
	if (splitName.length != 4)
	    throw new Exception("Can't decipher Calendar, Bad Name");
	String accountType = splitName[0];
	String accountName = splitName[1];
	String calendarId = splitName[3];
	Cursor cursor = null;
	Uri uri = asSyncAdapter(Events.CONTENT_URI, account.name, account.type);
	String selection = "((" + Events.CALENDAR_ID + " = ?) AND ("
		+ Events.CALENDAR_DISPLAY_NAME + " = ?))";
	String[] selectionArgs = { calendar.id, calendar.calendarName };

	cursor = contentResolver.query(uri, EVENT_PROJECTION, selection,
		selectionArgs, null);
	String[] columnNames = cursor.getColumnNames();

	while (cursor.moveToNext()) {
	    ContentValues values = new ContentValues();
	    values.put(Events.CALENDAR_ID, calendarId);
	    int eventId = cursor.getInt(0);
	    for (int i = 1; i < columnNames.length; i++) {
		String columnName = columnNames[i];
		String data = null;
		String rawdata = cursor.getString(i);
		if (rawdata != null && !rawdata.isEmpty()) {
		    if (columnName.equals(Events.TITLE)
			    || columnName.equals(Events.DESCRIPTION)
			    || columnName.equals(Events.EVENT_LOCATION)) {
			data = CipherTool.cipher(rawdata, secretKey);
			Log.v(TAG, "pushCalendar()" + data);
		    } else {
			data = rawdata;
		    }
		}
		values.put(columnNames[i], data);
	    }
	    int googleId = dbHelper.getGoogleId(eventId);
	    if (googleId == -1) {
		Uri insertUri = asSyncAdapter(Events.CONTENT_URI, accountName,
			accountType);
		Uri insertedUri = contentResolver.insert(insertUri, values);
		googleId = Integer.valueOf(insertedUri.getLastPathSegment());
		dbHelper.addEvent(eventId, googleId);
	    } else {
		Uri editsUri = asSyncAdapter(Events.CONTENT_URI, accountName,
			accountType);
		Uri editUri = ContentUris.withAppendedId(editsUri, googleId);
		int nbRow = contentResolver.update(editUri, values, null, null);
		if (nbRow == 0) {
		    dbHelper.deleteEvent(eventId, googleId);
		    Uri insertUri = asSyncAdapter(Events.CONTENT_URI,
			    accountName, accountType);
		    Uri insertedUri = contentResolver.insert(insertUri, values);
		    googleId = Integer
			    .valueOf(insertedUri.getLastPathSegment());
		    dbHelper.addEvent(eventId, googleId);
		}
	    }
	}
	Log.v(TAG, "push Success");
    }
}
