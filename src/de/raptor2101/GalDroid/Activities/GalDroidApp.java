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

package de.raptor2101.GalDroid.Activities;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import de.raptor2101.GalDroid.Config.GalDroidPreference;
import de.raptor2101.GalDroid.Config.GalleryConfig;
import de.raptor2101.GalDroid.WebGallery.GalleryCache;
import de.raptor2101.GalDroid.WebGallery.GalleryFactory;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
import android.app.Activity;
import android.app.Application;
import android.net.http.AndroidHttpClient;

public class GalDroidApp extends Application{

	private WebGallery mWebGallery = null;
	private GalleryCache mGalleryCache = null;
	private List<GalleryObject> mGalleryObjects = null;
	private String mStoredUniqueId = null;
	public WebGallery getWebGallery() {
		return mWebGallery;
	}

	public GalleryCache getGalleryCache() {
		return mGalleryCache;
	}

	public void Initialize(Activity activity) throws NoSuchAlgorithmException {
		GalDroidPreference.Initialize(this);
		
		if(mGalleryCache == null){
			mGalleryCache = new GalleryCache(activity);
		}
		
		if(mWebGallery == null){
			try {
				String galleryName = activity.getIntent().getExtras().getString("GalleryName");
				if(galleryName != null){
					GalleryConfig galleryConfig = GalDroidPreference.getSetupByName(galleryName);
					mWebGallery = GalleryFactory.createFromName(galleryConfig.TypeName, galleryConfig.RootLink, AndroidHttpClient.newInstance("GalDroid"));
					mWebGallery.setSecurityToken(galleryConfig.SecurityToken);
				}
			} catch (NullPointerException e) {
				mWebGallery = null;
			}
		}
	}

	public void storeGalleryObjects(String uniqueId, List<GalleryObject> galleryObjects) {
		mStoredUniqueId = uniqueId;
		mGalleryObjects = galleryObjects;
	}
	
	public List<GalleryObject> loadStoredGalleryObjects(String uniqueId) {
		if(uniqueId == null) {
			return null;
		}
		
		if(!uniqueId.equals(mStoredUniqueId)) {
			return null;
		}
		
		return mGalleryObjects;
	}
}
