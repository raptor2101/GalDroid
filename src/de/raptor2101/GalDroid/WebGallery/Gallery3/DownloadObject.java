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

package de.raptor2101.GalDroid.WebGallery.Gallery3;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryDownloadObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryProgressListener;

public class DownloadObject implements GalleryDownloadObject {
	
	private WeakReference<Gallery3Imp> mWebGallery;
	private String mSourceLink;
	
	public DownloadObject(Gallery3Imp webGallery, String sourceLink){				
		mSourceLink = sourceLink;
		mWebGallery = new WeakReference<Gallery3Imp>(webGallery);
	}
	
	public InputStream getFileStream() throws IOException {
		Gallery3Imp webGallery = mWebGallery.get();
		if(webGallery == null) {
			throw new IOException("GalleryContext disposed");
		}
		return webGallery.getFileStream(mSourceLink);
	}
	
	public String getUniqueId() {
		return mSourceLink;
	}
	
	@Override
	public String toString() {
		return mSourceLink;
	}
}
