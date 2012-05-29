package de.raptor2101.GalDroid.Activities.Views;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import de.raptor2101.GalDroid.R;
import de.raptor2101.GalDroid.Activities.GalDroidApp;
import de.raptor2101.GalDroid.Activities.Listeners.CommentLoaderListener;
import de.raptor2101.GalDroid.Activities.Listeners.TagLoaderListener;
import de.raptor2101.GalDroid.WebGallery.GalleryCache;
import de.raptor2101.GalDroid.WebGallery.GalleryImageView;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
import de.raptor2101.GalDroid.WebGallery.Tasks.CommentLoaderTask;
import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTask;
import de.raptor2101.GalDroid.WebGallery.Tasks.TagLoaderTask;
import android.content.Context;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

public class ImageInformationView extends TableLayout {	
	private final static String ClassTag = "ImageInformationView";
	
	public class ExtractInformationTask extends AsyncTask<Void,Void,Void> {
		private GalleryImageView mGalleryImageView;
		
		public ExtractInformationTask(GalleryImageView imageView) {
			mGalleryImageView = imageView;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			Thread.currentThread().setName(toString());
			if(!mGalleryImageView.isLoaded()) {
				ImageLoaderTask task = mGalleryImageView.getImageLoaderTask();
				if(task!=null) {
					try {
						task.get();
					} catch (Exception e) {
						/* 
						 * nothing to do here cause this task is only waiting for the image loader task
						 * if it fails, all error handling is done by the surrounding logic
						 */
					}
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if(mGalleryImageView.isLoaded()) {
				extractObjectInformation(mGalleryImageView.getGalleryObject());
			}
		}
		
		@Override
		public String toString() {
					return String.format("%s for %s", ClassTag, mGalleryImageView.getGalleryObject());
		}
	}
	
	private TagLoaderListener mTagLoaderListener;
	private CommentLoaderListener mCommentLoaderListener;
	
	private ExtractInformationTask mExtractTask;
	
	private GalleryImageView mCurrentImageView;
	
	public ImageInformationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public ImageInformationView(Context context) {
		super(context);
		initialize();
	}
	
	public void setGalleryImageView(GalleryImageView imageView) {
		Log.d(ClassTag, "setGalleryImageView");
		mCurrentImageView = imageView;
		if(getVisibility() == VISIBLE) {
			executeExtrationTask();
		}
	}
	
	@Override
	public void setVisibility(int visibility) {
		Log.d(ClassTag, "setVisibility");
		if(visibility == VISIBLE) {
			executeExtrationTask();
		}
		
		super.setVisibility(visibility);
	}
	
	private void executeExtrationTask() {
		Log.d(ClassTag, "executeExtrationTask");
		if (mCurrentImageView != null) {
			clearImageInformations();
			Log.d(ClassTag, "create ExtractInformationTask");
			
			CheckAndAbortExistingTask();
			
			mExtractTask = new ExtractInformationTask(mCurrentImageView);
			mExtractTask.execute();
		}
	}

	private void CheckAndAbortExistingTask() {
		TagLoaderTask tagLoaderTask = mTagLoaderListener.getTagLoaderTask();
		if(tagLoaderTask != null && tagLoaderTask.getStatus() != Status.FINISHED) {
			Log.d(ClassTag, "Aborting TagLoaderTask");
			tagLoaderTask.cancel(true);
		}
		
		CommentLoaderTask commentLoaderTask = mCommentLoaderListener.getCommentLoaderTask();
		if(commentLoaderTask != null && commentLoaderTask.getStatus() != Status.FINISHED) {
			Log.d(ClassTag, "Aborting CommentLoaderTask");
			commentLoaderTask.cancel(true);
		}
	}

	private void initialize(){
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.image_information_view, this);
		
		mTagLoaderListener = new TagLoaderListener((TextView)findViewById(R.id.textTags), (ProgressBar)findViewById(R.id.progressBarTags));
		mCommentLoaderListener = new CommentLoaderListener((ViewGroup)findViewById(R.id.layoutComments), (ProgressBar)findViewById(R.id.progressBarComments));
		
	}
	
