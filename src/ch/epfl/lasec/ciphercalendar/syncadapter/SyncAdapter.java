package ch.epfl.lasec.ciphercalendar.syncadapter;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidParameterSpecException;
import java.util.List;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;
import ch.epfl.lasec.ciphercalendar.Constants;
import ch.epfl.lasec.ciphercalendar.calendartools.CalendarContentResolver;
import ch.epfl.lasec.ciphercalendar.calendartools.CalendarItem;
import ch.epfl.lasec.ciphercalendar.utils.CipherTool;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";

    private final Context mContext;
    private final AccountManager mAccountManager;
    private final CalendarContentResolver calendarContentResolver;

    public SyncAdapter(Context context, boolean autoInitialize) {
	super(context, autoInitialize);
	mContext = context;
	mAccountManager = AccountManager.get(context);
	calendarContentResolver = new CalendarContentResolver(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
	    ContentProviderClient provider, SyncResult syncResult) {
	// Use the Account manager to get the Key we'll need to cipher and
	// decipher events.
	Log.v(TAG, "onPerformSync()");
	try {
	    final String password = mAccountManager.getPassword(account);
	    final String fileName = mAccountManager.getUserData(account,
		    Constants.KEY_FILENAME_KEYFILE);
	    final SecretKey secretKey = CipherTool.getKey(mContext, fileName,
		    password);
	    List<CalendarItem> Calendars = calendarContentResolver.getCalendar(
		    account.name, account.type);
	    for (CalendarItem calendar : Calendars) {
		calendarContentResolver.syncCalendar(getContext(), calendar,
			secretKey, account);
	    }
	} catch (UnrecoverableKeyException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (InvalidKeyException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (KeyStoreException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (NoSuchAlgorithmException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (CertificateException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (NoSuchPaddingException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (InvalidParameterSpecException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (InvalidAlgorithmParameterException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

}
