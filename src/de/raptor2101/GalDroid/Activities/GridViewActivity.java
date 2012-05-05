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
import de.raptor2101.GalDroid.WebGallery.GalleryImageAdapter.ScaleMode;
import de.raptor2101.GalDroid.WebGallery.GalleryImageView;

import de.raptor2101.GalDroid.WebGallery.GalleryImageAdapter.TitleConfig;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;

public class GridViewActivity extends GalleryActivity implements OnItemClickListener {
	
	private static final int CURRENT_INDEX = 0;
	private GridView mGridView;
	private GalleryImageAdapter mAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_view_activity);
        
        mGridView = (GridView) findViewById(R.id.gridViewWidget);
    	mGridView.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
    	mGridView.setWillNotCacheDrawing(true);
        mGridView.setOnItemClickListener(this);
        
        mAdapter = new GalleryImageAdapter(this, new GridView.LayoutParams(295, 295), ScaleMode.ScaleSource);
        mAdapter.setTitleConfig(TitleConfig.ShowTitle);
		
        mGridView.setAdapter(mAdapter);
        
        
    }
    
    @Override
    public void onBackPressed() {
    	mAdapter.cleanUp();
    	super.onBackPressed();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
      	
    	GalleryImageAdapter adapter = (GalleryImageAdapter) mGridView.getAdapter();
        if(adapter != null) {
        	adapter.refreshImages();
		}
    }

    @Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {     
      super.onActivityResult(requestCode, resultCode, data); 
      switch(requestCode) { 
        case (CURRENT_INDEX) : { 
          
          int scrollPos = data.getIntExtra("Current Index",-1);
          mGridView.smoothScrollToPositionFromTop(scrollPos, 600);
 
          break; 
        } 
      } 
    }
    
    
	public void onItemClick(AdapterView<?> parent, View view, int pos, long rowId) {
		GalleryImageView imageView = (GalleryImageView) view;
        GalleryObject galleryObject = imageView.getGalleryObject();
        
        Intent intent;
        if (!galleryObject.hasChildren()) {
			intent = new Intent(this, ImageViewActivity.class);
			intent.putExtra(".de.raptor2101.GalDroid.CurrentIndex", pos);
			intent.putExtra(".de.raptor2101.GalDroid.CurrentUniqueId", getUnqiueId());
			this.startActivityForResult(intent, CURRENT_INDEX);
        }
        else
        {
            intent = new Intent(this, GridViewActivity.class);
        	intent.putExtra(".de.raptor2101.GalDroid.CurrentUniqueId", galleryObject.getObjectId());
        	this.startActivity(intent);
        }
        
	}

	
	
	public void onGalleryObjectsLoaded(List<GalleryObject> galleryObjects){
		mAdapter.setGalleryObjects(galleryObjects);
	}

}