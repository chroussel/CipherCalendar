package ch.epfl.lasec.ciphercalendar.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

public class CipherTool {
    private static final String TAG = "CipherTool";
    private static final String cipherMethod = "AES/CBC/PKCS5Padding";
    private static final String cipherEntry = "CipherKey";

    private static SecretKey key;

    public static String Authenticate(Context context, String username,
	    String password) {
	Log.v(TAG, "Authenticate(" + username + "," + password + ")");
	Pattern p = Pattern.compile("[a-zA-Z0-9]+");
	Matcher m = p.matcher(username);
	if (!m.matches()) {
	    return null;
	}
	String fileName = username + ".key";
	try {
	    File file = context.getFileStreamPath(fileName);
	    if (file.exists()) {
		LoadKeyFromStore(context, fileName, password);
		System.out.println("Key Loaded");
		return fileName;
	    } else {
		CreateKeyAndKeyStore(context, fileName, password);
		System.out.println("Key and Keystore Created");
		return fileName;
	    }
	} catch (Exception e) {
	    return null;
	}
    }

    private static void CreateKeyAndKeyStore(Context context, String fileName,
	    String password) throws KeyStoreException,
	    NoSuchAlgorithmException, CertificateException, IOException,
	    InvalidKeySpecException, InvalidKeyException,
	    NoSuchPaddingException, InvalidParameterSpecException,
	    InvalidAlgorithmParameterException {
	Log.v(TAG, "CreateKeyAndKeyStore()");
	FileOutputStream fos = null;
	try {
	    fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
	    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	    ks.load(null, password.toCharArray());
	    KeyGenerator kgen = KeyGenerator.getInstance("AES");
	    kgen.init(256);
	    key = kgen.generateKey();
	    SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(key);
	    ks.setEntry(cipherEntry, skEntry, new KeyStore.PasswordProtection(
		    password.toCharArray()));
	    ks.store(fos, password.toCharArray());
	} finally {
	    if (fos != null) {
		fos.close();
	    }
	}
    }

    public static String cipher(String plainText, SecretKey key)
	    throws InvalidKeyException, NoSuchAlgorithmException,
	    NoSuchPaddingException, IllegalBlockSizeException,
	    BadPaddingException, InvalidParameterSpecException,
	    UnsupportedEncodingException {
	Log.v(TAG, "cipher()");
	Cipher cipher = Cipher.getInstance(cipherMethod);
	cipher.init(Cipher.ENCRYPT_MODE, key);
	byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));
	AlgorithmParameters params = cipher.getParameters();
	byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
	byte[] message = new byte[cipherText.length + iv.length];
	System.arraycopy(cipherText, 0, message, 0, cipherText.length);
	System.arraycopy(iv, 0, message, cipherText.length, iv.length);

	String cipherText64 = Base64.encodeToString(message, Base64.DEFAULT);

	return cipherText64;
    }

    public static String decipher(String cipherText64, SecretKey key)
	    throws InvalidKeyException, InvalidAlgorithmParameterException,
	    NoSuchAlgorithmException, NoSuchPaddingException,
	    IllegalBlockSizeException, BadPaddingException,
	    UnsupportedEncodingException {

	byte[] message = Base64.decode(cipherText64, Base64.DEFAULT);
	Log.v(TAG, "decipher(): " + Arrays.toString(message));
	byte[] cipherText = Arrays.copyOfRange(message, 0, message.length - 16);
	byte[] iv = Arrays.copyOfRange(message, message.length - 16,
		message.length);
	IvParameterSpec spec = new IvParameterSpec(iv);
	Cipher decipher = Cipher.getInstance(cipherMethod);
	decipher.init(Cipher.DECRYPT_MODE, key, spec);

	byte[] plainText = decipher.doFinal(cipherText);
	return new String(plainText, "UTF-8");
    }

    public static void LoadKeyFromStore(Context context, String fileName,
	    String password) throws IOException, KeyStoreException,
	    NoSuchAlgorithmException, CertificateException,
	    UnrecoverableKeyException, InvalidKeyException,
	    NoSuchPaddingException, InvalidParameterSpecException,
	    InvalidAlgorithmParameterException {
	Log.v(TAG, "LoadKeyFromStore()");

	FileInputStream fis = null;
	try {
	    fis = context.openFileInput(fileName);
	    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	    ks.load(fis, password.toCharArray());

	    key = (SecretKey) ks.getKey(cipherEntry, password.toCharArray());

	} finally {
	    if (fis != null)
		fis.close();
	}
    }

    public static SecretKey getKey(Context mContext, String fileName,
	    String password) throws UnrecoverableKeyException,
	    InvalidKeyException, KeyStoreException, NoSuchAlgorithmException,
	    CertificateException, NoSuchPaddingException,
	    InvalidParameterSpecException, InvalidAlgorithmParameterException,
	    IOException {
	Log.v(TAG, "getKey()");
	if (key != null)
	    return key;
	else
	    LoadKeyFromStore(mContext, fileName, password);
	return key;
    }

}
