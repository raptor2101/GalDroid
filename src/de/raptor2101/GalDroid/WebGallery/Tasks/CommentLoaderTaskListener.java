package de.raptor2101.GalDroid.WebGallery.Tasks;

import java.util.List;

import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObjectComment;

public interface CommentLoaderTaskListener {
	void onLoadingStarted();
	void onLoadingProgress(int elementCount, int maxCount);
	void onLoadingCompleted(List<GalleryObjectComment> comments);
	void onLoadingCanceled();
}
