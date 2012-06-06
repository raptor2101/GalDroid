package de.raptor2101.GalDroid.WebGallery.Tasks;

import java.lang.ref.WeakReference;

import android.view.ViewGroup.LayoutParams;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryDownloadObject;

class ImageDownload{
  private final LayoutParams mLayoutParams;
  private final WeakReference<ImageLoaderTaskListener> mListener;
  private final GalleryDownloadObject mDownloadObject;

  public ImageDownload(GalleryDownloadObject downloadObject, LayoutParams layoutParams , ImageLoaderTaskListener listener) {
    mListener = new WeakReference<ImageLoaderTaskListener>(listener);
    mDownloadObject = downloadObject;
    mLayoutParams = layoutParams;
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
}
