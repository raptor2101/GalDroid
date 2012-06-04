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

package de.raptor2101.GalDroid.WebGallery.Gallery3.JSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.raptor2101.GalDroid.WebGallery.Gallery3.Gallery3Imp;

public class AlbumEntity extends Entity {

  private final List<String> mMembers;

  public AlbumEntity(JSONObject jsonObject, Gallery3Imp gallery3) throws JSONException {
    super(jsonObject, gallery3);
    JSONArray memberArray = jsonObject.getJSONArray("members");

    try {
      jsonObject = jsonObject.getJSONObject("entity");
      String albumCover_RestLink = jsonObject.getString("album_cover");
      albumCover_RestLink = albumCover_RestLink.substring(gallery3.LinkRest_LoadItem.length() - 2);

      int coverId = Integer.parseInt(albumCover_RestLink);

      mLink_Full = String.format(gallery3.LinkRest_LoadPicture, coverId, "full");
      mLink_Thumb = String.format(gallery3.LinkRest_LoadPicture, getId(), "thumb");
      mFileSize_Full = 100000; // For some wired reason no FileSize is
      // reported by Gallery3 and a second
      // request cost to much io...
      mFileSize_Thumb = jsonObject.getInt("thumb_size");
    } catch (JSONException e) {
      mLink_Full = "";
      mLink_Thumb = "";
      mFileSize_Full = 0;
      mFileSize_Thumb = 0;
    }

    mMembers = new ArrayList<String>(memberArray.length());

    for (int i = 0; i < memberArray.length(); i++) {
      mMembers.add(memberArray.getString(i));
    }
  }

  public List<String> getMembers() {
    return mMembers;
  }

  public boolean hasChildren() {
    return mMembers.size() > 0;
  }

}
