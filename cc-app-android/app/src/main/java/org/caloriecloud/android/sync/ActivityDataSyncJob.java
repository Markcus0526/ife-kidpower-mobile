package org.caloriecloud.android.sync;

import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;

public class ActivityDataSyncJob extends Job {

    public static final String TAG = "ActivityDataSyncJob";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Log.d(TAG, "Starting job...");
        return new ActivityDataSyncEngine(getContext()).syncActivityData() ? Result.SUCCESS : Result.FAILURE;
    }

}
