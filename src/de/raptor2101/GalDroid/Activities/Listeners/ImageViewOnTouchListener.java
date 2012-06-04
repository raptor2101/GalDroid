package de.raptor2101.GalDroid.Activities.Listeners;

import de.raptor2101.GalDroid.WebGallery.GalleryImageView;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Gallery;

public class ImageViewOnTouchListener implements OnTouchListener {

  private enum TouchMode {
    None, Drag, Zoom,
  }

  private final Gallery mGalleryFullscreen;
  private final Gallery mGalleryThumbnails;

  private final float mMinDragHeight;

  private TouchMode mTouchMode;
  private PointF mScalePoint = new PointF();
  private float mTouchStartY, mTouchStartX, mOldDist = 1;

  public ImageViewOnTouchListener(Gallery galleryFullscreen, Gallery galleryThumbnails, float minDragHeight) {
    mGalleryFullscreen = galleryFullscreen;
    mGalleryThumbnails = galleryThumbnails;
    mMinDragHeight = minDragHeight;
  }

  public boolean onTouch(View v, MotionEvent event) {
    Log.d("ImageViewActivity", "EventAction: " + event.getAction());
    switch (event.getAction()) {
    case MotionEvent.ACTION_DOWN:
      mTouchStartY = event.getY();
      mTouchStartX = event.getX();
      mTouchMode = TouchMode.Drag;
      Log.d("ImageViewActivity", "Start Drag");
      break;
    case MotionEvent.ACTION_POINTER_3_DOWN:
    case MotionEvent.ACTION_POINTER_2_DOWN:
    case MotionEvent.ACTION_POINTER_DOWN:
      /*
       * mOldDist = getSpacing(event); if(mOldDist > 10f) { mTouchMode =
       * TouchMode.Zoom; setScalePoint(event); }
       */
      break;
    case MotionEvent.ACTION_POINTER_3_UP:
    case MotionEvent.ACTION_POINTER_2_UP:
    case MotionEvent.ACTION_POINTER_UP:
      mTouchMode = TouchMode.None;
      break;
    case MotionEvent.ACTION_UP:
      if (mTouchMode == TouchMode.Drag) {
        if (Math.abs(event.getX() - mTouchStartX) < 50) {
          float diffY = event.getY() - mTouchStartY;
          Log.d("ImageViewActivity", String.format("DragLength %.2f MinLength %.2f", Math.abs(diffY), mMinDragHeight));
          if (Math.abs(diffY) > mMinDragHeight) {
            if (diffY > 0 && mGalleryThumbnails.getVisibility() == View.VISIBLE) {
              mGalleryThumbnails.setVisibility(View.GONE);
            } else if (diffY < 0 && mGalleryThumbnails.getVisibility() == View.GONE) {
              mGalleryThumbnails.setVisibility(View.VISIBLE);
            }
          }
        }
      }
      break;
    case MotionEvent.ACTION_MOVE:
      if (mTouchMode == TouchMode.Zoom) {
        float dist = getSpacing(event);
        if (dist > 10f) {
          GalleryImageView imageView = (GalleryImageView) mGalleryFullscreen.getSelectedView();
          float scale = dist / mOldDist;
          if (scale >= 1 && scale <= 10) {
            Log.d("ImageViewActivity", "ACTION_MOVE Scale:" + scale);
            Matrix matrix = imageView.getImageMatrix();

            matrix.postScale(scale, scale, mScalePoint.x - imageView.getLeft(), mScalePoint.y - imageView.getTop());
            imageView.setImageMatrix(matrix);
          }
        }
      }
      break;
    }
    return false;
  }

  private float getSpacing(MotionEvent event) {
    float x = event.getX(0) - event.getX(1);
    float y = event.getY(0) - event.getY(1);
    return FloatMath.sqrt(x * x + y * y);
  }

  private void setScalePoint(MotionEvent event) {
    float x = event.getX(0) + event.getX(1);
    float y = event.getY(0) + event.getY(1);
    mScalePoint.set(x / 2, y / 2);
  }
}
