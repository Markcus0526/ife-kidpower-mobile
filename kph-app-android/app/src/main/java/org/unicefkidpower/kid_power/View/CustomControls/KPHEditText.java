package org.unicefkidpower.kid_power.View.CustomControls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import org.unicefkidpower.kid_power.R;

/**
 * Created by Ruifeng Shi on 9/1/2015.
 */
public class KPHEditText extends AppCompatEditText {
	private static final String TAG = "EditText";
	private onKeyListener keyListener = null;

	public KPHEditText(Context context) {
		super(context);
		initKeyListener();
	}

	public KPHEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		setCustomFont(context, attrs);
		initKeyListener();
	}

	public KPHEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setCustomFont(context, attrs);
		initKeyListener();
	}

	private void setCustomFont(Context context, AttributeSet attributeSet) {
		TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KPHEditText);
		String customFont = typedArray.getString(R.styleable.KPHEditText_customEditTextFont);
		setCustomFont(context, customFont);
		typedArray.recycle();

		initKeyListener();
	}

	private void initKeyListener() {
		this.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE ||
						actionId == EditorInfo.IME_ACTION_NEXT ||
						(event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					if (keyListener != null)
						keyListener.onKeyDownEnter();
				}

				return false;
			}
		});
	}

	public boolean setCustomFont(Context context, String asset) {
		Typeface typeface;

		try {
			typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + asset);
		} catch (Exception e) {
			Log.e(TAG, "Could not getV1 typeface: " + e.getMessage());
			return false;
		}

		setTypeface(typeface);

		return true;
	}

	public void setOnKeyListener(onKeyListener listener) {
		this.keyListener = listener;
	}


	public interface onKeyListener {
		void onKeyDownEnter();
	}
}
