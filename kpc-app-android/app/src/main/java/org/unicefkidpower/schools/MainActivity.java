package org.unicefkidpower.schools;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.schools.context.UserContext;
import org.unicefkidpower.schools.define.EventNames;
import org.unicefkidpower.schools.event.EventManager;
import org.unicefkidpower.schools.event.SEvent;
import org.unicefkidpower.schools.helper.DebouncedOnClickListener;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.model.Team;
import org.unicefkidpower.schools.model.UserManager;
import org.unicefkidpower.schools.server.ServerManager;
import org.unicefkidpower.schools.server.apimanage.CommandService;
import org.unicefkidpower.schools.server.apimanage.RestCallback;
import org.unicefkidpower.schools.server.apimanage.UserService;
import org.unicefkidpower.schools.support.SupportFragment;
import org.unicefkidpower.schools.ui.KPSideMenu;

import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Dayong on 8/31/2016.
 */
public class MainActivity extends BaseActivityWithNavBar {
	static final String TAG = "MainActivity";

	static final String LE_LINKED_BANDS_SUCCESS = "Loaded All Linked Bands";
	static final String LE_LINKED_BANDS_FAILED = "Failed All Linked Bands";

	private int mContainerLayoutId;

	private Fragment _currentFragment = null;
	private Fragment _teamsFragment = null;
	private Fragment _coachSettingsFragment = null;
	private Fragment _supportFragment = null;
	private Fragment _teamStatsFragment = null;
	private Fragment _newTeamFragment = null;
	private DrawerLayout mDrawerLayout;
	private FrameLayout mSideBar;
	private ListView lvTeam;
	private TeamAdapter _adapter;
	private KPSideMenu smCurriculum;
	private KPSideMenu smInbox;
	private KPSideMenu smSupport;
	private KPSideMenu smAccount;
	private KPSideMenu smLogOut;
	private KPSideMenu _smLastActive;
	private List<Team> _mTeams;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main_activity);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.layout_parent);
		mDrawerLayout.setDrawerShadow(R.drawable.navbar_shadow, GravityCompat.START);
		mSideBar = (FrameLayout) this.findViewById(R.id.rlSideBar);
		mSideBar.setOnClickListener(new DebouncedOnClickListener(100) {
			@Override
			public void onDebouncedClick(View v) {
				closeSideBar();
			}
		});

		mContainerLayoutId = R.id.layout_content;
		FlurryAgent.onStartSession(this, "School Main Activity");

		try {
			lvTeam = (ListView) findViewById(R.id.lvTeam);
			_adapter = new TeamAdapter(this, R.layout.layout_sidebar_item);
			lvTeam.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					closeSideBar();
					KPSideMenu menu = (KPSideMenu) view;
					if (menu == _smLastActive)
						return;

					setSideBarActive(menu);

					Team team = null;

					if (_mTeams != null && position < _mTeams.size()) {
						team = _mTeams.get(position);
					}
					if (team != null) {
						goTeamStatsFragment(team);
					} else {
						showNewTeam();
					}
				}
			});
			lvTeam.setAdapter(_adapter);

			smCurriculum = (KPSideMenu) findViewById(R.id.llCurriculum);
			smCurriculum.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showCurriculum();
				}
			});
			smInbox = (KPSideMenu) findViewById(R.id.llInbox);
			smInbox.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showInbox();
				}
			});
			smSupport = (KPSideMenu) findViewById(R.id.llSupport);
			smSupport.setOnClickListener(new DebouncedOnClickListener(100) {
				@Override
				public void onDebouncedClick(View v) {
					showSupport();
				}
			});
			smSupport.setVisibility(View.VISIBLE);

			smAccount = (KPSideMenu) findViewById(R.id.llAccount);
			smAccount.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showAccountSettings();
				}
			});
			smLogOut = (KPSideMenu) findViewById(R.id.llLogOut);
			smLogOut.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickedLogOut();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		initZDEngine();

		goHomeFragment();

		EventManager.sharedInstance().post(EventNames.EVENT_LOAD_ALL_LINKED_BAND);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}


	@Override
	public void onMenuClicked() {
		mDrawerLayout.openDrawer(mSideBar);
	}

	void closeSideBar() {
		mDrawerLayout.closeDrawer(mSideBar);
	}

	@Override
	public void onBackClicked() {
		if (_currentFragment == _teamsFragment ||
				_currentFragment == _coachSettingsFragment ||
				_currentFragment == _teamStatsFragment) {
			// disable back button for these fragments
		} else {
			goHomeFragment();
		}
	}

	@Override
	protected boolean isUseEvent() {
		return true;
	}

	@Override
	public String getActionBarTitle() {
		return "Home";
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent e) {
		if (EventManager.isEvent(e, EventNames.EVENT_LOAD_ALL_LINKED_BAND)) {
			loadAllLinkedBand();
		} else if (EventManager.isEvent(e, LE_LINKED_BANDS_SUCCESS)) {

		} else if (EventManager.isEvent(e, LE_LINKED_BANDS_FAILED)) {

		}
	}

	// public members
	public void showDialogFragment(BaseDialogFragment fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		android.app.Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.addToBackStack(null);
		}
		fragment.show(ft, "dialog");
	}


	public void loadedTeams(List<Team> teams) {
		_mTeams = teams;
		_adapter.setTeams(_mTeams);
	}


	public void goHomeFragment() {
		// home
		if (_teamsFragment == null)
			_teamsFragment = new TeamsFragment();

		FragmentManager fragmentManager = getSupportFragmentManager();
		if (_currentFragment != _teamsFragment) {
			fragmentManager.beginTransaction().replace(mContainerLayoutId, _teamsFragment).commit();
			_currentFragment = _teamsFragment;
		}

		setSideBarActive(null);
	}


	public void goTeamStatsFragment(Team team) {
		Logger.log(TAG, "Team selected - Team Name: %s", team._name);

		_teamStatsFragment = TeamStatsFragment.newInstance(team);
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(mContainerLayoutId, _teamStatsFragment).commit();
		_currentFragment = _teamStatsFragment;
	}


	// click handlers
	public void showNewTeam() {
		closeSideBar();

		_newTeamFragment = TeamAddFragment.newInstance();
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(mContainerLayoutId, _newTeamFragment).commit();
		_currentFragment = _newTeamFragment;
	}

	void showCurriculum() {
		closeSideBar();
		setSideBarActive(smCurriculum);
	}

	void showInbox() {
		closeSideBar();
		setSideBarActive(smInbox);
	}


	void showSupport() {
		closeSideBar();

		Logger.log(TAG, "open Zendesk from menu");

		_supportFragment = new SupportFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(mContainerLayoutId, _supportFragment).commit();
		_currentFragment = _supportFragment;

		// Added by Ruifeng to make the support menu selected.
		setSideBarActive(smSupport);
	}


	public void showAccountSettings() {
		closeSideBar();

		Logger.log(TAG, "Coach Settings item clicked from menu");

		// coach settings
		if (_coachSettingsFragment == null)
			_coachSettingsFragment = new CoachSettingsFragment();
		if (_currentFragment != _coachSettingsFragment) {
			new CoachPasswordDialog(
					this,
					new CoachPasswordDialog.CoachPasswordDialogListener() {
						@Override
						public void onEnter(String coachPassword) {
							setSideBarActive(smAccount);

							FragmentManager fragmentManager = getSupportFragmentManager();
							fragmentManager.beginTransaction().replace(mContainerLayoutId, _coachSettingsFragment).commit();
							_currentFragment = _coachSettingsFragment;
						}

						@Override
						public void onCancel() {
						}
					}
			).show();
		}
	}

	void onClickedLogOut() {
		closeSideBar();

		Logger.log(TAG, "Sign out item clicked from menu");

		final Dialog dialog = new Dialog(MainActivity.this);
		dialog.setContentView(R.layout.layout_confirmsignout);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

		Button btnOk = (Button) dialog.findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Logger.log(TAG, "Signed out");
				ServerManager.sharedInstance().logout(new RestCallback<UserService.ResLogout>() {
					@Override
					public void success(UserService.ResLogout resLogout, Response response) {
					}

					@Override
					public void failure(RetrofitError retrofitError, String message) {
					}
				});

				UserManager.sharedInstance()._currentUser = null;

				// false auto login
				UserContext.sharedInstance().setLoggedIn(false);

				// sign out
				Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});

		TextView btnCancel = (TextView) dialog.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	// private members
	void setSideBarActive(KPSideMenu menu) {
		if (_smLastActive != null) {
			_smLastActive.setActive(false);
		}

		_smLastActive = menu;

		if (_smLastActive != null)
			_smLastActive.setActive(true);
	}

	void loadAllLinkedBand() {
		Logger.log(TAG, "loading all linked bands...");
		ServerManager.sharedInstance().loadAllBand(
				UserManager.sharedInstance()._currentUser._id,
				new RestCallback<List<CommandService.FilteringDevice>>() {
					@Override
					public void failure(RetrofitError retrofitError, String message) {
						Logger.error(TAG, "Failed loading all linked bands");
						EventManager.sharedInstance().post(LE_LINKED_BANDS_FAILED);
					}

					@Override
					public void success(List<CommandService.FilteringDevice> resDeviceIds, Response response) {
						Logger.log(TAG, "Success loading all linked bands");
						EventManager.sharedInstance().post(LE_LINKED_BANDS_SUCCESS);
					}
				}
		);
	}

	// initialize functions for Zendesk engine
	void initZDEngine() {
		/*Initializing Zendesk Mobile SDK 1.1.5 for Message Base Sample Application.
		SDK initialization configuration is directly copied from Help Desk Mobil SDK settings screen.
         */


        /* Anonymous user authentication will be used. One sample user identity will be set for trial Sample Application.
		User identity will consist of following fields: Email and Name&Surname.
         */


//        Identity user;
//        user = new AnonymousIdentity.Builder()
//                .withEmailIdentifier("dayong@caloriecloud.org")
//                .withNameIdentifier("Dayong Li")
//                .build();
//        ZendeskConfig.INSTANCE.setIdentity(user);

        /*Starting a Support Activity which is necessary for ticketing(Contact Us) & self service support(Help Center).
		First the configuration will be set as follows:
        */
//        ZendeskConfig.INSTANCE.setContactConfiguration(new BaseZendeskFeedbackConfiguration() {
//            @Override
//            public String getRequestSubject() {
//                return "Support Request";
//            }
//        });
	}

	public class TeamAdapter extends ArrayAdapter {
		List<Team> teams;

		public TeamAdapter(Context context, int resource) {
			super(context, resource);
		}

		public void setTeams(List<Team> teams) {
			this.teams = teams;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (teams == null)
				return 1;
			return teams.size() + 1;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			KPSideMenu sideMenu;

			Team team = null;
			if (teams != null &&
					position < teams.size()) {
				team = teams.get(position);
			}

			sideMenu = new KPSideMenu(getContext());
			if (team != null) {
				sideMenu.setText(team._name);
				sideMenu.setImage(R.drawable.icon_team);
				sideMenu.setActiveImage(R.drawable.icon_team_active);
			} else {
				sideMenu.setText(getString(R.string.sidebar_new_team));
				sideMenu.setImage(R.drawable.icon_new_team);
			}
			return sideMenu;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == SupportFragment.PERMISSION_REQUEST_STORAGE && _currentFragment instanceof SupportFragment) {
			_currentFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}
}