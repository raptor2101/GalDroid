package de.raptor2101.GalDroid.WebGallery.Gallery3.JSON;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObjectComment;

public class CommentEntity implements GalleryObjectComment {
    public enum CommentState {
	Published;
    }

    private final String mId;

    private final Date mCreateDate;
    private final Date mUpdateDate;
    private final String mMessage;
    private final CommentState mCommentState;

    private final String mAuthorId;
    private String mAuthorEmail = null;
    private String mAuthorName = null;
    private String mAuthorUrl = null;

    public CommentEntity(JSONObject jsonObject) throws JSONException {
	jsonObject = jsonObject.getJSONObject("entity");
	mId = jsonObject.getString("id");
	mMessage = jsonObject.getString("text");

	long msElapsed = jsonObject.getLong("created") * 1000;
	mCreateDate = new Date(msElapsed);

	msElapsed = jsonObject.getLong("updated") * 1000;
	mUpdateDate = new Date(msElapsed);

	String state = jsonObject.getString("state");
	if (state.equals("published")) {
	    mCommentState = CommentState.Published;
	} else {
	    // TODO weitere states auslesen
	    mCommentState = CommentState.Published;
	}

	mAuthorId = jsonObject.getString("author_id");
	// TODO prüfen ob das wirklich null wird
	if (mAuthorId == null) {
	    mAuthorEmail = jsonObject.getString("guest_email");
	    mAuthorName = jsonObject.getString("guest_name");
	    mAuthorUrl = jsonObject.getString("guest_url");
	}
    }

    public String getId() {
	return mId;
    }

    public Date getCreateDate() {
	return mCreateDate;
    }

    public Date getUpdateDate() {
	return mUpdateDate;
    }

    public String getMessage() {
	return mMessage;
    }

    public CommentState getState() {
	return mCommentState;
    }

    public String getAuthorId() {
	return mAuthorId;
    }

    public String getAuthorEmail() {
	return mAuthorEmail;
    }

    public String getAuthorName() {
	return mAuthorName;
    }

    public String getAuthorUrl() {
	return mAuthorUrl;
    }

    public boolean isAuthorInformationLoaded() {
	return mAuthorName != null;
    }
}
