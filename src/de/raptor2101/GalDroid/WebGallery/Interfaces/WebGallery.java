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

package de.raptor2101.GalDroid.WebGallery.Interfaces;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.json.JSONException;

import de.raptor2101.GalDroid.WebGallery.Stream;

public interface WebGallery {
  public enum ImageSize {
    Thumbnail, Full
  }

  public GalleryObject getDisplayObject(String path) throws ClientProtocolException, IOException, JSONException;

  public List<GalleryObject> getDisplayObjects();

  public List<GalleryObject> getDisplayObjects(GalleryProgressListener progressListener);

  public List<GalleryObject> getDisplayObjects(String path);

  public List<GalleryObject> getDisplayObjects(String path, GalleryProgressListener progressListener);

  public List<GalleryObject> getDisplayObjects(GalleryObject galleryObject);

  public List<GalleryObject> getDisplayObjects(GalleryObject galleryObject, GalleryProgressListener progressListener);

  public List<String> getDisplayObjectTags(GalleryObject galleryObject, GalleryProgressListener progressListener) throws IOException;

  public List<GalleryObjectComment> getDisplayObjectComments(GalleryObject galleryObject, GalleryProgressListener progressListener) throws IOException, ClientProtocolException, JSONException;

  public void setPreferedDimensions(int height, int width);

  public String getSecurityToken(String user, String password) throws SecurityException;

  public Stream getFileStream(GalleryDownloadObject downloadObject) throws IOException, ClientProtocolException;

  public void setSecurityToken(String token);

  public void setHttpClient(HttpClient httpClient);
}
