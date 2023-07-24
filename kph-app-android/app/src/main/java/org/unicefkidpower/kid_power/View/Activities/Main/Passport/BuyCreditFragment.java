package org.unicefkidpower.kid_power.View.Activities.Main.Passport;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.unicefkidpower.kid_power.Misc.BillingUtil.IabHelper;
import org.unicefkidpower.kid_power.Misc.BillingUtil.IabResult;
import org.unicefkidpower.kid_power.Misc.BillingUtil.Inventory;
import org.unicefkidpower.kid_power.Misc.BillingUtil.Purchase;
import org.unicefkidpower.kid_power.Misc.BillingUtil.SkuDetails;
import org.unicefkidpower.kid_power.Misc.KPHBroadcastSignals;
import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.Misc.KPHUtils;
import org.unicefkidpower.kid_power.Misc.Logger;
import org.unicefkidpower.kid_power.Misc.UIManager;
import org.unicefkidpower.kid_power.Misc.onActionListener;
import org.unicefkidpower.kid_power.Misc.onActivityResultListener;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.EventManager;
import org.unicefkidpower.kid_power.Model.MicroService.Bluetooth.Event.SEvent;
import org.unicefkidpower.kid_power.Model.MicroService.KPHAnalyticsService;
import org.unicefkidpower.kid_power.Model.MicroService.KPHUserService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestCallback;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestError;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.RestService;
import org.unicefkidpower.kid_power.Model.MicroService.WebService.SyncRestService;
import org.unicefkidpower.kid_power.Model.Structure.KPHCreditPurchase;
import org.unicefkidpower.kid_power.Model.Structure.KPHCreditReceipt;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserData;
import org.unicefkidpower.kid_power.Model.Structure.KPHUserSummary;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Activities.Main.MainActivity;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperNormalSizeDialogFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit.client.Response;

import static java.lang.Thread.sleep;


/**
 * Created by Dayong Li on 9/16/2015.
 * Copyright 2015 ~ 2019 UNICEF CalorieCloud org
 * DaYong@CalorieCloud.Org
 */
public class BuyCreditFragment extends SuperNormalSizeDialogFragment {
	/***********************************************************************************************
	 * Constants
	 **********************************************************************************************/
	private static final String				TAG						= "BuyCredit";

	private static final String				eid_Step1ForBuyCredit	= "Step1ForBuyCredit";
	private static final String				eid_Step2ForConsume		= "Step2ForConsume";
	private static final String				eid_Step3ForBackend		= "Step3ForBackend";
	private static final String				eid_Step4ForComplete	= "Step4ForComplete";

	public static final int					CREDIT_REQUEST_CODE		= 0x1234;

	protected static final int				BACKEND_RETRY_COUNT		= 5;
	protected int							retryCount				= BACKEND_RETRY_COUNT;
	/***********************************************************************************************
	 * End of 'Constants'
	 **********************************************************************************************/



	/***********************************************************************************************
	 * UI Controls
	 **********************************************************************************************/
	protected View							contentView				= null;

	protected KPHTextView					statusTextView			= null;
	protected KPHTextView					priceDescTextView		= null;
	protected KPHTextView					descriptionTextView		= null;

	protected RecyclerView					familyRecyclerView		= null;
	protected FamilyListAdapter				familyAdapter			= new FamilyListAdapter();
	/***********************************************************************************************
	 * End of 'UI Controls'
	 **********************************************************************************************/



	/***********************************************************************************************
	 * Data variables
	 **********************************************************************************************/
	protected IabHelper						iabHelper				= null;
	protected KPHUserService				serviceForUser			= null;
	protected KPHUserData					userData				= null;
	protected KPHUserData					boughtUser				= null;
	protected String						skuToBuy				= KPHConstants.SKU_BUY_CREDIT;
	protected SkuDetails					skuDetails				= null;

	private MemberInfo						currentUser				= null;

