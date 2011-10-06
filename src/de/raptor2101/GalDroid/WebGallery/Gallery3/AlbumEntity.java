package de.raptor2101.GalDroid.WebGallery.Gallery3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AlbumEntity extends Entity {

	private final List<String> mMembers;
	
	public AlbumEntity(JSONObject jsonObject, Gallery3Imp gallery3)
			throws JSONException, ClientProtocolException, IOException {
		super(jsonObject, gallery3);
		JSONArray memberArray = jsonObject.getJSONArray("members");
		
		String albumCover_RestLink = jsonObject.getJSONObject("entity").getString("album_cover");
		albumCover_RestLink = albumCover_RestLink.substring(gallery3.LinkRest_LoadItem.length()-2);
		
		int coverId = Integer.parseInt(albumCover_RestLink);
		
		mLink_Full = String.format(gallery3.LinkRest_LoadPicture, coverId, "full");
		mLink_Thumb = String.format(gallery3.LinkRest_LoadPicture, getId(), "thumb");
		
		mMembers = new ArrayList<String>(memberArray.length());
		
		for(int i=0;i<memberArray.length();i++)
		{
			mMembers.add(memberArray.getString(i)); 
		}
	}

	
	public List<String> getMembers()
	{
		return mMembers;
	}
	
	public boolean hasChildren() {
		return mMembers.size() > 0;
	}
	
	
}
