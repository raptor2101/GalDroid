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

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import de.raptor2101.GalDroid.R;
import de.raptor2101.GalDroid.WebGallery.GalleryCache;
import de.raptor2101.GalDroid.WebGallery.GalleryImageAdapter;
import de.raptor2101.GalDroid.WebGallery.GalleryImageAdapter.CleanupMode;
import de.raptor2101.GalDroid.WebGallery.GalleryImageAdapter.DisplayTarget;
import de.raptor2101.GalDroid.WebGallery.GalleryImageAdapter.ScaleMode;
import de.raptor2101.GalDroid.WebGallery.GalleryImageAdapter.TitleConfig;
import de.raptor2101.GalDroid.WebGallery.GalleryImageView;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTaskListener;



public class ImageViewActivity extends GalleryActivity implements OnTouchListener, OnItemSelectedListener, ImageLoaderTaskListener {
	private enum TouchMode {
		None,
		Drag,
		Zoom,
	}
	
	private Gallery mGalleryFullscreen;
	private Gallery mGalleryThumbnails;
	private PointF mScalePoint = new PointF();
	
	private float mTouchStartY, mTouchStartX,  mOldDist=1, minDragHeight;
	private GalleryImageAdapter mAdapterFullscreen,mAdapterThumbnails;
	
	
	private TouchMode mTouchMode;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	setContentView(R.layout.image_view_activity);
    	super.onCreate(savedInstanceState);
    	mGalleryFullscreen = (Gallery) findViewById(R.id.singleImageGallery);
    	mGalleryThumbnails = (Gallery) findViewById(R.id.thumbnailImageGallery);
    	mGalleryThumbnails.setWillNotCacheDrawing(true);
    	
    	mGalleryFullscreen.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
    	mGalleryFullscreen.setWillNotCacheDrawing(true);
    	
    	LayoutParams params = this.getWindow().getAttributes();
    	minDragHeight = params.height/5f;
    	
    	mAdapterFullscreen = new GalleryImageAdapter(this, new Gallery.LayoutParams(params.width,params.height), ScaleMode.ScaleSource);
    	mAdapterFullscreen.setTitleConfig(TitleConfig.HideTitle);
    	mAdapterFullscreen.setDisplayTarget(DisplayTarget.FullScreen);
    	mAdapterFullscreen.setCleanupMode(CleanupMode.ForceCleanup);
    	mAdapterFullscreen.setMaxActiveDownloads(3);
    	mAdapterFullscreen.setListener(this);
    	mGalleryFullscreen.setAdapter(mAdapterFullscreen);
    	
    	mAdapterThumbnails = new GalleryImageAdapter(this, new Gallery.LayoutParams(100,100), ScaleMode.DontScale);
    	mAdapterThumbnails.setTitleConfig(TitleConfig.HideTitle);
    	mAdapterThumbnails.setDisplayTarget(DisplayTarget.Thumbnails);
    	mAdapterThumbnails.setMaxActiveDownloads(20);
    	mGalleryThumbnails.setAdapter(mAdapterThumbnails);
    	
    	mGalleryFullscreen.setOnTouchListener(this);
    	mGalleryFullscreen.setOnItemSelectedListener(this);
    	mGalleryThumbnails.setOnItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
    	
    	GalleryImageAdapter adapter = (GalleryImageAdapter) mGalleryFullscreen.getAdapter();
    	adapter.cleanUp();
    	adapter = (GalleryImageAdapter) mGalleryThumbnails.getAdapter();
    	Intent resultIntent = new Intent(this, ImageViewActivity.class);
    	resultIntent.putExtra(GalDroidApp.INTENT_EXTRA_DISPLAY_INDEX, getCurrentIndex());
    	setResult(Activity.RESULT_OK, resultIntent);
    	
