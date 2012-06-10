package de.raptor2101.GalDroid.WebGallery.Tasks;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.media.ExifInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import de.raptor2101.GalDroid.WebGallery.DegMinSec;
import de.raptor2101.GalDroid.WebGallery.ImageCache;
import de.raptor2101.GalDroid.WebGallery.ImageInformation;
import de.raptor2101.GalDroid.WebGallery.ImageInformation.WhiteBalance;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryDownloadObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObjectComment;
import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;

public class ImageInformationLoaderTask extends RepeatingTask<GalleryObject, Void, Void> {
  private final static String CLASS_TAG = "ImageInformationLoaderTask";
  public static final int MESSAGE_IMAGE_INFORMATION = 0;
  public static final int MESSAGE_IMAGE_TAGS = 1;
  public static final int MESSAGE_IMAGE_COMMENTS = 2;

  private class MessageBody {
    public final GalleryObject mGalleryObject;
    public final Object mObject;

    public MessageBody(GalleryObject galleryObject, Object object) {
      mGalleryObject = galleryObject;
      mObject = object;
    }
  }

  private class InternHandler extends Handler {
    @SuppressWarnings("unchecked")
    @Override
    public void handleMessage(Message msg) {
      ImageInformationLoaderTaskListener listener = mListener.get();
      MessageBody messsage = (MessageBody) msg.obj;
      if (listener != null) {
        switch (msg.what) {
        case ImageInformationLoaderTask.MESSAGE_IMAGE_INFORMATION:
          listener.onImageInformationLoaded(messsage.mGalleryObject, (ImageInformation) messsage.mObject);

          break;
        case ImageInformationLoaderTask.MESSAGE_IMAGE_COMMENTS:
          listener.onImageCommetsLoaded(messsage.mGalleryObject, (List<GalleryObjectComment>) messsage.mObject);
          break;

        case ImageInformationLoaderTask.MESSAGE_IMAGE_TAGS:
          listener.onImageTagsLoaded(messsage.mGalleryObject, (List<String>) messsage.mObject);
          break;

        }
      }
    }
  }

  private final InternHandler mResponseHandler = new InternHandler();
  private final WebGallery mWebGallery;
  private final ImageCache mCache;
  private final WeakReference<ImageInformationLoaderTaskListener> mListener;

  public ImageInformationLoaderTask(ImageInformationLoaderTaskListener listener, WebGallery webGallery, ImageCache cache) {
    super(1);
    mThreadName = "ImageInformationLoaderTask";
    mListener = new WeakReference<ImageInformationLoaderTaskListener>(listener);
    mWebGallery = webGallery;
    mCache = cache;
  }

  public void load(GalleryObject galleryObject) {
    if (galleryObject == null) {
      return;
    }
    Log.d(CLASS_TAG, String.format("enqueuing %s for loading of ImageInformations", galleryObject));
    enqueueTask(galleryObject);
  }

  @Override
  protected Void doInBackground(GalleryObject galleryObject) {
    ImageInformation imageInformation = loadImageInformations(galleryObject);

    Message message = mResponseHandler.obtainMessage(MESSAGE_IMAGE_INFORMATION, new MessageBody(galleryObject, imageInformation));
    message.sendToTarget();

    if (!isCancelled()) {
      try {
        Log.d(CLASS_TAG, String.format("Try to load tags for %s", galleryObject));

        List<String> tags = mWebGallery.getDisplayObjectTags(galleryObject, null);

        message = mResponseHandler.obtainMessage(MESSAGE_IMAGE_TAGS, new MessageBody(galleryObject, tags));
        message.sendToTarget();
      } catch (Exception e) {
        Log.e(CLASS_TAG, String.format("Something goes wrong while loading tags for %s", galleryObject), e);
      }
    }

    if (!isCancelled()) {
      try {
        Log.d(CLASS_TAG, String.format("Try to load comments for %s", galleryObject));
        List<GalleryObjectComment> comments = mWebGallery.getDisplayObjectComments(galleryObject, null);

        message = mResponseHandler.obtainMessage(MESSAGE_IMAGE_COMMENTS, new MessageBody(galleryObject, comments));
        message.sendToTarget();
      } catch (Exception e) {
        Log.e(CLASS_TAG, String.format("Something goes wrong while comments tags for %s", galleryObject), e);
      }
    }

    return null;
  }

