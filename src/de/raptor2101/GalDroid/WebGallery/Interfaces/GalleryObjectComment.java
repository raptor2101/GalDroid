package de.raptor2101.GalDroid.WebGallery.Interfaces;

import java.util.Date;

public interface GalleryObjectComment {
    public Date getCreateDate();

    public Date getUpdateDate();

    public String getMessage();

    public String getAuthorEmail();

    public String getAuthorName();

    public String getAuthorUrl();
}