	protected ArrayList<MemberInfo>			childrenSummaryArray	= new ArrayList<>();

	protected boolean						didBuy					= false;
	protected boolean						isProcessing			= false;

	protected String						purchase_data			= null;
	protected String						purchase_signature		= null;
	/***********************************************************************************************
	 * End of 'Data variables'
	 **********************************************************************************************/



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Logger.log(TAG, "onCreateView : Buy Credit");

		serviceForUser = KPHUserService.sharedInstance();
		contentView = super.onCreateView(inflater, container, savedInstanceState);
		userData = serviceForUser.getUserData();

		familyRecyclerView = (RecyclerView) contentView.findViewById(R.id.familyAccountsListView);
		familyRecyclerView.setAdapter(familyAdapter);
		familyRecyclerView.setLayoutManager(new LinearLayoutManager(getSafeContext(), LinearLayoutManager.VERTICAL, false));

		statusTextView = (KPHTextView) contentView.findViewById(R.id.tvState);
		priceDescTextView = (KPHTextView) contentView.findViewById(R.id.tvPriceDesc);
		descriptionTextView = (KPHTextView) contentView.findViewById(R.id.tvDescription);

		priceDescTextView.setText(R.string.unlock_credit_default_price);

		if (serviceForUser != null && serviceForUser.getUserData() != null) {
			setCreditStateString(serviceForUser.getUserData().getCreditBalance());
		}

