package ch.epfl.lasec.ciphercalendar.authenticator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AuhentificationService extends Service {
    private static final String TAG = "AuthenticationService";

    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {

	Log.v(TAG, "CipherCalendar Authentication Service Started.");

	mAuthenticator = new Authenticator(this);
    }

    @Override
    public void onDestroy() {

	Log.v(TAG, "CipherCalendar Authentication Service stopped.");

    }

    @Override
    public IBinder onBind(Intent intent) {

	Log.v(TAG,
		"getBinder()... Returning the AccountAuthenticator binder for intent");

	return mAuthenticator.getIBinder();
    }

}
