package de.raptor2101.GalDroid.Activities.Listeners;

import android.graphics.Bitmap;
import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTaskListener;

public class ImageViewImageLoaderTaskListener implements ImageLoaderTaskListener {

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
		
		//extractImageInformations();
	}

	public void onLoadingCancelled(String uniqueId) {
		// TODO Auto-generated method stub
		
	}

}
