package org.unicefkidpower.schools.adapter;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by Ruifeng Shi on 10/7/2016.
 */

public class ActivityLifecycleAdapter implements Application.ActivityLifecycleCallbacks {
	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

	@Override
	public void onActivityStarted(Activity activity) {}

	@Override
	public void onActivityResumed(Activity activity) {}

	@Override
	public void onActivityPaused(Activity activity) {}

	@Override
	public void onActivityStopped(Activity activity) {}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

	@Override
	public void onActivityDestroyed(Activity activity) {}

}
