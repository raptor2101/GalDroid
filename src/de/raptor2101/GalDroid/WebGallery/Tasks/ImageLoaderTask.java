package de.raptor2101.GalDroid.WebGallery.Tasks;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.util.Log;
import de.raptor2101.GalDroid.WebGallery.GalleryCache;
import de.raptor2101.GalDroid.WebGallery.GalleryDownloadObject;
import de.raptor2101.GalDroid.WebGallery.GalleryImage;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;

public class ImageLoaderTask extends AsyncTask<Void, Progress, GalleryDownloadObject> {

	private WebGallery mWebGallery;
	private GalleryCache mCache;
	private GalleryDownloadObject mDownloadObject;
	private WeakReference<ImageLoaderTaskListener> mListener;
	private InputStream mInputStream;
	private Boolean isCached;
	
	public ImageLoaderTask(WebGallery webGallery, GalleryCache cache, GalleryDownloadObject downloadObject){
		mWebGallery = webGallery;
		mCache = cache;
		mDownloadObject = downloadObject;
		mListener = new WeakReference<ImageLoaderTaskListener>(null); 
	}
	
	public void setListener(ImageLoaderTaskListener listener){
		mListener = new WeakReference<ImageLoaderTaskListener>(listener); 
	}
	
	@Override
	protected void onPreExecute() {
		mInputStream = mCache.getFileStream(mDownloadObject.getUniqueId());
		
		ImageLoaderTaskListener listener = mListener.get();
		if(listener != null){
			listener.onLoadingStarted(mDownloadObject.getUniqueId());
		}
	};
	
	@Override
	protected GalleryDownloadObject doInBackground(Void... params) {
		try {
			
			if(mInputStream == null){
				mInputStream = mWebGallery.getImageRawData(mDownloadObject.getGalleryObject(),mDownloadObject.getImageSize());
				isCached = false;
			}
			else
			{
				isCached = true;
			}
			
			Options options = new Options();
			options.inPreferQualityOverSpeed = true;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			options.inDither = true;
			options.inScaled = false;
			options.inPurgeable = true;
			options.inInputShareable = true;
			System.gc();
			Bitmap bitmap = BitmapFactory.decodeStream(mInputStream,null,options );
			mDownloadObject.setBitmap(bitmap);
			return mDownloadObject;
		} catch (Exception e) {
			Log.w("ImageLoaderTask", String.format("Something goes wrong while Downloading %s. ExceptionMessage: %s",mDownloadObject,e.getMessage()));
			return mDownloadObject;
		}
	}
	
	@Override
	protected void onPostExecute(GalleryDownloadObject downloadObject) {
		if(!isCancelled()&&downloadObject.isValid())
		{
			Bitmap bitmap = downloadObject.getBitmap();
			if (!isCached) {
				mCache.storeBitmap(downloadObject.getUniqueId(), bitmap);
			}
			else{
				mCache.cacheBitmap(downloadObject.getUniqueId(), bitmap);
			}
				
			GalleryImage image = downloadObject.getGalleryImage();
		    if (image!=null) {
		    	image.setBitmap(bitmap);
		    }
		}
		ImageLoaderTaskListener listener = mListener.get();
		if(listener != null){
			listener.onLoadingCompleted(mDownloadObject.getUniqueId());
		}
	}
}
