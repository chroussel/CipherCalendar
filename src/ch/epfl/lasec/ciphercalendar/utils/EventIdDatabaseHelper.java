package ch.epfl.lasec.ciphercalendar.utils;

import java.util.ArrayList;
import java.util.List;

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
    public static final String COLUMN_ID_CIPHER = "_id_event";
    public static final String COLUMN_ID_GOOGLE = "_id_google";
    private static final String DATABASE_CREATE = "create table " + TABLE_EVENT
	    + "(" + COLUMN_ID + " integer primary key autoincrement, "
	    + COLUMN_ID_CIPHER + " integer," + COLUMN_ID_GOOGLE + " integer);";
    private final String[] allColumns = { EventIdDatabaseHelper.COLUMN_ID,
	    EventIdDatabaseHelper.COLUMN_ID_CIPHER,
	    EventIdDatabaseHelper.COLUMN_ID_GOOGLE };

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

    public int getCipherId(int googleId) {
	SQLiteDatabase db = this.getReadableDatabase();
	String[] selection = { COLUMN_ID_CIPHER };
	Cursor cursor = db.query(TABLE_EVENT, selection, COLUMN_ID_GOOGLE
		+ "=?", new String[] { String.valueOf(googleId) }, null, null,
		null);
	if (cursor != null)
	    cursor.moveToFirst();
	else
	    return -1;
	if (cursor.getCount() == 0)
	    return -1;
	int id = cursor.getInt(0);
	return id;
    }

    public int getGoogleId(int cipherId) {
	SQLiteDatabase db = this.getReadableDatabase();
	String[] selection = { COLUMN_ID_GOOGLE };
	Cursor cursor = db.query(TABLE_EVENT, selection, COLUMN_ID_CIPHER
		+ "=?", new String[] { String.valueOf(cipherId) }, null, null,
		null);
	if (cursor != null)
	    cursor.moveToFirst();
	else
	    return -1;
	if (cursor.getCount() == 0)
	    return -1;
	int id = cursor.getInt(0);
	return id;
    }

    public List<Integer> getAllGoogleId() {
	List<Integer> listGoogleId = new ArrayList<Integer>();
	String select = "SELECT" + COLUMN_ID_GOOGLE + "FROM " + TABLE_EVENT;
	SQLiteDatabase db = this.getReadableDatabase();
	Cursor cursor = db.rawQuery(select, null);
	if (cursor.moveToFirst()) {
	    do {
		listGoogleId.add(cursor.getInt(0));
	    } while (cursor.moveToNext());
	}
	return listGoogleId;
    }

    public List<Integer> getAllCipherId() {
	List<Integer> listCipherId = new ArrayList<Integer>();
	String select = "SELECT" + COLUMN_ID_CIPHER + "FROM " + TABLE_EVENT;
	SQLiteDatabase db = this.getReadableDatabase();
	Cursor cursor = db.rawQuery(select, null);
	if (cursor.moveToFirst()) {
	    do {
		listCipherId.add(cursor.getInt(0));
	    } while (cursor.moveToNext());
	}
	return listCipherId;
    }

    public void addEvent(int cipherId, int googleId) {
	SQLiteDatabase db = this.getWritableDatabase();

	ContentValues values = new ContentValues();
	values.put(COLUMN_ID_CIPHER, cipherId);
	values.put(COLUMN_ID_GOOGLE, googleId);

	db.insert(TABLE_EVENT, null, values);
	db.close();
    }

    public void deleteEvent(int cipherId, int googleId) {
	SQLiteDatabase db = this.getWritableDatabase();
	String[] selectionArgs = new String[] { Integer.toString(googleId),
		Integer.toString(cipherId) };
	String selection = "((" + COLUMN_ID_GOOGLE + "=?) AND ("
		+ COLUMN_ID_CIPHER + "=?))";
	db.delete(TABLE_EVENT, selection, selectionArgs);
	db.close();
    }
}
