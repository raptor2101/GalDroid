/*
 * GalDroid - a webgallery frontend for android
 * Copyright (C) 2011  Raptor 2101 [raptor2101@gmx.de]
 *		
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
 */

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
			mConfigInstance.mGalleryObjects = galleryObjects;
			onGalleryObjectsLoaded(galleryObjects);		
		}
	}
	
	private class GalleryNonConfigurationInstance{
		public List<GalleryObject> mGalleryObjects;
		public int currentIndex;
	}
	private GalleryNonConfigurationInstance mConfigInstance;
	
	private ProgressDialog mProgressDialog;
	private GalleryLoaderTaskListener mListener;
	
	@SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	try {
			GalDroidApp app = (GalDroidApp)getApplicationContext();
			app.Initialize(this);
			
			String uniqueId = getUnqiueId();
			
			// Try to get galleryobjects from activity cache
			mConfigInstance = (GalleryNonConfigurationInstance) getLastNonConfigurationInstance();
			if(mConfigInstance == null){
				mConfigInstance = new GalleryNonConfigurationInstance();
				mConfigInstance.currentIndex = -1;
			}
			
			
			// Try to get galleryobjects from application cache
			if(mConfigInstance.mGalleryObjects == null){
				mConfigInstance.mGalleryObjects = app.loadStoredGalleryObjects(uniqueId);
			}
			
			// Now we have time, load the object from the remote source
			if(mConfigInstance.mGalleryObjects == null){
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
				onGalleryObjectsLoaded(mConfigInstance.mGalleryObjects);
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public final Object onRetainNonConfigurationInstance() {
		return mConfigInstance;
	}
	
	public abstract void onGalleryObjectsLoaded(List<GalleryObject> galleryObjects);
	
	public String getUnqiueId(){
		try {
			return getIntent().getExtras().getString("Current UniqueId");
		} catch (NullPointerException e) {
			return null;
		} 
		
	}
	
	public int getCurrentIndex(){
		return mConfigInstance.currentIndex;
	}
	
	protected void setCurrentIndex(int index){
		mConfigInstance.currentIndex = index;
	}
}
