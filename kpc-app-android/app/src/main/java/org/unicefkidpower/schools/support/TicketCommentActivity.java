package org.unicefkidpower.schools.support;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.zendesk.sdk.model.request.Comment;
import com.zendesk.sdk.model.request.CommentResponse;
import com.zendesk.sdk.model.request.CommentsResponse;
import com.zendesk.sdk.model.request.EndUserComment;
import com.zendesk.sdk.model.request.User;
import com.zendesk.sdk.network.RequestProvider;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;

import org.unicefkidpower.schools.BaseActivity;
import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.UIManager;
import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.ui.KPEditText;
import org.unicefkidpower.schools.ui.KPTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Dayong Li on 3/21/2017.
 */

public class TicketCommentActivity extends BaseActivity {
	private final String TAG = "TicketCommentActivity";

	public static String				EXTRA_TICKETID = "TicketId";

	private RequestProvider 			requestProvider = null;
	List<CommentResponse> 				commentList = new ArrayList<CommentResponse>();
	List<User>							userList = new ArrayList<User>();

	private RecyclerView 				commentListView = null;
	private SearchResultAdapter 		commentAdapter = new SearchResultAdapter();
	private KPEditText					commentEdit = null;

	private String 						ticketId = null;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ticketId = getIntent().getStringExtra(EXTRA_TICKETID);

		setContentView(R.layout.activity_myticket_comment);
		ImageButton btnBack = (ImageButton) findViewById(R.id.image_back);

		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		ImageButton btnAdd = (ImageButton) findViewById(R.id.image_send);
		btnAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onAddCommentClicked();
			}
		});

		commentEdit = (KPEditText) findViewById(R.id.edit_comment);

		commentListView = (RecyclerView) findViewById(R.id.list_ticket);
		commentListView.setLayoutManager(new LinearLayoutManager(TicketCommentActivity.this, LinearLayoutManager.VERTICAL, false));
		commentAdapter.setData(commentList);
		commentListView.setAdapter(commentAdapter);

		if (ticketId != null)
			initTicketList();
	}


	@Override
	protected boolean isUseEvent() {
		return false;
	}


	private void initTicketList() {
		UIManager.sharedInstance().showProgressDialog(this, null, getString(R.string.app_onemoment), true);

		requestProvider = ZendeskConfig.INSTANCE.provider().requestProvider();

		requestProvider.getComments(ticketId, new ZendeskCallback<CommentsResponse>() {
			@Override
			public void onSuccess(CommentsResponse commentsResponse) {
				UIManager.sharedInstance().dismissProgressDialog();

				commentList = commentsResponse.getComments();
				userList = commentsResponse.getUsers();

				if (commentList != null) {
					commentAdapter.setData(commentList);
					commentAdapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onError(ErrorResponse errorResponse) {
				UIManager.sharedInstance().dismissProgressDialog();
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error), getString(R.string.error_network), TicketCommentActivity.this);
			}
		});
	}


	private void onAddCommentClicked() {
		if (commentEdit.getText().toString().isEmpty())
			return;

		UIManager.sharedInstance().showProgressDialog(this, null, getString(R.string.app_onemoment), true);

		EndUserComment comment = new EndUserComment();
		comment.setValue(commentEdit.getText().toString());
		requestProvider.addComment(ticketId, comment, new ZendeskCallback<Comment>() {
			@Override
			public void onSuccess(Comment comment) {
				UIManager.sharedInstance().dismissProgressDialog();

				commentEdit.setText("");

				initTicketList();
			}

			@Override
			public void onError(ErrorResponse errorResponse) {
				UIManager.sharedInstance().dismissProgressDialog();
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error), getString(R.string.error_network), TicketCommentActivity.this);
			}
		});
	}


	private class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private List<CommentResponse> items;

		@Override
		public int getItemCount() {
			if (items == null)
				return 0;

			return items.size();
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View itemView = getLayoutInflater().inflate(R.layout.item_myticket_comment, null);

			ItemViewHolder itemViewHolder = new ItemViewHolder(itemView);
			return itemViewHolder;
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
			Object item = items.get(position);
			if (item == null)
				return;

			holder.itemView.setTag(position);

			KPTextView userView = ((ItemViewHolder) holder).userView;
			long userId = ((CommentResponse) item).getAuthorId();
			for (User user : userList) {
				if (user.getId() == userId) {
					userView.setText(user.getName());
					break;
				}
			}

			KPTextView dateView = ((ItemViewHolder) holder).dateView;
			SimpleDateFormat df = new SimpleDateFormat("d MMMM yyyy");
			dateView.setText(df.format(((CommentResponse) item).getCreatedAt()));

			KPTextView commentView = ((ItemViewHolder) holder).commentView;
			commentView.setText(((CommentResponse) item).getBody());
		}


		public void setData(List<CommentResponse> items) {
			this.items = items;
		}


		public class ItemViewHolder extends RecyclerView.ViewHolder {
			private ImageButton userImage = null;
			private KPTextView userView = null;
			private KPTextView dateView = null;
			private KPTextView commentView = null;

			public ItemViewHolder(final View itemView) {
				super(itemView);

				userView = (KPTextView) itemView.findViewById(R.id.text_user);
				dateView = (KPTextView) itemView.findViewById(R.id.text_date);
				commentView = (KPTextView) itemView.findViewById(R.id.text_comment);
			}
		}
	}
}
