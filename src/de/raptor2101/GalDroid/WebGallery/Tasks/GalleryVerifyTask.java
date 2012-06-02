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

    public GalleryVerifyTask(GalleryConfig galleryConfig, String username, String password, EditGalleryActivity owner) {
	mGalleryConfig = galleryConfig;
	mUsername = username;
	mPassword = password;
	mOwner = owner;
    }

    @Override
    protected String doInBackground(Void... params) {
	AndroidHttpClient client = AndroidHttpClient.newInstance("GalDroid");
	try {
	    mWebGallery = GalleryFactory.createFromName(mGalleryConfig.TypeName, mGalleryConfig.RootLink, client);

	    return mWebGallery.getSecurityToken(mUsername, mPassword);
	} catch (SecurityException e) {
	    return null;
	} finally {
	    client.close();
	}
    }

    @Override
    protected void onPostExecute(String result) {
	if (result != null) {
	    GalDroidPreference.StoreGallery(mGalleryConfig.Id, mGalleryConfig.Name, mGalleryConfig.TypeName, mGalleryConfig.RootLink, result);
	    mOwner.onGalleryVerified(true);
	} else {
	    mOwner.onGalleryVerified(false);
	}
    }
}
