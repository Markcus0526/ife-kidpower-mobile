package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Misc.UIManager;


public class KPHVideoLayout extends KPHVideoView implements MediaPlayer.OnPreparedListener {
	private static final String			TAG = "KPHVideoLayout";

	// Counter
	protected static final Handler		TIME_THREAD = new Handler();
	private static final String			BROADCAST_ALARM_ALERT_ACTION = "com.android.deskclock.ALARM_ALERT";
	private static final String			BROADCAST_ALARM_SNOOZE_ACTION = "com.android.deskclock.ALARM_SNOOZE";
	private static final String			BROADCAST_ALARM_DISMISS_ACTION = "com.android.deskclock.ALARM_DISMISS";
	private static final String			BROADCAST_ALARM_DONE_ACTION = "com.android.deskclock.ALARM_DONE";

	// Control views
	protected View						videoControlsView;
	protected SeekBar					seekBar;
	protected ImageButton				imgplay;
	protected ImageButton				imgfullscreen;
	protected TextView					textTotal, textElapsed;
	protected OnTouchListener			touchListener;
	protected BroadcastReceiver			recvCallStateRinging, recvCallStateIdle;


	protected Runnable updateTimeRunnable = new Runnable() {
		public void run() {
			updateCounter();
			TIME_THREAD.postDelayed(this, 200);
		}
	};

	public KPHVideoLayout(Context context) {
		super(context);
	}

	public KPHVideoLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public KPHVideoLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	@Override
	protected void init() {
		Log.d(TAG, "init");

		super.init();

		LayoutInflater inflater = (LayoutInflater) context.getSystemService
				(Context.LAYOUT_INFLATER_SERVICE);
		this.videoControlsView = inflater.inflate(R.layout.layout_videocontrols, null);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT
		);
		params.addRule(ALIGN_PARENT_BOTTOM);
		addView(videoControlsView, params);

		this.seekBar = (SeekBar) this.videoControlsView.findViewById(R.id.vcv_seekbar);
		this.imgfullscreen = (ImageButton) this.videoControlsView.findViewById(R.id.vcv_img_fullscreen);
		this.imgplay = (ImageButton) this.videoControlsView.findViewById(R.id.vcv_img_play);
		this.textTotal = (TextView) this.videoControlsView.findViewById(R.id.vcv_txt_total);
		this.textElapsed = (TextView) this.videoControlsView.findViewById(R.id.vcv_txt_elapsed);

