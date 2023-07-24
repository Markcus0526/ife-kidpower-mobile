package org.unicefkidpower.kid_power.Model.MicroService;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import org.unicefkidpower.kid_power.Model.Structure.KPHCheerInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHDailyDetailData;
import org.unicefkidpower.kid_power.Model.Structure.KPHDelight;
import org.unicefkidpower.kid_power.Model.Structure.KPHDelightInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHDelightUnlocked;
import org.unicefkidpower.kid_power.Model.Structure.KPHMission;
import org.unicefkidpower.kid_power.Model.Structure.KPHMissionInformation;
import org.unicefkidpower.kid_power.Model.Structure.KPHSyncSnapshots;
import org.unicefkidpower.kid_power.Model.Structure.KPHTravelLogUnreadItemCount;
import org.unicefkidpower.kid_power.Model.Structure.KPHUploadActivityData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMission;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserMissionStats;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserStats;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserTravelLog;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserTravelLogResponse;
import org.unicefkidpower.kid_power.Model.Structure.MissionLog;
import org.unicefkidpower.kid_power.Application.KPHApplication;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.BandService;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamBandDetails;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamLinkBand;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamSimple;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamStartMission;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.CmdHelper.Parameters.CBParamSync;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.SyncRestEndpoint;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.SyncRestService;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.OSDate;
import org.unicefkidpower.kid_power.Misc.PlistParser;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Misc.onBandActionListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit.client.Response;

/**
 * Created by Dayong Li on 11/11/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */

public class KPHMissionService {
	private static final String 			TAG						= "KPHMissionService";

	public static final String				MISSION_ACTIVE			= "active";
	public static final String				MISSION_COMPLETED		= "complete";
	public static final String				MISSION_PENDING			= "pending";

	private static final int				MISSION_ID_ALEX_MORGAN_1		= 1;
	private static final int				MISSION_ID_ALEX_MORGAN_901		= 901;
	private static final int				MISSION_ID_ALEX_MORGAN_902		= 902;

	protected static KPHMissionService		instance;
	protected KPHUserService				userService;
	protected int							offsetUTC = 0;

	/* All missions list */
	protected ArrayList<KPHMission>			missionsArray				= null;
	/* Missions list for user */
	protected ArrayList<KPHUserMission>		userMissionsArray			= null;
	/* Started or completed missions list for user */
	protected List<KPHUserMissionStats>		userMissionsStats			= null;

	/* XML local data for delights, missions, cheers */
	protected List<KPHDelightInformation>	arrDelights					= null;
	protected List<KPHMissionInformation>	arrMissions					= null;
	protected List<KPHCheerInformation>		arrCheers					= null;

	protected boolean						loadingTravelLogs			= false;
	protected List<KPHUserTravelLog>		travelLogs					= null;
	protected int							unreadTravelLogCount		= 0;

	static final String						UTCDateFormatString			= "yyyy-MM-dd'T'HH:mm:ss'Z'";


	public static KPHMissionService sharedInstance() {
		if (instance == null)
			instance = new KPHMissionService();

		return instance;
	}


	public KPHMissionService() {
		userService = KPHUserService.sharedInstance();
		offsetUTC = KPHUtils.sharedInstance().getUTCOffset();
	}


	public void loadData(Context context) {
		loadMissionsPlistFromXML(context);
		loadDelightsPlistFromXML(context);
		loadCheersPlistFromXML(context);
	}


	public void clearUsersInformation() {
		if (userMissionsArray != null) {
			userMissionsArray.clear();
			userMissionsArray = null;
		}

		if (travelLogs != null) {
			travelLogs.clear();
			travelLogs = null;
		}
	}


	public boolean shouldShowAutoStartMissionDialog() {
		// return false;
		if (userMissionsArray == null || userMissionsStats == null)
			return false;

		int unlockedMission = 0;
		for (KPHUserMissionStats mission : userMissionsStats) {
			if (mission.isCompletedMission()) {
				// ignore this mission
			} else if (mission.isStartedMission()) {
				// has current mission, stop search right now.
				Logger.log(TAG, "shouldShowAutoStartMissionDialog : found the progressing mission : "
						+ mission.getMissionName());
				return false;
			} else if (mission.isUnlockedMission()) {
				unlockedMission++;
			}
		}

		if (unlockedMission > 0) {
			Logger.log(TAG, "shouldShowAutoStartMissionDialog : found %d locked missions" + unlockedMission);
			return true;
		}

		return false;
	}