	private void clearImageInformations() {
		
		ViewGroup commentView = (ViewGroup)findViewById(R.id.layoutComments);
		commentView.removeAllViews();
		
		TextView textView = (TextView) findViewById(R.id.textTitle);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textTags);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textUploadDate);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifCreateDate);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifAperture);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifExposure);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifFlash);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifISO);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifModel);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifModel);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifMake);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifFocalLength);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textExifWhiteBalance);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textGeoLat);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textGeoLong);
		textView.setText("");
		
		textView = (TextView) findViewById(R.id.textGeoHeight);
		textView.setText("");
	}

	private void extractObjectInformation(GalleryObject galleryObject) {
		GalDroidApp app = (GalDroidApp) getContext().getApplicationContext();
		WebGallery webGallery = app.getWebGallery();
		GalleryCache galleryCache = app.getGalleryCache();
		
		TextView textTitle = (TextView) findViewById(R.id.textTitle);
		TextView textUploadDate = (TextView) findViewById(R.id.textUploadDate);
		
		
		textTitle.setText(galleryObject.getTitle());
		textUploadDate.setText(galleryObject.getDateUploaded().toLocaleString());
		
		CheckAndAbortExistingTask();
		
		TagLoaderTask tagLoaderTask = new TagLoaderTask(webGallery, mTagLoaderListener);
		mTagLoaderListener.setTagLoaderTask(tagLoaderTask);
		tagLoaderTask.execute(galleryObject);
		
		
		
		CommentLoaderTask commentLoaderTask = new CommentLoaderTask(webGallery, mCommentLoaderListener);
		mCommentLoaderListener.setCommentLoaderTask(commentLoaderTask);
		commentLoaderTask.execute(galleryObject);
		
		extractExifInformation(galleryObject, galleryCache);
	}
	
	private void extractExifInformation(GalleryObject galleryObject, GalleryCache galleryCache) {
		File cachedFile = galleryCache.getFile(galleryObject.getImage().getUniqueId());
		
		try {
			ExifInterface exif = new ExifInterface(cachedFile.getAbsolutePath());
			
			
			TextView textField = (TextView) findViewById(R.id.textExifCreateDate);
			textField.setText(exif.getAttribute(ExifInterface.TAG_DATETIME));
			
			textField = (TextView) findViewById(R.id.textExifAperture);
			textField.setText(exif.getAttribute(ExifInterface.TAG_APERTURE));
			
			try {
				textField = (TextView) findViewById(R.id.textExifExposure);
				float exposureTime = 1.0f / Float.parseFloat(exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
				textField.setText(String.format("1/%.0fs", exposureTime));
			} catch (Exception e) {
				
			}
			
			try {
				textField = (TextView) findViewById(R.id.textExifFlash);
				int flash = exif.getAttributeInt(ExifInterface.TAG_FLASH, 0);
				textField.setText(String.format("%d", flash));
			} catch (Exception e) {
				
			}
			
			textField = (TextView) findViewById(R.id.textExifISO);
			textField.setText(exif.getAttribute(ExifInterface.TAG_ISO));
			
			textField = (TextView) findViewById(R.id.textExifModel);
			textField.setText(exif.getAttribute(ExifInterface.TAG_MODEL));
			
			textField = (TextView) findViewById(R.id.textExifModel);
			textField.setText(exif.getAttribute(ExifInterface.TAG_MODEL));
			
			textField = (TextView) findViewById(R.id.textExifMake);
			textField.setText(exif.getAttribute(ExifInterface.TAG_MAKE));
			
			try {
				double focalLength = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0);
				textField = (TextView) findViewById(R.id.textExifFocalLength);
				textField.setText(String.format("%.0fmm", focalLength));
			} catch (Exception e) {

			}
			
			try {
				textField = (TextView) findViewById(R.id.textExifWhiteBalance);
				int whiteBalance = exif.getAttributeInt(ExifInterface.TAG_FLASH, 0);
				if(whiteBalance == ExifInterface.WHITEBALANCE_AUTO) {
					textField.setText(R.string.object_exif_whitebalance_auto);
				}
				else {
					textField.setText(R.string.object_exif_whitebalance_manual);
				}
			} catch (Exception e) {
				
			}
			
			textField = (TextView) findViewById(R.id.textGeoLat);
			String latitude = parseDegMinSec(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
			textField.setText(latitude);
			
			textField = (TextView) findViewById(R.id.textGeoLong);
			String longitude = parseDegMinSec(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
			textField.setText(longitude);
			
			textField = (TextView) findViewById(R.id.textGeoHeight);
			String height = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
			textField.setText(parseHeight(height));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String parseDegMinSec(String tagGpsValue) {
		try {
			String[] values = tagGpsValue.split("[,/]");
			float deg = Float.parseFloat(values[0])/Float.parseFloat(values[1]);
			float min = Float.parseFloat(values[2])/Float.parseFloat(values[3]);
			float sec = Float.parseFloat(values[4])/Float.parseFloat(values[5]);
			return String.format("%.0f° %.0f' %.2f\"", deg,min,sec);
		} catch (Exception e) {
			return "";
		}
	}
	
	private String parseHeight(String tagGpsValue) {
		try {
			String[] values = tagGpsValue.split("/");
			float height = Float.parseFloat(values[0])/Float.parseFloat(values[1]);
			return String.format("%.2fm", height);
		} catch (Exception e) {
			return "";
		}
	}
	
	public ExtractInformationTask getExtractionInfromationTask() {
		return mExtractTask;
	}

	public CommentLoaderTask getCommentLoaderTask() {
		return mCommentLoaderListener.getCommentLoaderTask();
	}
	
	public TagLoaderTask getTagLoaderTask() {
		return mTagLoaderListener.getTagLoaderTask();
	}
}
