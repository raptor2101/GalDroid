package de.raptor2101.GalDroid.WebGallery;

import java.lang.ref.WeakReference;

import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery.ImageSize;

import android.graphics.Bitmap;

public class GalleryDownloadObject {
	private GalleryObject mGalleryObject;
	private Bitmap mBitmap;
	private WeakReference<GalleryImage> mGalleryImage;
	private ImageSize mImageSize;
	
	public GalleryDownloadObject(GalleryObject galleryObject, ImageSize imageSize, GalleryImage galleryImage){				
		mGalleryObject = galleryObject;
		mImageSize = imageSize;
		mGalleryImage = new WeakReference<GalleryImage>(galleryImage);
	}
	
	public GalleryObject getGalleryObject(){
		return mGalleryObject;
	}
	
	public void setBitmap(Bitmap bitmap){
		mBitmap = bitmap;
	}
	
	public Bitmap getBitmap(){
		return mBitmap;
	}

	public GalleryImage getGalleryImage() {
		return mGalleryImage.get();
	}
	
	public ImageSize getImageSize(){
		return mImageSize;
	}
	
	public String getUniqueId(){
		return mGalleryObject.getUniqueId(mImageSize);
	}

	public boolean isValid() {
		return mBitmap != null;
	}
}
