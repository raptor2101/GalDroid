package de.raptor2101.GalDroid.WebGallery.Tasks;

import java.util.List;

public interface TagLoaderTaskListener {
	void onLoadingStarted();
	void onLoadingProgress(int elementCount, int maxCount);
	void onLoadingCompleted(List<String> tags);
	void onLoadingCanceled();
}
