package de.raptor2101.GalDroid.Activities;

import java.security.NoSuchAlgorithmException;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.raptor2101.GalDroid.R;
import de.raptor2101.GalDroid.Config.GalDroidPreference;
import de.raptor2101.GalDroid.WebGallery.GalleryCache;

public class GalleryListingActivitiy extends ListActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerForContextMenu(getListView());
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		TextView view = (TextView) info.targetView;
		
		String galleryName = view.getText().toString();
		if (item.getItemId() == R.id.item_edit_gallery) {
			Intent intent = new Intent(this, EditGalleryActivity.class);
			intent.putExtra("GalleryName", galleryName);
			this.startActivity(intent);
		}
		else {
			GalDroidPreference.deleteGallery(galleryName);
			recreate();
		}
		return true;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gallery_context_menu, menu);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.gallery_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.item_create_new) {
			Intent intent = new Intent(this, EditGalleryActivity.class);
			this.startActivity(intent);
		}
		
		return true;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		TextView view = (TextView) v;
		String galleryName = view.getText().toString();
		
		Intent intent = new Intent(this, GridViewActivity.class);
		intent.putExtra("GalleryName", galleryName);
		
		this.startActivity(intent);
	}
	
	@Override
	protected void onResume() {
		try {
			GalDroidApp app = (GalDroidApp) getApplication();
			app.Initialize(this);
			GalleryCache cache = app.getGalleryCache();
			
			cache.cleanup(50);
			
			
			setListAdapter(new ArrayAdapter<String>(this, R.layout.gallery_listing_activity, GalDroidPreference.getGalleryNames()));
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		super.onResume();
	}
}
