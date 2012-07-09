package com.github.androidimageprocessing.bacteria.dbsync;

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
	StringBuilder query;
	String charset = "UTF-8";

	public RESTClient(String baseUrl) {
		this.baseUrl = baseUrl;
		query=new StringBuilder();
	}

	public void addParam(String name, String value) {
		if (query.length()>0)
			query.append("&");
		query.append(name);
		query.append("=");
		try {
			query.append(URLEncoder.encode(value, charset));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public String get() throws IOException, RESTClientException
	{
		// Construct url
		URL url;
		try {
			url = new URL(baseUrl + "?" + query.toString());
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		try {
			return readStreamString(connection.getInputStream());
		}
		catch (IOException ioex) {
			int code = connection.getResponseCode();
			throw new RESTClientException(code);
		}
		finally {
			connection.disconnect();
		}
	}
	
	public String post() throws IOException, RESTClientException
	{
		// Construct url
		URL url;
		try {
			url = new URL(baseUrl);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		try {
			connection.setDoOutput(true); // Triggers POST.
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
		}
		catch (IOException ioex) {
			int code = connection.getResponseCode();
			throw new RESTClientException(code);
		}
		finally {
			connection.disconnect();
		}
	}

	String readStreamString(InputStream inputStream) throws IOException {
		final char[] buffer = new char[8*1024];
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
		    if (read>0) {
		      out.append(buffer, 0, read);
		    }
		  } while (read>=0);
		} finally {
			in.close();
		}
		return out.toString();	
	}
	
}
