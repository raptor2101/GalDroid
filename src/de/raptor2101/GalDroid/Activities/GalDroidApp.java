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
