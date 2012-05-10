package de.raptor2101.GalDroid.WebGallery.Gallery3;

import java.lang.ref.WeakReference;

import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryProgressListener;

public class ProgressListener {
	private final int mMaxCount;
	private int mObjectCount;
	private WeakReference<GalleryProgressListener> mListener;
	
	public ProgressListener(GalleryProgressListener listener, int maxCount){
		mMaxCount = maxCount;
		mObjectCount = 0;
		mListener = new WeakReference<GalleryProgressListener>(listener);			
	}
	
	public void progress() {
		mObjectCount++;
		GalleryProgressListener listener = mListener.get();
		if(listener != null) {
			listener.handleProgress(mObjectCount, mMaxCount);
		}
	}
}
