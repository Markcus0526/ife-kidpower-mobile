package org.unicefkidpower.kid_power.View.CustomControls.KPHCreditCardEntry.internal;

import android.widget.EditText;

import org.unicefkidpower.kid_power.View.CustomControls.KPHCreditCardEntry.fields.CreditEntryFieldBase;
import org.unicefkidpower.kid_power.View.CustomControls.KPHCreditCardEntry.library.CardType;

/**
 * contract for delegate
 * <p/>
 * TODO gut this delegate business
 */
public interface CreditCardFieldDelegate {
	// When the card type is identified
	void onCardTypeChange(CardType type);

	void onCreditCardNumberValid(String remainder);

	void onExpirationDateValid(String remainder);

	// Image should flip to back for security code
	void onSecurityCodeValid(String remainder);

	void onZipCodeValid();

	void onBadInput(EditText field);

	void focusOnField(CreditEntryFieldBase field, String initialValue);

	void focusOnPreviousField(CreditEntryFieldBase field);
}
