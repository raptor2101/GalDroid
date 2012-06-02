package de.raptor2101.GalDroid.WebGallery.Tasks;

import java.io.File;
import java.lang.ref.WeakReference;

import de.raptor2101.GalDroid.Config.GalDroidPreference;
import de.raptor2101.GalDroid.WebGallery.ImageCache;
import android.os.AsyncTask;

public class SyncronizeCacheTask extends AsyncTask<Void, Integer, Void> {
    private WeakReference<CacheTaskListener> mListener;
    private File[] mFiles;

    public SyncronizeCacheTask(ImageCache cache, CacheTaskListener listener) {
	mListener = new WeakReference<CacheTaskListener>(listener);
	mFiles = cache.ListCachedFiles();
    }

    @Override
    protected void onPreExecute() {
	CacheTaskListener listener = mListener.get();
	if (listener != null) {
	    listener.onCacheOperationStart(mFiles.length);
	}
    }

    @Override
    protected Void doInBackground(Void... params) {
	GalDroidPreference preference = GalDroidPreference.GetAsyncAccess();
	try {
	    preference.clearCacheTable();
	    int length = mFiles.length;
	    for (int i = 0; i < length; i++) {
		preference.insertCacheObject(mFiles[i]);
		publishProgress(i);
	    }
	} finally {
	    preference.close();
	}
	return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
	CacheTaskListener listener = mListener.get();
	if (listener != null) {
	    listener.onCacheOperationProgress(values[0]);
	}
    }

    @Override
    protected void onPostExecute(Void result) {
	CacheTaskListener listener = mListener.get();
	if (listener != null) {
	    listener.onCacheOperationDone();
	}
    }
}