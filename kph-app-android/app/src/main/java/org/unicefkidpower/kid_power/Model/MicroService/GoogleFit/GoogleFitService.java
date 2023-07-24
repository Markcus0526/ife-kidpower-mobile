package org.unicefkidpower.kid_power.Model.MicroService.GoogleFit;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Model.MicroService.KPHAnalyticsService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.Structure.KPHDailyDetailData;
import org.unicefkidpower.kid_power.Model.Structure.KPHTracker;
import org.unicefkidpower.kid_power.Model.Structure.TrackerSyncResult;
import org.unicefkidpower.kid_power.Application.KPHApplication;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.OSDate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.client.Response;

/**
 * Created by Ruifeng Shi on 2/9/2017.
 */

public class GoogleFitService {
	private static final String 		TAG = "GoogleFitService";

	public static final int 			ACTION_NONE = -1;
	public static final int 			ACTION_CONNECT = 1;
	public static final int 			ACTION_SYNC = 2;

	private final long					TRACKER_CONNECT_TIME_LIMIT		= 48 * 60 * 60 * 1000;
	private final int 					SYNC_TIME_FRAGMENT				= 15 * 60 * 1000;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Error codes
	public static final int GOOGLE_FIT_ERROR_NONE								= ConnectionResult.SUCCESS;
	public static final int GOOGLE_FIT_ERROR_NOT_INITIALIZED					= -1;
	public static final int GOOGLE_FIT_ERROR_SUSPENDED							= -2;
	public static final int GOOGLE_FIT_ERROR_NO_SYNCED_DATA						= -3;
	public static final int GOOGLE_FIT_ERROR_API_UNAVAILABLE					= ConnectionResult.API_UNAVAILABLE;
	public static final int GOOGLE_FIT_ERROR_CANCELED							= ConnectionResult.CANCELED;
	public static final int GOOGLE_FIT_ERROR_DEVELOPER_ERROR					= ConnectionResult.DEVELOPER_ERROR;
	public static final int GOOGLE_FIT_ERROR_INTERNAL_ERROR						= ConnectionResult.INTERNAL_ERROR;
	public static final int GOOGLE_FIT_ERROR_INTERRUPTED						= ConnectionResult.INTERRUPTED;
	public static final int GOOGLE_FIT_ERROR_INVALID_ACCOUNT					= ConnectionResult.INVALID_ACCOUNT;
	public static final int GOOGLE_FIT_ERROR_LICENSE_CHECK_FAILED				= ConnectionResult.LICENSE_CHECK_FAILED;
	public static final int GOOGLE_FIT_ERROR_NETWORK_ERROR						= ConnectionResult.NETWORK_ERROR;
	public static final int GOOGLE_FIT_ERROR_RESOLUTION_REQUIRED				= ConnectionResult.RESOLUTION_REQUIRED;
	public static final int GOOGLE_FIT_ERROR_RESTRICTED_PROFILE					= ConnectionResult.RESTRICTED_PROFILE;
	public static final int GOOGLE_FIT_ERROR_SERVICE_DISABLED					= ConnectionResult.SERVICE_DISABLED;
	public static final int GOOGLE_FIT_ERROR_SERVICE_INVALID					= ConnectionResult.SERVICE_INVALID;
	public static final int GOOGLE_FIT_ERROR_SERVICE_MISSING					= ConnectionResult.SERVICE_MISSING;
	public static final int GOOGLE_FIT_ERROR_SERVICE_MISSING_PERMISSION			= ConnectionResult.SERVICE_MISSING_PERMISSION;
	public static final int GOOGLE_FIT_ERROR_SERVICE_UPDATING					= ConnectionResult.SERVICE_UPDATING;
	public static final int GOOGLE_FIT_ERROR_SERVICE_VERSION_UPDATE_REQUIRED	= ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED;
	public static final int GOOGLE_FIT_ERROR_SIGN_IN_FAILED						= ConnectionResult.SIGN_IN_FAILED;
	public static final int GOOGLE_FIT_ERROR_SIGN_IN_REQUIRED					= ConnectionResult.SIGN_IN_REQUIRED;
	public static final int GOOGLE_FIT_ERROR_TIMEOUT							= ConnectionResult.TIMEOUT;

