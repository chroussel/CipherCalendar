package ch.epfl.lasec.ciphercalendar.authenticator;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Authenticator extends AbstractAccountAuthenticator {

    private static final String TAG = "Authenticator";

    private final Context mContext;

    public Authenticator(Context context) {
	super(context);
	mContext = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
	    String accountType, String authTokenType,
	    String[] requiredFeatures, Bundle options) {
	Log.v(TAG, "addAccount()");
	final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
	intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
		response);
	final Bundle bundle = new Bundle();
	bundle.putParcelable(AccountManager.KEY_INTENT, intent);
	return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
	    Account account, Bundle options) throws NetworkErrorException {
	Log.v(TAG, "confirmCredentials()");
	return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response,
	    String accountType) {
	Log.v(TAG, "editProperties()");
	throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
	    Account account, String authTokenType, Bundle options)
	    throws NetworkErrorException {
	Log.v(TAG, "getAuthToken()");
	return null;

    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
	Log.v(TAG, "getAuthTokenLabel()");
	return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
	    Account account, String[] features) throws NetworkErrorException {
	Log.v(TAG, "hasFeatures()");
	final Bundle result = new Bundle();
	result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
	return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
	    Account account, String authTokenType, Bundle options)
	    throws NetworkErrorException {
	Log.v(TAG, "updateCredentials()");

	return null;
    }

}
