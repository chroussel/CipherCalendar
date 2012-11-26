package ch.epfl.lasec.ciphercalendar.authenticator;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import ch.epfl.lasec.ciphercalendar.Constants;
import ch.epfl.lasec.ciphercalendar.R;
import ch.epfl.lasec.ciphercalendar.utils.CipherTool;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";

    public static final String PARAM_PASSWORD = "password";

    public static final String PARAM_USERNAME = "username";

    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    private static final String TAG = "AuthenticatorActivity";

    private AccountManager mAccountManager;

    private UserLoginTask mAuthTask = null;
    private Boolean mConfirmCredentials = false;
    private TextView mMessage;
    private String mPassword;
    private EditText mPasswordEdit;

    protected boolean mRequestNewAccount = false;
    private String mUsername;
    private EditText mUsernameEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	Log.i(TAG, "onCreate(" + savedInstanceState + ")");
	super.onCreate(savedInstanceState);
	mAccountManager = AccountManager.get(this);
	Log.i(TAG, "loading data from intent");

	final Intent intent = getIntent();
	mUsername = intent.getStringExtra(PARAM_USERNAME);
	mRequestNewAccount = mUsername == null;

	mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRM_CREDENTIALS,
		false);
	Log.i(TAG, "   request new: " + mRequestNewAccount);
	requestWindowFeature(Window.FEATURE_LEFT_ICON);
	setContentView(R.layout.activity_authenticator);
	getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
		android.R.drawable.ic_dialog_alert);
	mMessage = (TextView) findViewById(R.id.message);
	mUsernameEdit = (EditText) findViewById(R.id.username_edit);
	mPasswordEdit = (EditText) findViewById(R.id.password_edit);

	if (!TextUtils.isEmpty(mUsername))
	    mUsernameEdit.setText(mUsername);
	mMessage.setText(getMessage());
    }

    public void handleLogin(View view) {
	Log.v(TAG, "handleLogin()");
	if (mRequestNewAccount) {
	    mUsername = mUsernameEdit.getText().toString();
	}

	mPassword = mPasswordEdit.getText().toString();

	if (TextUtils.isEmpty(mUsername) || TextUtils.isEmpty(mPassword)) {
	    mMessage.setText(getMessage());
	} else {
	    // TODO create KeyStore or load KeyStore
	    mAuthTask = new UserLoginTask();
	    mAuthTask.execute();
	}
    }

    private CharSequence getMessage() {
	Log.v(TAG, "getMessage()");
	getString(R.string.label);
	if (TextUtils.isEmpty(mUsername)) {
	    final CharSequence msg = getText(R.string.login_activity_newaccount_text);
	    return msg;
	}

	if (TextUtils.isEmpty(mPassword)) {
	    return getText(R.string.login_activity_loginfail_text_pwmissing);
	}
	return null;
    }

    private void finishConfirmCredentials(boolean result) {
	Log.i(TAG, "finishConfirmCredentials()");
	final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);
	mAccountManager.setPassword(account, mPassword);
	final Intent intent = new Intent();
	intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result);
	setAccountAuthenticatorResult(intent.getExtras());
	setResult(RESULT_OK, intent);
	finish();
    }

    /**
     * Called when response is received from the server for authentication
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller. We store the
     * authToken that's returned from the server as the 'password' for this
     * account - so we're never storing the user's actual password locally.
     * 
     * @param result
     *            the confirmCredentials result.
     */
    private void finishLogin(String fileName) {

	Log.i(TAG, "finishLogin()");
	final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);
	if (mRequestNewAccount) {
	    mAccountManager.addAccountExplicitly(account, mPassword, null);
	    ContentResolver.setSyncAutomatically(account,
		    CalendarContract.AUTHORITY, true);
	    mAccountManager.setUserData(account,
		    Constants.KEY_FILENAME_KEYFILE, fileName);
	} else {
	    mAccountManager.setPassword(account, mPassword);
	}

	final Intent intent = new Intent();
	intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
	intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);

	setAccountAuthenticatorResult(intent.getExtras());

	setResult(RESULT_OK, intent);
	finish();
    }

    public void onAuthenticationResult(String fileName) {

	Log.i(TAG, "onAuthenticationResult(" + fileName + ")");

	// Our task is complete, so clear it out
	mAuthTask = null;
	boolean empty = TextUtils.isEmpty(fileName);
	if (!empty) {
	    if (!mConfirmCredentials) {
		finishLogin(fileName);
	    } else {
		finishConfirmCredentials(TextUtils.isEmpty(fileName));
	    }
	} else {
	    Log.e(TAG, "onAuthenticationResult: failed to authenticate");
	    if (mRequestNewAccount) {
		// "Please enter a valid username/password.
		mMessage.setText(getText(R.string.login_activity_loginfail_text_both));
	    } else {
		// "Please enter a valid password." (Used when the
		// account is already in the database but the password
		// doesn't work.)
		mMessage.setText(getText(R.string.login_activity_loginfail_text_pwonly));
	    }
	}
    }

    public class UserLoginTask extends AsyncTask<Void, Void, String> {

	@Override
	protected String doInBackground(Void... params) {
	    try {
		return CipherTool.Authenticate(getApplicationContext(),
			mUsername, mPassword);

	    } catch (Exception ex) {
		Log.e(TAG,
			"UserLoginTask.doInBackground: failed to authenticate");
		Log.i(TAG, ex.toString());
		return null;
	    }
	}

	@Override
	protected void onPostExecute(final String fileName) {
	    // On a successful authentication, call back into the Activity to
	    // communicate the authToken (or null for an error).
	    onAuthenticationResult(fileName);
	}
    }
}
