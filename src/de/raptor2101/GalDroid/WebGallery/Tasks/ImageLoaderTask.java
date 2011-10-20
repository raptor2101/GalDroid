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

package de.raptor2101.GalDroid.WebGallery.Tasks;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import de.raptor2101.GalDroid.WebGallery.GalleryCache;
import de.raptor2101.GalDroid.WebGallery.GalleryDownloadObject;
import de.raptor2101.GalDroid.WebGallery.GalleryImage;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;

public class ImageLoaderTask extends AsyncTask<Void, Progress, GalleryDownloadObject> {

	private WebGallery mWebGallery;
	private GalleryCache mCache;
	private GalleryDownloadObject mDownloadObject;
	private WeakReference<ImageLoaderTaskListener> mListener;
	private LayoutParams mLayoutParams;
	
	public ImageLoaderTask(WebGallery webGallery, GalleryCache cache, GalleryDownloadObject downloadObject){
		mWebGallery = webGallery;
		mCache = cache;
		mDownloadObject = downloadObject;
		mListener = new WeakReference<ImageLoaderTaskListener>(null); 
	}
	
	public void setListener(ImageLoaderTaskListener listener){
		mListener = new WeakReference<ImageLoaderTaskListener>(listener); 
	}
	
	public void setLayoutParams(LayoutParams layoutParams) {
		mLayoutParams = layoutParams;
	}
	
	@Override
	protected void onPreExecute() {
		ImageLoaderTaskListener listener = mListener.get();
		if(listener != null){
			listener.onLoadingStarted(mDownloadObject.getUniqueId());
		}
	};
	
	@Override
	protected GalleryDownloadObject doInBackground(Void... params) {
		try {
			synchronized (mCache) {
				String uniqueId = mDownloadObject.getUniqueId();
				InputStream inputStream = mCache.getFileStream(uniqueId);
				
				if(inputStream == null) {
					DownloadImage(uniqueId);
					ScaleImage(uniqueId);
					inputStream = mCache.getFileStream(uniqueId);
				}
				
				Options options = new Options();
				options.inPreferQualityOverSpeed = true;
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				options.inDither = true;
				options.inScaled = false;
				options.inPurgeable = true;
				options.inInputShareable = true;
				
				
				System.gc();
				Bitmap bitmap = BitmapFactory.decodeStream(inputStream,null,options );
				
				mDownloadObject.setBitmap(bitmap);
			}
			return mDownloadObject;
		} catch (Exception e) {
			Log.w("ImageLoaderTask", String.format("Something goes wrong while Downloading %s. ExceptionMessage: %s",mDownloadObject,e.getMessage()));
			return mDownloadObject;
		}
	}

	private void DownloadImage(String uniqueId) throws IOException {
		
		InputStream networkStream = mWebGallery.getImageRawData(mDownloadObject.getGalleryObject(),mDownloadObject.getImageSize());
		OutputStream fileStream = mCache.createFileStream(uniqueId);
		byte[] writeCache = new byte[1024];
		int readCounter;
		while((readCounter = networkStream.read(writeCache)) > 0){
			fileStream.write(writeCache, 0, readCounter);
		}
		fileStream.close();
		networkStream.close();
		
		mCache.refreshCacheFile(uniqueId);
	}
	
	private void ScaleImage(String uniqueId) throws IOException {
		if(mLayoutParams != null) {
			
			FileInputStream bitmapStream = mCache.getFileStream(uniqueId);
			Options options = new Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream( bitmapStream, null, options);
			bitmapStream.close();
			
			int imgHeight = options.outHeight;
			int imgWidth = options.outWidth;
			
			int highestLayoutDimension =  mLayoutParams.height > mLayoutParams.width? mLayoutParams.height : mLayoutParams.width;
			int highestImageDimension = imgHeight > imgWidth ? imgHeight : imgWidth;
			
			int sampleSize = highestImageDimension / highestLayoutDimension;
			
			options = new Options();
			options.inInputShareable = true;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			options.inDither = true;
			options.inPurgeable = true;
			options.inPreferQualityOverSpeed = true;
			
			if(sampleSize > 1) {
				options.inSampleSize = sampleSize;
			}
			bitmapStream = mCache.getFileStream(uniqueId);
			Bitmap bitmap = BitmapFactory.decodeStream( bitmapStream, null, options);
			bitmapStream.close();
			
			mCache.storeBitmap(uniqueId, bitmap);
			bitmap.recycle();
			System.gc();
		}
	}
	
	@Override
	protected void onPostExecute(GalleryDownloadObject downloadObject) {
		if(!isCancelled()&&downloadObject.isValid())
		{
			Bitmap bitmap = downloadObject.getBitmap();
				
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
