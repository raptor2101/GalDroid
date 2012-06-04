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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GalDroidPreference {
  private static String CONFIG_DB_NAME = "GalDroid.db";
  private static String CONFIG_TABLE = "galleries";
  private static String CACHE_TABLE = "cached_objects";
  private static Context mContext;

  public static void Initialize(Context context) {
    if (mContext == null) {
      mContext = context;
    }
  }

  private static SQLiteDatabase createConnection() {
    SQLiteDatabase dataBase = mContext.openOrCreateDatabase(CONFIG_DB_NAME, Context.MODE_PRIVATE, null);
    dataBase.execSQL("CREATE TABLE IF NOT EXISTS " + CONFIG_TABLE + " (id integer primary key AUTOINCREMENT, name varchar(100) unique, type_name varchar(100), "
        + "root_link varchar(255), security_token varchar(255));");
    dataBase.execSQL("CREATE TABLE IF NOT EXISTS " + CACHE_TABLE + " (hash varchar(31) primary key, lastAccessed bigint, size bigint);");

    return dataBase;
  }

  public static List<String> getGalleryNames() {

    synchronized (mContext) {
      SQLiteDatabase database = createConnection();
      Cursor dbCursor = database.rawQuery("SELECT name FROM " + CONFIG_TABLE, null);
      List<String> returnList = new ArrayList<String>(dbCursor.getCount());
      dbCursor.isBeforeFirst();
      while (dbCursor.moveToNext()) {
        returnList.add(dbCursor.getString(0));
      }
      dbCursor.close();
      database.close();
      return returnList;
    }
  }

  public static GalleryConfig getSetupByName(String name) {
    synchronized (mContext) {
      SQLiteDatabase database = createConnection();
      Cursor dbCursor = database.rawQuery("SELECT id, type_name,root_link,security_token FROM " + CONFIG_TABLE, null);
      GalleryConfig returnValue = null;
      if (dbCursor.moveToFirst()) {
        returnValue = new GalleryConfig(dbCursor.getInt(0), name, dbCursor.getString(1), dbCursor.getString(2), dbCursor.getString(3));
      }
      dbCursor.close();
      database.close();
      return returnValue;
    }
  }

  public static void StoreGallery(int id, String galleryName, String galleryType, String galleryLink, String securityToken) {
    synchronized (mContext) {
      SQLiteDatabase database = createConnection();

      String[] selectionArgs = new String[] { String.format("%d", id) };

      ContentValues values = new ContentValues(4);
      values.put("name", galleryName);
      values.put("type_name", galleryType);
      values.put("root_link", galleryLink);
      values.put("security_token", securityToken);
      int returnValue = database.update(CONFIG_TABLE, values, "id = @1", selectionArgs);

      if (returnValue == 0) {
        values.put("name", galleryName);
        values.put("type_name", galleryType);
        values.put("root_link", galleryLink);
        values.put("security_token", securityToken);
        database.insert(CONFIG_TABLE, null, values);
      }
      database.close();
    }
  }

  public static void deleteGallery(String galleryName) {
    synchronized (mContext) {
      SQLiteDatabase database = createConnection();
      String[] selectionArgs = new String[] { galleryName };

      database.delete(CONFIG_TABLE, "name = @1", selectionArgs);
      database.close();
    }

  }

  public static void accessCacheObject(String hash, long size) {
    synchronized (mContext) {
      SQLiteDatabase database = createConnection();
      String[] selectionArgs = new String[] { hash };

      ContentValues values = new ContentValues(3);
      values.put("lastAccessed", System.currentTimeMillis());
      values.put("size", size);

      int returnValue = database.update(CACHE_TABLE, values, "hash = @1", selectionArgs);
      if (returnValue == 0) {
        values.put("hash", hash);
        database.insert(CACHE_TABLE, null, values);
      }
      database.close();
    }
  }

  public static boolean cacheObjectExists(String hash) {
    boolean result;
    synchronized (mContext) {
      SQLiteDatabase database = createConnection();
      String[] selectionArgs = new String[] { hash };

      Cursor dbCursor = database.query(CACHE_TABLE, new String[] { "hash" }, "hash = @1", selectionArgs, null, null, null);
      result = dbCursor.getCount() == 1;
      dbCursor.close();
      database.close();
    }
    return result;
  }

  public static long getCacheSpaceNeeded() {
    synchronized (mContext) {
      SQLiteDatabase database = createConnection();

      Cursor dbCursor = database.rawQuery("SELECT SUM(size) FROM " + CACHE_TABLE, null);
      long returnValue = -1;
      if (dbCursor.moveToFirst()) {
        returnValue = dbCursor.getLong(0);
      }
      dbCursor.close();
      database.close();
      return returnValue;
    }
  }

  public static List<String> getCacheOjectsOrderedByAccessTime() {
    synchronized (mContext) {
      SQLiteDatabase database = createConnection();

      Cursor dbCursor = database.query(CACHE_TABLE, new String[] { "hash" }, null, null, null, null, "lastAccessed desc");
      List<String> returnList = new ArrayList<String>(dbCursor.getCount());
      dbCursor.isBeforeFirst();
      while (dbCursor.moveToNext()) {
        returnList.add(dbCursor.getString(0));
      }
      dbCursor.close();
      database.close();
      return returnList;
    }
  }

  public static void deleteCacheObject(String hash) {
    synchronized (mContext) {
      SQLiteDatabase database = createConnection();
      String[] selectionArgs = new String[] { hash };

      database.delete(CACHE_TABLE, "hash = @1", selectionArgs);
      database.close();
    }
  }

  public static GalDroidPreference GetAsyncAccess() {
    synchronized (mContext) {
      return new GalDroidPreference(createConnection());
    }
  }

  private SQLiteDatabase mDbObject;

  private GalDroidPreference(SQLiteDatabase dbObject) {
    mDbObject = dbObject;
  }

  public void clearCacheTable() {
    mDbObject.delete(CACHE_TABLE, null, null);
  }

  public void insertCacheObject(File file) {
    ContentValues values = new ContentValues(3);
    values.put("hash", file.getName());
    values.put("lastAccessed", file.lastModified());
    values.put("size", file.length());
    mDbObject.insert(CACHE_TABLE, null, values);
  }

  @Override
  protected void finalize() throws Throwable {
    if (mDbObject != null) {
      close();
    }
  }

  public void close() {
    mDbObject.close();
    mDbObject = null;
  }
}