    	super.onBackPressed();
    }
    

	public boolean onTouch(View v, MotionEvent event) {
		Log.d("ImageViewActivity", "EventAction: " + event.getAction());
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mTouchStartY = event.getY();
				mTouchStartX = event.getX();
				mTouchMode = TouchMode.Drag;
				break;
			case MotionEvent.ACTION_POINTER_3_DOWN:
			case MotionEvent.ACTION_POINTER_2_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				/*mOldDist = getSpacing(event);
				if(mOldDist > 10f) {
					mTouchMode = TouchMode.Zoom;
					setScalePoint(event);
				}*/
				break;
			case MotionEvent.ACTION_POINTER_3_UP:
			case MotionEvent.ACTION_POINTER_2_UP:
			case MotionEvent.ACTION_POINTER_UP:
				mTouchMode = TouchMode.None;
				break;
			case MotionEvent.ACTION_UP:
				if(mTouchMode == TouchMode.Drag) {
					if(Math.abs(event.getX()-mTouchStartX) < 50) {
						float diffY = event.getY()-mTouchStartY;
						if(Math.abs(diffY)>minDragHeight) {
							if(diffY > 0 && mGalleryThumbnails.getVisibility() == View.VISIBLE) {
								mGalleryThumbnails.setVisibility(View.GONE);
							}
							else if(diffY < 0 && mGalleryThumbnails.getVisibility() == View.GONE) {
								mGalleryThumbnails.setVisibility(View.VISIBLE);
							}
						}
					}
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if(mTouchMode == TouchMode.Zoom)
				{
					float dist = getSpacing(event);
					if(dist > 10f)
					{
						GalleryImageView imageView = (GalleryImageView) mGalleryFullscreen.getSelectedView();
						float scale = dist/mOldDist;
						if(scale >= 1 && scale <= 10)
						{
							Log.d("ImageViewActivity", "ACTION_MOVE Scale:" + scale);
							Matrix matrix = imageView.getImageMatrix();
							
							matrix.postScale(scale, scale, mScalePoint.x-imageView.getLeft(), mScalePoint.y-imageView.getTop());
							imageView.setImageMatrix(matrix);
						}
					}
				}
				break;
		}
		return false;
	}


	private float getSpacing(MotionEvent event)
	{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}
	
	private void  setScalePoint(MotionEvent event)
	{
	   float x = event.getX(0) + event.getX(1);
	   float y = event.getY(0) + event.getY(1);
	   mScalePoint.set(x / 2, y / 2);
	}

	public void onItemSelected(AdapterView<?> gallery, View view, int position,
			long arg3) {
		setCurrentIndex(position);
		if(gallery == mGalleryFullscreen){
			mGalleryThumbnails.setSelection(position);
			
			extractImageInformations();
		}
		else {
			mGalleryFullscreen.setSelection(position);
		}
		
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		// Empty Stub, cause nothing to do
	}
	
	@Override
	public void onGalleryObjectsLoaded(List<GalleryObject> galleryObjects) {
		mAdapterFullscreen.setGalleryObjects(galleryObjects);
		mAdapterThumbnails.setGalleryObjects(galleryObjects);
		
		int currentIndex = getCurrentIndex();
        if(currentIndex == -1){
        	currentIndex = getIntent().getExtras().getInt(GalDroidApp.INTENT_EXTRA_DISPLAY_INDEX);
        }
        
        mGalleryFullscreen.setSelection(currentIndex);
        mGalleryThumbnails.setSelection(currentIndex);
	}

	public void onLoadingStarted(String uniqueId) {
		// Nothing todo		
	}

	public void onLoadingProgress(String uniqueId, int currentValue,
			int maxValue) {
		// Nothing todo
		
	}

	public void onLoadingCompleted(String uniqueId, Bitmap bitmap) {
		// if a Download is completed it could be the current diplayed image.
		// so start decoding of its embeded informations
		extractImageInformations();
	}

	private void extractImageInformations() {
		GalleryImageView imageView = (GalleryImageView) mGalleryFullscreen.getSelectedView();
		if(imageView != null && imageView.isLoaded()) {
			GalleryObject galleryObject = imageView.getGalleryObject();
			extractObjectInformation(galleryObject);
			extractExifInformation(galleryObject);
		} else {
			clearImageInformations();
		}
		
	}
	
	private void clearImageInformations() {
		TextView textView = (TextView) findViewById(R.id.textTitle);
		textView.setText("");
		textView = (TextView) findViewById(R.id.textUploadDate);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifCreateDate);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifAperture);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifExposure);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifFlash);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifISO);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifModel);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifModel);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifMake);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifFocalLength);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifWhiteBalance);
		textView.setText("");
	}

	private void extractObjectInformation(GalleryObject galleryObject) {

		TextView textTitle = (TextView) findViewById(R.id.textTitle);
		TextView textUploadDate = (TextView) findViewById(R.id.textUploadDate);
		
		
		textTitle.setText(galleryObject.getTitle());
		textUploadDate.setText(galleryObject.getDateUploaded().toLocaleString());
	}
	
	private void extractExifInformation(GalleryObject galleryObject) {
		GalDroidApp appContext = (GalDroidApp) this.getApplicationContext();
		GalleryCache cache = appContext.getGalleryCache();
		File cachedFile = cache.getFile(galleryObject.getImage().getUniqueId());
		try {
			ExifInterface exif = new ExifInterface(cachedFile.getAbsolutePath());
			
			
			TextView textField = (TextView) findViewById(R.id.textExifCreateDate);
			textField.setText(exif.getAttribute(ExifInterface.TAG_DATETIME));
			
			textField = (TextView) findViewById(R.id.textExifAperture);
			textField.setText(exif.getAttribute(ExifInterface.TAG_APERTURE));
			
			try {
				textField = (TextView) findViewById(R.id.textExifExposure);
				float exposureTime = 1.0f / Float.parseFloat(exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
				textField.setText(String.format("1/%.0fs", exposureTime));
			} catch (Exception e) {
				
			}
			
			try {
				textField = (TextView) findViewById(R.id.textExifFlash);
				int flash = exif.getAttributeInt(ExifInterface.TAG_FLASH, 0);
				textField.setText(String.format("%d", flash));
			} catch (Exception e) {
				
			}
			
			textField = (TextView) findViewById(R.id.textExifISO);
			textField.setText(exif.getAttribute(ExifInterface.TAG_ISO));
			
			textField = (TextView) findViewById(R.id.textExifModel);
			textField.setText(exif.getAttribute(ExifInterface.TAG_MODEL));
			
			textField = (TextView) findViewById(R.id.textExifModel);
			textField.setText(exif.getAttribute(ExifInterface.TAG_MODEL));
			
			textField = (TextView) findViewById(R.id.textExifMake);
			textField.setText(exif.getAttribute(ExifInterface.TAG_MAKE));
			
			try {
				double focalLength = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0);
				textField = (TextView) findViewById(R.id.textExifFocalLength);
				textField.setText(String.format("%.0fmm", focalLength));
			} catch (Exception e) {

			}
			
			try {
				textField = (TextView) findViewById(R.id.textExifWhiteBalance);
				int whiteBalance = exif.getAttributeInt(ExifInterface.TAG_FLASH, 0);
				if(whiteBalance == ExifInterface.WHITEBALANCE_AUTO) {
					textField.setText(R.string.object_exif_whitebalance_auto);
				}
				else {
					textField.setText(R.string.object_exif_whitebalance_manual);
				}
			} catch (Exception e) {
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void onLoadingCancelled(String uniqueId) {
		// Nothing todo
		
	}
}