	public boolean shouldShowTutorial() {
		if (KPHUserService.sharedInstance().loadCurrentTrackerType() == KPHUserService.TRACKER_TYPE_GOOGLEFIT)
			return false;

		if (userMissionsArray == null)
			return true;

		boolean hasCompleted = false;
		for (KPHUserMissionStats mission : userMissionsStats) {
			if (mission.isCompletedMission()) {
				hasCompleted = true;
				break;
			}
		}

		if (hasCompleted)
			return false;

		Long lastSeenDate = KPHUserService.sharedInstance().seenTutorial();

		if (lastSeenDate == 0)
			return true;

		// compare time
		long ts = new Date().getTime();

		return false;
	}


	public void writeSeenTutorialTime() {
		KPHUserService.sharedInstance().setSawTutorial(new Date().getTime());
	}


	private boolean isStartedOrCompletedMission(long missionId) {
		KPHUserMissionStats stats = userMissionStateById(missionId);
		if (stats == null)
			return false;

		return stats.isStartedMission() || stats.isCompletedMission();
	}


	// For Backend communicate
	public boolean isSupportedMission(long missionId) {
		return isSupportedMission(missionId, false);
	}

	public boolean isSupportedMission(long missionId, boolean shouldCheckActiveMission) {
		if (missionId == 0)
			return true;

		// Show / hide shortened first mission according to the existence of original first mission
		{
			if (isStartedOrCompletedMission(MISSION_ID_ALEX_MORGAN_1)) {
				// Global mission with Alex Morgan 1 is started or completed
				if (missionId == MISSION_ID_ALEX_MORGAN_901 || missionId == MISSION_ID_ALEX_MORGAN_902) {
					return false;
				}
			} else if (isStartedOrCompletedMission(MISSION_ID_ALEX_MORGAN_901)) {
				if (missionId == MISSION_ID_ALEX_MORGAN_1 || missionId == MISSION_ID_ALEX_MORGAN_902) {
					return false;
				}
			}

			if (missionId == MISSION_ID_ALEX_MORGAN_1 && userMissionStateById(MISSION_ID_ALEX_MORGAN_1) == null)
				return false;
		}


		KPHMissionInformation missionAssets = getMissionInformationById(missionId);
		if (missionAssets == null)
			return false;

		KPHMission mission = missionById(missionId);
		if (mission != null && shouldCheckActiveMission && mission.isActive())
			return true;

		KPHUserMission userMission = userMissionById(missionId);
		if (userMission != null)
			return true;

		return false;
	}


	/***********************************************************************************************
	 * For user mission list
	 */
	public void fetchUserMissionsAllForUser(long userId, final onActionListener listener) {
		assert (listener != null);

		RestService.get().getUserMissionsAllForUser(userId,
				new RestCallback<List<KPHUserMission>>() {
					@Override
					public void success(List<KPHUserMission> kphMissions, Response response) {
						userMissionsArray = new ArrayList<>(kphMissions);
						// Sort array of missions by their IDs
						Collections.sort(userMissionsArray, new Comparator<KPHUserMission>() {
							@Override
							public int compare(KPHUserMission lhs, KPHUserMission rhs) {
								return lhs.getId() < rhs.getId() ? -1 : (lhs.getId() == rhs.getId() ? 0 : 1);
							}
						});

						listener.completed(userMissionsArray);
					}

					@Override
					public void failure(RestError restError) {
						userMissionsArray = null;
						listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
					}
				}
		);
	}


	public KPHUserMission userMissionById(long missionId) {
		if (userMissionsArray == null)
			return null;

		for (KPHUserMission mission : userMissionsArray) {
			if (mission.getMissionId() == missionId)
				return mission;
		}
		return null;
	}


	///////////// for mission list
	public void fetchSupportedMissionsList(final onActionListener listener) {
		assert (listener != null);

		RestService.get().getMissions(
				new RestCallback<List<KPHMission>>() {
					@Override
					public void success(List<KPHMission> kphMissions, Response response) {
						missionsArray = new ArrayList<> (kphMissions);
						// Sort array of missions by their IDs
						Collections.sort(missionsArray, new Comparator<KPHMission>() {
							@Override
							public int compare(KPHMission lhs, KPHMission rhs) {
								return lhs.getSortOrder() - rhs.getSortOrder();
							}
						});

						listener.completed(missionsArray);
					}

					@Override
					public void failure(RestError restError) {
						missionsArray = null;
						listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
					}
				}
		);

	}


	public KPHMission missionById(long missionId) {
		if (missionsArray == null)
			return null;

		for (KPHMission mission : missionsArray) {
			if (mission.getId() == missionId)
				return mission;
		}
		return null;
	}


