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

import org.json.JSONException;
import org.json.JSONObject;

import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery.ImageSize;

abstract class Entity implements GalleryObject {
	private final String mTitle;
	private final String mLink;
	private final int mId;
	protected String mLink_Full;
	protected String mLink_Thumb;
	
	public Entity(JSONObject jsonObject, Gallery3Imp gallery3) throws JSONException
	{
		jsonObject = jsonObject.getJSONObject("entity");
		
		mId = jsonObject.getInt("id");
		
		mTitle = jsonObject.getString("title");
		mLink = gallery3.getItemLink(mId);
	}
	
	
	public String getTitle()
	{
		return mTitle;
	}
	
	public int getId(){
		return mId;
	}
	
	public String getObjectId(){
		return mLink;
	}
	
	public String getUniqueId(ImageSize imageSize) {
		return imageSize == ImageSize.Full? mLink_Full : mLink_Thumb;
	}
}
