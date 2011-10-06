package de.raptor2101.GalDroid.WebGallery.Interfaces;

import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery.ImageSize;


public interface GalleryObject 
{
	public String getTitle();
	public String getUniqueId(ImageSize imageSize);
	public boolean hasChildren();	
	public String getObjectId();
	
	
}