	public boolean isAlexMorganMission(long missionId) {
		return missionId == MISSION_ID_ALEX_MORGAN_1 ||
				missionId == MISSION_ID_ALEX_MORGAN_901 ||
				missionId == MISSION_ID_ALEX_MORGAN_902;
	}

	private ArrayList<KPHMission> getSortedMissions(boolean hideMorganMissions) {
		ArrayList<KPHMission> allMissionArray = new ArrayList<>();

		if (missionsArray != null) {
			allMissionArray.addAll(missionsArray);

			// Sort by Sort order
			Collections.sort(allMissionArray, new Comparator<KPHMission>() {
				@Override
				public int compare(KPHMission lhs, KPHMission rhs) {
					return lhs.getSortOrder() - rhs.getSortOrder();
				}
			});


			// Hide/Resort Global Missions with Alex Morgan
			if (hideMorganMissions) {
				// Typically for new missions
				if (!isStartedOrCompletedMission(MISSION_ID_ALEX_MORGAN_1) &&
						!isStartedOrCompletedMission(MISSION_ID_ALEX_MORGAN_901) &&
						!isStartedOrCompletedMission(MISSION_ID_ALEX_MORGAN_902)) {
					// There is no Global mission with Alex Morgan which is already started or completed
					// Should show only 902 mission
					for (int i = allMissionArray.size() - 1; i >= 0; i--) {
						KPHMission mission = allMissionArray.get(i);
						if (mission.getId() == MISSION_ID_ALEX_MORGAN_1 ||
								mission.getId() == MISSION_ID_ALEX_MORGAN_901) {
							allMissionArray.remove(i);
						}
					}

					for (KPHMission mission : allMissionArray) {
						if (mission.getId() == MISSION_ID_ALEX_MORGAN_902) {
							// Global Mission with Alex Morgan. Should be top of the list
							allMissionArray.remove(mission);
							allMissionArray.add(0, mission);
							break;
						}
					}
				} else if (!isStartedOrCompletedMission(MISSION_ID_ALEX_MORGAN_1)) {
					for (int i = allMissionArray.size() - 1; i >= 0; i--) {
						KPHMission mission = allMissionArray.get(i);
						if (mission.getId() == MISSION_ID_ALEX_MORGAN_901 ||
								mission.getId() == MISSION_ID_ALEX_MORGAN_902) {
							allMissionArray.remove(i);
						}
					}

					for (KPHMission mission : allMissionArray) {
						if (mission.getId() == MISSION_ID_ALEX_MORGAN_1) {
							// Global Mission with Alex Morgan. Should be top of the list
							allMissionArray.remove(mission);
							allMissionArray.add(0, mission);
							break;
						}
					}
				} else if (!isStartedOrCompletedMission(MISSION_ID_ALEX_MORGAN_901)) {
					for (int i = allMissionArray.size() - 1; i >= 0; i--) {
						KPHMission mission = allMissionArray.get(i);
						if (mission.getId() == MISSION_ID_ALEX_MORGAN_1 ||
								mission.getId() == MISSION_ID_ALEX_MORGAN_902) {
							allMissionArray.remove(i);
						}
					}

					for (KPHMission mission : allMissionArray) {
						if (mission.getId() == MISSION_ID_ALEX_MORGAN_901) {
							// Global Mission with Alex Morgan. Should be top of the list
							allMissionArray.remove(mission);
							allMissionArray.add(0, mission);
							break;
						}
					}
				} else if (!isStartedOrCompletedMission(MISSION_ID_ALEX_MORGAN_902)) {
					for (int i = allMissionArray.size() - 1; i >= 0; i--) {
						KPHMission mission = allMissionArray.get(i);
						if (mission.getId() == MISSION_ID_ALEX_MORGAN_1 ||
								mission.getId() == MISSION_ID_ALEX_MORGAN_901) {
							allMissionArray.remove(i);
						}
					}

					for (KPHMission mission : allMissionArray) {
						if (mission.getId() == MISSION_ID_ALEX_MORGAN_902) {
							// Global Mission with Alex Morgan. Should be top of the list
							allMissionArray.remove(mission);
							allMissionArray.add(0, mission);
							break;
						}
					}
				}
			} else {
				// No need to hide. But should sort and leave only one Alex Morgan mission.
				// Typically for completed missions
				boolean contains902 = false;
				for (KPHMission mission : allMissionArray) {
					if (mission.getId() == MISSION_ID_ALEX_MORGAN_902) {
						KPHUserMissionStats missionStats = userMissionStateById(mission.getId());
						if (missionStats == null)
							break;

						if (missionStats.isCompletedMission()) {
							allMissionArray.remove(mission);
							allMissionArray.add(0, mission);
							contains902 = true;
							Logger.log(TAG, "902 mission is on Completed list");
						}

						break;
					}
				}

				if (contains902) {
					for (int i = allMissionArray.size() - 1; i >= 0; i--) {
						KPHMission mission = allMissionArray.get(i);
						if (mission.getId() == MISSION_ID_ALEX_MORGAN_901 ||
								mission.getId() == MISSION_ID_ALEX_MORGAN_1) {
							allMissionArray.remove(i);
						}
					}
				} else {
					boolean contains901 = false;
					for (KPHMission mission : allMissionArray) {
						if (mission.getId() == MISSION_ID_ALEX_MORGAN_901) {
							KPHUserMissionStats missionStats = userMissionStateById(mission.getId());
							if (missionStats == null)
								break;

							if (missionStats.isCompletedMission()) {
								allMissionArray.remove(mission);
								allMissionArray.add(0, mission);
								contains901 = true;
								Logger.log(TAG, "901 mission is on Completed list");
							}

							break;
						}
					}

					if (contains901) {
						for (int i = allMissionArray.size() - 1; i >= 0; i--) {
							KPHMission mission = allMissionArray.get(i);
							if (mission.getId() == MISSION_ID_ALEX_MORGAN_1) {
								allMissionArray.remove(i);
							}
						}
					} else {
						for (KPHMission mission : allMissionArray) {
							if (mission.getId() == MISSION_ID_ALEX_MORGAN_1) {
								KPHUserMissionStats missionStats = userMissionStateById(mission.getId());
								if (missionStats == null)
									break;

								if (missionStats.isCompletedMission()) {
									allMissionArray.remove(mission);
									allMissionArray.add(0, mission);
									Logger.log(TAG, "1 mission is on Completed list");
								}

								break;
							}
						}
					}
				}
			}
		}

		return allMissionArray;
	}


