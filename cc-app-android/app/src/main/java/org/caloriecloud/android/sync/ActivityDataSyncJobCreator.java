package org.caloriecloud.android.sync;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class ActivityDataSyncJobCreator implements JobCreator{

    @Override
    public Job create(String tag) {
        switch(tag) {
            case ActivityDataSyncJob.TAG:
                return new ActivityDataSyncJob();
            default:
                return null;
        }
    }
}
