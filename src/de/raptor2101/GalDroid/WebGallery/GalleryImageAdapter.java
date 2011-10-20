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
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
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

	private GalleryCache mCache;
	private WebGallery mWebGallery;
	private Context mContext;
	
	private List<GalleryObject> mGalleryObjects;
	private TitleConfig mTitleConfig;
	private LayoutParams mLayoutParams;
	private ScaleMode mScaleMode;
	private ImageSize mImageSize;
	
	
	private ArrayList<WeakReference<GalleryImageView>> mImageViews;
	
	
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
		mScaleMode = scaleMode;
	}
	
	public void setGalleryObjects(List<GalleryObject> galleryObjects) {
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
		
		GalleryImageView imageView;
		if(cachedView != null){
			imageView =(GalleryImageView)cachedView;
			if(imageView.getGalleryObject().getObjectId() != galleryObject.getObjectId()) {
				imageView.cleanUp();
			}
					
		}
		
		System.gc();
		
		imageView= mImageViews.get(position).get();
		if(imageView == null) {
			imageView = new GalleryImageView(mContext,this.mLayoutParams,this.mTitleConfig == TitleConfig.ShowTitle);
			imageView.setLayoutParams(mLayoutParams);
				
			imageView.setGalleryObject(galleryObject);
			
			
			LoadGalleryImage(galleryObject, uniqueID, imageView);
			
			
			
			mImageViews.set(position, new WeakReference<GalleryImageView>(imageView));
		}
		
		return imageView;
		
	}

	private void LoadGalleryImage(GalleryObject galleryObject, String uniqueID,	GalleryImageView imageView) {
		GalleryImage galleryImage = new GalleryImage();
		
		Bitmap cachedBitmap = mCache.getBitmap(uniqueID);
		if(cachedBitmap == null){
			GalleryDownloadObject dowbloadObject = new GalleryDownloadObject(galleryObject, mImageSize, galleryImage);
			ImageLoaderTask downloadTask = new ImageLoaderTask(mWebGallery, mCache, dowbloadObject);
			imageView.setImageLoaderTask(downloadTask);
			downloadTask.setListener(imageView);
			if (mScaleMode == ScaleMode.ScaleSource) {
				downloadTask.setLayoutParams(mLayoutParams);
			}
			downloadTask.execute();
		}
		else
		{
			galleryImage.setBitmap(cachedBitmap);
		}
		
		imageView.setGalleryImage(galleryImage);
	}

	public void cleanUp() {
		for(WeakReference<GalleryImageView> reference:mImageViews) {
			GalleryImageView imageView = reference.get();
			if(imageView != null) {
				imageView.cleanUp();
			}
		}
	}

	public void refreshImages() {
		for(WeakReference<GalleryImageView> reference:mImageViews) {
			GalleryImageView imageView = reference.get();
			if(imageView != null) {
				GalleryObject galleryObject = imageView.getGalleryObject();
				String uniqueID = galleryObject.getUniqueId(mImageSize);
				
				LoadGalleryImage(galleryObject, uniqueID, imageView);
			}
		}
	}
}
