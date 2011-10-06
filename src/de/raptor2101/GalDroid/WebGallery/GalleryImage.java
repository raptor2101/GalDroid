package de.raptor2101.GalDroid.WebGallery;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;


public class GalleryImage extends Drawable {
	private Bitmap mBitmap = null;
	
	@Override
	public void draw(Canvas canvas) {
		if(mBitmap != null && !mBitmap.isRecycled())
		{
			Paint paint = new Paint();
			paint.setAntiAlias(true);
		    paint.setFilterBitmap(true);
		    paint.setDither(true);
		    
			canvas.drawBitmap(mBitmap, 0, 0, paint);
		}
	}

	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int getMinimumHeight() {
		if(mBitmap == null) {
			return 0;
		}
		else {
			return mBitmap.getHeight();
		}
	}
	
	@Override
	public int getMinimumWidth() {
		if(mBitmap == null) {
			return 0;
		}
		else {
			return mBitmap.getWidth();
		}
	}
	
	@Override
	public int getIntrinsicWidth() {
		if(mBitmap == null) {
			return 0;
		}
		else {
			return mBitmap.getWidth();
		}
	}
	
	@Override
	public int getIntrinsicHeight() {
		if(mBitmap == null) {
			return 0;
		}
		else {
			return mBitmap.getHeight();
		}
	}
	
	public void setBitmap(Bitmap bitmap) {
		mBitmap = bitmap;
		this.invalidateSelf();		
	}
	
	public boolean isLoaded(){
		return mBitmap != null;
	}

	public void cleanup() {
		if(mBitmap != null){
			mBitmap = null;
		}
	}
}
