package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.ResolutionSet;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHButton;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;

import java.io.IOException;


/**
 * Created by Ruifeng Shi on 8/24/2015.
 */
public class WelcomeActivity extends SuperActivity {
	private static final String		TAG				= "WelcomeActivity";

	private int						position		= -1;
	private MediaPlayer				mp				= null;
	private SurfaceHolder			holder			= null;
	private SurfaceView				vwSurface		= null;
	private LinearLayout			placeholder		= null;

	private RelativeLayout			videoLayout		= null;
	private RelativeLayout			infoLayout		= null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		ClickableSpan learnMore = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onInfoClicked();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
			}
		};
		ClickableSpan logIn = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onLoginClicked();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
			}
		};
		ClickableSpan createNew = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				onCreateAccountClicked();
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
			}
		};


		SpannableString sContent;
		if (!KPHUserService.sharedInstance().loadSignInFlag()) {
			((KPHTextView) findViewById(R.id.txtHeader)).setText(getString(R.string.start_saving_lives_with_a_nunicef_kid_power_band));
			findViewById(R.id.txtHeader2).setVisibility(View.VISIBLE);
			((KPHButton) findViewById(R.id.btnCreateLogin)).setText(getString(R.string.create_new_user));

			findViewById(R.id.btnCreateLogin).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onCreateAccountClicked();
				}
			});

			sContent = new SpannableString(getString(R.string.or_have_account));
			// Learn More Underline
			sContent.setSpan(learnMore, 0, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			// Log in
			sContent.setSpan(logIn, 32, 38, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		} else {
			((KPHTextView) findViewById(R.id.txtHeader)).setText(getString(R.string.welcome_back));
			findViewById(R.id.txtHeader2).setVisibility(View.GONE);
			((KPHButton) findViewById(R.id.btnCreateLogin)).setText(getString(R.string.log_in));

			findViewById(R.id.btnCreateLogin).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onLoginClicked();
				}
			});

			sContent = new SpannableString(getString(R.string.or_create));
			// Learn More underline
			sContent.setSpan(learnMore, 0, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			// Create a New account string
			sContent.setSpan(createNew, 15, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		KPHTextView txtSubAction = (KPHTextView) findViewById(R.id.txtSubAction);
		txtSubAction.setText(sContent);
		txtSubAction.setMovementMethod(LinkMovementMethod.getInstance());
		txtSubAction.setHighlightColor(Color.TRANSPARENT);

		// Video Player
		vwSurface = (SurfaceView) findViewById(R.id.vwSurface);
		holder = vwSurface.getHolder();
		holder.addCallback(new Callback() {
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				Logger.log(TAG, "SurfaceHolder::surfaceCreated : called");
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				Logger.log(TAG, "SurfaceHolder::surfaceChanged : called");
				playVideo(KPHConstants.WELCOME_VIDEO_PATH);
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				Logger.log(TAG, "SurfaceHolder::surfaceDestroyed : called");
			}
		});

		placeholder = (LinearLayout) findViewById(R.id.llVideoPlaceholder);


		// Control sizes arrangement
		{
			videoLayout = (RelativeLayout) findViewById(R.id.video_layout);
			infoLayout = (RelativeLayout) findViewById(R.id.main_content_layout);

			Point screenSize = ResolutionSet.getScreenSize(WelcomeActivity.this, false);

			int screenWidth = screenSize.x;
			int screenHeight = screenSize.y - ResolutionSet.getStatusBarHeight(WelcomeActivity.this);

			RelativeLayout.LayoutParams videoLayoutParams = (RelativeLayout.LayoutParams) videoLayout.getLayoutParams();
			if (videoLayoutParams == null) {
				videoLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, screenWidth);
			} else {
				videoLayoutParams.height = screenWidth;
			}
			videoLayout.setLayoutParams(videoLayoutParams);

			final int remainingHeight = screenHeight - screenWidth;
			infoLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					if (infoLayout.getHeight() == 0) {
						return;
					}

					if (infoLayout.getHeight() < remainingHeight) {
						ViewGroup.LayoutParams layoutParams = infoLayout.getLayoutParams();
						if (layoutParams == null) {
							layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, remainingHeight);
						} else {
							layoutParams.height = remainingHeight;
						}
						infoLayout.setLayoutParams(layoutParams);
					}

					infoLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
			});
		}
	}



	@Override
	protected void onDestroy() {
		try {
			if (mp != null && mp.isPlaying()) {
				mp.stop();
				mp.release();
				mp.setOnPreparedListener(null);
				mp.setOnErrorListener(null);
				mp.setOnSeekCompleteListener(null);
				mp.setOnCompletionListener(null);
			}
		} catch (Exception e) {
			Logger.error(TAG, "onDestroy : %s", e.getMessage());
		}

		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (position >= 0) {
			mp.seekTo(position);
			mp.start();

			position = -1;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		try {
			if (mp != null && mp.isPlaying()) {
				mp.pause();
				position = mp.getCurrentPosition();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Called when "Create an account" button has been clicked.
	 */
	private void onCreateAccountClicked() {
		Bundle extra = new Bundle();
		extra.putInt(OnboardingActivity.EXTRA_FROM_ACTIVITY, OnboardingActivity.FROM_WELCOME_ACTIVITY);
		pushNewActivityAnimated(OnboardingActivity.class, extra);
		popOverCurActivityAnimated();
	}

	/**
	 * Called when "Log in" button has been clicked
	 */
	private void onLoginClicked() {
		Bundle extra = new Bundle();
		extra.putBoolean(LoginActivity.IS_FROM_WELCOME_ACTIVITY, true);
		pushNewActivityAnimated(LoginActivity.class, extra);
		popOverCurActivityAnimated();
	}

	/**
	 * Called when Information button has been clicked
	 */
	private void onInfoClicked() {
		pushNewActivityAnimated(AboutActivity.class);
	}


	private void playVideo(final String path) {
		if (path == null)
			return;

		try {
			Logger.log(TAG, "playVideo : play %s", path);

			mp = new MediaPlayer();
			mp.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Logger.error(TAG, "MediaPlayer::onError");

					if (mp != null) {
						mp.stop();
						mp.release();
					}

					return false;
				}
			});
			mp.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
				@Override
				public void onBufferingUpdate(MediaPlayer mp, int percent) {
					Logger.log(TAG, "MediaPlayer::onBufferingUpdate : called");
				}
			});
			mp.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					Logger.log(TAG, "MediaPlayer::onCompletion : called");
				}
			});
			mp.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					Logger.log(TAG, "MediaPlayer::onPrepared : called");
					placeholder.setVisibility(View.GONE);
					mp.start();
				}
			});
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mp.setLooping(true);
			mp.setDisplay(holder);

			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						AssetFileDescriptor afd = getAssets().openFd(path);
						mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
						mp.prepareAsync();
					} catch (IOException e) {
						Logger.error(TAG, "playVideo : runnable error %s", e.getMessage());
					}
				}
			};
			new Thread(r).start();
		} catch (Exception e) {
			Logger.error(TAG, "playVideo : error %s", e.getMessage());
			if (mp != null) {
				mp.stop();
				mp.release();
			}
		}
	}
}