	public static final int GOOGLE_FIT_ERROR_SUBSCRIPTION_NEEDS_OAUTH_PERMISSIONS		= FitnessStatusCodes.NEEDS_OAUTH_PERMISSIONS;
	public static final int GOOGLE_FIT_ERROR_SUBSCRIPTION_CONFLICTING_DATA_TYPE			= FitnessStatusCodes.CONFLICTING_DATA_TYPE;
	public static final int GOOGLE_FIT_ERROR_SUBSCRIPTION_INCONSISTENT_DATA_TYPE		= FitnessStatusCodes.INCONSISTENT_DATA_TYPE;
	public static final int GOOGLE_FIT_ERROR_SUBSCRIPTION_DATA_TYPE_NOT_FOUND			= FitnessStatusCodes.DATA_TYPE_NOT_FOUND;
	public static final int GOOGLE_FIT_ERROR_SUBSCRIPTION_APP_MISMATCH					= FitnessStatusCodes.APP_MISMATCH;
	public static final int GOOGLE_FIT_ERROR_SUBSCRIPTION_UNKNOWN_AUTH_ERROR			= FitnessStatusCodes.UNKNOWN_AUTH_ERROR;


	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// Google fit service callback
	private GoogleFitServiceCallbacks			serviceCallbacks = null;

	// Google api client, callbacks
	private FragmentActivity					activityReference = null;
	private GoogleApiClient						googleApiClient = null;
	private int									actionType;
	private Date								lastSyncDate;


	public GoogleFitService(FragmentActivity activity, int action, Date syncDate) {
		activityReference = activity;
		actionType = action;
		lastSyncDate = syncDate;

		googleApiClient = new GoogleApiClient.Builder(activityReference)
				.addApi(Fitness.RECORDING_API)
				.addApi(Fitness.HISTORY_API)
				.addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
				.addConnectionCallbacks(connectionCallbacks)
				.enableAutoManage(activityReference, connectionFailedListener)
				.build();
	}