		// We need to add it to show/hide the controls
		super.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				boolean bHandledHere = false;

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (videoControlsView != null) {
						if (videoControlsView.getVisibility() == View.VISIBLE)
							hideControls();
						else
							showControls();

						bHandledHere = true;
					}
				}

				if (touchListener != null) {
					return touchListener.onTouch(KPHVideoLayout.this, event) || bHandledHere;
				}

				return bHandledHere;
			}
		});

		this.imgplay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedPlay();
			}
		});
		this.imgfullscreen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedFullScreen();
			}
		});
		this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				Log.d(TAG, "onProgressChanged");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				stopCounter();
				Log.d(TAG, "onStartTrackingTouch");
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = seekBar.getProgress();
				seekTo(progress);
				Log.d(TAG, "onStopTrackingTouch");
			}
		});

		// Start controls invisible. Make it visible when it is prepared
		this.videoControlsView.setVisibility(View.GONE);

		recvCallStateRinging = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				pause();
			}
		};

		IntentFilter disruptFilter = new IntentFilter(KPHBroadcastSignals.BROADCAST_SIGNAL_CALLSTATE_RINGING);
		disruptFilter.addAction(BROADCAST_ALARM_ALERT_ACTION);
		LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(
				recvCallStateRinging, disruptFilter
		);

		recvCallStateIdle = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				start();
			}
		};

		IntentFilter resumeFilter = new IntentFilter(BROADCAST_ALARM_DISMISS_ACTION);
		resumeFilter.addAction(BROADCAST_ALARM_DONE_ACTION);
		resumeFilter.addAction(BROADCAST_ALARM_SNOOZE_ACTION);
		LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(
				recvCallStateIdle, resumeFilter
		);

		final ViewTreeObserver vto = getViewTreeObserver();
		vto.addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						Point screenSize = ResolutionSet.getScreenSize(context, false);
						Rect scrollBounds = new Rect(0, 0, screenSize.x, screenSize.y);

						if (!getLocalVisibleRect(scrollBounds)) {
							try {
								if (mediaPlayer != null && isPlaying())
									pause();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
		);
	}

	protected void startCounter() {
		Log.d(TAG, "startCounter");

		TIME_THREAD.postDelayed(updateTimeRunnable, 200);
	}

	protected void stopCounter() {
		Log.d(TAG, "stopCounter");

		TIME_THREAD.removeCallbacks(updateTimeRunnable);
	}

	protected void updateCounter() {
		int elapsed = getCurrentPosition();
		// getCurrentPosition is a little bit buggy :(
		if (elapsed > 0 && elapsed < getDuration()) {
			seekBar.setProgress(elapsed);

			elapsed = Math.round(elapsed / 1000.f);
			long s = elapsed % 60;
			long m = (elapsed / 60) % 60;
			long h = (elapsed / (60 * 60)) % 24;

			if (h > 0)
				textElapsed.setText(String.format("%d:%02d:%02d", h, m, s));
			else
				textElapsed.setText(String.format("%02d:%02d", m, s));
		}
	}

	@Override
	public void setOnTouchListener(View.OnTouchListener l) {
		touchListener = l;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(TAG, "onCompletion");

		super.onCompletion(mp);
		stopCounter();
		updateCounter();
		updateControls();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		boolean result = super.onError(mp, what, extra);
		stopCounter();
		updateControls();
		return result;
	}


	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (getCurrentState() == State.END) {
			Log.d(TAG, "onDetachedFromWindow END");
			stopCounter();
		}


		if (recvCallStateRinging != null) {
			LocalBroadcastManager.getInstance(context.getApplicationContext()).unregisterReceiver(recvCallStateRinging);
		}

		if (recvCallStateIdle != null) {
			LocalBroadcastManager.getInstance(context.getApplicationContext()).unregisterReceiver(recvCallStateIdle);
		}
	}

	@Override
	protected void tryToPrepare() {
		Log.d(TAG, "tryToPrepare");
		super.tryToPrepare();

		if (getCurrentState() == State.PREPARED || getCurrentState() == State.STARTED) {
			int total = getDuration();
			if (total > 0) {
				seekBar.setMax(total);
				seekBar.setProgress(0);

				total = total / 1000;
				long s = total % 60;
				long m = (total / 60) % 60;
				long h = (total / (60 * 60)) % 24;
				if (h > 0) {
					textElapsed.setText("00:00:00");
					textTotal.setText(String.format("%d:%02d:%02d", h, m, s));
				} else {
					textElapsed.setText("00:00");
					textTotal.setText(String.format("%02d:%02d", m, s));
				}
			}
		}
	}

	@Override
	public void start() throws IllegalStateException {
		Log.d(TAG, "start");

		if (mediaPlayer == null)
			return;

		try {
			if (!isPlaying()) {
				super.start();
				startCounter();
				updateControls();
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void pause() throws IllegalStateException {
		Log.d(TAG, "pause");

		if (mediaPlayer == null)
			return;

		try {
			if (isPlaying()) {
				stopCounter();
				super.pause();
				updateControls();
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void reset() {
		Log.d(TAG, "reset");

		super.reset();
		stopCounter();
		updateControls();
	}

	@Override
	public void stop() throws IllegalStateException {
		Log.d(TAG, "stop");

		super.stop();
		stopCounter();
		updateControls();
	}

	protected void updateControls() {
		Drawable icon;
		if (getCurrentState() == State.STARTED) {
			icon = UIManager.sharedInstance().getImageDrawable(R.drawable.fvl_selector_pause);
		} else {
			icon = UIManager.sharedInstance().getImageDrawable(R.drawable.fvl_selector_play);
		}
		UIManager.sharedInstance().setBackgroundDrawable(imgplay, icon);
	}

	public void hideControls() {
		Log.d(TAG, "hideControls");
		if (videoControlsView != null) {
			videoControlsView.setVisibility(View.GONE);
		}
	}

	public void showControls() {
		Log.d(TAG, "showControls");
		if (videoControlsView != null) {
			videoControlsView.setVisibility(View.VISIBLE);
		}
	}


	public void onClickedPlay() {
		if (mediaPlayer == null)
			return;

		if (isPlaying()) {
			pause();
		} else {
			start();
		}
	}

	public void onClickedFullScreen() {
		if (mediaPlayer == null)
			return;

		if (isPlaying()) {
			pause();
			fullscreen();
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					start();
				}
			});
		} else {
			fullscreen();
		}

		hideControls();
	}
}
