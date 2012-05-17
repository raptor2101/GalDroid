package de.raptor2101.GalDroid.Activities.Listeners;

import java.util.List;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.raptor2101.GalDroid.WebGallery.Tasks.TagLoaderTaskListener;

public class TagLoaderListener implements TagLoaderTaskListener {
	private final TextView mTagTextView;
	private final ProgressBar mProgressBar;
	
	public TagLoaderListener(TextView targetTextView, ProgressBar progressBar) {
		mProgressBar = progressBar;
		mTagTextView = targetTextView;
	}
	
	public void onLoadingStarted() {
		mProgressBar.setVisibility(View.VISIBLE);
		mTagTextView.setVisibility(View.GONE);
		
	}

	public void onLoadingProgress(int elementCount, int maxCount) {
		mProgressBar.setMax(maxCount);
		mProgressBar.setProgress(elementCount);
		
	}

	public void onLoadingCompleted(List<String> tags) {
		StringBuilder stringBuilder = new StringBuilder(tags.size()*10);
		for(String tag:tags) {
			stringBuilder.append(String.format("%s, ", tag));
		}
		int length = stringBuilder.length() ;
		if(length > 0) {
			stringBuilder.delete(stringBuilder.length()-2, stringBuilder.length());
		}
		
		mTagTextView.setText(stringBuilder);
		
		mProgressBar.setVisibility(View.GONE);
		mTagTextView.setVisibility(View.VISIBLE);
	}

	public void onLoadingCanceled() {
		mTagTextView.setText(null);
		mProgressBar.setVisibility(View.GONE);
		mTagTextView.setVisibility(View.VISIBLE);
	}
	
};