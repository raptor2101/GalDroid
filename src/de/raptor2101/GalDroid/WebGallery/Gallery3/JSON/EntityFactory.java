package de.raptor2101.GalDroid.WebGallery.Gallery3.JSON;

import org.json.JSONException;
import org.json.JSONObject;

import de.raptor2101.GalDroid.WebGallery.Gallery3.Gallery3Imp;

public class EntityFactory {
	public static Entity parseJSON(JSONObject jsonObject, Gallery3Imp gallery3)  throws JSONException {
		String type = jsonObject.getJSONObject("entity").getString("type");
		if(type.equals("album")){
			return new AlbumEntity(jsonObject, gallery3);
		}
		else {
			return new PictureEntity(jsonObject, gallery3);
		}
	}
	
	public static String parseTag(JSONObject jsonObject) throws JSONException {
		return jsonObject.getJSONObject("entity").getString("name");
	}
	
	public static CommentEntity parseComment(JSONObject jsonObject) throws JSONException {
		return new CommentEntity(jsonObject);
	}
}
