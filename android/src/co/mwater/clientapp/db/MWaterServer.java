package co.mwater.clientapp.db;

import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import co.mwater.clientapp.dbsync.RESTClient;

// TODO cleanup this class
public class MWaterServer {
	private static final String PREF_NAME = "Login";

	//###static final public String serverUrl = "https://data.mwater.co/mwater/sync/";

	static final public String serverUrl = "http://192.168.0.2:8000/mwater/sync/";

	static public void login(Context context, String username, String clientUid, List<String> roles) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString("username", username);
		editor.putString("clientUid", clientUid);
		editor.putString("roles", PreferenceUtils.listToString(roles));
		editor.commit();
	}

	static public String getClientUid(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return prefs.getString("clientUid", null);
	}

	static public String getUsername(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return prefs.getString("username", null);
	}

	static public boolean hasRole(Context context, String role) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return PreferenceUtils.stringToList(prefs.getString("roles", "")).contains(role);
	}

	static public RESTClient createClient(Context context) {
		try {
			String userAgent = "mWater/" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
			return new RESTClient(serverUrl, userAgent);
		} catch (NameNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	// TODO get real cert
	static {
		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, javax.net.ssl.SSLSession session) {
				if (hostname.equals("data.mwater.co"))
					return true;
				return false;
			}
		};
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs, String authType) throws CertificateException {
				byte[] trusted = new byte[] {
						56, 78, -127, 94, -29, -46, -31, -88, -72, 25, 7, 12, -45, -74, 51, 73, 16, -30, -7, 23, -63, -4, -77, -125, -60, -3, -70, 111, -93,
						79, -82, -49, -29, -38, -92, -11, 121, 82, -53, -56, -14, 18, 94, -24, -31, 122, -75, 2, -63, -82, 25, -9, -91, 103, -62, -80, 86, 73,
						9, -72, 121, 38, -33, -47, -34, 26, -79, 66, -123, -34, 48, 63, -84, 24, -107, 64, 60, 11, 2, -54, -98, -56, -92, -25, -73, 19, 9, -62,
						-104, -95, -116, 109, -124, 20, 105, -33, 121, 1, 48, -66, -61, -101, 8, 89, -90, 84, -100, -50, -70, 124, -70, -41, 44, -52, -43, 56,
						63, -11, 127, 18, -42, -94, 29, 44, -101, 84, -82, -127
				};
				if (Arrays.equals(certs[0].getSignature(), trusted))
					return;
				throw new CertificateException("Untrusted cert");
			}
		}
		};

		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