		View btnClose = contentView.findViewById(R.id.btnClose);
		btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isProcessing()) {
					showErrorDialog(getSafeContext().getString(R.string.you_are_processing_purchases_now));
				} else {
					dismiss();
				}
			}
		});


		if (getParentActivity() instanceof MainActivity) {
			MainActivity activity = (MainActivity) getParentActivity();
			activity.setSharedListener(activityResultListener);
			iabHelper = activity.getInAppBillingHelper();
		}


		View btnBuy = contentView.findViewById(R.id.btnBuyCredit);
		if (btnBuy != null) {
			btnBuy.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Logger.log(TAG, "onCreateView : clicked buy button");
					if (!serviceForUser.enabledPurchases()) {
						showErrorDialog(getSafeContext().getString(R.string.disabled_purchases));
						Logger.error(TAG, getSafeContext().getString(R.string.disabled_purchases));
						return;
					}

					if (isProcessing()) {
						showErrorDialog(getSafeContext().getString(R.string.you_are_processing_purchases_now));
						Logger.error(TAG, getSafeContext().getString(R.string.you_are_processing_purchases_now));
						return;
					}

					EventManager.sharedInstance().post(eid_Step1ForBuyCredit, skuToBuy, null);
				}
			});
		}

		return contentView;
	}


	@Override
	public void startAction() {
		if (serviceForUser.enabledPurchases() && !iabHelper.isAvailablePurchase()) {
			showErrorDialog(getSafeContext().getString(R.string.buy_credit_not_setup));
			dismiss();
			return;
		}

		currentUser = new MemberInfo();
		currentUser.isActive = true;
		currentUser.id = userData.getId();
		currentUser.handle = userData.getHandle() + "(me)";
		currentUser.friendlyName = userData.getFriendlyName() + "(me)";
		currentUser.avatar = userData.getAvatarId();

		fetchChildrenSummary();
	}


	@Override
	public int contentLayout() {
		return R.layout.fragment_buy_credit;
	}


	@Override
	public void onStart() {
		super.onStart();

		if (!EventManager.sharedInstance().isRegistered(this)) {
			EventManager.sharedInstance().register(this);
		}
	}


	@Override
	public void onDetach() {
		super.onDetach();

		if (EventManager.sharedInstance().isRegistered(this)) {
			EventManager.sharedInstance().unregister(this);
		}

		if (didBuy) {
			Intent intent = new Intent(KPHBroadcastSignals.BROADCAST_SIGNAL_CREDIT_BALANCE_UPDATED);
			LocalBroadcastManager.getInstance(getSafeContext()).sendBroadcast(intent);
		}
	}


	private void fetchChildrenSummary() {
		showProgressDialog();

		childrenSummaryArray = new ArrayList<>();
		childrenSummaryArray.add(currentUser);

		RestService.get().getChildrenSummary(
				KPHUserService.sharedInstance().getUserData().getId(),
				new RestCallback<List<KPHUserSummary>>() {
					@Override
					public void failure(RestError restError) {
						showErrorDialog(KPHUtils.sharedInstance().getNonNullMessage(restError));
					}

					@Override
					public void success(List<KPHUserSummary> kphUserSummaries, Response response) {
						dismissProgressDialog();

						for (KPHUserSummary user : kphUserSummaries) {
							MemberInfo member = new MemberInfo();
							member.isActive = false;
							member.id = user.getId();
							member.handle = user.getHandle();
							member.friendlyName = user.getFriendlyName();
							member.avatar = user.getAvatarId();

							childrenSummaryArray.add(member);
						}

						familyAdapter.notifyDataSetChanged();

						// Query inventory
						queryInventory();
					}
				}
		);
	}


	private void queryInventory() {
		List additionalSkuList = new ArrayList<>();
		additionalSkuList.add(KPHConstants.SKU_BUY_CREDIT);

		setProcessing(true);
		iabHelper.queryInventoryAsync(true, additionalSkuList, gotInventoryListener);
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventMainThread(SEvent event) {
		if (EventManager.isEvent(event, eid_Step1ForBuyCredit)) {
			if (isProcessing()) {
				handleError(-1, "Buy : Now it is processing other action!");
				return;
			}

			if (!(event.object instanceof String)) {
				handleError(-1, "Buy : Parameter is wrong!");
				return;
			}

			String str = String.format("onEventMainThread : buy credit : Step1(buy product) -> %s", skuToBuy);
			Logger.log(TAG, str);
			setProcessing(true);

			try {
				iabHelper.launchPurchaseFlow(
						getParentActivity(),
						skuToBuy,
						CREDIT_REQUEST_CODE,
						new IabHelper.OnIabPurchaseFinishedListener() {
							@Override
							public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
								if (result.isFailure()) {
									// maybe iap library show error dialog
									String error_message = result.getMessage();
									Logger.log(TAG, "onEventMainThread : Step1 -> failed : \"%s\"", error_message);

									if (result.getResponse() == -1005) {
										// don't show error message when user did cancel purchase
										handleError(-2, null);
									} else {
										handleError(-2, error_message);
									}

									return;
								}

								if (!verifyDeveloperPayload(purchase)) {
									Logger.log(TAG, "onEventMainThread : Step1 -> verify failed : \"%s\"", "Verify failed");
									handleError(-2, "Cannot verify this purchases!");
									return;
								}

								Logger.log(TAG, "onEventMainThread : Step1(buy product) -> success");

								EventManager.sharedInstance().post(eid_Step2ForConsume, purchase, null);
							}
						},
						SyncRestService.getUserToken()
				);
			} catch (IllegalStateException illegalException) {
				handleError(-1, "You are processing purchases now.");
			}
		} else if (EventManager.isEvent(event, eid_Step2ForConsume)) {
			if (!(event.object instanceof Purchase)) {
				handleError(-5, "Consume : Parameter is wrong!");
				return;
			}

			Logger.log(TAG, "onEventMainThread : Step2(consume purchase) -> ");

			Purchase purchase = (Purchase) event.object;

			iabHelper.consumeAsync(
					purchase,
					new IabHelper.OnConsumeFinishedListener() {
						@Override
						public void onConsumeFinished(Purchase purchase, IabResult result) {
							String signature = null;
							if (purchase != null)
								signature = purchase.getSignature();

							Logger.log(TAG, "Consumption finished. \nPurchase: " + purchase + ", \nsignature: " + signature + ", \nresult: " + result);

							if (iabHelper == null) {
								handleError(-6, "internal error");
								return;
							}

							if (!result.isSuccess()) {
								Logger.log(TAG, "onEventMainThread : Step2(consume purchase) -> consumption failed");
								handleError(-6, "Cannot consumption purchases");
								return;
							}

							Logger.log(TAG, "onEventMainThread : Step2(consume purchase) -> consumption success");

							EventManager.sharedInstance().post(eid_Step3ForBackend, purchase, null);
						}
					}
			);
		} else if (EventManager.isEvent(event, eid_Step3ForBackend)) {
			if (!(event.object instanceof Purchase)) {
				handleError(-7, "Backend : Parameter is wrong!");
				return;
			}

			Logger.log(TAG, "onEventMainThread : Step3(call backend service) -> ");
			final Purchase purchase = (Purchase) event.object;

			KPHCreditReceipt receipt = new KPHCreditReceipt();
			receipt.purchase = new KPHCreditPurchase(purchase);
			receipt.signature = purchase_signature;

			serviceForUser.verifyPurchaseCredit(
					receipt,
					currentUser.id,
					new onActionListener() {
						@Override
						public void completed(Object object) {
							if (object == null || !(object instanceof KPHUserData)) {
								handleError(-8, "Argument is not compatible");
								return;
							}

							Logger.log(TAG, "verifyPurchaseCredit : Step3(call backend service) -> success");
							boughtUser = (KPHUserData) object;
							if (userData != null && boughtUser.getId() == userData.getId()) {
								userData = boughtUser;

								KPHUserData savedUserData = KPHUserService.sharedInstance().getUserData();
								savedUserData.setCreditBalance(userData.getCreditBalance());
								KPHUserService.sharedInstance().saveUserData(savedUserData);

								if (skuDetails != null) {
									Logger.log(TAG, "verifyPurchaseCredit : Purchase In-app notified");
									KPHAnalyticsService.sharedInstance().logPurchase(skuDetails.getSku(), skuDetails.getPriceAmount(), skuDetails.getCurrency());
								}
							}

							EventManager.sharedInstance().post(eid_Step4ForComplete, skuToBuy, null);
						}

						@Override
						public void failed(int code, String message) {
							if (code == -2 && retryCount > 0) {
								// if network error
								retryCount--;

								try {
									sleep(2000);
								} catch (InterruptedException ex) {
									ex.printStackTrace();
								}

								Logger.log(TAG, "verifyPurchaseCredit : Step3(call backend service) -> failed, try again");
								EventManager.sharedInstance().post(eid_Step3ForBackend, purchase, null);
							} else {
								Logger.log(TAG, "verifyPurchaseCredit : Step3(call backend service) -> failed(%s)", message);
								handleError(-8, message);
							}
						}
					}
			);
		} else if (EventManager.isEvent(event, eid_Step4ForComplete)) {
			setProcessing(false);

			if (!(event.object instanceof String)) {
				handleError(-9, "Complete : Parameter is wrong!");
				return;
			}

			if (boughtUser.getId() == userData.getId()) {
				setCreditStateString(userData.getCreditBalance());

				didBuy = true;
			}
			Logger.log(TAG, "onEventMainThread : buy credit completed ");

			getParentActivity().showAlertDialog("Success", getSafeContext().getString(R.string.you_have_bought_one_credit), null);
		}
	}


	protected void handleError(int code, String message) {
		setProcessing(false);

		if (message == null || message.isEmpty()) {
			return;
		}

		Logger.error(TAG, "error : code=%d, message=%s", code, message);
		showErrorDialog(message);
	}


	/**
	 * Verifies the developer payload of a purchase.
	 */
	boolean verifyDeveloperPayload(Purchase p) {
		String payload = p.getDeveloperPayload();

		String token = SyncRestService.getUserToken();
		if (token == null || token.isEmpty())
			return false;

		if (!payload.equals(token))
			return false;

		return true;
	}


	protected boolean isProcessing() {
		return isProcessing;
	}


	protected void setProcessing(boolean isProcess) {
		isProcessing = isProcess;

		if (getParentActivity() == null)
			return;

		if (isProcess) {
			showProgressDialog(getSafeContext().getString(R.string.buy_credit), getSafeContext().getString(R.string.processing));
		} else {
			dismissProgressDialog();
		}
	}


	protected void setCreditStateString(int count) {
		String state;

		if (count == 1) {
			state = getSafeContext().getString(R.string.you_have_one_credit);
		} else {
			state = String.format(getSafeContext().getString(R.string.you_have_count_credits), count);
		}
		statusTextView.setText(state);
	}


	// Listener that's called when we finish querying the items and subscriptions we own
	private IabHelper.QueryInventoryFinishedListener gotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		@Override
		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
			Logger.log(TAG, "gotInventoryListener : Query inventory finished.");

			// Have we been disposed of in the meantime? If so, quit.
			if (iabHelper == null) {
				setProcessing(false);
				return;
			}

			// Is it a failure?
			if (result.isFailure()) {
				Logger.error(TAG, "gotInventoryListener : Failed to query inventory: " + result);
				handleError(0, null);
				return;
			}

			skuDetails = inventory.getSkuDetails(KPHConstants.SKU_BUY_CREDIT);

