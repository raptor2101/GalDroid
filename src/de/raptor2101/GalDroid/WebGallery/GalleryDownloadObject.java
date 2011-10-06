/*
 * GalDroid - a webgallery frontend for android
 * Copyright (C) 2011  Raptor 2101 [raptor2101@gmx.de]
 *		
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
 */

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
