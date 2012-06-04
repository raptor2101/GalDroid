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

package de.raptor2101.GalDroid.Config;

public class GalleryConfig {

  public final int Id;
  public final String Name;
  public final String TypeName;
  public final String RootLink;
  public final String SecurityToken;

  public GalleryConfig(int id, String name, String typeName, String rootLink, String securityToken) {
    Id = id;
    Name = name;
    TypeName = typeName;
    RootLink = rootLink;
    SecurityToken = securityToken;
  }

}
