package de.raptor2101.GalDroid.WebGallery.Tasks;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryProgressListener;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;

public class GalleryLoaderTask extends AsyncTask<String, Progress, List<GalleryObject>> implements GalleryProgressListener {

	private WeakReference<GalleryLoaderTaskListener> mListener;
	private WebGallery mWebGallery;
	
	public GalleryLoaderTask(WebGallery webGallery, GalleryLoaderTaskListener listener)
	{
		mListener = new WeakReference<GalleryLoaderTaskListener>(listener);
		mWebGallery = webGallery;
	}
	
	@Override
	protected List<GalleryObject> doInBackground(String... params) {
		List<GalleryObject> objects;
		
		if (mWebGallery != null) {
			if (params.length == 0 || params[0] == null) {
				objects = mWebGallery.getDisplayObjects(this);
			} else {
				objects = mWebGallery.getDisplayObjects(params[0], this);
			}
		}
		else {
			objects = new ArrayList<GalleryObject>(0);
		}
		return objects;
	}
	@Override
	protected void onPreExecute() {
		GalleryLoaderTaskListener listener = mListener.get();
		if(listener != null){
			listener.onDownloadStarted();
		}
	};
	
	@Override
	protected void onProgressUpdate(Progress... values) {
		GalleryLoaderTaskListener listener = mListener.get();
		if(values.length > 0 && listener != null)
		{
			listener.onDownloadProgress(values[0].curValue,values[0].maxValue);
		}		
	}
	
	@Override
	protected void onPostExecute(List<GalleryObject> galleryObjects) {
		GalleryLoaderTaskListener listener = mListener.get();
		Log.d("GalleryLoaderTask", String.format("onPostExecute isCanceled: %s isListenerAvaible: %s",isCancelled(),listener != null));
		if(!isCancelled() && listener != null)
		{
			listener.onDownloadCompleted(galleryObjects);
		}
	}

	public void handleProgress(int curValue, int maxValue) {
		publishProgress(new Progress(curValue, maxValue));
	}

}
