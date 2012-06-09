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
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import de.raptor2101.GalDroid.WebGallery.ImageCache;
import de.raptor2101.GalDroid.WebGallery.Stream;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryDownloadObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;

public class ImageLoaderTask implements TaskInterface {
  protected final static String CLASS_TAG = "ImageLoaderTask";

  public class ImageDownload{
    private final LayoutParams mLayoutParams;
    private WeakReference<ImageLoaderTaskListener> mListener;
    private final GalleryDownloadObject mDownloadObject;

    public ImageDownload(GalleryDownloadObject downloadObject, LayoutParams layoutParams , ImageLoaderTaskListener listener) {
      mListener = new WeakReference<ImageLoaderTaskListener>(listener);
      mDownloadObject = downloadObject;
      mLayoutParams = layoutParams;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getOuterType().hashCode();
      result = prime * result + ((mDownloadObject == null) ? 0 : mDownloadObject.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      ImageDownload other = (ImageDownload) obj;
      if (!getOuterType().equals(other.getOuterType())) {
        return false;
      }
      if (mDownloadObject == null) {
        if (other.mDownloadObject != null) {
          return false;
        }
      } else if (!mDownloadObject.equals(other.mDownloadObject)) {
        return false;
      }
      return true;
    }

    public ImageLoaderTaskListener getListener() {
      return mListener.get();
    }

    public GalleryDownloadObject getDownloadObject() {
      return mDownloadObject;
    }
    
    public LayoutParams getLayoutParams() {
      return mLayoutParams;
    }

    @Override
    public String toString() {
      return mDownloadObject.toString();
    }

    private ImageLoaderTask getOuterType() {
      return ImageLoaderTask.this;
    }

    public void updateListener(ImageLoaderTaskListener listener) {
      mListener = new WeakReference<ImageLoaderTaskListener>(listener);
    }
  }
  
  private class DownloadTask extends RepeatingTask<ImageDownload, Progress, Bitmap> {
    protected final static String CLASS_TAG = "ImageLoaderTask";
    
    public DownloadTask() {
      mThreadName = "ImageLoaderTask";
    }
    @Override
    protected void onPreExecute(ImageDownload imageDownload) {
      ImageLoaderTaskListener listener = imageDownload.getListener();
      GalleryDownloadObject galleryDownloadObject = imageDownload.getDownloadObject();
      Log.d(CLASS_TAG, String.format("%s - Task started - Listener %s", galleryDownloadObject, listener != null));
      
      if (listener != null) {
        listener.onLoadingStarted(galleryDownloadObject.getUniqueId());
      }
    };

    @Override
    protected void onCancelled(ImageDownload imageDownload, Bitmap bitmap) {
      GalleryDownloadObject galleryDownloadObject = imageDownload.getDownloadObject();
      Log.d(CLASS_TAG, String.format("%s - Task canceled", galleryDownloadObject));

      synchronized (mCache) {
        mCache.removeCacheFile(galleryDownloadObject.getUniqueId());
      }

      ImageLoaderTaskListener listener = imageDownload.getListener();
      if (listener != null) {
        listener.onLoadingCancelled(galleryDownloadObject.getUniqueId());
      }
    }