	public ArrayList<KPHMission> getSortedNewMissionsList() {
		ArrayList<KPHMission> resultArray = new ArrayList<>();

		ArrayList<KPHMission> allMissionArray = getSortedMissions(true);
		ArrayList<KPHMission> newMissions = new ArrayList<>();
		ArrayList<KPHMission> unStartedUnlockedMissions = new ArrayList<>();

		for (KPHMission mission : allMissionArray) {
			if (!isSupportedMission(mission.getId(), true))
				continue;

			KPHUserMissionStats userMissionStats = userMissionStateById(mission.getId());
			if (userMissionStats != null) {
				if (!userMissionStats.isStartedMission() && !userMissionStats.isCompletedMission()) {
					if (userMissionStats.isUnlockedMission()) {
						unStartedUnlockedMissions.add(mission);
					} else {
						newMissions.add(mission);
					}
				}
			} else {
				newMissions.add(mission);
			}
		}

		resultArray.addAll(unStartedUnlockedMissions);
		resultArray.addAll(newMissions);

		return resultArray;
	}


	public ArrayList<KPHMission> getSortedCompleteMissionsList() {
		ArrayList<KPHMission> resultArray = new ArrayList<>();

		ArrayList<KPHMission> allMissionArray = getSortedMissions(false);
		ArrayList<KPHUserMissionStats> completedMissionStats = new ArrayList<>();

		for (KPHMission mission : allMissionArray) {
			if (!isSupportedMission(mission.getId()))
				continue;

			KPHUserMissionStats userMissionStats = userMissionStateById(mission.getId());
			if (userMissionStats != null) {
				if (userMissionStats.isCompletedMission()) {
					completedMissionStats.add(userMissionStats);
				}
			}
		}

		Collections.sort(completedMissionStats, new Comparator<KPHUserMissionStats>() {
			@Override
			public int compare(KPHUserMissionStats lhs, KPHUserMissionStats rhs) {
				Date leftCompleteDate = OSDate.fromUTCString(lhs.getCompletedAt());
				Date rightCompleteDate = OSDate.fromUTCString(rhs.getCompletedAt());
				return rightCompleteDate.getTime() < leftCompleteDate.getTime() ?
						-1 : (rightCompleteDate.getTime() == leftCompleteDate.getTime() ? 0 : 1);
			}
		});

		for (KPHUserMissionStats completedMissionStat : completedMissionStats) {
			for (KPHMission mission : allMissionArray) {
				if (mission.getId() == completedMissionStat.getMissionId())
					resultArray.add(mission);
			}
		}

		return resultArray;
	}


