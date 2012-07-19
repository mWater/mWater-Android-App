package co.mwater.clientapp.dbsync;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class RESTClient {
	String baseUrl;
	String charset = "UTF-8";
	String userAgent;

	public RESTClient(String baseUrl, String userAgent) {
		this.baseUrl = baseUrl;
		this.userAgent = userAgent;
	}

	String createQuery(String... args) {
		StringBuilder query = new StringBuilder();
		for (int i = 0; i < args.length; i += 2) {
			if (query.length() > 0)
				query.append("&");
			query.append(args[i]);
			query.append("=");
			try {
				query.append(URLEncoder.encode(args[i + 1], charset));
			} catch (UnsupportedEncodingException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return query.toString();
	}

	public String get(String command, String... args) throws RESTClientException {
		// Construct url
		URL url;
		try {
			url = new URL(baseUrl + command + "?" + createQuery(args));
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}

		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			throw new RESTClientException(e);
		}

		try {
			if (userAgent != null)
				connection.setRequestProperty("User-Agent", userAgent);
			return readStreamString(connection.getInputStream());
		} catch (IOException ioex) {
			int code;
			try {
				code = connection.getResponseCode();
				String errorString = readStreamString(connection.getErrorStream());
				throw new RESTClientException(code, errorString, ioex);
			} catch (IOException e) {
				throw new RESTClientException(e);
			}
		} finally {
			connection.disconnect();
		}
	}

	public String post(String command, String... args) throws RESTClientException {
		// Construct url
		URL url;
		try {
			url = new URL(baseUrl + command);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}

		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			throw new RESTClientException(e);
		}

		try {
			if (userAgent != null)
				connection.setRequestProperty("User-Agent", userAgent);
			connection.setDoOutput(true); // Triggers POST
			String query = createQuery(args);
			connection.setFixedLengthStreamingMode(query.length());
			connection.setRequestProperty("Accept-Charset", charset);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
			OutputStream output = null;
			try {
				output = connection.getOutputStream();
				output.write(query.toString().getBytes(charset));
			} finally {
				if (output != null)
					try {
						output.close();
					} catch (IOException logOrIgnore) {
						// Ignore since on close only
					}
			}

			return readStreamString(connection.getInputStream());
		} catch (IOException ioex) {
			int code;
			try {
				code = connection.getResponseCode();
				String errorString = readStreamString(connection.getErrorStream());
				throw new RESTClientException(code, errorString, ioex);
			} catch (IOException e) {
				throw new RESTClientException(e);
			}
		} finally {
			connection.disconnect();
		}
	}

	String readStreamString(InputStream inputStream) throws IOException {
		if (inputStream == null)
			return "";

		final char[] buffer = new char[8 * 1024];
		StringBuilder out = new StringBuilder();
		Reader in;
		try {
			in = new InputStreamReader(inputStream, charset);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}

		try {
			int read;
			do {
				read = in.read(buffer, 0, buffer.length);
				if (read > 0) {
					out.append(buffer, 0, read);
				}
			} while (read >= 0);
		} finally {
			in.close();
		}
		return out.toString();
	}

}
