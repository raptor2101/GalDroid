package de.raptor2101.GalDroid.WebGallery.Gallery3.Tasks;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.raptor2101.GalDroid.WebGallery.Gallery3.ProgressListener;
import de.raptor2101.GalDroid.WebGallery.Gallery3.RestCall;
import de.raptor2101.GalDroid.WebGallery.Gallery3.JSON.Entity;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import android.os.AsyncTask;

public class JSONArrayLoaderTask extends AsyncTask<RestCall, JSONObject, Void> {
		private ArrayList<JSONObject> mJSONObjects;
		private int mIndex;
		private final ProgressListener mProgressListener;
		
		public JSONArrayLoaderTask(ArrayList<JSONObject> jsonObjects, int offset, ProgressListener progressListener){
			mIndex = offset;
			mJSONObjects = jsonObjects;
			mProgressListener = progressListener;
		}
		@Override
		protected Void doInBackground(RestCall... params) {
			try {
				RestCall restCall = params[0];
				JSONArray jsonArray = restCall.loadJSONArray();
				
				int length = jsonArray.length();
				for (int pos = 0; pos < length ; pos++) {
					publishProgress(jsonArray.getJSONObject(pos));
				}
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
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(JSONObject... values) {
			mJSONObjects.set(mIndex++, values[0]);
			mProgressListener.progress();
		}
		
	}