	/**
	 * load all of missions information from XML file like plist in iOS
	 *
	 * @param context : application context
	 * @return : each mission information
	 */
	public void loadMissionsPlistFromXML(Context context) {
		if (arrMissions != null) {
			return;
		}

		XmlResourceParser xml = context.getResources().getXml(R.xml.missions_plist);

		arrMissions = new ArrayList<>();
		List<HashMap<String, Object>> pList = PlistParser.parse(xml);
		if (pList == null) {
			return;
		}

		for (HashMap<String, Object> item : pList) {
			KPHMissionInformation mission = new KPHMissionInformation();
			if (mission.parseHashMap(item))
				arrMissions.add(mission);
		}

		//sort mission array at first.
		Collections.sort(arrMissions, new Comparator<KPHMissionInformation>() {
			@Override
			public int compare(KPHMissionInformation lhs, KPHMissionInformation rhs) {
				return lhs.getMissionSortOrder() < rhs.getMissionSortOrder() ? -1 : (lhs.getMissionSortOrder() == rhs.getMissionSortOrder() ? 0 : 1);
			}
		});
	}


	public KPHMissionInformation getMissionInformationById(long id) {
		if (arrMissions == null || arrMissions.size() == 0) {
			loadMissionsPlistFromXML(KPHUtils.sharedInstance().getApplicationContext());
		}

		for (KPHMissionInformation mission : arrMissions) {
			if (mission.missionId() == id) {
				return mission;
			}
		}

		Log.e("getMissionInformation", String.format("Couldn\'t find info for mission %d", id));
		return null;
	}


	/**
	 * load all of delights information from XML file like plist in iOS
	 *
	 * @param context : application context
	 * @return : each delight information
	 */
	public void loadDelightsPlistFromXML(Context context) {
		if (arrDelights != null) {
			return;
		}

		XmlResourceParser xml = context.getResources().getXml(R.xml.delights_plist);

		arrDelights = new ArrayList<>();

		List<HashMap<String, Object>> pList = PlistParser.parse(xml);
		if (pList == null)
			return;

		for (HashMap<String, Object> item : pList) {
			KPHDelightInformation delight = new KPHDelightInformation();
			if (delight.parseHashMap(item))
				arrDelights.add(delight);
		}
	}


	public void fetchDelights() {
		if (arrDelights != null)
			return;

		RestService.get().getDelights(new RestCallback<List<KPHDelightInformation>>() {
			@Override
			public void success(List<KPHDelightInformation> delights, Response response) {
				arrDelights = delights;
			}

			@Override
			public void failure(RestError restError) {
				arrDelights = null;
			}
		});
	}


	public KPHDelightInformation getDelightInformationById(long id) {
		if (arrDelights == null) {
			return null;
		}

		for (KPHDelightInformation delight : arrDelights) {
			if (delight.getDelightId() == id) {
				return delight;
			}
		}
		return null;
	}


	public void fetchCheers() {
		if (arrCheers != null)
			return;

		RestService.get().getCheers(new RestCallback<List<KPHCheerInformation>>() {
			@Override
			public void success(List<KPHCheerInformation> cheers, Response response) {
				arrCheers = cheers;
			}

			@Override
			public void failure(RestError restError) {
				arrCheers = null;
			}
		});
	}


	public void loadCheersPlistFromXML(Context context) {
		if (arrCheers != null) {
			return;
		}

		XmlResourceParser xml = context.getResources().getXml(R.xml.cheers_plist);

		arrCheers = new ArrayList<>();

		List<HashMap<String, Object>> pList = PlistParser.parse(xml);
		if (pList == null)
			return;

		for (HashMap<String, Object> item : pList) {
			KPHCheerInformation cheer = new KPHCheerInformation();
			if (cheer.parseHashMap(item))
				arrCheers.add(cheer);
		}

		Collections.sort(arrCheers, new Comparator<KPHCheerInformation>() {
			@Override
			public int compare(KPHCheerInformation lhs, KPHCheerInformation rhs) {
				return lhs.getId() < rhs.getId() ? 1 : 0;
			}
		});
	}


	public KPHCheerInformation getCheerInformation(long cheerId) {
		if (arrCheers == null)
			loadCheersPlistFromXML(KPHApplication.sharedInstance().getApplicationContext());

		for (KPHCheerInformation cheerInformation : arrCheers) {
			if (cheerInformation.getId() == cheerId) {
				return cheerInformation;
			}
		}

		return null;
	}


