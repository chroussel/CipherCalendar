package ch.epfl.lasec.ciphercalendar.calendartools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EventIdDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "IdDatabaseHelper";

    public static final String TABLE_EVENT = "eventId";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ID_LOCAL = "_id_local";
    public static final String COLUMN_ID_DISTANT = "_id_distant";
    private static final String DATABASE_CREATE = "create table " + TABLE_EVENT
	    + "(" + COLUMN_ID + " integer primary key autoincrement, "
	    + COLUMN_ID_LOCAL + " long," + COLUMN_ID_DISTANT + " long);";

    public EventIdDatabaseHelper(Context context, String name,
	    CursorFactory factory, int version) {
	super(context, name, factory, version);
    }

    private static final String dbName = "eventSync";

    public EventIdDatabaseHelper(Context context) {
	this(context, dbName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
	db.execSQL(DATABASE_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	Log.w(TAG, "Upgrading database from version" + oldVersion + " to "
		+ newVersion);
    }

    public long getLocalId(long distantId) {
	SQLiteDatabase db = getReadableDatabase();
	String[] selection = { COLUMN_ID_LOCAL };
	Cursor cursor = db.query(TABLE_EVENT, selection, COLUMN_ID_DISTANT
		+ "=?", new String[] { String.valueOf(distantId) }, null, null,
		null);
	if (cursor != null)
	    cursor.moveToFirst();
	else
	    return -1;
	if (cursor.getCount() == 0)
	    return -1;
	long id = cursor.getLong(0);
	db.close();
	return id;
    }

    public long getDistantId(long localId) {
	SQLiteDatabase db = getReadableDatabase();
	String[] selection = { COLUMN_ID_DISTANT };
	Cursor cursor = db.query(TABLE_EVENT, selection,
		COLUMN_ID_LOCAL + "=?",
		new String[] { String.valueOf(localId) }, null, null, null);
	if (cursor != null)
	    cursor.moveToFirst();
	else
	    return -1;
	if (cursor.getCount() == 0)
	    return -1;
	long id = cursor.getLong(0);
	db.close();
	return id;
    }

    public void addEvent(Long localId, Long distantId) {
	SQLiteDatabase db = getWritableDatabase();

	ContentValues values = new ContentValues();
	values.put(COLUMN_ID_LOCAL, localId);
	values.put(COLUMN_ID_DISTANT, distantId);

	db.insert(TABLE_EVENT, null, values);
	db.close();
    }

    public int deleteEventDistant(long distantId) {
	SQLiteDatabase db = getWritableDatabase();
	String[] selectionArgs = new String[] { Long.toString(distantId) };
	String selection = COLUMN_ID_DISTANT + "=?";
	int result = db.delete(TABLE_EVENT, selection, selectionArgs);
	db.close();
	return result;
    }

    public int deleteEventLocal(long localId) {
	SQLiteDatabase db = getWritableDatabase();
	String[] selectionArgs = new String[] { Long.toString(localId) };
	String selection = COLUMN_ID_LOCAL + "=?";
	int result = db.delete(TABLE_EVENT, selection, selectionArgs);
	db.close();
	return result;
    }
}
