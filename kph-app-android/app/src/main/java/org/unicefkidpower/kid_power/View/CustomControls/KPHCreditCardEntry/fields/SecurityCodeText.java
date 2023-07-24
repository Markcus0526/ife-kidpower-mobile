package org.unicefkidpower.kid_power.View.CustomControls.KPHCreditCardEntry.fields;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;

import org.unicefkidpower.kid_power.View.CustomControls.KPHCreditCardEntry.internal.CreditCardUtil;
import org.unicefkidpower.kid_power.View.CustomControls.KPHCreditCardEntry.library.CardType;
import org.unicefkidpower.kid_power.R;

public class SecurityCodeText extends CreditEntryFieldBase {
	private CardType		type;
	private int				length;

	public SecurityCodeText(Context context) {
		super(context);
		init();
	}

	public SecurityCodeText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SecurityCodeText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	protected  void init() {
		super.init();
		setHint("CVV");
	}


	/* TextWatcher Implementation Methods */
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}


	public void afterTextChanged(Editable s) {
		if (type == null) {
			this.removeTextChangedListener(this);
			this.setText("");
			this.addTextChangedListener(this);
		}
	}


	public void formatAndSetText(String s) {
		setText(s);
	}


	public void textChanged(CharSequence s, int start, int before, int count) {
		if (type != null) {
			if (s.length() >= length) {
				setValid(true);
				String remainder = null;
				if (s.length() > length()) remainder = String.valueOf(s).substring(length);
				this.removeTextChangedListener(this);
				setText(String.valueOf(s).substring(0, length));
				this.addTextChangedListener(this);
				delegate.onSecurityCodeValid(remainder);
			} else {
				setValid(false);
			}
		}
	}


	@SuppressWarnings("unused")
	public CardType getType() {
		return type;
	}


	public void setType(CardType type) {
		this.type = type;
		this.length = CreditCardUtil.securityCodeValid(type);
	}


	@Override
	public String helperText() {
		return context.getString(R.string.SecurityCodeHelp);
	}
}
