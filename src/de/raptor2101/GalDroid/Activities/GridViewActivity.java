package de.raptor2101.GalDroid.Activities;

import java.util.List;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import de.raptor2101.GalDroid.R;
import de.raptor2101.GalDroid.WebGallery.GalleryImageAdapter;
import de.raptor2101.GalDroid.WebGallery.GalleryImageView;

import de.raptor2101.GalDroid.WebGallery.GalleryImageAdapter.TitleConfig;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;

public class GridViewActivity extends GalleryActivity implements OnItemClickListener {
	
	private GridView mGridView;
	private GalleryImageAdapter mAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.grid_view_activity);
        
        mGridView = (GridView) findViewById(R.id.gridViewWidget);
    	mGridView.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
    	mGridView.setWillNotCacheDrawing(true);
        mGridView.setOnItemClickListener(this);
        
        mAdapter = new GalleryImageAdapter(this, new GridView.LayoutParams(295, 295));
        mAdapter.setTitleConfig(TitleConfig.ShowTitle);
		
        mGridView.setAdapter(mAdapter);
        
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void onResume() {
    	GalleryImageAdapter adapter = (GalleryImageAdapter) mGridView.getAdapter();
        if(adapter != null) {
        	adapter.refreshImages();
        }
    	super.onResume();
    }
    
    @Override
    public void onBackPressed() {
    	mAdapter.cleanUp();
    	super.onBackPressed();
    }

	public void onItemClick(AdapterView<?> parent, View view, int pos, long rowId) {
		GalleryImageView imageView = (GalleryImageView) view;
        GalleryObject galleryObject = imageView.getGalleryObject();
        
        Intent intent;
        if (!galleryObject.hasChildren()) {
			intent = new Intent(this, ImageViewActivity.class);
			intent.putExtra("Current Index", pos);
			intent.putExtra("Current UniqueId", getUnqiueId());
        }
        else
        {
            intent = new Intent(this, GridViewActivity.class);
        	intent.putExtra("Current UniqueId", galleryObject.getObjectId());
        }
        
        this.startActivity(intent);
	}

	
	
	public void onGalleryObjectsLoaded(List<GalleryObject> galleryObjects){
		mAdapter.setGalleryObjects(galleryObjects);
	}

}