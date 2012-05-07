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

import java.lang.ref.WeakReference;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
abstract class Entity implements GalleryObject {
	private final String mRootLink;
	private final String mTitle;
	private final String mLink;
	private final int mId;
	
	private final Date mUploadDate;
	
	protected String mLink_Full;
	protected String mLink_Thumb;
	
	protected int mFileSize_Full;
	protected int mFileSize_Thumb;
	
	public Entity(JSONObject jsonObject, Gallery3Imp gallery3) throws JSONException
	{
		jsonObject = jsonObject.getJSONObject("entity");
		
		mId = jsonObject.getInt("id");
		
		long msElapsed = jsonObject.getLong("created")*1000;
		mUploadDate = new Date(msElapsed);
		
		mTitle = jsonObject.getString("title");
		mLink = gallery3.getItemLink(mId);
		mRootLink = gallery3.getRootLink();
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public int getId() {
		return mId;
	}
	
	public String getObjectId() {
		return mLink;
	}
	
	public Date getUpadloadDate() {
		return mUploadDate;
	}
	
	public DownloadObject getImage() {
		return createDownloadObject(mLink_Full, mFileSize_Full);
	}
	
	public DownloadObject getThumbnail() {
		return createDownloadObject(mLink_Thumb, mFileSize_Thumb);
	}
	
	private DownloadObject createDownloadObject(String link, int fileSize) {
		return !link.equals("")? new DownloadObject(mRootLink, link, fileSize) : null;
	}
}
