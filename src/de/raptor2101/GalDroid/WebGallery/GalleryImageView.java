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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTask;
import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTaskListener;

public class GalleryImageView extends LinearLayout implements ImageLoaderTaskListener{
	private final ProgressBar mProgressBar;
	private final ImageView mImageView;
	private final TextView mTitleTextView;
	private final boolean mShowTitle;
	private GalleryObject mGalleryObject;
	private Bitmap mBitmap;
	private WeakReference<ImageLoaderTask> mImageLoaderTask;
	 
	
	public GalleryImageView(Context context, android.view.ViewGroup.LayoutParams layoutParams, boolean showTitle) {
		super(context);
		mShowTitle = showTitle;
		
		mImageView = CreateImageView(context);
		
		mProgressBar = new ProgressBar(context,null,android.R.attr.progressBarStyle);
		mProgressBar.setVisibility(GONE);
		this.addView(mProgressBar);
		
		this.setOrientation(VERTICAL);
		this.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
		this.setLayoutParams(layoutParams);
		
		this.addView(mImageView);
		
		
		
		if(mShowTitle){
			mTitleTextView = new TextView(context);
			mTitleTextView.setTextSize(16);
			mTitleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
			mTitleTextView.setTypeface(Typeface.create("Tahoma", Typeface.BOLD));
			this.addView(mTitleTextView);
		}
		else{
			mTitleTextView = null;
		}
		
		mImageLoaderTask = new WeakReference<ImageLoaderTask>(null);
	}
	
	public void setGalleryObject(GalleryObject galleryObject)
	{
		mGalleryObject = galleryObject;
		this.setTilte(galleryObject.getTitle());
	}
	
	public GalleryObject getGalleryObject(){
		return mGalleryObject;
	}
		
	public void setTilte(String title)
	{
		if(mTitleTextView != null){
			mTitleTextView.setText(title);
		}
	}
	
	private ImageView CreateImageView(Context context) {
		ImageView imageView = new ImageView(context);
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setPadding(5, 5, 5, 5);
        imageView.setDrawingCacheEnabled(false);
        
		return imageView;
	}

	public void onLoadingStarted(String uniqueId) {
		mProgressBar.setVisibility(VISIBLE);
	}

	public void onLoadingCompleted(String uniqueId, Bitmap bitmap) {
		mProgressBar.setVisibility(GONE);
		mImageView.setImageBitmap(bitmap);
		mBitmap = bitmap;
		
	}
	
	public void cancelDownloadTask(){
		ImageLoaderTask task = mImageLoaderTask.get();
		if(task != null){
			task.cancel(true);
		}
	}
	
	public void recylceBitmap(){
		if (mBitmap != null) {
			Log.d("GalleryImageView", String.format("Recycle %s",mGalleryObject));
			mImageView.setImageBitmap(null);
			mBitmap.recycle();
			mBitmap = null;
		}
	}
	
	public Matrix getImageMatrix(){
		return mImageView.getMatrix();
	}
	
	public void setImageMatrix(Matrix matrix) {
		mImageView.setScaleType(ImageView.ScaleType.MATRIX);
		mImageView.setImageMatrix(matrix);
	}

	public void setImageLoaderTask(ImageLoaderTask downloadTask) {
		mImageLoaderTask = new WeakReference<ImageLoaderTask>(downloadTask);		
	}

	public boolean isLoaded() {
		return mBitmap != null;
	}

	public boolean isLoading() {
		ImageLoaderTask task = mImageLoaderTask.get();
		return task != null && task.getStatus() != Status.FINISHED;
	}
}

