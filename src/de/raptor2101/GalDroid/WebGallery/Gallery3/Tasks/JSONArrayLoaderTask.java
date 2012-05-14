package de.raptor2101.GalDroid.WebGallery.Gallery3.Tasks;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;

import de.raptor2101.GalDroid.WebGallery.Gallery3.RestCall;
import android.os.AsyncTask;

public class JSONArrayLoaderTask extends AsyncTask<RestCall, Void, JSONArray> {
	
	@Override
	protected JSONArray doInBackground(RestCall... params) {
		JSONArray jsonArray = null;
		try {
			RestCall restCall = params[0];
			jsonArray = restCall.loadJSONArray();

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonArray;
	}
}