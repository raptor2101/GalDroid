package de.raptor2101.GalDroid.WebGallery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.List;

import de.raptor2101.GalDroid.Config.GalDroidPreference;



import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

public class GalleryCache {
	private File mCacheDir; 
	private MessageDigest mDigester;
	private Hashtable<String,WeakReference<Bitmap>> mCachedBitmaps;
	
	public GalleryCache(Context context) throws NoSuchAlgorithmException{
		if (mCacheDir == null) {
			mCacheDir = context.getExternalCacheDir();
			
			if(mCacheDir == null){
				mCacheDir = context.getCacheDir();
			}
		}
		
		mCachedBitmaps = new Hashtable<String,WeakReference<Bitmap>>(50);
		mDigester = MessageDigest.getInstance("MD5");
	}
	
	public void storeBitmap(String sourceUrl, Bitmap bitmap)
	{
		String hash = buildHash(sourceUrl);
		File cacheFile = new File(mCacheDir, hash);
		mCachedBitmaps.put(hash, new WeakReference<Bitmap>(bitmap));
		
		if (cacheFile.exists()) {
			cacheFile.delete();
		}
		try {
			cacheFile.createNewFile();
			
			FileOutputStream output = new FileOutputStream(cacheFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
			output.close();
			
			GalDroidPreference.AccessCacheObject(hash, cacheFile.length());
		} catch (IOException e) {
			Log.e("GalleryCache", "Error while storing");
		}	
	}
	
	public void cacheBitmap(String sourceUrl, Bitmap bitmap){
		String hash = buildHash(sourceUrl);
		mCachedBitmaps.put(hash, new WeakReference<Bitmap>(bitmap));
	}
	
	public InputStream getFileStream(String sourceUrl)
	{
		String hash = buildHash(sourceUrl);
		File cacheFile = new File(mCacheDir, hash);
		if (cacheFile.exists()) {
			try {
				Log.d("GalleryCache", "Cache Hit " + sourceUrl);
				GalDroidPreference.AccessCacheObject(hash, cacheFile.length());
				return new FileInputStream(cacheFile);
				
			} catch (IOException e) {
				Log.e("GalleryCache", "Error while accessing");
				return null;				
			}	
		}
		Log.d("GalleryCache", "Cache Mis " + sourceUrl);
		return null;
	}
	
	public Bitmap getBitmap(String sourceUrl)
	{
		String hash = buildHash(sourceUrl);
		WeakReference<Bitmap> reference = mCachedBitmaps.get(hash);
		if(reference == null){
			return null;
		}
		Bitmap bitmap = reference.get();
		if(bitmap == null || bitmap.isRecycled()){
			mCachedBitmaps.remove(hash);
			return null;
		}
		Log.d("GalleryCache", "Cache Hit Reference " + sourceUrl);
		return bitmap;
	}
	
	private String buildHash(String sourceUrl)
	{
		mDigester.reset();
		byte[] bytes = sourceUrl.getBytes();
		mDigester.update(bytes,0,bytes.length);
		byte[] diggest = mDigester.digest();
		StringBuffer returnString = new StringBuffer(diggest.length);
		for (byte b : diggest) {
			returnString.append(Integer.toHexString(0xFF & b));
		}
		
		return returnString.toString();
	}

	public void clearCachedBitmaps() {
		for(WeakReference<Bitmap> reference:mCachedBitmaps.values()) {
			Bitmap bitmap = reference.get();
			if(bitmap != null){
				bitmap.recycle();
			}
		}
		mCachedBitmaps.clear();
	}

	public void cleanup(long maxCacheSize) {
		maxCacheSize *= 1024 *1024; // convert MByte to Byte
		long currentCacheSize = GalDroidPreference.getCacheSpaceNeeded();
		if(currentCacheSize > maxCacheSize){
			List<String> cachedObjects = GalDroidPreference.getCacheOjectsOrderedByAccessTime();
			maxCacheSize = maxCacheSize - (maxCacheSize / 3);
			for(int i=0;currentCacheSize>maxCacheSize;i++){
				String hash = cachedObjects.get(i);
				File cacheFile = new File(mCacheDir, hash);
				if (cacheFile.exists()){
					currentCacheSize -= cacheFile.length(); 
					cacheFile.delete();
				}
				GalDroidPreference.deleteCacheObject(hash);
			}
		}
	}
	
	public void syncronize(){
		GalDroidPreference.syncronizeCacheObject(mCacheDir.listFiles());
	}
}
