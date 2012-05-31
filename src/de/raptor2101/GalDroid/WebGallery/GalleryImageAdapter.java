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

package de.raptor2101.GalDroid.WebGallery;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import de.raptor2101.GalDroid.Activities.GalDroidApp;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryDownloadObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery.ImageSize;
import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTask;
import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTaskListener;

public class GalleryImageAdapter extends BaseAdapter {
	private final static String ClassTag = "GalleryImageAdapter";
	public enum DisplayTarget{
		Thumbnails,
		FullScreen
	}
	
	public enum TitleConfig{
		ShowTitle,
		HideTitle
	}
	
	public enum ScaleMode {
		ScaleSource,
		DontScale
	}

	public enum CleanupMode {
		ForceCleanup,
		None
	}
	
	//private WebGallery mWebGallery;
	private GalleryCache mCache;
	private Context mContext;
	
	private List<GalleryObject> mGalleryObjects;
	private TitleConfig mTitleConfig;
	private LayoutParams mLayoutParams;
	private ImageSize mImageSize;
	private ScaleMode mScaleMode;
	private CleanupMode mCleanupMode;
	
	private WeakReference<ImageLoaderTaskListener> mListener;
	private ArrayList<WeakReference<GalleryImageView>> mImageViews;
	
	private ImageLoaderTask mImageLoaderTask;
	
	public GalleryImageAdapter(Context context, LayoutParams layoutParams, ScaleMode scaleMode, ImageLoaderTask loaderTask) {
		super();
		GalDroidApp appContext = (GalDroidApp)context.getApplicationContext();
		mContext = context;
		mCache = appContext.getGalleryCache();
		
		mGalleryObjects = new ArrayList<GalleryObject>(0);
		mImageViews = new ArrayList<WeakReference<GalleryImageView>>(0);
		
		mTitleConfig = TitleConfig.ShowTitle;
		mImageSize = ImageSize.Thumbnail;
		mCleanupMode = CleanupMode.None;
		mLayoutParams = layoutParams;
		
		mListener = new WeakReference<ImageLoaderTaskListener>(null);
		mImageLoaderTask = loaderTask;
		mScaleMode = scaleMode;
	}
	
	public void setListener(ImageLoaderTaskListener listener) {
		mListener = new WeakReference<ImageLoaderTaskListener>(listener);
	}
	
	public void setGalleryObjects(List<GalleryObject> galleryObjects) {	
		if(isLoaded()){
			cleanUp();
		}
		this.mGalleryObjects = galleryObjects;
		this.mImageViews = new ArrayList<WeakReference<GalleryImageView>>(galleryObjects.size());
		
		for(int i=0;i<galleryObjects.size();i++){
			mImageViews.add(new WeakReference<GalleryImageView>(null));
		}
		
		notifyDataSetChanged();
	}
	
	public void setTitleConfig(TitleConfig titleConfig) {
		this.mTitleConfig = titleConfig;
	}
	
	public void setDisplayTarget(DisplayTarget displayTarget) {
		mImageSize = displayTarget == DisplayTarget.FullScreen ? ImageSize.Full : ImageSize.Thumbnail;
	}
	
	public void setCleanupMode(CleanupMode cleanupMode) {
		mCleanupMode = cleanupMode;
	}
	
	public List<GalleryObject> getGalleryObjects()
	{
		return mGalleryObjects;
	}
	public int getCount() {
		
		return mGalleryObjects.size();
	}

	public Object getItem(int position) {
		return mGalleryObjects.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
	public View getView(int position, View cachedView, ViewGroup parent) {		
		GalleryObject galleryObject =  mGalleryObjects.get(position);
		
		String objectId = galleryObject.getObjectId();
		Log.d(ClassTag, String.format("Request View %s", objectId));
		
		GalleryImageView imageView = getCachedView(cachedView, galleryObject);
		
		imageView= mImageViews.get(position).get();
		if(imageView == null) {
			Log.d(ClassTag, String.format("Miss ImageView Reference", objectId));
			imageView = CreateImageView(galleryObject);
			mImageViews.set(position, new WeakReference<GalleryImageView>(imageView));
		}
		
		GalleryDownloadObject downloadObject = getDownloadObject(galleryObject);
		
		if(!imageView.isLoaded() && ! mImageLoaderTask.isDownloading(downloadObject)){
			Log.d(ClassTag, String.format("Init Reload", galleryObject.getObjectId()));
			loadGalleryImage(imageView, downloadObject);
		}
		
		return imageView;
	}

	private GalleryDownloadObject getDownloadObject(GalleryObject galleryObject) {
		GalleryDownloadObject downloadObject = mImageSize == ImageSize.Full ? galleryObject.getImage() : galleryObject.getThumbnail();
		return downloadObject;
	}

	private GalleryImageView CreateImageView(GalleryObject galleryObject) {
		GalleryImageView imageView;
		imageView = new GalleryImageView(mContext,this.mLayoutParams,this.mTitleConfig == TitleConfig.ShowTitle);
		imageView.setLayoutParams(mLayoutParams);
		imageView.setGalleryObject(galleryObject);
		imageView.setListener(mListener.get());
		return imageView;
	}

	private GalleryImageView getCachedView(View cachedView, GalleryObject galleryObject) {
		GalleryImageView imageView = null;
		if(cachedView != null){
			imageView =(GalleryImageView)cachedView;
			Log.d(ClassTag, String.format("Cached View %s", imageView.getObjectId()));
			if(imageView.getGalleryObject().getObjectId() != galleryObject.getObjectId()) {
				Log.d(ClassTag, String.format("Abort downloadTask %s", imageView.getObjectId()));
				// TODO abort...
				if(mCleanupMode == CleanupMode.ForceCleanup) {
					imageView.recylceBitmap();
				}
			}					
		}
		return imageView;
	}

	private void loadGalleryImage(GalleryImageView imageView, GalleryDownloadObject downloadObject) {
		if(downloadObject == null) {
			return;
		}
		
		Bitmap cachedBitmap = mCache.getBitmap(downloadObject.getUniqueId());
		if(cachedBitmap == null){
			// TODO Scale mode wieder einbauen
			mImageLoaderTask.download(downloadObject, imageView);
		}
		else
		{
			imageView.onLoadingCompleted(downloadObject.getUniqueId(), cachedBitmap);
		}
	}

	public void cleanUp() {
		Log.d(ClassTag, String.format("CleanUp"));
		for(WeakReference<GalleryImageView> reference:mImageViews) {
			GalleryImageView imageView = reference.get();
			if(imageView != null) {
				Log.d(ClassTag, String.format("CleanUp ", imageView.getObjectId()));
				imageView.recylceBitmap();
			}
		}
		mImageLoaderTask.cancel();
		System.gc();
	}

	public void refreshImages() {
		for(WeakReference<GalleryImageView> reference:mImageViews) {
			GalleryImageView imageView = reference.get();
			if(imageView != null && !imageView.isLoaded()) {
				GalleryObject galleryObject = imageView.getGalleryObject();
				Log.d(ClassTag, String.format("Relaod ", imageView.getObjectId()));
				GalleryDownloadObject downloadObject = getDownloadObject(galleryObject);
				loadGalleryImage(imageView, downloadObject);
			}
		}
	}

	public boolean isLoaded() {
		return mGalleryObjects.size() != 0;
	}

	public ImageLoaderTask getImageLoaderTask() {
		return mImageLoaderTask;
	}
}
