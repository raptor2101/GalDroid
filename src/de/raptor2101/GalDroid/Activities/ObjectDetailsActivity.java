package de.raptor2101.GalDroid.Activities;

import java.io.File;
import java.io.IOException;

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
			
			
			TextView textField = (TextView) findViewById(R.id.textExifCreateDate);
			textField.setText(exif.getAttribute(ExifInterface.TAG_DATETIME));
			
			textField = (TextView) findViewById(R.id.textExifAperture);
			textField.setText(exif.getAttribute(ExifInterface.TAG_APERTURE));
			
			textField = (TextView) findViewById(R.id.textExifExposure);
			float exposureTime = 1.0f / Float.parseFloat(exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
			textField.setText(String.format("1/%.0fs", exposureTime));
			
			textField = (TextView) findViewById(R.id.textExifFlash);
			int flash = exif.getAttributeInt(ExifInterface.TAG_FLASH, 0);
			textField.setText(String.format("%d", flash));
			
			textField = (TextView) findViewById(R.id.textExifISO);
			textField.setText(exif.getAttribute(ExifInterface.TAG_ISO));
			
			textField = (TextView) findViewById(R.id.textExifModel);
			textField.setText(exif.getAttribute(ExifInterface.TAG_MODEL));
			
			textField = (TextView) findViewById(R.id.textExifModel);
			textField.setText(exif.getAttribute(ExifInterface.TAG_MODEL));
			
			textField = (TextView) findViewById(R.id.textExifMake);
			textField.setText(exif.getAttribute(ExifInterface.TAG_MAKE));
			
			double focalLength = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0);
			textField = (TextView) findViewById(R.id.textExifFocalLength);
			textField.setText(String.format("%.0fmm", focalLength));
			
			textField = (TextView) findViewById(R.id.textExifWhiteBalance);
			int whiteBalance = exif.getAttributeInt(ExifInterface.TAG_FLASH, 0);
			if(whiteBalance == ExifInterface.WHITEBALANCE_AUTO) {
				textField.setText(R.string.object_exif_whitebalance_auto);
			}
			else {
				textField.setText(R.string.object_exif_whitebalance_manual);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
