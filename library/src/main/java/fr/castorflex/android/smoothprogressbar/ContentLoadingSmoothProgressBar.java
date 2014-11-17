package fr.castorflex.android.smoothprogressbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * This is a copy of the ContentLoadingProgressBar from the support library, but extends
 * SmoothProgressBar.
 */
public class ContentLoadingSmoothProgressBar extends SmoothProgressBar {

	private static final int DEFAULT_SHOW_TIME = 500; // ms
	private static final int DEFAULT_DELAY = 500; // ms

	private long mShowTime = DEFAULT_SHOW_TIME;
	private long mStartTime = -1;

	private boolean mPostedHide = false;

	private boolean mPostedShow = false;

	private boolean mDismissed = false;

	private final Runnable mDelayedHide = new Runnable() {

		@Override
		public void run() {
			mPostedHide = false;
			mStartTime = -1;
			setVisibility(View.GONE);
		}
	};

	private final Runnable mDelayedShow = new Runnable() {

		@Override
		public void run() {
			mPostedShow = false;
			if (!mDismissed) {
				mStartTime = System.currentTimeMillis();
				setVisibility(View.VISIBLE);
			}
		}
	};

	public ContentLoadingSmoothProgressBar(Context context) {
		this(context, null);
	}

	public ContentLoadingSmoothProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
	}

	public void setShowTime(long ShowTime) {
		this.mShowTime = ShowTime;
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		removeCallbacks();
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		removeCallbacks();
	}

	private void removeCallbacks() {
		removeHideCallback();
		removeShowCallback();
	}

	private void removeShowCallback() {
		mPostedShow = false;
		removeCallbacks(mDelayedShow);
	}

	private void removeHideCallback() {
		mPostedHide = false;
		removeCallbacks(mDelayedHide);
	}

	/**
	 * Hide the progress view if it is visible. The progress view will not be
	 * hidden until it has been shown for at least a minimum show time. If the
	 * progress view was not yet visible, cancels showing the progress view.
	 */
	public void hide() {
		mDismissed = true;
		removeShowCallback();
		long diff = System.currentTimeMillis() - mStartTime;
		if (diff >= mShowTime || mStartTime == -1) {
			// The progress spinner has been shown long enough
			// OR was not shown yet. If it wasn't shown yet,
			// it will just never be shown.
			setVisibility(View.GONE);
		} else {
			// The progress spinner is shown, but not long enough,
			// so put a delayed message in to hide it when its been
			// shown long enough.
			if (!mPostedHide) {
				postDelayed(mDelayedHide, mShowTime - diff);
				mPostedHide = true;
			}
		}
	}

	/**
	 * Show the progress view after waiting for a minimum delay. If
	 * during that time, hide() is called, the view is never made visible.
	 */
	public void show(int startDelay) {
		// Reset the start time.
		mStartTime = -1;
		mDismissed = false;
		removeHideCallback();
		if (!mPostedShow) {
			postDelayed(mDelayedShow, startDelay);
			mPostedShow = true;
		}
	}

	public void show() {
		show(DEFAULT_DELAY);
	}
}
