package de.raptor2101.GalDroid.WebGallery.Interfaces;

import java.io.IOException;
import java.io.InputStream;

public interface GalleryDownloadObject {
	public InputStream getFileStream() throws IOException;
	public String getUniqueId();
}