	public void getUserMissionProgress(final RestCallback<List<KPHUserMissionStats>> callback) {
		KPHUserData userData = userService.getUserData();
		if (userData == null) {
			callback.failure((RestError) null);
			return;
		}

		RestService.get().getUserMissionProgressForUser(
				userData.getId(),
				new RestCallback<List<KPHUserMissionStats>>() {
					@Override
					public void failure(RestError restError) {
						userMissionsStats = new ArrayList<>();
						callback.failure(restError);
					}

					@Override
					public void success(List<KPHUserMissionStats> kphUserMissionStats, Response response) {
						userMissionsStats = kphUserMissionStats;
						sortMissionStatus();
						callback.success(userMissionsStats, response);
					}
				}
		);
	}


	protected void sortMissionStatus() {
		if (userMissionsStats == null)
			return;

		// Sort array of missions by their IDs, direction : reverse
		Collections.sort(userMissionsStats, new Comparator<KPHUserMissionStats>() {
			@Override
			public int compare(KPHUserMissionStats lhs, KPHUserMissionStats rhs) {
				return lhs.getUserMissionId() < rhs.getUserMissionId() ? 1 : (lhs.getUserMissionId() == rhs.getUserMissionId() ? 0 : -1);
			}
		});

		for (KPHUserMissionStats missionStats : userMissionsStats) {
			List<KPHDelightUnlocked> unlockeds = missionStats.getDelightsUnlocked();
			if (unlockeds == null) {
				continue;
			}
			// Sort array of missions by their IDs, direction
			Collections.sort(unlockeds, new Comparator<KPHDelightUnlocked>() {
				@Override
				public int compare(KPHDelightUnlocked lhs, KPHDelightUnlocked rhs) {
					return lhs.getDelightId() < rhs.getDelightId() ? -1 : (lhs.getDelightId() == rhs.getDelightId() ? 0 : 1);
				}
			});
		}
	}

	public void updateUserMissionStatus(KPHUserMissionStats status) {
		KPHUserData userData = userService.getUserData();
		KPHUserStats userCurrentState = userData.getUserStats();

		if (status == null || status.getMissionId() == -1 || userCurrentState == null)
			return;

		if (userMissionsStats == null) {
			userMissionsStats = new ArrayList<>();
		}

		KPHUserMissionStats mission = userMissionStateById(status.getMissionId());
		if (mission == null) {
			// add new status
			userMissionsStats.add(status);
		} else if (mission != status) {
			// remove old status
			userMissionsStats.remove(mission);
			userMissionsStats.add(status);
		}

		if (status.isCompletedMission()) {
			userCurrentState.incMissionsCompleted();
			KPHUserService.sharedInstance().saveUserData(userData);
		}
		// reorder
		sortMissionStatus();
	}

	public void updateUserMissionFromSnapshot(KPHSyncSnapshots snapshots) {
		KPHUserData userData = userService.getUserData();
		KPHUserStats userCurrentState = userData.getUserStats();

		KPHUserMissionStats stats = userMissionStateById(snapshots.missionId);
		if (stats != null) {
			stats.updateFromSnapshot(snapshots);
		}

		if (userCurrentState != null) {
			userCurrentState.updateFromSnapshot(snapshots);
			userService.saveUserData(userData);
		}
	}

	/**
	 * Find and return user mission stats object by mission ID
	 *
	 * @param missionId Mission Identifier
	 * @return Returns the user mission stats found if exist; otherwise returns null
	 */
	public KPHUserMissionStats userMissionStateById(long missionId) {
		KPHUserMissionStats retVal = null;

		if (userMissionsStats == null)
			return null;

		for (KPHUserMissionStats userMissionStats : userMissionsStats) {
			if (userMissionStats.getMissionId() == missionId) {
				retVal = userMissionStats;
				break;
			}
		}
		return retVal;
	}


	public List<KPHUserMissionStats> getUserMissionsStats() {
		return userMissionsStats;
	}


	public KPHUserMissionStats getActiveUserMission() {
		if (userMissionsStats == null)
			return null;

		KPHUserMissionStats retVal = null;
		for (KPHUserMissionStats userMissionStats : userMissionsStats) {
			if (userMissionStats.getStatus().equals(MISSION_ACTIVE)) {
				retVal = userMissionStats;
				break;
			}
		}
		return retVal;
	}

	public KPHUserMissionStats userMissionStatsFromUserMission(KPHUserMission userMission) {
		KPHUserMissionStats missionStats = userMissionStateById(userMission.getMissionId());

		if (missionStats == null) {
			missionStats = new KPHUserMissionStats();
		}

		missionStats.setUserMissionId(userMission.getId());
		missionStats.setMissionId(userMission.getMissionId());

		missionStats.setStatus(userMission.getStatus());

		missionStats.setStartedAt(userMission.getStartedAt());
		missionStats.setUnlockedAt(userMission.getUnlockedAt());
		missionStats.setEndDate(userMission.getEndDate());
		missionStats.setCompletedAt(userMission.getCompletedAt());
		missionStats.setCompleted(userMission.getCompleted());

		KPHMissionInformation missionInformation = KPHMissionService.sharedInstance().getMissionInformationById(userMission.getMissionId());

		if (missionInformation != null) {
			missionStats.setMissionName(missionInformation.name());
			missionStats.setMissionGoal(missionInformation.calorieGoal());
		}

		missionStats.setDelightsUnlocked(new ArrayList<KPHDelightUnlocked>());

		return missionStats;
	}


