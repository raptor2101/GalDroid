package de.raptor2101.GalDroid.WebGallery;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTask;
import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTaskListener;

public class GalleryImageView extends LinearLayout implements ImageLoaderTaskListener{
	private final ProgressBar mProgressBar;
	private final ImageView mImageView;
	private final TextView mTitleTextView;
	private final boolean mShowTitle;
	private GalleryObject mGalleryObject;
	private GalleryImage mGalleryImage;
	private WeakReference<ImageLoaderTask> mImageLoaderTask;
	
	public GalleryImageView(Context context, android.view.ViewGroup.LayoutParams layoutParams, boolean showTitle) {
		super(context);
		mShowTitle = showTitle;
		
		mImageView = CreateImageView(context);
		
		mProgressBar = new ProgressBar(context,null,android.R.attr.progressBarStyle);
		mProgressBar.setVisibility(GONE);
		this.addView(mProgressBar);
		
		this.setOrientation(VERTICAL);
		this.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
		this.setLayoutParams(layoutParams);
		
		this.addView(mImageView);
		
		
		
		if(mShowTitle){
			mTitleTextView = new TextView(context);
			mTitleTextView.setTextSize(16);
			mTitleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
			mTitleTextView.setTypeface(Typeface.create("Tahoma", Typeface.BOLD));
			this.addView(mTitleTextView);
		}
		else{
			mTitleTextView = null;
		}
		
		mImageLoaderTask = new WeakReference<ImageLoaderTask>(null);
	}
	
	public void setGalleryObject(GalleryObject galleryObject)
	{
		mGalleryObject = galleryObject;
		this.setTilte(galleryObject.getTitle());
	}
	
	public GalleryObject getGalleryObject(){
		return mGalleryObject;
	}
	public void setGalleryImage(GalleryImage galleryImage){
		mGalleryImage = galleryImage;
		
		if(galleryImage.isLoaded()){
			mImageView.setVisibility(VISIBLE);
			mImageView.setImageDrawable(galleryImage);	
		}
		else
		{
			mImageView.setVisibility(GONE);
		}
	}
	
	public void setTilte(String title)
	{
		if(mTitleTextView != null){
			mTitleTextView.setText(title);
		}
	}
	
	private ImageView CreateImageView(Context context) {
		ImageView imageView = new ImageView(context);
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setPadding(5, 5, 5, 5);
        
		return imageView;
	}
	
	public boolean isLoaded(){
		return mGalleryImage.isLoaded();
	}

	public void onLoadingStarted(String uniqueId) {
		mProgressBar.setVisibility(VISIBLE);
	}

	public void onLoadingCompleted(String uniqueId) {
		mProgressBar.setVisibility(GONE);
		setGalleryImage(mGalleryImage);		
	}
	
	public void cleanUp(){
		if(mGalleryImage != null){
			mGalleryImage.cleanup();
			mGalleryImage = null;
			mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		}
		ImageLoaderTask task = mImageLoaderTask.get();
		if(task != null){
			task.cancel(true);
		}
	}
	
	public Matrix getImageMatrix(){
		return mImageView.getMatrix();
	}
	
	public void setImageMatrix(Matrix matrix) {
		mImageView.setScaleType(ImageView.ScaleType.MATRIX);
		mImageView.setImageMatrix(matrix);
	}

	public void setImageLoaderTask(ImageLoaderTask downloadTask) {
		mImageLoaderTask = new WeakReference<ImageLoaderTask>(downloadTask);		
	}
}

