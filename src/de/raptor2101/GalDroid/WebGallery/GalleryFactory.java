package de.raptor2101.GalDroid.WebGallery;

import org.apache.http.client.HttpClient;

import de.raptor2101.GalDroid.WebGallery.Gallery3.Gallery3Imp;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;

public class GalleryFactory {
	public static WebGallery createFromName(String name, String serverUrl, HttpClient client)
	{
		Gallery3Imp gallery = new Gallery3Imp(serverUrl);
		gallery.setHttpClient(client);
		return gallery;
	}
}
