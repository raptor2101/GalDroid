package de.raptor2101.GalDroid.WebGallery.Gallery3;

import org.json.JSONException;
import org.json.JSONObject;

public class PictureEntity extends Entity {
	
	
	public PictureEntity(JSONObject jsonObject, Gallery3Imp gallery3)
			throws JSONException {
		super(jsonObject, gallery3);
		mLink_Full = String.format(gallery3.LinkRest_LoadPicture, getId(), "full");
		mLink_Thumb = String.format(gallery3.LinkRest_LoadPicture, getId(), "thumb");
	}

	public boolean hasChildren() {
		// A Image never have childs
		return false;
	}
}
