package de.raptor2101.GalDroid.Activities;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import de.raptor2101.GalDroid.R;
import de.raptor2101.GalDroid.WebGallery.GalleryCache;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTask;
import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTaskListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

public class ObjectDetailsActivity extends Activity implements ImageLoaderTaskListener {
	ImageView mImageView;
	ImageLoaderTask mLoaderTask;
	ProgressDialog mProgressDialog;
	GalleryObject mGalleryObject;
	GalleryCache mCache;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.object_details_activity);
	    
	    
	    Bundle extras = getIntent().getExtras();
	    if(extras != null)
	    {
		    mGalleryObject = (GalleryObject) extras.getSerializable(".de.raptor2101.GalDroid.GalleryObject");
		    
		    mImageView = (ImageView) findViewById(R.id.imageView);
		    
		    mProgressDialog = new ProgressDialog(this);
		    mProgressDialog.setTitle(R.string.progress_title_load_image);
		    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		    mProgressDialog.setCancelable(false);
		    mProgressDialog.dismiss();
		    
		    GalDroidApp app = (GalDroidApp)getApplicationContext();
		    
		    mCache = app.getGalleryCache();

			WebGallery webGallery = app.getWebGallery();
			mLoaderTask = new ImageLoaderTask(webGallery, mCache, mGalleryObject.getImage());
			mLoaderTask.setListener(this);
			mLoaderTask.execute();
	    }
	}

	public void onLoadingStarted(String uniqueId) {
		mProgressDialog.show();
	}

	public void onLoadingProgress(String uniqueId, int currentValue, int maxValue) {
		mProgressDialog.setMax(maxValue/1024);
		mProgressDialog.setProgress(currentValue/1024);
	}
	
	public void onLoadingCompleted(String uniqueId, Bitmap bitmap) {
		mProgressDialog.dismiss();
		mImageView.setImageBitmap(bitmap);
		mImageView.setVisibility(View.VISIBLE);
		mLoaderTask = null;
		extractObjectInformation();
		extractExifInformation();
	}

	public void onLoadingCancelled(String uniqueId) {
		mProgressDialog.dismiss();
	}
	
	private void extractObjectInformation() {

		TextView textTitle = (TextView) findViewById(R.id.textTitle);
		TextView textUploadDate = (TextView) findViewById(R.id.textUploadDate);
		
		
		textTitle.setText(mGalleryObject.getTitle());
		textUploadDate.setText(mGalleryObject.getDateUploaded().toLocaleString());
	}
	
	private void extractExifInformation() {
		File cachedFile = mCache.getFile(mGalleryObject.getImage().getUniqueId());
		try {
			ExifInterface exif = new ExifInterface(cachedFile.getAbsolutePath());
			
			
			TextView textField = (TextView) findViewById(R.id.textCreateDate);
			textField.setText(exif.getAttribute(ExifInterface.TAG_DATETIME));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
