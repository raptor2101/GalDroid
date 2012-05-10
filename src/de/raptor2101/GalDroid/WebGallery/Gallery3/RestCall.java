package de.raptor2101.GalDroid.WebGallery.Gallery3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.raptor2101.GalDroid.WebGallery.GalleryStream;

public class RestCall {
	
	private final HttpUriRequest mRequest;
	private final long mSuggestedLength;
	private final Gallery3Imp mWebGallery;
	
	public RestCall(Gallery3Imp webGallery, HttpUriRequest request, long suggestedLength) {
		mRequest = request;
		mSuggestedLength = suggestedLength;
		mWebGallery = webGallery;
	}
	
	public GalleryStream open() throws IOException, ClientProtocolException {
		HttpResponse response = mWebGallery.getHttpClient().execute(mRequest);
		if(response.getStatusLine().getStatusCode() > 200) {
			throw new IOException("Something goes wrong!");
		}
        Header[] headers = response.getHeaders("Content-Length");
        if(headers.length > 0) {
        	long contentLength = Long.parseLong(headers[0].getValue());
        	return new GalleryStream(response.getEntity().getContent(),contentLength);
        } else {
        	return new GalleryStream(response.getEntity().getContent(), mSuggestedLength);
        }
	}
	
	public JSONObject loadJSONObject() throws ClientProtocolException, IOException, JSONException {
		InputStream inputStream = open();
		return parseJSON(inputStream);
	}
	
	public JSONArray loadJSONArray() throws ClientProtocolException, IOException, JSONException {
		InputStream inputStream = open();
		return parseJSONArray(inputStream);
	}
	
	private JSONObject parseJSON(InputStream inputStream) throws IOException, JSONException {
		String content = loadContent(inputStream);
		return new JSONObject(content);
	}
	
	private JSONArray parseJSONArray(InputStream inputStream)
			throws IOException, JSONException {
		String content = loadContent(inputStream);
		return new JSONArray(content);
	}
	private String loadContent(InputStream inputStream)
			throws IOException {
		InputStreamReader streamReader = new InputStreamReader(inputStream);
		BufferedReader reader = new BufferedReader(streamReader);
		StringBuilder stringBuilder = new StringBuilder();
		try {
			String line = null;

			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line + '\n');
			}
		} 
		finally
		{
			reader.close();
			inputStream.close();
		}
		return stringBuilder.toString();
	}

	public Gallery3Imp getWebGallery() {
		return mWebGallery;
	}
	
	

}
