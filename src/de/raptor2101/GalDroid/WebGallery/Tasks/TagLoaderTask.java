package de.raptor2101.GalDroid.WebGallery.Tasks;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryProgressListener;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
import android.os.AsyncTask;
import android.util.Log;

public class TagLoaderTask extends AsyncTask<GalleryObject, Progress, List<String>> implements GalleryProgressListener{
	
	private final WebGallery mWebGallery;
	private final WeakReference<TagLoaderTaskListener> mListener;
	
	public TagLoaderTask(WebGallery webGallery, TagLoaderTaskListener listener) {
		mWebGallery = webGallery;
		mListener = new WeakReference<TagLoaderTaskListener>(listener);
	}
	
	@Override
	protected List<String> doInBackground(GalleryObject... params) {
		try {
			return mWebGallery.getDisplayObjectTags(params[0], this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList<String>(0);
		}
	}

	@Override
	protected void onPreExecute() {
		TagLoaderTaskListener listener = mListener.get();
		if(listener != null){
			listener.onLoadingStarted();
		}
	};
	
	@Override
	protected void onProgressUpdate(Progress... values) {
		TagLoaderTaskListener listener = mListener.get();
		if(values.length > 0 && listener != null)
		{
			listener.onLoadingProgress(values[0].curValue,values[0].maxValue);
		}		
	}
	
	@Override
	protected void onPostExecute(List<String> tags) {
		TagLoaderTaskListener listener = mListener.get();
		Log.d("TagLoaderTask", String.format("onPostExecute isListenerAvaible: %s", listener != null));
		if(listener != null)
		{
			listener.onLoadingCompleted(tags);
		}
	}
	
	@Override
	protected void onCancelled() {
		TagLoaderTaskListener listener = mListener.get();
		Log.d("TagLoaderTask", String.format("onCancelled isListenerAvaible: %s", listener != null));
		if(listener != null)
		{
			listener.onLoadingCanceled();
		}
	}
	
	public void handleProgress(int curValue, int maxValue) {
		publishProgress(new Progress(curValue, maxValue));
	}
}
