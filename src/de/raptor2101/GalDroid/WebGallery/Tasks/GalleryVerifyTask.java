package de.raptor2101.GalDroid.WebGallery.Tasks;

import de.raptor2101.GalDroid.Activities.EditGalleryActivity;
import de.raptor2101.GalDroid.Config.GalDroidPreference;
import de.raptor2101.GalDroid.Config.GalleryConfig;
import de.raptor2101.GalDroid.WebGallery.GalleryFactory;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

public class GalleryVerifyTask extends AsyncTask<Void, Void, String> {

	private WebGallery mWebGallery;
	private GalleryConfig mGalleryConfig;
	private String mUsername;
	private String mPassword;
	private EditGalleryActivity mOwner;
	
	public GalleryVerifyTask (GalleryConfig galleryConfig, String username, String password, EditGalleryActivity owner){
		mGalleryConfig = galleryConfig;
		mUsername = username;
		mPassword = password;
		mOwner = owner;
	}
	
	@Override
	protected String doInBackground(Void... params) {
		AndroidHttpClient client = AndroidHttpClient.newInstance("GalDroid");
		try {
			mWebGallery = GalleryFactory.createFromName(mGalleryConfig.TypeName, mGalleryConfig.RootLink, client );
			
			return mWebGallery.getSecurityToken(mUsername, mPassword);
		} catch (SecurityException e) {
			return null;
		}
		finally{
			client.close();
		}
	}
	
	@Override
	protected void onPostExecute(String result) {
		if (result != null) {
			GalDroidPreference.StoreGallery(mGalleryConfig.Id,mGalleryConfig.Name,
					mGalleryConfig.TypeName, mGalleryConfig.RootLink, result);
			mOwner.onGalleryVerified(true);
		}
		else {
			mOwner.onGalleryVerified(false);
		}
	}
}