/* Sku Details is JSON object as follows. DO NOT DELETE
{
	"productId":"org.unicefkidpower.kph.credit_1",
	"type":"inapp",
	"price":"NT$122.22",
	"price_amount_micros":122221365,
	"price_currency_code":"TWD",
	"title":"One Mission Credit (UNICEF Kid Power)",
	"description":"Buy One Mission Credit"
}
*/

			Logger.log(TAG, "gotInventoryListener : Query inventory was successful.\nSKU:%s, Type:%s, Title:%s, Description:%s, Price:%s, amount:%d, currency:%s",
					skuDetails.getSku(), skuDetails.getType(), skuDetails.getTitle(),
					skuDetails.getDescription(), skuDetails.getPrice(),
					skuDetails.getPriceAmount(), skuDetails.getCurrency());

			priceDescTextView.setText(getSafeContext().getString(R.string.unlock_credit_price_format, skuDetails.getPrice()));

			// Check for gas delivery -- if we own gas, we should fill up the tank immediately
			Purchase Purchase = inventory.getPurchase(KPHConstants.SKU_BUY_CREDIT);
			if (Purchase != null) {
				Logger.log(TAG, "gotInventoryListener : Consuming purchase.");
				iabHelper.consumeAsync(Purchase, consumeFinishedListener);
			} else {
				setProcessing(false);
			}
		}
	};


	private IabHelper.OnConsumeFinishedListener consumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
		@Override
		public void onConsumeFinished(Purchase purchase, IabResult result) {
			Logger.log(TAG, "consumeFinishedListener : Consumption finished for in-completed purchase. Purchase: " + purchase + ", result: " + result);

			setProcessing(false);
			if (iabHelper == null) {
				handleError(-6, "internal error");
				return;
			}

			if (!result.isSuccess()) {
				Logger.log(TAG, "consumeFinishedListener : Step2(consume purchase) -> consumption failed");
				handleError(-6, "consumeFinishedListener : Cannot consumption purchases");
				return;
			}

			Logger.log(TAG, "consumeFinishedListener : Step2(consume purchase) -> consumption success");

			EventManager.sharedInstance().post(eid_Step3ForBackend, purchase, null);
		}
	};


	public class FamilyListAdapter extends RecyclerView.Adapter {
		public FamilyListAdapter() {
			super();
		}

		@Override
		public int getItemCount() {
			return childrenSummaryArray.size();
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			RecyclerView.ViewHolder holder;

			View itemView = getParentActivity().getLayoutInflater().inflate(R.layout.item_buy_credit_family, null);
			holder = new MemberItemHolder(itemView);

			return holder;
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
			MemberInfo user = childrenSummaryArray.get(position);
			user.holder = (MemberItemHolder)holder;
			if (user.isActive) {
				currentUser = user;
			}

			((MemberItemHolder)holder).setMemberInfo(user);
		}
	}


	/**
	 * Variable to detect pay result
	 */
	private onActivityResultListener activityResultListener = new onActivityResultListener() {
		/**
		 * Method to process payment result
		 *
		 * @param requestCode
		 * @param resultCode
		 * @param intent
		 *
		 * @return Returns true if In-App Purchase succeed, false otherwise
		 */
		@Override
		public boolean onSharedActivityResult(int requestCode, int resultCode, Intent intent) {
			if (requestCode != CREDIT_REQUEST_CODE || iabHelper == null)
				return false;

			String str = String.format("activityResultListener : onActivityResult(" + requestCode + "," + resultCode + ") : " + intent);
			Logger.log(TAG, str);

			purchase_data = intent.getStringExtra("INAPP_PURCHASE_DATA");
			purchase_signature = intent.getStringExtra("INAPP_DATA_SIGNATURE");

			if (purchase_data != null && purchase_signature != null) {
				Logger.log(TAG, "activityResultListener : purchase_data : " + purchase_data + " , " + "purchase_signature : " + purchase_signature);
			}

			// Pass on the activity result to the helper for handling
			if (!iabHelper.handleActivityResult(requestCode, resultCode, intent)) {
				setProcessing(false);
				return false;
			}

			Logger.log(TAG, "onActivityResult handled by IABUtil.");

			return true;
		}
	};


	private class MemberItemHolder extends RecyclerView.ViewHolder {
		public ImageView		ivRadio			= null;
		public RadioButton		rdActive		= null;
		public ImageView		ivAvatar		= null;
		public KPHTextView		txtUserName		= null;

		public MemberItemHolder(View itemView) {
			super(itemView);

			ivRadio = (ImageView) itemView.findViewById(R.id.ivRadio);
			rdActive = (RadioButton) itemView.findViewById(R.id.rdActive);

			ColorStateList colorStateList = new ColorStateList(new int[][] {
					{-android.R.attr.state_checked},
					{android.R.attr.state_checked}},
					new int[] {Color.WHITE, Color.rgb(0x80, 0x80, 0x80)});
			if (Build.VERSION.SDK_INT >= 21) {
				rdActive.setButtonTintList(colorStateList);
			} else {
			}


			ivAvatar = (ImageView) itemView.findViewById(R.id.ivAvatar);
			txtUserName = (KPHTextView) itemView.findViewById(R.id.txtUsername);

			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onClickedSelf(v);
				}
			});
		}

		public void setMemberInfo(MemberInfo info) {
			itemView.setTag(info);

			Drawable drawable = serviceForUser.getAvatarSmallDrawable(info.avatar);
			if (drawable != null)
				ivAvatar.setImageDrawable(drawable);
			else {
				ivAvatar.setImageDrawable(
						UIManager.sharedInstance().getImageDrawable(R.drawable.avatar_placeholder)
				);
			}

			txtUserName.setText(info.handle);
			rdActive.setChecked(info.isActive);
		}


		private void onClickedSelf(View v) {
			if (currentUser != null) {
				currentUser.isActive = false;
				currentUser.holder.rdActive.setChecked(false);
			}

			currentUser = (MemberInfo) v.getTag();
			currentUser.isActive = true;
			currentUser.holder.rdActive.setChecked(true);
		}
	}

	public class MemberInfo {
		public boolean				isActive;
		public int					id;
		public String				handle;
		public String				friendlyName;
		public String				avatar;

		public MemberItemHolder		holder;
	}

}