    @Override
    protected Bitmap doInBackground(ImageDownload imageDownload) {
      GalleryDownloadObject galleryDownloadObject = imageDownload.getDownloadObject();
      try {
        Log.d(CLASS_TAG, String.format("%s - Task running", galleryDownloadObject));
        String uniqueId = galleryDownloadObject.getUniqueId();
        InputStream inputStream = mCache.getFileStream(uniqueId);

        if (inputStream == null) {
          DownloadImage(galleryDownloadObject);
          
          LayoutParams layoutParams = imageDownload.getLayoutParams();
          if(layoutParams != null) {
            ScaleImage(galleryDownloadObject, layoutParams);
          }
          
          inputStream = mCache.getFileStream(uniqueId);
        }

        Options options = new Options();
        options.inPreferQualityOverSpeed = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inDither = true;
        options.inScaled = false;
        options.inPurgeable = true;
        options.inInputShareable = true;

        
        if (isCancelled()) {
          return null;
        }

        Log.d(CLASS_TAG, String.format("%s - Decoding local image", galleryDownloadObject));
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        
        if (bitmap != null) {
          mCache.cacheBitmap(uniqueId, bitmap);
          Log.i(CLASS_TAG, String.format("%s - Decoding local image - complete", galleryDownloadObject));
        } else {
          Log.w(CLASS_TAG, String.format("Something goes wrong while Decoding %s, removing CachedFile", galleryDownloadObject));
          mCache.removeCacheFile(uniqueId);
        }
        return bitmap;
      } catch (Exception e) {
        Log.w(CLASS_TAG, String.format("Something goes wrong while Downloading %s. ExceptionMessage: %s", galleryDownloadObject, e.getMessage()));
        return null;
      }
    }

    @Override
    protected void onProgressUpdate(ImageDownload imageDownload, Progress progress) {
      GalleryDownloadObject galleryDownloadObject = imageDownload.getDownloadObject();
      ImageLoaderTaskListener listener = imageDownload.getListener();
      if (!isCancelled() && listener != null) {
        listener.onLoadingProgress(galleryDownloadObject.getUniqueId(), progress.curValue, progress.maxValue);
      }
    }
    
    @Override
    protected void onPostExecute(ImageDownload imageDownload, Bitmap bitmap) {
      ImageLoaderTaskListener listener = imageDownload.getListener();
      GalleryDownloadObject galleryDownloadObject = imageDownload.getDownloadObject();
      Log.d(CLASS_TAG, String.format("%s - Task done - Listener %s", galleryDownloadObject, listener != null));
      
      if (!isCancelled() && listener != null) {
        listener.onLoadingCompleted(galleryDownloadObject.getUniqueId(), bitmap);
      }
    }
    
    private void DownloadImage(GalleryDownloadObject galleryDownloadObject) throws IOException {
      Log.d(CLASS_TAG, String.format("%s - Downloading to local cache file", galleryDownloadObject));

      Stream networkStream = mWebGallery.getFileStream(galleryDownloadObject);
      String uniqueId = galleryDownloadObject.getUniqueId();
      OutputStream fileStream = mCache.createCacheFile(uniqueId);

      byte[] writeCache = new byte[10 * 1024];
      int readCounter;
      int currentPos = 0;
      int length = (int) networkStream.getContentLength();
      
      while ((readCounter = networkStream.read(writeCache)) > 0 && !isCancelled()) {
        fileStream.write(writeCache, 0, readCounter);
        currentPos += readCounter;
        
        publishProgress(new Progress(currentPos, length));
      }
      
      publishProgress(new Progress(length, length));
      
      fileStream.close();
      networkStream.close();

      if (isCancelled()) {
        // if the download is aborted, the file is waste of bytes...
        Log.i(CLASS_TAG, String.format("%s - Download canceled", galleryDownloadObject));
        mCache.removeCacheFile(uniqueId);
      } else {
        mCache.refreshCacheFile(uniqueId);
      }
      Log.d(CLASS_TAG, String.format("%s - Downloading to local cache file - complete", galleryDownloadObject));
    }

