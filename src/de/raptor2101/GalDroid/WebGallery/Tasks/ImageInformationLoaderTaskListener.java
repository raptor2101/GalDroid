package de.raptor2101.GalDroid.WebGallery.Tasks;

import java.util.List;

import de.raptor2101.GalDroid.WebGallery.ImageInformation;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObjectComment;

public interface ImageInformationLoaderTaskListener {
    public void onImageInformationLoaded(GalleryObject galleryObject, ImageInformation imageInformation);

    public void onImageTagsLoaded(GalleryObject galleryObject, List<String> tags);

    public void onImageCommetsLoaded(GalleryObject galleryObject, List<GalleryObjectComment> comments);
}
