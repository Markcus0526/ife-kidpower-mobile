package org.unicefkidpower.schools.helper;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Created by xiaoxuehan on 13/12/14.
 */
public class EditTextBackEvent extends AppCompatEditText {
	private EditTextImeBackListener mOnImeBack;

	public EditTextBackEvent(Context context) {
		super(context);
	}

	public EditTextBackEvent(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EditTextBackEvent(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onKeyPreIme(int keycode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
			if (mOnImeBack != null)
				mOnImeBack.onImeBack(this, this.getText().toString());
		}
		return super.dispatchKeyEvent(event);
	}

	public void setOnEditTextImeBackListener(EditTextImeBackListener listener) {
		mOnImeBack = listener;
	}

	public interface EditTextImeBackListener {
		public void onImeBack(EditTextBackEvent ctrl, String text);
	}
}
