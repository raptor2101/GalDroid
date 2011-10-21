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

import android.graphics.Bitmap;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery.ImageSize;

public class GalleryDownloadObject {
	private GalleryObject mGalleryObject;
	private Bitmap mBitmap;
	private ImageSize mImageSize;
	
	public GalleryDownloadObject(GalleryObject galleryObject, ImageSize imageSize){				
		mGalleryObject = galleryObject;
		mImageSize = imageSize;
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
	
	public ImageSize getImageSize(){
		return mImageSize;
	}
	
	public String getUniqueId(){
		return mGalleryObject.getUniqueId(mImageSize);
	}

	public boolean isValid() {
		return mBitmap != null;
	}
	
	@Override
	public String toString() {
		return mGalleryObject.getUniqueId(mImageSize);
	}
}
