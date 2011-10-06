package de.raptor2101.GalDroid.Activities;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import de.raptor2101.GalDroid.R;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
import de.raptor2101.GalDroid.WebGallery.Tasks.GalleryLoaderTask;
import de.raptor2101.GalDroid.WebGallery.Tasks.GalleryLoaderTaskListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;

public abstract class GalleryActivity extends Activity {
	
	private class GalleryLoadingListener implements GalleryLoaderTaskListener{

		public void onDownloadStarted() {
			mProgressDialog.show();
		}

		public void onDownloadProgress(int elementCount, int maxCount) {
			mProgressDialog.setMax(maxCount);
			mProgressDialog.setProgress(elementCount);
			
		}
		
		public void onDownloadCompleted(List<GalleryObject> galleryObjects) {
			mProgressDialog.dismiss();
			GalDroidApp app = (GalDroidApp)getApplicationContext();
			app.storeGalleryObjects(getUnqiueId(), galleryObjects);
			onGalleryObjectsLoaded(galleryObjects);		
		}
	}
	
	private ProgressDialog mProgressDialog;
	private GalleryLoaderTaskListener mListener;
	private List<GalleryObject> mGalleryObjects;
	
	@SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	try {
			GalDroidApp app = (GalDroidApp)getApplicationContext();
			app.Initialize(this);
			
			String uniqueId = getUnqiueId();
			
			// Try to get galleryobjects from activity cache
			mGalleryObjects = (List<GalleryObject>) getLastNonConfigurationInstance();
			
			// Try to get galleryobjects from application cache
			if(mGalleryObjects == null){
				mGalleryObjects = app.loadStoredGalleryObjects(uniqueId);
			}
			
			// Now we have time, load the object from the remote source
			if(mGalleryObjects == null){
			    mProgressDialog = new ProgressDialog(this);
			    mProgressDialog.setTitle(R.string.progress_title_load);
			    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			    mProgressDialog.setCancelable(false);
			    mProgressDialog.dismiss();
			    
			    WebGallery gallery = app.getWebGallery();
			    if(gallery != null) {
					
					mListener = new GalleryLoadingListener();
					GalleryLoaderTask task = new GalleryLoaderTask(gallery, mListener);
					task.execute(uniqueId);
			    } else {
			    	// Without a valid Gallery nothing can be displayed... back to previous activity
			    	this.finish();
			    }
			} else {
				onGalleryObjectsLoaded(mGalleryObjects);
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public final Object onRetainNonConfigurationInstance() {
		return mGalleryObjects;
	}
	
	public abstract void onGalleryObjectsLoaded(List<GalleryObject> galleryObjects);
	
	public String getUnqiueId(){
		try {
			return getIntent().getExtras().getString("Current UniqueId");
		} catch (NullPointerException e) {
			return null;
		} 
		
	}
}