	private boolean alreadyConnected = false;
	private GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
		@Override
		public void onConnected(@Nullable Bundle bundle) {
			if (alreadyConnected)
				return;

			alreadyConnected = true;

			Logger.log(TAG, "Google Fit Service connected");
			subscribe();
		}
		@Override
		public void onConnectionSuspended(int i) {
			if (serviceCallbacks != null)
				serviceCallbacks.onConnectionResult(GOOGLE_FIT_ERROR_SUSPENDED, activityReference.getString(R.string.google_fit_service_error_suspended));
		}
	};

	private GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
		@Override
		public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
			if (serviceCallbacks != null) {
				String errorMessage = "";
				switch (connectionResult.getErrorCode()) {
					case GOOGLE_FIT_ERROR_API_UNAVAILABLE:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_api_unavailable);
						break;
					case GOOGLE_FIT_ERROR_CANCELED:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_cancelled);
						break;
					case GOOGLE_FIT_ERROR_DEVELOPER_ERROR:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_developer_error);
						break;
					case GOOGLE_FIT_ERROR_INTERNAL_ERROR:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_internal_error);
						break;
					case GOOGLE_FIT_ERROR_INTERRUPTED:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_interrupted);
						break;
					case GOOGLE_FIT_ERROR_INVALID_ACCOUNT:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_invalid_account);
						break;
					case GOOGLE_FIT_ERROR_LICENSE_CHECK_FAILED:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_license_check_failed);
						break;
					case GOOGLE_FIT_ERROR_NETWORK_ERROR:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_network_error);
						break;
					case GOOGLE_FIT_ERROR_RESOLUTION_REQUIRED:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_resolution_required);
						break;
					case GOOGLE_FIT_ERROR_RESTRICTED_PROFILE:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_restricted_profile);
						break;
					case GOOGLE_FIT_ERROR_SERVICE_DISABLED:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_service_disabled);
						break;
					case GOOGLE_FIT_ERROR_SERVICE_INVALID:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_service_invalid);
						break;
					case GOOGLE_FIT_ERROR_SERVICE_MISSING:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_service_missing);
						break;
					case GOOGLE_FIT_ERROR_SERVICE_MISSING_PERMISSION:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_service_missing_permission);
						break;
					case GOOGLE_FIT_ERROR_SERVICE_UPDATING:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_service_updating);
						break;
					case GOOGLE_FIT_ERROR_SERVICE_VERSION_UPDATE_REQUIRED:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_service_version_update_required);
						break;
					case GOOGLE_FIT_ERROR_SIGN_IN_FAILED:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_sign_in_failed);
						break;
					case GOOGLE_FIT_ERROR_SIGN_IN_REQUIRED:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_sign_in_required);
						break;
					case GOOGLE_FIT_ERROR_TIMEOUT:
						errorMessage = activityReference.getString(R.string.google_fit_service_error_timeout);
						break;
				}

				Logger.error(TAG, "OnConnectionFailedListener : " + errorMessage);

				serviceCallbacks.onConnectionResult(connectionResult.getErrorCode(), errorMessage);
			}
		}
	};


	private void subscribe() {
		// To create a subscription, invoke the Recording API.
		// As soon as the subscription is active, fitness data will start recording.
		Fitness.RecordingApi.subscribe(googleApiClient, DataType.TYPE_STEP_COUNT_DELTA)
				.setResultCallback(new ResultCallback<Status>() {
					@Override
					public void onResult(Status status) {
						if (status.isSuccess()) {
							if (actionType == ACTION_CONNECT) {
								if (serviceCallbacks != null) {
									serviceCallbacks.onConnectionResult(GOOGLE_FIT_ERROR_NONE, activityReference.getString(R.string.google_fit_service_error_none));
								}
							} else if (actionType == ACTION_SYNC) {
								syncGoogleFit();
							}
						} else {
							if (serviceCallbacks != null) {
								String errorMessage = activityReference.getString(R.string.google_fit_service_error_subscription_error);
								switch (status.getStatusCode()) {
									case GOOGLE_FIT_ERROR_SUBSCRIPTION_NEEDS_OAUTH_PERMISSIONS:
										errorMessage = "Needs OAuth permissions";
										break;
									case GOOGLE_FIT_ERROR_SUBSCRIPTION_CONFLICTING_DATA_TYPE:
										errorMessage = "Conflicting data type";
										break;
									case GOOGLE_FIT_ERROR_SUBSCRIPTION_INCONSISTENT_DATA_TYPE:
										errorMessage = "Inconsistent data type";
										break;
									case GOOGLE_FIT_ERROR_SUBSCRIPTION_DATA_TYPE_NOT_FOUND:
										errorMessage = "Data type not found";
										break;
									case GOOGLE_FIT_ERROR_SUBSCRIPTION_APP_MISMATCH:
										errorMessage = "App mismatch";
										break;
									case GOOGLE_FIT_ERROR_SUBSCRIPTION_UNKNOWN_AUTH_ERROR:
										errorMessage = "Unknown Auth error";
										break;
								}

								Logger.error(TAG, "subscribe : status=fail, error=%s", errorMessage);

								serviceCallbacks.onConnectionResult(status.getStatusCode(), errorMessage);
							} else {
								Logger.error(TAG, "subscribe : status=fail, serviceCallbacks=null");
							}
						}
					}
				});
	}


	/**
	 * Set the google fit service callbacks
	 *
	 * @param callbacks
	 */
	public void setServiceCallbacks(GoogleFitServiceCallbacks callbacks) {
		this.serviceCallbacks = callbacks;
	}


	/**
	 * Method to release the singleton instance of GoogleFitService.
	 * Once this method is called, GoogleFitService is never available.
	 * Need to create singleton instance again.
	 * This method is typically called when the google fit is un-linked.
	 */
	public void disconnectToGoogleFit() {
		if (googleApiClient == null) {
			Logger.error(TAG, "disconnectToGoogleFit : googleApiClient=null");
			return;
		}

		googleApiClient.disconnect();

		if (googleApiClient.isConnectionCallbacksRegistered(connectionCallbacks)) {
			googleApiClient.unregisterConnectionCallbacks(connectionCallbacks);
		}

		if (googleApiClient.isConnectionFailedListenerRegistered(connectionFailedListener)) {
			googleApiClient.unregisterConnectionFailedListener(connectionFailedListener);
		}

		googleApiClient.stopAutoManage(activityReference);

		googleApiClient = null;
	}


	private void syncGoogleFit() {
		// Sync. Need to getV1 data using history api and upload to our service.
		new HistoryDataTask().execute(lastSyncDate);
	}


	public interface GoogleFitServiceCallbacks {
		void onConnectionResult(int errorCode, String errorMessage);
		void onSyncResult(int errorCode, String errorMessage, Object object);
	}

	/**
	 * Read the current daily step total, computed from midnight of the current day
	 * on the device's current timezone.
	 */
	private class HistoryDataTask extends AsyncTask<Date, Void, Void> {
		protected Void doInBackground(Date... lastSyncDate) {
			Logger.log(TAG, "doInBackground");

			if (googleApiClient == null) {
				if (serviceCallbacks != null) {
					serviceCallbacks.onSyncResult(GOOGLE_FIT_ERROR_NOT_INITIALIZED,
							activityReference.getString(R.string.google_fit_service_error_not_initialized_yet), null);
				}

				Logger.error(TAG, "doInBackground : googleApiClient=null");

				return null;
			}

			KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_TRACKER_SYNC_START);

			int offsetUTC = KPHUtils.sharedInstance().getUTCOffset();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			// set start time of syncing to 24 hours before lastSyncDate
			OSDate lastSyncTime = new OSDate(lastSyncDate[0]).offsetByHHMMSS(-24, 0, 0);

			Calendar cal = Calendar.getInstance();
			cal.setTime(lastSyncTime);

			int minutes = (cal.get(Calendar.MINUTE) / 15) * 15 - cal.get(Calendar.MINUTE);
			OSDate startTime = lastSyncTime.offsetByHHMMSS(0, minutes, -cal.get(Calendar.SECOND));
			OSDate nowTime = new OSDate(new Date());

			boolean hasActivities = false;
			TrackerSyncResult syncResult = new TrackerSyncResult();
			List<KPHDailyDetailData> activities = new ArrayList<>();
			KPHDailyDetailData dailyData = new KPHDailyDetailData();

			while(true) {
				if (startTime.after(nowTime))
					break;

				OSDate endTime;
				if (startTime.offsetDay(1).after(nowTime))
					endTime = nowTime;
				else
					endTime = startTime.offsetDay(1);

				long stepMillis = startTime.getTime();
				DataReadRequest readRequest = new DataReadRequest.Builder()
						.aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
						.bucketByTime(15, TimeUnit.MINUTES)
						.setTimeRange(startTime.getTime(), endTime.getTime(), TimeUnit.MILLISECONDS)
						.build();

				Logger.log(TAG, "doInBackground : Data Read Request From GF startTime : %s, endTime : %s", startTime.toString(), endTime.toString());

				DataReadResult dataReadResult = Fitness.HistoryApi.readData(googleApiClient, readRequest).await();

				if (dataReadResult.getStatus().isSuccess()) {
					Logger.log(TAG, "doInBackground : Data Read Result Success");
					if (dataReadResult.getBuckets().size() > 0) {
						Logger.log(TAG, "doInBackground : Bucket size=%s", dataReadResult.getBuckets().size());

						Date date = OSDate.fromStringWithFormat(df.format(startTime.getTime()), "yyyy-MM-dd HH:mm:ss", false);

						for (Bucket bucket : dataReadResult.getBuckets()) {
							List<DataSet> dataSets = bucket.getDataSets();

							for (DataSet dataSet : dataSets) {
								if (dataSet.getDataType().getName().equals(DataType.TYPE_STEP_COUNT_DELTA.getName())) {
									for (DataPoint dp : dataSet.getDataPoints()) {
										if (dp.getValue(Field.FIELD_STEPS) != null) {
											int steps = dp.getValue(Field.FIELD_STEPS).asInt();
											if (steps > 0) {
												Logger.log(TAG, "doInBackground : date=%s, Steps=%d", date.toString(), steps);

												KPHDailyDetailData.DetailItem detailItem = new KPHDailyDetailData.DetailItem(
														new OSDate(date)
																.offsetByHHMMSS(0, offsetUTC, 0)
																.toStringWithFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
														0,
														0,
														0
												);
												detailItem.steps = steps;

												dailyData.addDetailItem(detailItem);

												Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_SYNC_GOOGLE_FIT_SYNC_DATE);
												intent.putExtra("SyncDate", new OSDate(date).toStringWithFormat("yyyy-MM-dd"));
												LocalBroadcastManager.getInstance(KPHApplication.sharedInstance().getApplicationContext()).sendBroadcast(intent);

												dailyData.totalSteps += steps;
											}
										}
									}
									break;
								}
							}

							stepMillis += SYNC_TIME_FRAGMENT;
							date = OSDate.fromStringWithFormat(df.format(stepMillis), "yyyy-MM-dd HH:mm:ss", false);
						}
					} else {
						Logger.log(TAG, "doInBackground : Bucket size 0");
					}
				} else {
					Logger.error(TAG, "doInBackground : Data Read Result Failure");
				}

				startTime = startTime.offsetDay(1);
			}

			dailyData.totalCalories = 0;
			dailyData.totalPowerPoints = 0;
			dailyData.totalDuration = 0;

			activities.add(dailyData);

			syncResult.activities = activities;
			syncResult.newSteps = dailyData.totalSteps;
			syncResult.newCalories = dailyData.totalSteps / 20;
			syncResult.newDuration = 0;
			syncResult.newPowerPoints = dailyData.totalSteps / (20 * 50);

			if (dailyData.totalSteps > 0) {
				hasActivities = true;
			}

			if (serviceCallbacks != null) {
				if (hasActivities) {
					serviceCallbacks.onSyncResult(GOOGLE_FIT_ERROR_NONE,
							activityReference.getString(R.string.google_fit_service_error_none),
							syncResult);
				} else {
					boolean hasLastAcitivties = getLastActivityState();
					boolean trackerConnectedTimeExceeded = false;

					long currentTimeStamp = Calendar.getInstance().getTimeInMillis();
					long trackerConnectedTimestamp = KPHUserService.sharedInstance().loadGFTime();
					if (trackerConnectedTimestamp != 0 || currentTimeStamp - trackerConnectedTimestamp > TRACKER_CONNECT_TIME_LIMIT) {
						trackerConnectedTimeExceeded = true;
					}

					Logger.log(TAG, "trackerConnectedTimeExceeded : " + trackerConnectedTimeExceeded);

					if (hasLastAcitivties || !trackerConnectedTimeExceeded) {
						Logger.log(TAG, "doInBackground : getLastActivityState's hasLastAcitivties=true");

						serviceCallbacks.onSyncResult(GOOGLE_FIT_ERROR_NONE,
								activityReference.getString(R.string.google_fit_service_error_none),
								syncResult);
					} else {
						Logger.log(TAG, "doInBackground : getLastActivityState's hasLastAcitivties=false");

						KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_TRACKER_SYNC_NODATA);

						serviceCallbacks.onSyncResult(GOOGLE_FIT_ERROR_NO_SYNCED_DATA,
								activityReference.getString(R.string.google_fit_service_error_no_synced_data),
								null);
					}
				}
			}

			return null;
		}
	}


	private boolean getLastActivityState() {
		Date endTime = new Date();
		OSDate startTime = new OSDate(endTime).offsetByHHMMSS(-48, 0, 0);

		DataReadRequest readRequest = new DataReadRequest.Builder()
				.aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
				.bucketByTime(48, TimeUnit.HOURS)
				.setTimeRange(startTime.getTime(), endTime.getTime(), TimeUnit.MILLISECONDS)
				.build();

		DataReadResult dataReadResult = Fitness.HistoryApi.readData(googleApiClient, readRequest).await();

		boolean existSteps = false;

		if (dataReadResult.getStatus().isSuccess()) {
			if (dataReadResult.getBuckets().size() > 0) {
				for (Bucket bucket : dataReadResult.getBuckets()) {
					List<DataSet> dataSets = bucket.getDataSets();
					for (DataSet dataSet : dataSets) {
						if (dataSet.getDataType().getName().equals(DataType.TYPE_STEP_COUNT_DELTA.getName())) {
							for (DataPoint dp : dataSet.getDataPoints()) {
								int steps = dp.getValue(Field.FIELD_STEPS).asInt();
								if (steps > 0) {
									Logger.log(TAG, "getLastActivityState : existStep=true");

									existSteps = true;
									break;
								}
							}
						}

						if (existSteps) {
							break;
						}
					}

					if (existSteps) {
						break;
					}
				}
			} else {
				Logger.log(TAG, "getLastActivityState : Bucket size 0");
			}
		} else {
			Logger.log(TAG, "getLastActivityState : readRequest is not successed");
		}

		return existSteps;
	}

	/***********************************************************************************************
	 * Public utility functions
	 */
	public static void checkIfAttached(final OnAttachedListener listener) {
		RestService.get().getTrackerByDeviceId(KPHUtils.sharedInstance().getDeviceIdentifier(), new RestCallback<List<KPHTracker>>() {
			@Override
			public void failure(RestError restError) {
				Logger.error(TAG, "checkIfAttached : getTrackerByDeviceId failed");

				if (listener != null)
					listener.onFailure(KPHUtils.sharedInstance().getNonNullMessage(restError));
			}

			@Override
			public void success(List<KPHTracker> kphTrackers, Response response) {
				if (kphTrackers == null) {
					listener.onSuccess(0);
					return;
				}

				for (int i = 0; i < kphTrackers.size(); i++) {
					KPHTracker trackerItem = kphTrackers.get(i);
					if (trackerItem.getCurrentFlag()) {
						listener.onSuccess(trackerItem.getUserId());
						return;
					}
				}

				listener.onSuccess(0);
			}
		});
	}

	public interface OnAttachedListener {
		void onSuccess(int userid);
		void onFailure(String message);
	}

}
