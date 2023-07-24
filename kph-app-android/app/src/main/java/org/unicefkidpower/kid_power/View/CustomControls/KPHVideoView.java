package org.unicefkidpower.kid_power.View.CustomControls;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.io.IOException;

/**
 * Created by Ruifeng Shi on 11/7/2015.
 */
public class KPHVideoView extends RelativeLayout implements
		TextureView.SurfaceTextureListener,
		OnPreparedListener,
		OnErrorListener,
		OnSeekCompleteListener,
		OnCompletionListener {
	/**
	 * Debug Tag for use logging debug output to LogCat
	 */
	private static final String			TAG = "FullscreenVideoView";

	protected Context				context;
	protected Activity				activity;

	protected MediaPlayer			mediaPlayer;
	protected SurfaceTexture		surfaceTexture;
	protected TextureView			textureView;
	protected RelativeLayout		rlTextureView;
	protected boolean				videoIsReady, textureIsReady;
	protected boolean				detachedByFullscreen;
	protected State					currentState;
	protected State					lastState; // Tells onSeekCompletion what to do

	protected View					loadingView;

	// Controla o fullscreen
	protected ViewGroup						parentView;
	protected ViewGroup.LayoutParams		currentLayoutParams;

	protected boolean						isFullscreen;
	protected boolean						shouldAutoplay;
	protected int							initialConfigOrientation;
	protected int							initialMovieWidth, initialMovieHeight;

	protected OnErrorListener				errorListener;
	protected OnPreparedListener			preparedListener;
	protected OnSeekCompleteListener		seekCompleteListener;
	protected OnCompletionListener			completionListener;
	protected OnAudioFocusChangeListener	focusChangeListener;


	public KPHVideoView(Context context) {
		super(context);
		this.context = context;

		init();
	}

	public KPHVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		init();
	}

	public KPHVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;

		init();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Log.d(TAG, "onSaveInstanceState");
		return super.onSaveInstanceState();
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		Log.d(TAG, "onRestoreInstanceState");
		super.onRestoreInstanceState(state);
	}

	@Override
	protected void onDetachedFromWindow() {
		Log.d(TAG, "onDetachedFromWindow - detachedByFullscreen: " + detachedByFullscreen);

		super.onDetachedFromWindow();

		if (!detachedByFullscreen) {
			if (mediaPlayer != null) {
				this.mediaPlayer.setOnPreparedListener(null);
				this.mediaPlayer.setOnErrorListener(null);
				this.mediaPlayer.setOnSeekCompleteListener(null);
				this.mediaPlayer.setOnCompletionListener(null);

				if (mediaPlayer.isPlaying())
					mediaPlayer.stop();
				mediaPlayer.release();
				mediaPlayer = null;
			}
			videoIsReady = false;
			textureIsReady = false;
			currentState = State.END;
		}

		detachedByFullscreen = false;
	}

	@Override
	synchronized public void onPrepared(MediaPlayer mp) {
		Log.d(TAG, "onPrepared called");
		videoIsReady = true;
		tryToPrepare();
	}

	/**
	 * Restore the last State before seekTo()
	 *
	 * @param mp the MediaPlayer that issued the seek operation
	 */
	@Override
	public void onSeekComplete(MediaPlayer mp) {
		Log.d(TAG, "onSeekComplete");

		stopLoading();
		if (lastState != null) {
			switch (lastState) {
				case STARTED: {
					start();
					break;
				}
				case PAUSED: {
					pause();
					break;
				}
				case PLAYBACKCOMPLETED: {
					currentState = State.PLAYBACKCOMPLETED;
					break;
				}
				case PREPARED: {
					currentState = State.PREPARED;
					break;
				}
			}
		}

		if (this.seekCompleteListener != null)
			this.seekCompleteListener.onSeekComplete(mp);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.d(TAG, "onCompletion");
		if (!this.mediaPlayer.isLooping())
			this.currentState = State.PLAYBACKCOMPLETED;
		else
			start();

		if (this.completionListener != null) {
			this.completionListener.onCompletion(mp);
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.d(TAG, "onError called - " + what + " - " + extra);

		stopLoading();
		this.currentState = State.ERROR;

		if (this.errorListener != null)
			return this.errorListener.onError(mp, what, extra);
		return false;
	}

	/**
	 * Initializes the UI
	 */
	protected void init() {
		if (isInEditMode())
			return;

		this.shouldAutoplay = false;
		this.currentState = State.IDLE;
		this.isFullscreen = false;
		this.initialConfigOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		this.setBackgroundColor(Color.BLACK);

		this.mediaPlayer = new MediaPlayer();

		this.textureView = new TextureView(context);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
		);
		layoutParams.addRule(CENTER_IN_PARENT);
		this.textureView.setLayoutParams(layoutParams);

		this.rlTextureView = new RelativeLayout(context);
		RelativeLayout.LayoutParams lpSurfaceViewLayout = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
		);
		this.rlTextureView.setLayoutParams(lpSurfaceViewLayout);
		this.rlTextureView.addView(this.textureView);
		addView(this.rlTextureView);

		this.textureView.setSurfaceTextureListener(this);

		this.loadingView = new ProgressBar(context);
		layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(CENTER_IN_PARENT);
		this.loadingView.setLayoutParams(layoutParams);
		addView(this.loadingView);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            this.addOnLayoutChangeListener(new OnLayoutChangeListener() {
//                @Override
//                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                    Log.i(TAG, "onLayoutChange");
//
//                    Handler handler = new Handler(Looper.getMainLooper());
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            resize();
//                        }
//                    });
//                }
//            });
//        }
	}

	/**
	 * Calls prepare() method of MediaPlayer
	 */
	protected void prepare() throws IllegalStateException {
		startLoading();

		this.videoIsReady = false;
		this.initialMovieHeight = -1;
		this.initialMovieWidth = -1;

		this.mediaPlayer.setOnPreparedListener(this);
		this.mediaPlayer.setOnErrorListener(this);
		this.mediaPlayer.setOnSeekCompleteListener(this);
		this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		this.currentState = State.PREPARING;
		this.mediaPlayer.prepareAsync();
	}

	/**
	 * Try to call state PREPARED
	 * Only if SurfaceView is already created and MediaPlayer is prepared
	 * Video is loaded and is ok to play.
	 */
	protected void tryToPrepare() {
		if (this.textureIsReady && this.videoIsReady) {
			if (this.mediaPlayer != null) {
				this.initialMovieWidth = this.mediaPlayer.getVideoWidth();
				this.initialMovieHeight = this.mediaPlayer.getVideoHeight();
			}

			resize();
			stopLoading();
			currentState = State.PREPARED;

			if (shouldAutoplay)
				start();

			if (this.preparedListener != null)
				this.preparedListener.onPrepared(mediaPlayer);
		}
	}

	protected void startLoading() {
		this.loadingView.setVisibility(View.VISIBLE);
	}

	protected void stopLoading() {
		this.loadingView.setVisibility(View.GONE);
	}

	/**
	 * Get the current {@link KPHVideoView.State}.
	 *
	 * @return
	 */
	synchronized public State getCurrentState() {
		return currentState;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
		this.initialConfigOrientation = activity.getRequestedOrientation();
	}

	public void resize() {
		if (initialMovieHeight == -1 || initialMovieWidth == -1)
			return;

		View currentParent = (View) this.textureView.getParent();
		if (currentParent != null) {
			float videoProportion = (float) initialMovieWidth / (float) initialMovieHeight;

			int screenWidth = currentParent.getWidth();
			int screenHeight = currentParent.getHeight();
			float screenProportion = (float) screenWidth / (float) screenHeight;

			int newWidth, newHeight;
			if (videoProportion > screenProportion) {
				newWidth = screenWidth;
				newHeight = (int) ((float) screenWidth / videoProportion);
			} else {
				newWidth = (int) (videoProportion * (float) screenHeight);
				newHeight = screenHeight;
			}

			ViewGroup.LayoutParams lp = textureView.getLayoutParams();
			if (lp.width != newWidth || lp.height != newHeight) {
				lp.width = newWidth;
				lp.height = newHeight;
				textureView.setLayoutParams(lp);
			}

			Log.d(TAG, "Resizing: initialMovieWidth: " + initialMovieWidth + " - initialMovieHeight: " + initialMovieHeight);
			Log.d(TAG, "Resizing: screenWidth: " + screenWidth + " - screenHeight: " + screenHeight);
		}
	}

	public boolean isShouldAutoplay() {
		return shouldAutoplay;
	}

	/**
	 * Tells application that it should begin playing as soon as buffering
	 * is ok
	 *
	 * @param shouldAutoplay If true, call start() method when getCurrentState() == PREPARED. Default is false.
	 */
	public void setShouldAutoplay(boolean shouldAutoplay) {
		this.shouldAutoplay = shouldAutoplay;
	}

	/**
	 * Toggles view to fullscreen mode
	 * It saves currentState and calls pause() method.
	 * When fullscreen is finished, it calls the saved currentState before pause()
	 * In practice, it only affects STARTED state.
	 * If currenteState was STARTED when fullscreen() is called, it calls start() method
	 * after fullscreen() has ended.
	 */
	public void fullscreen() throws IllegalStateException {
		if (mediaPlayer == null) throw new RuntimeException("Media Player is not initialized");

		boolean wasPlaying = mediaPlayer.isPlaying();
		if (wasPlaying)
			pause();

		if (!isFullscreen) {
			isFullscreen = true;

			if (activity != null)
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

			View rootView = getRootView();
			View v = rootView.findViewById(android.R.id.content);
			ViewParent viewParent = getParent();
			if (viewParent instanceof ViewGroup) {
				if (parentView == null)
					parentView = (ViewGroup) viewParent;

				// Prevents MediaPlayer to became invalidated and released
				detachedByFullscreen = true;

				// Saves the last state (LayoutParams) of view to restore after
				currentLayoutParams = this.getLayoutParams();

				parentView.removeView(this);
			} else
				Log.e(TAG, "Parent View is not a ViewGroup");

			if (v instanceof ViewGroup) {
				((ViewGroup) v).addView(this);
			} else
				Log.e(TAG, "RootView is not a ViewGroup");
		} else {
			isFullscreen = false;

			if (activity != null)
				activity.setRequestedOrientation(initialConfigOrientation);

			ViewParent viewParent = getParent();
			if (viewParent instanceof ViewGroup) {
				// Check if parent view is still available
				boolean parentHasParent = false;
				if (parentView != null && parentView.getParent() != null) {
					parentHasParent = true;
					detachedByFullscreen = true;
				}

				((ViewGroup) viewParent).removeView(this);
				if (parentHasParent) {
					parentView.addView(this);
					this.setLayoutParams(currentLayoutParams);
				}
			}
		}

		resize();

		if (wasPlaying && mediaPlayer != null)
			start();
	}

	public boolean isFullscreen() {
		return isFullscreen && mediaPlayer != null;
	}

	/**
	 * Exits from full screen mode
	 */
	public void exitFullScreen() {
		if (mediaPlayer == null)
			return;

		boolean wasPlaying = mediaPlayer.isPlaying();
		if (wasPlaying)
			pause();

		if (!isFullscreen) {
			return;
		} else {
			isFullscreen = false;

			if (activity != null)
				activity.setRequestedOrientation(initialConfigOrientation);

			ViewParent viewParent = getParent();
			if (viewParent instanceof ViewGroup) {
				// Check if parent view is still available
				boolean parentHasParent = false;
				if (parentView != null && parentView.getParent() != null) {
					parentHasParent = true;
					detachedByFullscreen = true;
				}

				((ViewGroup) viewParent).removeView(this);
				if (parentHasParent) {
					parentView.addView(this);
					this.setLayoutParams(currentLayoutParams);
				}
			}
		}

		resize();

		if (wasPlaying && mediaPlayer != null)
			start();
	}

	/**
	 * {@link MediaPlayer} method (getCurrentPosition)
	 * http://developer.android.com/reference/android/media/MediaPlayer.html#getCurrentPosition%28%29
	 */
	public int getCurrentPosition() {
		if (mediaPlayer != null)
			return mediaPlayer.getCurrentPosition();
		else throw new RuntimeException("Media Player is not initialized");
	}

	/**
	 * {@link MediaPlayer} method (getDuration)
	 * http://developer.android.com/reference/android/media/MediaPlayer.html#getDuration%28%29
	 */
	public int getDuration() {
		if (mediaPlayer != null)
			return mediaPlayer.getDuration();
		else throw new RuntimeException("Media Player is not initialized");
	}

	/**
	 * {@link MediaPlayer} method (getVideoHeight)
	 * http://developer.android.com/reference/android/media/MediaPlayer.html#getVideoHeight%28%29
	 */
	public int getVideoHeight() {
		if (mediaPlayer != null)
			return mediaPlayer.getVideoHeight();
		else throw new RuntimeException("Media Player is not initialized");
	}

	/**
	 * {@link MediaPlayer} method (getVideoWidth)
	 * http://developer.android.com/reference/android/media/MediaPlayer.html#getVideoWidth%28%29
	 */
	public int getVideoWidth() {
		if (mediaPlayer != null)
			return mediaPlayer.getVideoWidth();
		else throw new RuntimeException("Media Player is not initialized");
	}

	/**
	 * {@link MediaPlayer} method (isLooping)
	 * http://developer.android.com/reference/android/media/MediaPlayer.html#isLooping%28%29
	 */
	public boolean isLooping() {
		if (mediaPlayer != null)
			return mediaPlayer.isLooping();
		else throw new RuntimeException("Media Player is not initialized");
	}

	public void setLooping(boolean looping) {
		if (mediaPlayer != null)
			mediaPlayer.setLooping(looping);
		else throw new RuntimeException("Media Player is not initialized");
	}

	/**
	 * {@link MediaPlayer} method (isPlaying)
	 * http://developer.android.com/reference/android/media/MediaPlayer.html#isLooping%28%29
	 */
	public boolean isPlaying() throws IllegalStateException {
		if (mediaPlayer != null)
			return mediaPlayer.isPlaying();
		else throw new RuntimeException("Media Player is not initialized");
	}

	/**
	 * {@link MediaPlayer} method (pause)
	 * http://developer.android.com/reference/android/media/MediaPlayer.html#pause%28%29
	 */
	public void pause() throws IllegalStateException {
		Log.d(TAG, "pause");
		if (mediaPlayer != null) {
			currentState = State.PAUSED;
			mediaPlayer.pause();
		} else throw new RuntimeException("Media Player is not initialized");
	}

	/**
	 * {@link MediaPlayer} method (reset)
	 * http://developer.android.com/reference/android/media/MediaPlayer.html#reset%28%29
	 */
	public void reset() {
		Log.d(TAG, "reset");

		if (mediaPlayer != null) {
			currentState = State.IDLE;
			mediaPlayer.reset();
		} else throw new RuntimeException("Media Player is not initialized");
	}

	/**
	 * {@link MediaPlayer} method (start)
	 * http://developer.android.com/reference/android/media/MediaPlayer.html#start%28%29
	 */
	public void start() throws IllegalStateException {
		Log.d(TAG, "start");

		if (mediaPlayer != null) {
			stopOtherAudioSources();

			mediaPlayer.setWakeMode(activity, PowerManager.FULL_WAKE_LOCK);
			currentState = State.STARTED;
			mediaPlayer.setOnCompletionListener(this);
			mediaPlayer.start();
		} else throw new RuntimeException("Media Player is not initialized");
	}

	/**
	 * {@link MediaPlayer} method (stop)
	 * http://developer.android.com/reference/android/media/MediaPlayer.html#stop%28%29
	 */
	public void stop() throws IllegalStateException {
		Log.d(TAG, "stop");

		if (mediaPlayer != null) {
			currentState = State.STOPPED;
			mediaPlayer.stop();
		} else throw new RuntimeException("Media Player is not initialized");
	}

	/**
	 * {@link MediaPlayer} method (seekTo)
	 * http://developer.android.com/reference/android/media/MediaPlayer.html#stop%28%29
	 * <p/>
	 * It calls pause() method before calling MediaPlayer.seekTo()
	 *
	 * @param msec the offset in milliseconds from the start to seek to
	 * @throws IllegalStateException if the internal player engine has not been initialized
	 */
	public void seekTo(int msec) throws IllegalStateException {
		Log.d(TAG, "seekTo = " + msec);

		if (mediaPlayer != null) {
			// No live streaming
			if (mediaPlayer.getDuration() > -1 && msec <= mediaPlayer.getDuration()) {
				lastState = currentState;
				pause();
				mediaPlayer.seekTo(msec);

				startLoading();
			}
		} else throw new RuntimeException("Media Player is not initialized");
	}

	public void setOnCompletionListener(OnCompletionListener l) {
		if (mediaPlayer != null)
			this.completionListener = l;
		else throw new RuntimeException("Media Player is not initialized");
	}

	public void setOnErrorListener(OnErrorListener l) {
		if (mediaPlayer != null)
			errorListener = l;
		else throw new RuntimeException("Media Player is not initialized");
	}

	public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
		if (mediaPlayer != null)
			mediaPlayer.setOnBufferingUpdateListener(l);
		else throw new RuntimeException("Media Player is not initialized");
	}

	public void setOnInfoListener(OnInfoListener l) {
		if (mediaPlayer != null)
			mediaPlayer.setOnInfoListener(l);
		else throw new RuntimeException("Media Player is not initialized");
	}

	public void setOnSeekCompleteListener(OnSeekCompleteListener l) {
		if (mediaPlayer != null)
			this.seekCompleteListener = l;
		else throw new RuntimeException("Media Player is not initialized");
	}

	public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener l) {
		if (mediaPlayer != null)
			mediaPlayer.setOnVideoSizeChangedListener(l);
		else throw new RuntimeException("Media Player is not initialized");
	}

	public void setOnPreparedListener(OnPreparedListener l) {
		if (mediaPlayer != null)
			this.preparedListener = l;
		else throw new RuntimeException("Media Player is not initialized");
	}

	public void setVolume(float leftVolume, float rightVolume) {
		if (mediaPlayer != null)
			mediaPlayer.setVolume(leftVolume, rightVolume);
		else throw new RuntimeException("Media Player is not initialized");
	}

	/**
	 * VideoView method (setVideoPath)
	 */
	public void setVideoPath(String path) throws IOException, IllegalStateException, SecurityException, IllegalArgumentException, RuntimeException {
		if (mediaPlayer != null) {
			if (currentState != State.IDLE)
				throw new IllegalStateException("FullscreenVideoView Invalid State: " + currentState);

			mediaPlayer.setDataSource(path);

			currentState = State.INITIALIZED;
			prepare();
		} else throw new RuntimeException("Media Player is not initialized");
	}

	/**
	 * VideoView method (setVideoURI)
	 */
	public void setVideoURI(Uri uri) throws IOException, IllegalStateException, SecurityException, IllegalArgumentException, RuntimeException {
		if (mediaPlayer != null) {
			if (currentState != State.IDLE)
				throw new IllegalStateException("FullscreenVideoView Invalid State: " + currentState);
			mediaPlayer.setDataSource(context, uri);
			currentState = State.INITIALIZED;
			prepare();
		} else throw new RuntimeException("Media Player is not initialized");
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		Log.d(TAG, "onSurfaceTextureAvailable called = " + currentState);

		mediaPlayer.setSurface(new Surface(surface));

		// If is not prepared yet - tryToPrepare()
		if (!textureIsReady) {
			textureIsReady = true;
			if (currentState != State.PREPARED &&
					currentState != State.PAUSED &&
					currentState != State.STARTED &&
					currentState != State.PLAYBACKCOMPLETED)
				tryToPrepare();
		}
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

		Log.d(TAG, "surfaceDestroyed called");
		if (mediaPlayer != null && mediaPlayer.isPlaying())
			mediaPlayer.pause();

		textureIsReady = false;

		return false;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		Log.d(TAG, "onSurfaceTextureUpdated called");
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				resize();
			}
		});
	}

	public void stopOtherAudioSources() {
		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		int result = audioManager.requestAudioFocus(
				focusChangeListener,
				AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN
		);
	}

	/**
	 * States of MediaPlayer
	 * http://developer.android.com/reference/android/media/MediaPlayer.html
	 */
	public enum State {
		IDLE,
		INITIALIZED,
		PREPARED,
		PREPARING,
		STARTED,
		STOPPED,
		PAUSED,
		PLAYBACKCOMPLETED,
		ERROR,
		END
	}

	public class OnAudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

		@Override
		public void onAudioFocusChange(int focusChange) {
			AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			switch (focusChange) {

				case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
					// Lower the volume while ducking.
					mediaPlayer.setVolume(0.2f, 0.2f);
					break;
				case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
					pause();
					break;

				case (AudioManager.AUDIOFOCUS_LOSS):
					stop();
//                    ComponentName component =new ComponentName(context, MediaControlReceiver.class);
//                    am.unregisterMediaButtonEventReceiver(component);
					break;

				case (AudioManager.AUDIOFOCUS_GAIN):
					// Return the volume to normal and resume if paused.
					mediaPlayer.setVolume(1f, 1f);
					mediaPlayer.start();
					break;
				default:
					break;
			}
		}
	}
}
