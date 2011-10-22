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
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery.ImageSize;
import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTask;

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
	
	private GalleryCache mCache;
	private WebGallery mWebGallery;
	private Context mContext;
	
	private List<GalleryObject> mGalleryObjects;
	private TitleConfig mTitleConfig;
	private LayoutParams mLayoutParams;
	private ScaleMode mScaleMode;
	private ImageSize mImageSize;
	private CleanupMode mCleanupMode;
	
	private ArrayList<WeakReference<GalleryImageView>> mImageViews;
	
	private Queue<WeakReference<ImageLoaderTask>> mActiveDownloadTasks;
	private int mMaxActiveDownloadTask; 
	
	public GalleryImageAdapter(Context context, LayoutParams layoutParams, ScaleMode scaleMode) {
		super();
		GalDroidApp appContext = (GalDroidApp)context.getApplicationContext();
		this.mContext = context;
		this.mCache = appContext.getGalleryCache();
		this.mWebGallery = appContext.getWebGallery();
		this.mLayoutParams = layoutParams;
		
		mGalleryObjects = new ArrayList<GalleryObject>(0);
		mImageViews = new ArrayList<WeakReference<GalleryImageView>>(0);
		
		mTitleConfig = TitleConfig.ShowTitle;
		mImageSize = ImageSize.Thumbnail;
		mCleanupMode = CleanupMode.None;
		mScaleMode = scaleMode;
		mActiveDownloadTasks = new LinkedList<WeakReference<ImageLoaderTask>>(); 
		mMaxActiveDownloadTask = -1;
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
	
	public void setMaxActiveDownloads(int maxActiveDownloads) {
		mMaxActiveDownloadTask = maxActiveDownloads;
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
		String uniqueID = galleryObject.getUniqueId(mImageSize);
		Log.d(ClassTag, String.format("Request View %s", uniqueID));
		
		GalleryImageView imageView;
		if(cachedView != null){
			imageView =(GalleryImageView)cachedView;
			Log.d(ClassTag, String.format("Cached View %s", imageView.getUniqueId()));
			if(imageView.getGalleryObject().getObjectId() != galleryObject.getObjectId()) {
				Log.d(ClassTag, String.format("Abort downloadTask %s", imageView.getUniqueId()));
				imageView.cancelImageLoaderTask();
				if(mCleanupMode == CleanupMode.ForceCleanup) {
					imageView.recylceBitmap();
				}
			}					
		}
		
		imageView= mImageViews.get(position).get();
		if(imageView == null) {
			Log.d(ClassTag, String.format("Miss ImageView Reference", uniqueID));
			imageView = new GalleryImageView(mContext,this.mLayoutParams,this.mTitleConfig == TitleConfig.ShowTitle);
			imageView.setLayoutParams(mLayoutParams);
			imageView.setGalleryObject(galleryObject, uniqueID);
			mImageViews.set(position, new WeakReference<GalleryImageView>(imageView));
		}
		if(!imageView.isLoaded() && ! imageView.isLoading()){
			Log.d(ClassTag, String.format("Init Reload", imageView.getGalleryObject()));
			loadGalleryImage(galleryObject, uniqueID, imageView);
		}
		
		return imageView;
		
	}

	private void loadGalleryImage(GalleryObject galleryObject, String uniqueId,	GalleryImageView imageView) {
		Bitmap cachedBitmap = mCache.getBitmap(uniqueId);
		if(cachedBitmap == null){
			GalleryDownloadObject dowbloadObject = new GalleryDownloadObject(galleryObject, mImageSize);
			ImageLoaderTask downloadTask = new ImageLoaderTask(mWebGallery, mCache, dowbloadObject);
			imageView.setImageLoaderTask(downloadTask);
			downloadTask.setListener(imageView);
			if (mScaleMode == ScaleMode.ScaleSource) {
				downloadTask.setLayoutParams(mLayoutParams);
			}
			mActiveDownloadTasks.offer(new WeakReference<ImageLoaderTask>(downloadTask));
			checkActiveDownloads();
			downloadTask.execute();
		}
		else
		{
			imageView.onLoadingCompleted(uniqueId, cachedBitmap);
		}
	}

	private void checkActiveDownloads() {
		while(mMaxActiveDownloadTask >-1 && mActiveDownloadTasks.size()>mMaxActiveDownloadTask) {
			WeakReference<ImageLoaderTask> reference = mActiveDownloadTasks.poll();
			ImageLoaderTask task = reference.get();
			
			if(task != null) {
				task.cancel(true);
				reference.clear();
			}
		}
		
	}

	public void cleanUp() {
		Log.d(ClassTag, String.format("CleanUp"));
		for(WeakReference<GalleryImageView> reference:mImageViews) {
			GalleryImageView imageView = reference.get();
			if(imageView != null) {
				Log.d(ClassTag, String.format("CleanUp ", imageView.getGalleryObject().getUniqueId(mImageSize)));
				imageView.cancelImageLoaderTask();
				imageView.recylceBitmap();
			}
		}
		System.gc();
	}

	public void refreshImages() {
		for(WeakReference<GalleryImageView> reference:mImageViews) {
			GalleryImageView imageView = reference.get();
			if(imageView != null && !imageView.isLoaded()) {
				Log.d(ClassTag, String.format("Relaod ", imageView.getGalleryObject()));
				GalleryObject galleryObject = imageView.getGalleryObject();
				String uniqueID = galleryObject.getUniqueId(mImageSize);
				
				loadGalleryImage(galleryObject, uniqueID, imageView);
			}
		}
	}

	public boolean isLoaded() {
		return mGalleryObjects.size() != 0;
	}
}
