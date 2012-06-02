package de.raptor2101.GalDroid.WebGallery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObjectComment;

public class ImageInformation {
    public enum WhiteBalance {
	Auto, Manual
    }

    public String mTitle;
    public Date mUploadDate;

    public String mExifCreateDate;
    public String mExifAperture;
    public float mExifExposure;
    public int mExifFlash;
    public WhiteBalance mExifWhiteBalance;
    public String mExifIso;
    public String mExifModel;
    public String mExifMake;
    public double mExifFocalLength;

    public DegMinSec mExifGpsLat;
    public DegMinSec mExifGpsLong;
    public float mExifHeight;
}