	// for last mission snapshot
	public void getLastSnapshot(String deviceId, final onActionListener listener) {
		KPHUserData userData = userService.getUserData();

		if (userData == null) {
			return;
		}

		SyncRestService.getV2().getLastSnapshot(deviceId, userData.getId(), new RestCallback<KPHSyncSnapshots>() {
			@Override
			public void failure(RestError restError) {
				listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
			}

			@Override
			public void success(KPHSyncSnapshots kphSyncSnapshots, Response response) {
				listener.completed(kphSyncSnapshots);
			}
		});
	}


	public void uploadActivityDetails(String deviceId, List<KPHDailyDetailData> dates, final onActionListener listener) {
		// make JSonObjects
		KPHUploadActivityData uploadDates = new KPHUploadActivityData();
		uploadDates.deviceId = deviceId;
		uploadDates.utcOffset = "" + offsetUTC;
		List<KPHDailyDetailData.DetailItem> datails = new ArrayList<>();

		for (KPHDailyDetailData item : dates) {
			for (KPHDailyDetailData.DetailItem detail : item.data) {
				datails.add(detail);
			}
		}
		uploadDates.dates = datails;

		Gson gson = new Gson();
		String json = gson.toJson(uploadDates);
		Logger.log(TAG, "uploadActivityDatails : uploadDates=%s",  json);

		SyncRestService.getV2().uploadActivityDetails(uploadDates, new RestCallback<SyncRestEndpoint._SyncSnapResult>() {
			@Override
			public void failure(RestError restError) {
				String message = KPHUtils.sharedInstance().getNonNullMessage(restError);
				int code = -1;
				if (message == null) {
					code = -2;
					message = "Rest Error";
					// network error
				}

				listener.failed(code, message);
			}

			@Override
			public void success(SyncRestEndpoint._SyncSnapResult s, Response response) {
				if (s.status.equals("success") && s.snapshot != null) {
					listener.completed(s.snapshot);
				} else {
					listener.failed(-1, s.status);
				}
			}
		});
	}


	public void startMission(long missionId, final onActionListener listener) {
		KPHUserData userData = userService.getUserData();
		if (userData == null) {
			return;
		}

		SyncRestService.getV1().missionEnabled(userData.getId(), missionId, true, new RestCallback<SyncRestEndpoint._MissionResult>() {
			@Override
			public void failure(RestError restError) {
				listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
			}

			@Override
			public void success(SyncRestEndpoint._MissionResult s, Response response) {
				listener.completed(s.mission);
			}
		});
	}

	public void completeMission(long missionId, final onActionListener listener) {
		KPHUserData userData = userService.getUserData();
		if (userData == null) {
			return;
		}

		SyncRestService.getV1().missionDisabled(userData.getId(), missionId, true, new RestCallback<SyncRestEndpoint._MissionResult>() {
			@Override
			public void failure(RestError restError) {
				listener.failed(-1, KPHUtils.sharedInstance().getNonNullMessage(restError));
			}

			@Override
			public void success(SyncRestEndpoint._MissionResult s, Response response) {
				listener.completed(s.mission);
			}
		});
	}

	////////////////////////////// Travel Logs //////////////////////////////////////

	public List<KPHUserTravelLog> userTravelLogs() {
		return travelLogs;
	}


	public interface OnLoadUserTravelLog {
		void onSuccess();
		void onFailure(String message);
	}

	public void loadUserTravelLog(int userId, final OnLoadUserTravelLog loadTravelLogListener) {
		if (loadingTravelLogs) {
			return;
		}

		loadingTravelLogs = true;

		RestService.get().getUserTravelLog(
				userId,
				new RestCallback<KPHUserTravelLogResponse>() {
					@Override
					public void failure(RestError restError) {
						loadingTravelLogs = false;
						if (loadTravelLogListener != null)
							loadTravelLogListener.onFailure(KPHUtils.sharedInstance().getNonNullMessage(restError));
					}

					@Override
					public void success(KPHUserTravelLogResponse kphUserTravelLogResponse, Response response) {
						loadingTravelLogs = false;

						unreadTravelLogCount = kphUserTravelLogResponse.getUnreadCount();
						travelLogs = kphUserTravelLogResponse.getTravelLogList();

						if (loadTravelLogListener != null)
							loadTravelLogListener.onSuccess();
					}
				}
		);
	}

