package de.raptor2101.GalDroid.WebGallery.Interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;


public interface WebGallery {
	public enum ImageSize{
		Thumbnail,
		Full
	}
	
	public List<GalleryObject> getDisplayObjects();
	public List<GalleryObject> getDisplayObjects(String path);
	public List<GalleryObject> getDisplayObjects(GalleryProgressListener progressListener);
	public List<GalleryObject> getDisplayObjects(String path, GalleryProgressListener progressListener);
	
	public InputStream getImageRawData(GalleryObject galleryObject, ImageSize imageSize)throws ClientProtocolException, IOException;
	
	public String getSecurityToken(String user, String password) throws SecurityException;
	
	public void setSecurityToken(String token);
	public void setHttpClient(HttpClient httpClient);
}