    private void ScaleImage(GalleryDownloadObject galleryDownloadObject, LayoutParams layoutParams) throws IOException {
      
      Log.d(CLASS_TAG, String.format("%s - Decoding Bounds", galleryDownloadObject));

      String uniqueId = galleryDownloadObject.getUniqueId();
      FileInputStream bitmapStream = mCache.getFileStream(uniqueId);
      Options options = new Options();
      options.inJustDecodeBounds = true;

      synchronized (mCache) {
        if (isCancelled()) {
          return;
        }

        BitmapFactory.decodeStream(bitmapStream, null, options);
      }

      bitmapStream.close();
      Log.d(CLASS_TAG, String.format("%s - Decoding Bounds - done", galleryDownloadObject));

      int imgHeight = options.outHeight;
      int imgWidth = options.outWidth;

      int highestLayoutDimension = layoutParams.height > layoutParams.width ? layoutParams.height : layoutParams.width;
      int highestImageDimension = imgHeight > imgWidth ? imgHeight : imgWidth;

      int sampleSize = highestImageDimension / highestLayoutDimension;

      options = new Options();
      options.inInputShareable = true;
      options.inPreferredConfig = Bitmap.Config.ARGB_8888;
      options.inDither = true;
      options.inPurgeable = true;
      options.inPreferQualityOverSpeed = true;

      if (sampleSize > 1) {
        options.inSampleSize = sampleSize;
      }

      synchronized (mCache) {
        if (isCancelled()) {
          return;
        }

        bitmapStream = mCache.getFileStream(uniqueId);
        Log.d(CLASS_TAG, String.format("%s - Resize Image", galleryDownloadObject));
        Bitmap bitmap = BitmapFactory.decodeStream(bitmapStream, null, options);
        bitmapStream.close();
        mCache.storeBitmap(uniqueId, bitmap);
        bitmap.recycle();
        Log.d(CLASS_TAG, String.format("%s - Resize Image - done", galleryDownloadObject));
      }
    }
  }
  
  private DownloadTask mDownloadTask;
  private ImageCache mCache;
  private WebGallery mWebGallery;

  public ImageLoaderTask(WebGallery webGallery, ImageCache cache) {
    mWebGallery = webGallery;
    mCache = cache;
    mDownloadTask = new DownloadTask();
  }

  public ImageDownload download(GalleryDownloadObject galleryDownloadObject, LayoutParams layoutParams, ImageLoaderTaskListener listener) {
    Log.d(CLASS_TAG, String.format("Enqueue download for %s - Listener: %s", galleryDownloadObject, listener != null));
    ImageDownload imageDownload = new ImageDownload(galleryDownloadObject, layoutParams, listener);
    mDownloadTask.enqueueTask(imageDownload);
    return imageDownload;
  }

  public ImageDownload getActiveDownload(){
    return mDownloadTask.getActiveTask();
  }
  
  public boolean isDownloading(GalleryDownloadObject galleryDownloadObject) {
    if (galleryDownloadObject == null) {
      return false;
    }
    ImageDownload imageDownload = new ImageDownload(galleryDownloadObject, null, null);
    boolean isActive = mDownloadTask.isActive(imageDownload);
    boolean isEnqueued = mDownloadTask.isEnqueued(imageDownload);
    return isActive || isEnqueued;
  }

  public void cancel(GalleryDownloadObject downloadObject, boolean waitForCancel) throws InterruptedException {
    if (downloadObject == null) {
      return;
    }

    ImageDownload imageDownload = new ImageDownload(downloadObject, null, null);
    if (mDownloadTask.isEnqueued(imageDownload)) {
      mDownloadTask.removeEnqueuedTask(imageDownload);
    } else if (mDownloadTask.isActive(imageDownload)) {
      mDownloadTask.cancelCurrentTask(waitForCancel);
    }
  }

  public void cancelActiveDownload(boolean waitForCancel) throws InterruptedException {
    mDownloadTask.cancelCurrentTask(waitForCancel);
  }
  
  public Status getStatus() {
    return mDownloadTask.getStatus();
  }

  public void start() {
    mDownloadTask.start();
    
  }

  public void stop(boolean waitForStopped) throws InterruptedException {
    mDownloadTask.stop(waitForStopped);
    
  }

  public void cancel(boolean waitForCancel) throws InterruptedException {
    mDownloadTask.cancel(waitForCancel);
    
  }
}
