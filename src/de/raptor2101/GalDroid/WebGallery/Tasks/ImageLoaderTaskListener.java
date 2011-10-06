package de.raptor2101.GalDroid.WebGallery.Tasks;

public interface ImageLoaderTaskListener {
	void onLoadingStarted(String uniqueId);
	void onLoadingCompleted(String uniqueId);
}