  private ImageInformation loadImageInformations(GalleryObject galleryObject) {
    ImageInformation imageInformation = new ImageInformation();

    imageInformation.mTitle = galleryObject.getTitle();
    imageInformation.mUploadDate = galleryObject.getDateUploaded();

    File cachedFile = mCache.getFile(galleryObject.getImage().getUniqueId());
    try {
      Log.d(CLASS_TAG, String.format("Try to decoding ExifInformations from %s", galleryObject));
      ExifInterface exif = new ExifInterface(cachedFile.getAbsolutePath());

      imageInformation.mExifCreateDate = exif.getAttribute(ExifInterface.TAG_DATETIME);
      imageInformation.mExifAperture = exif.getAttribute(ExifInterface.TAG_APERTURE);
      imageInformation.mExifIso = exif.getAttribute(ExifInterface.TAG_ISO);
      imageInformation.mExifModel = exif.getAttribute(ExifInterface.TAG_MODEL);
      imageInformation.mExifMake = exif.getAttribute(ExifInterface.TAG_MAKE);

      try {
        imageInformation.mExifExposure = 1.0f / Float.parseFloat(exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));

      } catch (Exception e) {
        imageInformation.mExifExposure = 0f;
      }

      try {
        imageInformation.mExifFlash = exif.getAttributeInt(ExifInterface.TAG_FLASH, 0);
      } catch (Exception e) {
        imageInformation.mExifFlash = 0;
      }

      try {
        imageInformation.mExifFocalLength = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0);
      } catch (Exception e) {
        imageInformation.mExifFocalLength = 0;
      }

      try {
        int whiteBalance = exif.getAttributeInt(ExifInterface.TAG_WHITE_BALANCE, 0);
        if (whiteBalance == ExifInterface.WHITEBALANCE_AUTO) {
          imageInformation.mExifWhiteBalance = WhiteBalance.Auto;
        } else {
          imageInformation.mExifWhiteBalance = WhiteBalance.Manual;
        }
      } catch (Exception e) {
        imageInformation.mExifWhiteBalance = WhiteBalance.Manual;
      }

      imageInformation.mExifGpsAvailable = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF) != null;
      imageInformation.mExifGpsLat = parseDegMinSec(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
      imageInformation.mExifGpsLong = parseDegMinSec(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
      imageInformation.mExifHeight = parseHeight(exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE)); 
    } catch (Exception e) {
      Log.e(CLASS_TAG, String.format("Something goes wrong while decoding ExifInformations from %s", galleryObject), e);
    }
    return imageInformation;
  }

  private float parseHeight(String tagGpsValue) {
    try {
      String[] values = tagGpsValue.split("/");
      return Float.parseFloat(values[0]) / Float.parseFloat(values[1]);
    } catch (Exception e) {
      return 0;
    }
  }

  private DegMinSec parseDegMinSec(String gpsValue) {
    try {
      String[] values = gpsValue.split("[,/]");
      float deg = Float.parseFloat(values[0]) / Float.parseFloat(values[1]);
      float min = Float.parseFloat(values[2]) / Float.parseFloat(values[3]);
      float sec = Float.parseFloat(values[4]) / Float.parseFloat(values[5]);
      return new DegMinSec(deg, min, sec);
    } catch (Exception e) {
      return new DegMinSec(0, 0, 0);
    }
  }

  @Override
  protected void onPostExecute(GalleryObject galleryObject, Void result) {
    // TODO Auto-generated method stub

  }

  public boolean isLoading(GalleryObject galleryObject) {
    boolean isActive = isActive(galleryObject);
    boolean isEnqueued = isEnqueued(galleryObject);
    Log.d(CLASS_TAG, String.format("isActive: %s isEnqueued %s", isActive, isEnqueued));
    return isActive || isEnqueued;
  }

  public void cancel(GalleryObject galleryObject, boolean waitForCancel) throws InterruptedException {
    if (isEnqueued(galleryObject)) {
      removeEnqueuedTask(galleryObject);
    } else if (isActive(galleryObject)) {
      cancelCurrentTask(waitForCancel);
    }
  }
}