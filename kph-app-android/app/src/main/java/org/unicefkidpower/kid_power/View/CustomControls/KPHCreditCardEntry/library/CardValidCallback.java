package org.unicefkidpower.kid_power.View.CustomControls.KPHCreditCardEntry.library;

public interface CardValidCallback {
	/**
	 * called when data entry is complete and the card is valid
	 *
	 * @param card the validated card
	 */
	void cardValid(CreditCard card);
}
