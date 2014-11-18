package fr.castorflex.android.smoothprogressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

/**
 * This is a copy of the ContentLoadingProgressBar from the support library, but extends
 * SmoothProgressBar.
 */
public class ContentLoadingSmoothProgressBar extends SmoothProgressBar {

	private static final int DEFAULT_SHOW_TIME = 1500; // ms
	private static final int DEFAULT_DELAY = 300; // ms

	private long mShowTime;
	private long mStartTime = -1;

	private boolean mPostedHide = false;

	private boolean mPostedShow = false;

	private boolean mDismissed = false;

	private final Runnable mDelayedHide = new Runnable() {

		@Override
		public void run() {
			mPostedHide = false;
			mStartTime = -1;
			ContentLoadingSmoothProgressBar.super.hide();
		}
	};

	private final Runnable mDelayedShow = new Runnable() {

		@Override
		public void run() {
			mPostedShow = false;
			if (!mDismissed) {
				mStartTime = System.currentTimeMillis();
				ContentLoadingSmoothProgressBar.super.show();
			}
		}
	};


	public ContentLoadingSmoothProgressBar(Context context) {
		this(context, null);
	}

	public ContentLoadingSmoothProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ContentLoadingSmoothProgressBar);
			mShowTime = a.getInteger(R.styleable.ContentLoadingSmoothProgressBar_clspb_min_show_time,
					DEFAULT_SHOW_TIME);
			a.recycle();
		}
	}

	public void setShowTime(long showTime) {
		this.mShowTime = showTime;
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
	@Override
	public void hide() {
		mDismissed = true;
		removeShowCallback();
		long diff = System.currentTimeMillis() - mStartTime;
		if (diff >= mShowTime || mStartTime == -1) {
			// The progress spinner has been shown long enough
			// OR was not shown yet. If it wasn't shown yet,
			// it will just never be shown.
			super.hide();
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

	@Override
	public void show() {
		show(DEFAULT_DELAY);
	}

	@Override
	public void instantHide() {
		super.instantHide();
		removeCallbacks();
	}
}