	public void addTravelLogFromSnapshot(KPHSyncSnapshots syncSnapshots) {
		if (loadingTravelLogs || travelLogs == null || travelLogs.isEmpty())
			return;

		if (syncSnapshots == null)
			return;

		for (KPHDelight delightUnlocked : syncSnapshots.delightsUnlocked) {
			KPHUserTravelLog travelLog = new KPHUserTravelLog(
					syncSnapshots.date, KPHUserTravelLog.TYPE_SNAPSHOT, syncSnapshots.missionId,
					delightUnlocked
			);

            synchronized (travelLog) {
				travelLogs.add(0, travelLog);
            }
		}
	}

	////////////////////////////////////////////////////////////////////////////////////

	public void stopForBand(Context context) {
		BandService.sharedInstance().stopLastCommand();
	}

	/**
	 * getV1 band detailed information
	 *
	 * @param param    : parameters
	 * @param listener : callback listener
	 */
	public void getDetailsForBand(CBParamBandDetails param, final onBandActionListener listener) {
		if (param.deviceCode == null ||
				listener == null) {
			if (listener != null) {
				listener.failed(-1, "Parameter is incorrect");
			}
			return;
		}
		BandService.sharedInstance().getBandDetails(param, listener);
	}

	/**
	 * link band
	 *
	 * @param param    : parameters
	 * @param listener : callback listener
	 */
	public void linkBandForBand(CBParamLinkBand param, final onBandActionListener listener) {
		if (param.deviceCode == null ||
				listener == null) {
			if (listener != null) {
				listener.failed(-1, "Parameter is incorrect");
			}
			return;
		}
		BandService.sharedInstance().linkBand(param, listener);
	}

	/**
	 * for syncing with ble api
	 * Get Daily detailed data
	 *
	 * @param param    : sync parameters
	 * @param listener : callback
	 */
	public void getDailyDetailedForBand(CBParamSync param, onBandActionListener listener) {
		if (param.deviceCode == null ||
				listener == null ||
				param.days < 0) {
			if (listener != null) {
				listener.failed(-1, "Parameter is incorrect");
			}
			return;
		}

		BandService.sharedInstance().getDetailedActivityForDeviceCode(param, listener);
	}

	/**
	 * start Mission goal, will be called when the user start a mission with goal
	 *
	 * @param param    : parameters
	 * @param listener : callback listener
	 */
	public void startMissionForBand(CBParamStartMission param, final onBandActionListener listener) {
		if (param.deviceCode == null ||
				listener == null) {
			if (listener != null) {
				listener.failed(-1, "Parameter is incorrect");
			}
			return;
		}

		BandService.sharedInstance().setMissionGoal(param, listener);
	}

	/**
	 * complete mission goal for band
	 *
	 * @param param    : parameters
	 * @param listener : callback listener
	 */
	public void completeMissionForBand(CBParamSimple param, final onBandActionListener listener) {
		if (param.deviceCode == null ||
				listener == null) {
			if (listener != null) {
				listener.failed(-1, "Parameter is incorrect");
			}
			return;
		}

		BandService.sharedInstance().disableMissionGoal(param, listener);
	}


	public int getUnreadTravelLogCount() {
		return unreadTravelLogCount;
	}

	public void setUnreadTravelLogCount(int unreadTravelLogCount) {
		this.unreadTravelLogCount = unreadTravelLogCount;
	}

	public interface OnTravelLogMarkListener {
		void onSuccess();
		void onFailure();
	}

	public void markTravelLogItemAsRead(MissionLog missionLog, final OnTravelLogMarkListener listener) {
		if (!missionLog.isUnread())
			return;

		RestService.get().markTravelLogItemAsRead(
				missionLog.getId(),
				new RestCallback<KPHTravelLogUnreadItemCount>() {
					@Override
					public void success(KPHTravelLogUnreadItemCount unreadItemCount, Response response) {
						KPHMissionService.sharedInstance().setUnreadTravelLogCount(unreadItemCount.getUnreadCount());

						Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_TAB_ITEM_BADGE_UPDATED);
						LocalBroadcastManager.getInstance(KPHApplication.sharedInstance().getApplicationContext()).sendBroadcast(intent);

						if (listener != null)
							listener.onSuccess();
					}

					@Override
					public void failure(RestError restError) {
						if (listener != null)
							listener.onFailure();
					}
				}
		);
	}


}
