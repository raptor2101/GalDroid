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

package de.raptor2101.GalDroid.Activities.Views;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.raptor2101.GalDroid.R;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTask;
import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTaskListener;

public class GalleryImageView extends LinearLayout implements ImageLoaderTaskListener {
  private static final String CLASS_TAG = "GalleryImageView";
  private final ProgressBar mProgressBar_determinate;
  private final ProgressBar mProgressBar_indeterminate;
  private final ImageView mImageView;
  private final TextView mTitleTextView;
  private GalleryObject mGalleryObject;
  private WeakReference<GalleryImageViewListener> mListener;
  private boolean mLoaded;

public GalleryImageView(Context context, boolean showTitle) {
    super(context);
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.gallery_image_view, this);

    mProgressBar_determinate = (ProgressBar) findViewById(R.id.progressBar_Determinate);
    mProgressBar_indeterminate = (ProgressBar) findViewById(R.id.progressBar_Indeterminate);
    
    mTitleTextView = (TextView) findViewById(R.id.text_Title);
    mImageView = (ImageView) findViewById(R.id.imageView_Image);
    
    if (showTitle) {
      mTitleTextView.setVisibility(VISIBLE);
    } else {
      mTitleTextView.setVisibility(GONE);
    }
    
    mListener = new WeakReference<GalleryImageViewListener>(null);
  }

  public void setGalleryObject(GalleryObject galleryObject) {
    cleanup();
    mGalleryObject = galleryObject;
    mProgressBar_determinate.setVisibility(GONE);
    mProgressBar_indeterminate.setVisibility(VISIBLE);
    this.setTitle(galleryObject.getTitle());
  }

  public GalleryObject getGalleryObject() {
    return mGalleryObject;
  }

  public void setTitle(String title) {
    if (mTitleTextView != null) {
      mTitleTextView.setText(title);
    }
  }
  
  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    cleanup();
  }

  public void cleanup() {
    if(mLoaded){
      Log.d(CLASS_TAG, String.format("Recycle %s", mGalleryObject.getObjectId()));
      mImageView.setImageBitmap(null);
      mImageView.destroyDrawingCache();
      mImageView.setVisibility(GONE);
      mLoaded=false;
    }
  }

  public Matrix getImageMatrix() {
    return mImageView.getMatrix();
  }

  public void setImageMatrix(Matrix matrix) {
    mImageView.setScaleType(ImageView.ScaleType.MATRIX);
    mImageView.setImageMatrix(matrix);
  }

  public boolean isLoaded() {
    return mLoaded;
  }

  public String getObjectId() {
    return mGalleryObject.getObjectId();
  }

  public void onLoadingStarted(String uniqueId) {
    Log.d(CLASS_TAG, String.format("Loading started %s", uniqueId));

    GalleryImageViewListener listener = mListener.get();
    if (listener != null) {
      listener.onLoadingStarted(mGalleryObject);
    }
  }

  public void onLoadingProgress(String uniqueId, int currentValue, int maxValue) {
    mProgressBar_determinate.setMax(maxValue);
    mProgressBar_determinate.setProgress(currentValue);
    
    if(maxValue != currentValue) {
      mProgressBar_indeterminate.setVisibility(GONE);
      mProgressBar_determinate.setVisibility(VISIBLE);
    } else {
      mProgressBar_indeterminate.setVisibility(VISIBLE);
      mProgressBar_determinate.setVisibility(GONE);
    }
    
    
    GalleryImageViewListener listener = mListener.get();
    if (listener != null) {
      listener.onLoadingProgress(mGalleryObject, currentValue, maxValue);
    }
  }

  public void onLoadingCompleted(String uniqueId, Bitmap bitmap) {
    mProgressBar_indeterminate.setVisibility(GONE);
    mProgressBar_determinate.setVisibility(GONE);
    mLoaded=true;
    mImageView.setImageBitmap(bitmap);
    mImageView.setVisibility(VISIBLE);
    Log.d(CLASS_TAG, String.format("Loading done %s", uniqueId));

    GalleryImageViewListener listener = mListener.get();
    if (listener != null) {
      listener.onLoadingCompleted(mGalleryObject);
    }
  }

  public void onLoadingCancelled(String uniqueId) {
    Log.d(CLASS_TAG, String.format("DownloadTask was cancalled %s", uniqueId));
    mProgressBar_indeterminate.setVisibility(GONE);
    mProgressBar_determinate.setVisibility(GONE);

    GalleryImageViewListener listener = mListener.get();
    if (listener != null) {
      listener.onLoadingCancelled(mGalleryObject);
    }
  }

  public void setListener(GalleryImageViewListener listener) {
    mListener = new WeakReference<GalleryImageViewListener>(listener);
  }

}
