package de.raptor2101.GalDroid.WebGallery;

import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;

public interface GalleryImageViewListener {
    public void onLoadingStarted(GalleryObject galleryObject);
    public void onLoadingProgress(GalleryObject galleryObject, int currentValue, int maxValue);
    public void onLoadingCompleted(GalleryObject galleryObject);
    public void onLoadingCancelled(GalleryObject galleryObject);
}
