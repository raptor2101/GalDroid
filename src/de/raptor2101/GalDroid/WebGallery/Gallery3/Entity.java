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
