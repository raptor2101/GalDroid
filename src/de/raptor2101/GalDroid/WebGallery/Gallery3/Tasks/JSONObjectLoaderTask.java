package de.raptor2101.GalDroid.WebGallery.Gallery3.Tasks;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import de.raptor2101.GalDroid.WebGallery.Gallery3.ProgressListener;
import de.raptor2101.GalDroid.WebGallery.Gallery3.RestCall;
import android.os.AsyncTask;

public class JSONObjectLoaderTask extends AsyncTask<RestCall, JSONObject, Void> {
		private ArrayList<JSONObject> mJSONObjects;
		private int mIndex;
		private final ProgressListener mProgressListener;
		
		public JSONObjectLoaderTask(ArrayList<JSONObject> jsonObjects, int offset, ProgressListener progressListener){
			mIndex = offset;
			mJSONObjects = jsonObjects;
			mProgressListener = progressListener;
		}
		@Override
		protected Void doInBackground(RestCall... params) {
			for(RestCall restCall:params) {
				try {
					JSONObject jsonObject = restCall.loadJSONObject();
					publishProgress(jsonObject);	
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
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(JSONObject... values) {
			mJSONObjects.set(mIndex++, values[0]);
			mProgressListener.progress();
		}
		
	}