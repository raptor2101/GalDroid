package de.raptor2101.GalDroid.WebGallery.Tasks;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;

import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObjectComment;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryProgressListener;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;

import android.os.AsyncTask;
import android.util.Log;

public class CommentLoaderTask extends AsyncTask<GalleryObject, Progress, List<GalleryObjectComment>> implements GalleryProgressListener {

	private final WebGallery mWebGallery;
	private final WeakReference<CommentLoaderTaskListener> mListener;
	
	public CommentLoaderTask(WebGallery webGallery, CommentLoaderTaskListener listener) {
		mWebGallery = webGallery;
		mListener = new WeakReference<CommentLoaderTaskListener>(listener);
	}
	
	@Override
	protected List<GalleryObjectComment> doInBackground(GalleryObject... params) {
		try {
			return mWebGallery.getDisplayObjectComments(params[0], this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList<GalleryObjectComment>(0);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList<GalleryObjectComment>(0);
		}
	}

	@Override
	protected void onPreExecute() {
		CommentLoaderTaskListener listener = mListener.get();
		if(listener != null){
			listener.onLoadingStarted();
		}
	};
	
	@Override
	protected void onProgressUpdate(Progress... values) {
		CommentLoaderTaskListener listener = mListener.get();
		if(values.length > 0 && listener != null)
		{
			listener.onLoadingProgress(values[0].curValue,values[0].maxValue);
		}		
	}
	
	@Override
	protected void onPostExecute(List<GalleryObjectComment> comments) {
		CommentLoaderTaskListener listener = mListener.get();
		Log.d("CommentLoaderTask", String.format("onPostExecute isCanceled: %s isListenerAvaible: %s",isCancelled(),listener != null));
		if(!isCancelled() && listener != null)
		{
			listener.onLoadingCompleted(comments);
		}
	}
	
	public void handleProgress(int curValue, int maxValue) {
		publishProgress(new Progress(curValue, maxValue));
	}

}
