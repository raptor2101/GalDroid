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
