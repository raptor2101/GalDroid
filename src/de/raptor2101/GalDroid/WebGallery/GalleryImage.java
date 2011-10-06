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
