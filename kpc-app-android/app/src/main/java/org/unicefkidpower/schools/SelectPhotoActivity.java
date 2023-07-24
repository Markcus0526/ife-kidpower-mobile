package org.unicefkidpower.schools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.flurry.android.FlurryAgent;

import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.ui.ClipView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: RuiFeng.Shi
 * Date: 15-01-26
 * To change this template use File | Settings | File Templates.
 */
public class SelectPhotoActivity extends BaseActivity implements View.OnClickListener {
	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	public static int REQCODE_TAKE_PHOTO = 0;
	public static int REQCODE_SELECT_GALLERY = 1;
	public static String szRetCode = "RET";
	public static String szRetPath = "PATH";
	public static int nRetSuccess = 1;
	public static int nRetCancelled = 0;
	public static int nRetFail = -1;
	private static Uri fileUri = null;
	// These matrices will be used to move and zoom image
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	// Remember some things for zooming
	private PointF start = new PointF();
	private PointF mid = new PointF();
	private float oldDist = 1f;
	private int mode = NONE;
	// Take photo buttons
	private Button buttonTake = null, buttonSelImage = null, buttonCancel = null;
	// Image crop controls
	private RelativeLayout maskLayout = null;
	private ClipView clipView = null;
	private ImageView imgSel = null;
	private RelativeLayout rlConfirm = null, rlCancel = null;
	private Button buttonImageConfirm, buttonImageCancel = null;
	private String photo_path = "";
	private Uri photo_uri = null;
	private Bitmap bmpPhoto = null;
	private boolean isFromCamera = true;
	private String resPath = "";
	private boolean needCrop = true;
	private int statusBarHeight = 0;
	private int titleBarHeight = 0;

