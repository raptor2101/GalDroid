package de.raptor2101.GalDroid.Config;

public class GalleryConfig {
	
	public final int Id;
	public final String Name;
	public final String TypeName;
	public final String RootLink;
	public final String SecurityToken;
	
	public GalleryConfig(int id, String name, String typeName, String rootLink, String securityToken)
	{
		Id = id;
		Name = name;
		TypeName = typeName;
		RootLink = rootLink;
		SecurityToken = securityToken;
	}

}
