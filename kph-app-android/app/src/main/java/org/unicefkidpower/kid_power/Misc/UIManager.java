package org.unicefkidpower.kid_power.Misc;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import org.unicefkidpower.kid_power.Application.KPHApplication;
import org.unicefkidpower.kid_power.R;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by donal_000 on 1/8/2015.
 */
public class UIManager {
	private final String			TAG					= "UIManager";

	private static UIManager		sharedInstance		= null;
	protected Context				contextInstance		= null;
	public static Toast				gToast				= null;


	public static UIManager initialize(Context context) {
		if (sharedInstance == null)
			sharedInstance = new UIManager(context);
		return sharedInstance;
	}


	public static UIManager sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new UIManager(KPHApplication.sharedInstance().getApplicationContext());
		return sharedInstance;
	}


	private UIManager(Context context) {
		contextInstance = context;
	}


	public void dismissProgressDialog(Object dlg) {
		if (dlg == null || !(dlg instanceof ProgressDialog))
			return;

		ProgressDialog progressDialog = (ProgressDialog) dlg;
		if (progressDialog.isShowing())
			progressDialog.dismiss();
	}


	public void showToastMessage(Context context, String message) {
		if (gToast == null || gToast.getView().getWindowVisibility() != View.VISIBLE) {
			gToast = Toast.makeText((context == null) ? contextInstance : context, message, Toast.LENGTH_LONG);
			gToast.setGravity(Gravity.CENTER, 0, 0);
			gToast.show();
		}
	}


	public Drawable loadAssetDrawableFromResourceName(String assetPath) {
		if (assetPath == null || assetPath.length() == 0) {
			if (assetPath == null)
				Logger.log(TAG, "assetPath is null");
			else
				Logger.log(TAG, "assetPath is empty");

			return null;
		}


		Drawable drawable = null;

		try {
			String assetPathWebP;
			if (assetPath.length() <= 5) {
				Logger.log(TAG, "assetPath is shorter than 5 chars : " + assetPath);
				assetPathWebP = assetPath + ".webp";
			} else {
				if (assetPath.toLowerCase().endsWith(".webp")) {
					Logger.log(TAG, "assetPath ends with .webp : " + assetPath);
					assetPathWebP = assetPath;
				} else if (assetPath.toLowerCase().endsWith(".jpg") ||
						assetPath.toLowerCase().endsWith(".png")) {
					Logger.log(TAG, "assetPath ends with .jpg or .png : " + assetPath);
					assetPathWebP = assetPath.substring(0, assetPath.length() - 4) + ".webp";
				} else {
					assetPathWebP = assetPath + ".webp";
				}
			}

			Logger.log(TAG, "assetPath for WebP : " + assetPathWebP);

			InputStream inputStream = KPHUtils.sharedInstance().getApplicationContext().getAssets().open(assetPathWebP);
			drawable = Drawable.createFromStream(inputStream, null);
		} catch (IOException webpEx) {
			Logger.log(TAG, webpEx.getMessage() == null ? "No asset file" : webpEx.getMessage());

			try {
				String assetPathPNG;
				if (assetPath.length() <= 4) {
					Logger.log(TAG, "assetPath is shorter than 4 chars : " + assetPath);
					assetPathPNG = assetPath + ".png";
				} else {
					if (assetPath.toLowerCase().endsWith(".png")) {
						Logger.log(TAG, "assetPath ends with .png : " + assetPath);
						assetPathPNG = assetPath;
					} else if (assetPath.toLowerCase().endsWith(".jpg")) {
						Logger.log(TAG, "assetPath ends with .jpg : " + assetPath);
						assetPathPNG = assetPath.substring(0, assetPath.length() - 4) + ".png";
					} else if (assetPath.toLowerCase().endsWith(".webp")) {
						Logger.log(TAG, "assetPath ends with .webp : " + assetPath);
						assetPathPNG = assetPath.substring(0, assetPath.length() - 5) + ".png";
					} else {
						assetPathPNG = assetPath + ".png";
					}
				}

				Logger.log(TAG, "assetPath for PNG : " + assetPathPNG);

				InputStream inputStream = KPHUtils.sharedInstance().getApplicationContext().getAssets().open(assetPathPNG);
				drawable = Drawable.createFromStream(inputStream, null);
			} catch (IOException pngEx) {
				// No asset for PNG. Try JPG asset
				Logger.log(TAG, pngEx.getMessage() == null ? "No asset file" : pngEx.getMessage());

				try {
					String assetPathJPG;
					if (assetPath.length() <= 4) {
						Logger.log(TAG, "assetPath is shorter than 4 chars : " + assetPath);
						assetPathJPG = assetPath + ".jpg";
					} else {
						if (assetPath.toLowerCase().endsWith(".jpg")) {
							Logger.log(TAG, "assetPath ends with .jpg : " + assetPath);
							assetPathJPG = assetPath;
						} else if (assetPath.toLowerCase().endsWith(".png")) {
							Logger.log(TAG, "assetPath ends with .png : " + assetPath);
							assetPathJPG = assetPath.substring(0, assetPath.length() - 4) + ".jpg";
						} else if (assetPath.toLowerCase().endsWith(".webp")) {
							Logger.log(TAG, "assetPath ends with .webp : " + assetPath);
							assetPathJPG = assetPath.substring(0, assetPath.length() - 5) + ".jpg";
						} else {
							assetPathJPG = assetPath + ".jpg";
						}
					}

					Logger.log(TAG, "assetPath for JPG : " + assetPathJPG);

					InputStream inputStream = KPHUtils.sharedInstance().getApplicationContext().getAssets().open(assetPathJPG);
					drawable = Drawable.createFromStream(inputStream, null);
				} catch (IOException jpgEx) {
					Logger.log(TAG, jpgEx.getMessage() == null ? "No asset file" : jpgEx.getMessage());
				}
			}
		}

		return drawable;
	}


	public String getDrawableResourceNameWithScreenDensitySuffix(long missionId, String resourceName) {
		if (resourceName == null)
			return null;

		if (missionId != 15) {
			return resourceName + "_" + ResolutionSet.getScreenDensityString(contextInstance);
		} else {
			return resourceName + "_" + ResolutionSet.RESOLUTION_XXXHDPI;
		}
	}


	/**
	 * Show and hide with fade in and out animation
	 *
	 * @param view View to be animated
	 */
	public void showToastAnimation(final View view) {
		final Runnable fadeOutRunnable = new Runnable() {
			@Override
			public void run() {
				Animation fadeOutAnimation = AnimationUtils.loadAnimation(contextInstance, R.anim.fade_out);
				fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {}
					@Override
					public void onAnimationEnd(Animation animation) {
						view.setVisibility(View.INVISIBLE);
					}
					@Override
					public void onAnimationRepeat(Animation animation) {}
				});
				view.startAnimation(fadeOutAnimation);
			}
		};

		Animation fadeInAnimation = AnimationUtils.loadAnimation(contextInstance, R.anim.fade_in);
		fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {}
			@Override
			public void onAnimationEnd(Animation animation) {
				new Handler().postDelayed(fadeOutRunnable, 1500);
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
		});
		view.startAnimation(fadeInAnimation);
	}


	public void setBackgroundDrawable(View view, Drawable drawable) {
		int sdk = Build.VERSION.SDK_INT;
		if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
			view.setBackgroundDrawable(drawable);
		} else {
			view.setBackground(drawable);
		}
	}

	public Drawable getImageDrawable(int resId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			return contextInstance.getResources().getDrawable(resId, contextInstance.getTheme());
		} else {
			return contextInstance.getResources().getDrawable(resId);
		}
	}

	public int getColor(int resId) {
		if (Build.VERSION.SDK_INT < 23) {
			return contextInstance.getResources().getColor(resId);
		} else {
			return contextInstance.getColor(resId);
		}
	}

	public Drawable getDrawable(int resId) {
		if (Build.VERSION.SDK_INT < 21) {
			return contextInstance.getResources().getDrawable(resId);
		} else {
			return contextInstance.getResources().getDrawable(resId, null);
		}
	}
}
