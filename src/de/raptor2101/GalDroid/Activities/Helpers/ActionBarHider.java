package de.raptor2101.GalDroid.Activities.Helpers;

import android.app.ActionBar;
import android.os.Handler;

public class ActionBarHider implements Runnable {

    private final ActionBar mActionBar;
    private final Handler mHandler;

    public ActionBarHider(ActionBar actionBar) {
	mActionBar = actionBar;
	mHandler = new Handler();
    }

    public boolean isShowing() {
	return mActionBar.isShowing();
    }

    public void show() {
	mActionBar.show();
	mHandler.postDelayed(this, 10000);
    }

    public void hide() {
	mActionBar.hide();
	mHandler.removeCallbacks(this);
    }

    public void run() {
	mActionBar.hide();
    }
}
