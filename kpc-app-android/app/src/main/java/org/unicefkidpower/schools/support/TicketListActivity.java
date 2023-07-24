package org.unicefkidpower.schools.support;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.zendesk.sdk.model.request.Request;
import com.zendesk.sdk.network.RequestProvider;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;

import org.unicefkidpower.schools.BaseActivity;
import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.UIManager;
import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.helper.Logger;
import org.unicefkidpower.schools.ui.KPTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.unicefkidpower.schools.support.SupportFragment.PERMISSION_REQUEST_STORAGE;


/**
 * Created by Dayong Li on 3/21/2017.
 */

public class TicketListActivity extends BaseActivity {
	private final String TAG = "TicketListActivity";

	private RequestProvider 			requestProvider = null;
	private List<Request> 				ticketList = new ArrayList<Request>();

	private RecyclerView 				ticketListView = null;
	private SearchResultAdapter 		ticketAdapter = new SearchResultAdapter();



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_myticket);
		ImageButton btnBack = (ImageButton) findViewById(R.id.image_back);

		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		ImageButton btnAdd = (ImageButton) findViewById(R.id.image_add);
		btnAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onContactUsButtonClicked();
			}
		});

		ticketListView = (RecyclerView) findViewById(R.id.list_ticket);
		ticketListView.setLayoutManager(new LinearLayoutManager(TicketListActivity.this, LinearLayoutManager.VERTICAL, false));
		ticketAdapter.setData(ticketList);
		ticketListView.setAdapter(ticketAdapter);
	}


	@Override
	protected boolean isUseEvent() {
		return false;
	}


	@Override
	protected void onResume() {
		super.onResume();

		initTicketList();
	}

	private void initTicketList() {
		UIManager.sharedInstance().showProgressDialog(this, null, getString(R.string.app_onemoment), true);

		requestProvider = ZendeskConfig.INSTANCE.provider().requestProvider();

		requestProvider.getAllRequests(new ZendeskCallback<List<Request>>() {
			@Override
			public void onSuccess(List<Request> requests) {
				UIManager.sharedInstance().dismissProgressDialog();

				ticketList = requests;

				ticketAdapter.setData(ticketList);
				ticketAdapter.notifyDataSetChanged();

			}

			@Override
			public void onError(ErrorResponse errorResponse) {
				UIManager.sharedInstance().dismissProgressDialog();
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error), getString(R.string.error_network), TicketListActivity.this);
			}
		});
	}


	private void onContactUsButtonClicked() {
		checkStoragePermission();
	}

	private void checkStoragePermission() {
		if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
					PERMISSION_REQUEST_STORAGE);
		} else {
			sendEMailToSupportTeam(true);
			Logger.log("Help", "Have already storage permission");
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSION_REQUEST_STORAGE) {
			if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				sendEMailToSupportTeam(true);
			} else {
				sendEMailToSupportTeam(false);
			}
			return;
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	void sendEMailToSupportTeam(final boolean addLog) {
		// goto cantact us
		Intent intent = new Intent(this, ContactUsActivity.class);
		startActivity(intent);
	}


	private class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private List<Request> items;

		@Override
		public int getItemCount() {
			if (items == null)
				return 0;

			return items.size();
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View itemView = getLayoutInflater().inflate(R.layout.item_myticket, null);

			ItemViewHolder itemViewHolder = new ItemViewHolder(itemView);
			return itemViewHolder;
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
			Object item = items.get(position);
			if (item == null)
				return;

			holder.itemView.setTag(position);

			KPTextView ticketTextView = ((ItemViewHolder) holder).txtTicketDesc;
			ticketTextView.setText(((Request) item).getDescription());

			KPTextView dateTextView = ((ItemViewHolder) holder).txtTicketDate;
			SimpleDateFormat df = new SimpleDateFormat("d MMMM yyyy");
			dateTextView.setText(df.format(((Request) item).getUpdatedAt()));
		}


		public void setData(List<Request> items) {
			this.items = items;
		}


		public class ItemViewHolder extends RecyclerView.ViewHolder {
			private KPTextView txtTicketDesc = null;
			private KPTextView txtTicketDate = null;

			public ItemViewHolder(final View itemView) {
				super(itemView);

				txtTicketDesc = (KPTextView) itemView.findViewById(R.id.text_ticket_desc);
				txtTicketDate = (KPTextView) itemView.findViewById(R.id.text_ticket_date);
				itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Request item = items.get((int)v.getTag());
						Intent intent = new Intent(TicketListActivity.this, TicketCommentActivity.class);
						intent.putExtra(TicketCommentActivity.EXTRA_TICKETID, item.getId());

						startActivity(intent);
					}
				});
			}
		}
	}
}
