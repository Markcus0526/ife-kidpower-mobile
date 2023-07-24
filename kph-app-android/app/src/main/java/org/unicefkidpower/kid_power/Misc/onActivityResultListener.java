package org.unicefkidpower.kid_power.Misc;

import android.content.Intent;

/**
 * Created by Dayong Li on 11/11/2015.
 * UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public interface onActivityResultListener {
	boolean onSharedActivityResult(final int requestCode, final int resultCode, final Intent data);
}
