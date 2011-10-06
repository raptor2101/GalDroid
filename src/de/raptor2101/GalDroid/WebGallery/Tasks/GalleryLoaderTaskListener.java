package de.raptor2101.GalDroid.WebGallery.Tasks;

import java.util.List;

import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;

public interface GalleryLoaderTaskListener {
	void onDownloadStarted();
	void onDownloadProgress(int elementCount, int maxCount);
	void onDownloadCompleted(List<GalleryObject> galleryObjects);
}
