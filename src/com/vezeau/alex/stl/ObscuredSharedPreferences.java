package com.vezeau.alex.stl;

import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Base64;

public class ObscuredSharedPreferences implements SharedPreferences {
	private static final String PBE_WITH_MD5_AND_DES = "PBEWithMD5AndDES";
	protected static final String UTF8 = "utf-8";
	private static final char[] SEKRIT = "Coucouroukwikwi666".toCharArray();
	// INSERT A RANDOM PASSWORD HERE.
	// Don't use anything you
	// wouldn't want to
	// get out there if someone
	// decompiled
	// your app.

	protected SharedPreferences delegate;
	protected Context context;

	public static final String MY_PREFS_FILE_NAME = "MYPREFS";

	public ObscuredSharedPreferences(Context context, SharedPreferences delegate) {
		this.delegate = delegate;
		this.context = context;
	}

	public class Editor implements SharedPreferences.Editor {
		protected SharedPreferences.Editor delegate;

		public Editor() {
			this.delegate = ObscuredSharedPreferences.this.delegate.edit();
		}

		public Editor putBoolean(String key, boolean value) {
			delegate.putString(key, encrypt(Boolean.toString(value)));
			return this;
		}

		public Editor putFloat(String key, float value) {
			delegate.putString(key, encrypt(Float.toString(value)));
			return this;
		}

		public Editor putInt(String key, int value) {
			delegate.putString(key, encrypt(Integer.toString(value)));
			return this;
		}

		public Editor putLong(String key, long value) {
			delegate.putString(key, encrypt(Long.toString(value)));
			return this;
		}

		public Editor putString(String key, String value) {
			delegate.putString(key, encrypt(value));
			return this;
		}

		public void apply() {
			delegate.apply();
		}

		public Editor clear() {
			delegate.clear();
			return this;
		}

		public boolean commit() {
			return delegate.commit();
		}

		public Editor remove(String s) {
			delegate.remove(s);
			return this;
		}

		public android.content.SharedPreferences.Editor putStringSet(
				String arg0, Set<String> arg1) {

			delegate.putStringSet(arg0, arg1);
			return this;
		}
	}

	public Editor edit() {
		return new Editor();
	}

	public Map<String, ?> getAll() {
		throw new UnsupportedOperationException(); // left as an exercise to the
													// reader
	}

	public boolean getBoolean(String key, boolean defValue) {
		final String v = delegate.getString(key, null);
		return v != null ? Boolean.parseBoolean(decrypt(v)) : defValue;
	}

	public float getFloat(String key, float defValue) {
		final String v = delegate.getString(key, null);
		return v != null ? Float.parseFloat(decrypt(v)) : defValue;
	}

	public int getInt(String key, int defValue) {
		final String v = delegate.getString(key, null);
		return v != null ? Integer.parseInt(decrypt(v)) : defValue;
	}

	public long getLong(String key, long defValue) {
		final String v = delegate.getString(key, null);
		return v != null ? Long.parseLong(decrypt(v)) : defValue;
	}

	public String getString(String key, String defValue) {
		final String v = delegate.getString(key, null);
		return v != null ? decrypt(v) : defValue;
	}

	public boolean contains(String s) {
		return delegate.contains(s);
	}

	public void registerOnSharedPreferenceChangeListener(
			OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
		delegate.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
	}

	public void unregisterOnSharedPreferenceChangeListener(
			OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
		delegate.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
	}

	protected String encrypt(String value) {

		try {
			final byte[] bytes = value != null ? value.getBytes(UTF8)
					: new byte[0];
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance(PBE_WITH_MD5_AND_DES);
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(SEKRIT));
			Cipher pbeCipher = Cipher.getInstance(PBE_WITH_MD5_AND_DES);
			pbeCipher.init(
					Cipher.ENCRYPT_MODE,
					key,
					new PBEParameterSpec(Settings.Secure.getString(
							context.getContentResolver(),
							Settings.Secure.ANDROID_ID).getBytes(UTF8), 20));
			return new String(Base64.encode(pbeCipher.doFinal(bytes),
					Base64.NO_WRAP), UTF8);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	protected String decrypt(String value) {
		try {
			final byte[] bytes = value != null ? Base64.decode(value,
					Base64.DEFAULT) : new byte[0];
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance(PBE_WITH_MD5_AND_DES);
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(SEKRIT));
			Cipher pbeCipher = Cipher.getInstance(PBE_WITH_MD5_AND_DES);
			pbeCipher.init(
					Cipher.DECRYPT_MODE,
					key,
					new PBEParameterSpec(Settings.Secure.getString(
							context.getContentResolver(),
							Settings.Secure.ANDROID_ID).getBytes(UTF8), 20));
			return new String(pbeCipher.doFinal(bytes), UTF8);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Set<String> getStringSet(String arg0, Set<String> arg1) {
		return delegate.getStringSet(arg0, arg1);

	}

}