	public static int getImageOrientation(String imagePath) {
		int nAngle = 0;
		try {
			File imageFile = new File(imagePath);
			ExifInterface exif = new ExifInterface(
					imageFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_270:
					nAngle = 270;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					nAngle = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_90:
					nAngle = 90;
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return nAngle;
	}

	// Rotate image clockwise
	public static Bitmap rotateImage(String pathToImage, int nAngle) {
		// 2. rotate matrix by post concatination
		Matrix matrix = new Matrix();
		matrix.postRotate(nAngle);

		// 3. create Bitmap from rotated matrix
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inDither = true;
		Bitmap sourceBitmap = BitmapFactory.decodeFile(pathToImage, options);
		return Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight(), matrix, true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_selphoto);

		FlurryAgent.onStartSession(this, "Select Photo Activity");

		initControls();
	}

	public void initControls() {
		buttonTake = (Button) findViewById(R.id.btn_take_photo);
		buttonTake.setOnClickListener(this);
		buttonSelImage = (Button) findViewById(R.id.btn_sel_image);
		buttonSelImage.setOnClickListener(this);
		buttonCancel = (Button) findViewById(R.id.btn_cancel);
		buttonCancel.setOnClickListener(this);

		maskLayout = (RelativeLayout) findViewById(R.id.crop_layout);
		maskLayout.setVisibility(View.GONE);

		imgSel = (ImageView) findViewById(R.id.img_sel);
		imgSel.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ImageView view = (ImageView) v;

				// Handle touch events here...
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_DOWN:
						savedMatrix.set(matrix);
						start.set(event.getX(), event.getY());

						mode = DRAG;
						break;
					case MotionEvent.ACTION_POINTER_DOWN:
						oldDist = spacing(event);

						if (oldDist > 10f) {
							savedMatrix.set(matrix);
							midPoint(mid, event);
							mode = ZOOM;
						}
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_POINTER_UP:
						mode = NONE;

						break;
					case MotionEvent.ACTION_MOVE:
						if (mode == DRAG) {
							matrix.set(savedMatrix);
							matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
						} else if (mode == ZOOM) {
							float newDist = spacing(event);
							if (newDist > 10f) {
								matrix.set(savedMatrix);
								float scale = newDist / oldDist;
								matrix.postScale(scale, scale, mid.x, mid.y);
							}
						}
						break;
				}

				view.setImageMatrix(matrix);
				return true;
			}
		});

		clipView = (ClipView) findViewById(R.id.clipview);

		rlConfirm = (RelativeLayout) findViewById(R.id.rlConfirm);
		rlConfirm.setOnClickListener(this);
		rlCancel = (RelativeLayout) findViewById(R.id.rlCancel);
		rlCancel.setOnClickListener(this);
		buttonImageConfirm = (Button) findViewById(R.id.buttonConfirm);
		buttonImageConfirm.setOnClickListener(this);
		buttonImageCancel = (Button) findViewById(R.id.buttonCancel);
		buttonImageCancel.setOnClickListener(this);
	}

	@Override
	protected boolean isUseEvent() {
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();    //To change body of overridden methods use File | Settings | File Templates.

		if (resPath == null || resPath.equals(""))
			return;

		if (isFromCamera)
			correctBitmap(resPath);

		if (!needCrop) {
			finishActivityWithImage();
		} else {
			try {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = false;
				options.inPreferredConfig = Bitmap.Config.RGB_565;
				options.inDither = true;

				File file = new File(resPath);
				if (file.exists()) {
					bmpPhoto = BitmapFactory.decodeFile(resPath, options);
					//Log.d(TAG, "h:w--->"+bmpPhoto.getHeight()+":"+bmpPhoto.getWidth());
					Point screenSize = new Point();
					screenSize = ResolutionSet.getScreenSize(SelectPhotoActivity.this, false, true);
					float scale = 0.75F * (float) screenSize.x / (float) bmpPhoto.getWidth();
					matrix.setScale(1F, 1F);
					matrix.postScale(scale, scale);
					matrix.postTranslate((float) screenSize.x / 8F, (float) screenSize.y * 0.25F - (float) bmpPhoto.getHeight() * scale * 0.25F);
					imgSel.setImageMatrix(matrix);
					imgSel.setImageBitmap(bmpPhoto);

					maskLayout.setVisibility(View.VISIBLE);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				maskLayout.setVisibility(View.GONE);
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
						getString(R.string.selectphoto_saveimage_failed),
						SelectPhotoActivity.this);
			}
		}
	}

	private void finishActivityWithImage() {
		Intent retIntent = new Intent();
		retIntent.putExtra(szRetCode, nRetSuccess);
		retIntent.putExtra(szRetPath, resPath);
		setResult(RESULT_OK, retIntent);
		finish();
	}

	/**
	 * Determine the space between the first two fingers
	 */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * Calculate the mid point of the first two fingers
	 */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	private void finishCrop() {
		Bitmap finalBmp = getBitmap();

		// Save image
		FileOutputStream out = null;
		try {
			if (!isFromCamera) {
				File file = getOutputPhotoFile();
				if (file == null)
					AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
							getString(R.string.selectphoto_cannot_take),
							SelectPhotoActivity.this);
				else
					resPath = file.getPath();
			}

			out = new FileOutputStream(resPath);
			finalBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		finishActivityWithImage();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (maskLayout.getVisibility() == View.VISIBLE) {
				maskLayout.setVisibility(View.GONE);
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK)
			return;

		if (requestCode == REQCODE_TAKE_PHOTO) {
			Uri photoUri = null;

			if (data == null)
				photoUri = fileUri;
			else
				photoUri = data.getData();

			try {
				if (photoUri != null) {
					String szPath = photoUri.getPath();
					if (szPath == null || szPath.equals("")) {
						AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
								getString(R.string.selectphoto_loadimage_failed),
								SelectPhotoActivity.this);
					} else {
						photo_path = szPath;
						photo_uri = null;
					}
				} else {
					photo_path = fileUri.getPath();
					photo_uri = null;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
						getString(R.string.selectphoto_takephoto_failed),
						SelectPhotoActivity.this);
			}
		} else if (requestCode == REQCODE_SELECT_GALLERY) {
			if (data != null) {
				Uri selImage = data.getData();
				if (selImage != null) {
					photo_path = "";
					photo_uri = selImage;
				}
			}
		}

		if (photo_path != null && !photo_path.equals("")) {
			resPath = photo_path;
			isFromCamera = true;
		} else if (photo_uri != null) {
			resPath = RealPathUtil.getRealPathFromURI(SelectPhotoActivity.this, photo_uri);
			isFromCamera = false;
		}
	}

	public void onTakePhoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File file = getOutputPhotoFile();
		if (file == null) {
			AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error),
					getString(R.string.selectphoto_cannot_take),
					SelectPhotoActivity.this);
		} else {
			fileUri = Uri.fromFile(file);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
			startActivityForResult(intent, REQCODE_TAKE_PHOTO);
		}
	}

	public void onSelImage() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, getString(R.string.selectphoto_title_selectpicture)), REQCODE_SELECT_GALLERY);
	}

	private File getOutputPhotoFile() {
		File retFile = null;
		try {
			retFile = new File(getApplicationContext().getFilesDir(), "IMG_" + "PHOTO" + ".JPG");
			//retFile = new File(getApplicationContext().getFilesDir(), "IMG_" + Long.toString(System.currentTimeMillis()) + ".JPG");
		} catch (Exception ex) {
			retFile = null;
		}

		return retFile;
	}

	private void cancelWithData() {
		Intent returnIntent = new Intent();
		setResult(RESULT_CANCELED, returnIntent);
		SelectPhotoActivity.this.finish();
	}

	private void correctBitmap(String szPath) {
		File oldFile = new File(szPath);
		if (oldFile.exists())
			return;

		int nAngle = getImageOrientation(szPath);
		if (nAngle == 0)                // Image is correct. No need to rotate
			return;

		Bitmap bmpRot = rotateImage(szPath, nAngle);
		FileOutputStream ostream = null;

		try {
			File file = new File(szPath);
			if (file.exists())
				file.delete();

			ostream = new FileOutputStream(file);
			bmpRot.compress(Bitmap.CompressFormat.JPEG, 50, ostream);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (ostream != null) {
				try {
					ostream.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private Bitmap getBitmap() {
		getBarHeight();
		Bitmap screenShoot = takeScreenShot();

		int width = clipView.getWidth();
		int height = clipView.getHeight();

		Bitmap finalBitmap = Bitmap.createBitmap(screenShoot,
				(width - height / 2) / 2,
				height / 4 + titleBarHeight + statusBarHeight,
				height / 2,
				height / 2);

		return finalBitmap;
	}

	private void getBarHeight() {
		Rect frame = new Rect();
		this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		statusBarHeight = frame.top;

		int contenttop = this.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
		titleBarHeight = contenttop - statusBarHeight;
	}

	private Bitmap takeScreenShot() {
		View view = this.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		return view.getDrawingCache();
	}

	@Override
	public void onClick(View v) {
		if (v == buttonTake) {
			onTakePhoto();
		} else if (v == buttonSelImage) {
			onSelImage();
		} else if (v == buttonCancel) {
			cancelWithData();
		} else if (v == rlConfirm) {
			finishCrop();
		} else if (v == rlCancel) {
			maskLayout.setVisibility(View.GONE);
			imgSel.setImageBitmap(null);
		} else if (v == buttonImageConfirm) {
			finishCrop();
		} else if (v == buttonImageCancel) {
			maskLayout.setVisibility(View.GONE);
			imgSel.setImageBitmap(null);
		}
	}


	public enum RealPathUtil {
		INSTANCE;

		public static String getRealPathFromURI(Context context, Uri uri) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				return getRealPathFromURI_BelowAPI11(context, uri);
			} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
				return getRealPathFromURI_API11to18(context, uri);
			} else {
				return getRealPathFromURI_API19(context, uri);
			}
		}

		@SuppressLint("NewApi")
		public static String getRealPathFromURI_API19(Context context, Uri uri) {
			String filePath = "";
			String wholeID = "";

			try {
				wholeID = DocumentsContract.getDocumentId(uri);
			} catch (Exception ex) {
				ex.printStackTrace();           // Android 4.4.2 can occur this exception.

				return getRealPathFromURI_API11to18(context, uri);
			}

			// Split at colon, use second item in the array
			String id = wholeID.split(":")[1];

			String[] column = {MediaStore.Images.Media.DATA};

			// where id is equal to
			String sel = MediaStore.Images.Media._ID + "=?";

			Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					column, sel, new String[]{id}, null);

			int columnIndex = cursor.getColumnIndex(column[0]);

			if (cursor.moveToFirst()) {
				filePath = cursor.getString(columnIndex);
			}

			cursor.close();
			return filePath;
		}


		@SuppressLint("NewApi")
		public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
			String[] proj = {MediaStore.Images.Media.DATA};
			String result = null;

			CursorLoader cursorLoader = new CursorLoader(
					context,
					contentUri, proj, null, null, null);
			Cursor cursor = cursorLoader.loadInBackground();

			if (cursor != null) {
				int column_index =
						cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				result = cursor.getString(column_index);
			}
			return result;
		}

		public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri) {
			String[] proj = {MediaStore.Images.Media.DATA};
			Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index
					= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
	}

}
