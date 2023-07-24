package org.unicefkidpower.kid_power.Model.MicroService;

import android.support.annotation.Nullable;

import org.json.JSONObject;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Model.Structure.BaseStringStatusResponse;
import org.unicefkidpower.kid_power.Model.Structure.KPHBlock;
import org.unicefkidpower.kid_power.Model.Structure.KPHFollower;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserSummary;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.onActionListener;

import java.util.List;

import retrofit.client.Response;

/**
 * Created by Ruifeng Shi on 10/26/2015.
 */
public class KPHFollowService {
	private static KPHFollowService			_instance;

	public static KPHFollowService sharedInstance() {
		if (_instance == null)
			_instance = new KPHFollowService();

		return _instance;
	}

	/**
	 * Fetch the list of followers who follow me
	 *
	 * @param listener On-complete listener
	 */
	public void fetchFollowingsList(@Nullable final onActionListener listener) {
		RestService.get().getFollowingsList(
				KPHUserService.sharedInstance().getUserData().getId(),
				new RestCallback<List<KPHUserSummary>>() {

					@Override
					public void success(List<KPHUserSummary> kphUserSummaries, Response response) {
						if (listener != null)
							listener.completed(kphUserSummaries);
					}

					@Override
					public void failure(RestError restError) {
						if (listener != null)
							listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
					}
				}
		);
	}

	/**
	 * Fetch the list of followers who I am following
	 *
	 * @param listener On-complete listener
	 */
	public void fetchFollowersList(final onActionListener listener) {
		RestService.get().getFollowersList(
				KPHUserService.sharedInstance().getUserData().getId(),
				new RestCallback<List<KPHUserSummary>>() {

					@Override
					public void success(List<KPHUserSummary> kphUserSummaries, Response response) {
						if (listener != null)
							listener.completed(kphUserSummaries);
					}

					@Override
					public void failure(RestError restError) {
						if (listener != null)
							listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
					}
				}
		);
	}

	/**
	 * Fetch the list of blocked users from server
	 *
	 * @param listener Completion Listener
	 */
	public void fetchBlockedList(final onActionListener listener) {
		RestService.get().getBlockedUserList(
				KPHUserService.sharedInstance().getUserData().getId(),
				new RestCallback<List<KPHBlock>>() {
					@Override
					public void success(List<KPHBlock> kphBlockedUsers, Response response) {
						if (listener != null)
							listener.completed(kphBlockedUsers);
					}
					@Override
					public void failure(RestError restError) {
						if (listener != null)
							listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
					}
				}
		);
	}

	/**
	 * Follow an user
	 *
	 * @param following A user to be followed
	 * @param listener  Callback
	 */
	public void followUser(KPHUserSummary following, final onActionListener listener) {
		int userId = KPHUserService.sharedInstance().getUserData().getId();
		RestService.get().follow(userId, following.getId(), new RestCallback<KPHFollower>() {
			@Override
			public void failure(RestError restError) {
				listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
			}

			@Override
			public void success(KPHFollower updatedFollowingRecord, Response response) {
				KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_FRIENDS_FOLLOW);
				listener.completed(updatedFollowingRecord);
			}
		});
	}

	/**
	 * Follow an user
	 *
	 * @param following User being followed
	 * @param listener  Callback
	 */
	public void unfollowUser(KPHUserSummary following, final onActionListener listener) {
		int userId = KPHUserService.sharedInstance().getUserData().getId();
		RestService.get().unfollow(userId, following.getId(), new RestCallback<JSONObject>() {
			@Override
			public void success(JSONObject jsonObject, Response response) {
				KPHAnalyticsService.sharedInstance().logEvent(KPHConstants.SWRVE_FRIENDS_UNFOLLOW);
				listener.completed(jsonObject);
			}

			@Override
			public void failure(RestError restError) {
				listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
			}
		});
	}

	/**
	 * Blocks follower
	 *
	 * @param follower Follower who should be blocked
	 * @param listener Callback
	 */
	public void blockUser(KPHUserSummary follower, final onActionListener listener) {
		int userId = KPHUserService.sharedInstance().getUserData().getId();
		RestService.get().blockUser(userId, follower.getId(), new RestCallback<KPHBlock>() {
			@Override
			public void failure(RestError restError) {
				listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
			}

			@Override
			public void success(KPHBlock kphBlock, Response response) {
				listener.completed(kphBlock);
			}
		});
	}

	/**
	 * Unblocks follower
	 *
	 * @param blocked  Blocked record who should be unblocked
	 * @param listener Callback
	 */
	public void unblockUser(KPHBlock blocked, final onActionListener listener) {
		RestService.get().unblockUser(
				blocked.getBlockerId(),
				blocked.getBlockedId(),
				new RestCallback<BaseStringStatusResponse>() {
					@Override
					public void success(BaseStringStatusResponse baseStringStatusResponse, Response response) {
						listener.completed(baseStringStatusResponse.getStatus());
					}

					@Override
					public void failure(RestError restError) {
						listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
					}
				}
		);
	}
}
