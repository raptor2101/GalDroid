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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryProgressListener;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;

public class GalleryLoaderTask extends AsyncTask<GalleryObject, Progress, List<GalleryObject>> implements GalleryProgressListener {

  private WeakReference<GalleryLoaderTaskListener> mListener;
  private WebGallery mWebGallery;

  public GalleryLoaderTask(WebGallery webGallery, GalleryLoaderTaskListener listener) {
    mListener = new WeakReference<GalleryLoaderTaskListener>(listener);
    mWebGallery = webGallery;
  }

  @Override
  protected List<GalleryObject> doInBackground(GalleryObject... params) {
    List<GalleryObject> objects;

    if (mWebGallery != null) {
      if (params.length == 0 || params[0] == null) {
        Thread.currentThread().setName(String.format("GalleryLoaderTask"));
        objects = mWebGallery.getDisplayObjects(this);
      } else {
        Thread.currentThread().setName(String.format("GalleryLoaderTask for %s", params[0]));
        objects = mWebGallery.getDisplayObjects(params[0], this);
      }
    } else {
      objects = new ArrayList<GalleryObject>(0);
    }
    return objects;
  }

  @Override
  protected void onPreExecute() {
    GalleryLoaderTaskListener listener = mListener.get();
    if (listener != null) {
      listener.onDownloadStarted();
    }
  };

  @Override
  protected void onProgressUpdate(Progress... values) {
    GalleryLoaderTaskListener listener = mListener.get();
    if (values.length > 0 && listener != null) {
      listener.onDownloadProgress(values[0].curValue, values[0].maxValue);
    }
  }

  @Override
  protected void onPostExecute(List<GalleryObject> galleryObjects) {
    GalleryLoaderTaskListener listener = mListener.get();
    Log.d("GalleryLoaderTask", String.format("onPostExecute isCanceled: %s isListenerAvaible: %s", isCancelled(), listener != null));
    if (!isCancelled() && listener != null) {
      listener.onDownloadCompleted(galleryObjects);
    }
  }

  public void handleProgress(int curValue, int maxValue) {
    publishProgress(new Progress(curValue, maxValue));
  }

}
