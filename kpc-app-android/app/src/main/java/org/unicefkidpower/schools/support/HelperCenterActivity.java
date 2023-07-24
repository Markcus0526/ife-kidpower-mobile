package org.unicefkidpower.schools.support;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zendesk.sdk.model.helpcenter.Article;
import com.zendesk.sdk.model.helpcenter.Category;
import com.zendesk.sdk.model.helpcenter.HelpCenterSearch;
import com.zendesk.sdk.model.helpcenter.SearchArticle;
import com.zendesk.sdk.model.helpcenter.Section;
import com.zendesk.sdk.network.HelpCenterProvider;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;

import org.unicefkidpower.schools.BaseActivity;
import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.UIManager;
import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.model.HelperCategoryItem;
import org.unicefkidpower.schools.model.HelperChildItem;
import org.unicefkidpower.schools.ui.KPEditText;
import org.unicefkidpower.schools.ui.KPExpandableListView;
import org.unicefkidpower.schools.ui.KPTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by Dayong Li on 3/21/2017.
 */

public class HelperCenterActivity extends BaseActivity {
	private final String TAG = "HelperCenterActivity";

	private List<HelperCategoryItem> 		categoryList = new ArrayList<HelperCategoryItem>();
	private List<HelperChildItem> 			searchList = new ArrayList<HelperChildItem>();

	private KPExpandableListView 			lstContent = null;
	private KPEditText 						edtSearch = null;
	private ImageButton 					btnSearch = null;
	private RecyclerView 					searchResultListView = null;

	private SearchResultAdapter 			searchAdapter = new SearchResultAdapter();
	private ListViewAdapter 				listAdapter = null;

	private int 							curCategoryPos = 0;
	private int 							curSectionCount = 0;
	private int 							curSectionIndex = 0;

	private HelpCenterProvider helperProvider;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_helpercenter);
		ImageButton btnBack = (ImageButton) findViewById(R.id.image_back);

		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		edtSearch = (KPEditText) findViewById(R.id.edit_search);
		edtSearch.setVisibility(View.GONE);
		edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					onPerformSearch();
					return true;
				}
				return false;
			}
		});

		btnSearch = (ImageButton) findViewById(R.id.image_search);
		btnSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSearhClicked();
			}
		});

		ImageButton btnMine = (ImageButton) findViewById(R.id.image_mine);
		btnMine.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onTicketClicked();
			}
		});

		lstContent = (KPExpandableListView) findViewById(R.id.list_category);
		lstContent.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				// We call collapseGroupWithAnimation(int) and
				// expandGroupWithAnimation(int) to animate group
				// expansion/collapse.
				if (lstContent.isGroupExpanded(groupPosition)) {
					lstContent.collapseGroupWithAnimation(groupPosition);
				} else {
					curCategoryPos = groupPosition;
					getArticlesByCategory();
				}
				return true;
			}
		});
		lstContent.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				onHelpCenterDetailClicked(groupPosition, childPosition);
				return true;
			}
		});


		listAdapter = new ListViewAdapter(this);
		listAdapter.setData(categoryList);
		lstContent.setAdapter(listAdapter);

		searchResultListView = (RecyclerView) findViewById(R.id.list_search);
		searchResultListView.setLayoutManager(new LinearLayoutManager(HelperCenterActivity.this, LinearLayoutManager.VERTICAL, false));
		searchAdapter.setData(searchList);
		searchResultListView.setAdapter(searchAdapter);
		searchResultListView.setVisibility(View.GONE);

		initCategoryList();
	}


	@Override
	protected boolean isUseEvent() {
		return false;
	}


	private void initCategoryList() {
		UIManager.sharedInstance().showProgressDialog(this, null, getString(R.string.app_onemoment), true);

		helperProvider = ZendeskConfig.INSTANCE.provider().helpCenterProvider();

		helperProvider.getCategories(new ZendeskCallback<List<Category>>() {
			@Override
			public void onSuccess(List<Category> categories) {
				UIManager.sharedInstance().dismissProgressDialog();

				for (Category category : categories) {
					HelperCategoryItem item = new HelperCategoryItem();

					item.id = category.getId();
					item.description = category.getDescription();
					item.htmlUrl = category.getHtmlUrl();
					item.name = category.getName();
					item.locale = category.getLocale();
					item.position = category.getPosition();

					if (category.isOutdated())
						continue;

					categoryList.add(item);
				}

				listAdapter.setData(categoryList);
				listAdapter.notifyDataSetChanged();
			}

			@Override
			public void onError(ErrorResponse errorResponse) {
				UIManager.sharedInstance().dismissProgressDialog();
			}
		});
	}


	private void getArticlesByCategory() {
		if (categoryList.get(curCategoryPos).items.size() > 0) {
			lstContent.expandGroupWithAnimation(curCategoryPos);
			return;
		}

		UIManager.sharedInstance().showProgressDialog(this, null, getString(R.string.app_onemoment), true);

		long catid = categoryList.get(curCategoryPos).id;

		helperProvider.getSections(catid, new ZendeskCallback<List<Section>>() {
			@Override
			public void onSuccess(List<Section> sections) {

				curSectionCount = sections.size();
				curSectionIndex = 0;

				int i = 1;
				for (Section section : sections) {
					HelperChildItem secItem = new HelperChildItem();

					secItem.id = section.getId();
					secItem.sectionId = section.getId();
					secItem.categoryId = section.getCategoryId();
					secItem.url = section.getUrl();
					secItem.description = section.getDescription();
					secItem.htmlUrl = section.getHtmlUrl();
					secItem.name = section.getName();
					secItem.locale = section.getLocale();
					secItem.position = section.getPosition();
					secItem.isSection = true;
					secItem.sortId = i * 1000;
					i++;

					if (section.isOutdated())
						continue;

					categoryList.get(curCategoryPos).items.add(secItem);

					helperProvider.getArticles(secItem.id, new ZendeskCallback<List<Article>>() {
						@Override
						public void onSuccess(List<Article> articles) {
							curSectionIndex++;

							if (articles.size() == 0)
								return;

							int parentSortId = 0;
							for (HelperChildItem item : categoryList.get(curCategoryPos).items) {
								if (item.isSection && item.id == articles.get(0).getSectionId()) {
									parentSortId = item.sortId;
									break;
								}
							}

							int j = 1;
							for (Article article : articles) {
								HelperChildItem artItem = new HelperChildItem();

								artItem.id = article.getId();
								artItem.sectionId = article.getSectionId();
								artItem.url = article.getUrl();
								artItem.htmlUrl = article.getHtmlUrl();
								artItem.name = article.getTitle();
								artItem.locale = article.getLocale();
								artItem.isSection = false;
								artItem.sortId = parentSortId + j;
								j++;

								categoryList.get(curCategoryPos).items.add(artItem);
							}

							if (curSectionIndex == curSectionCount) {
								UIManager.sharedInstance().dismissProgressDialog();

								sortCategoryList();

								listAdapter.setData(categoryList);
								listAdapter.notifyDataSetChanged();

								lstContent.expandGroupWithAnimation(curCategoryPos);
							}
						}

						@Override
						public void onError(ErrorResponse errorResponse) {
							curSectionIndex++;

							if (curSectionIndex == curSectionCount) {
								UIManager.sharedInstance().dismissProgressDialog();

								sortCategoryList();

								listAdapter.setData(categoryList);
								listAdapter.notifyDataSetChanged();

								lstContent.expandGroupWithAnimation(curCategoryPos);
							}
						}
					});
				}

			}

			@Override
			public void onError(ErrorResponse errorResponse) {
				UIManager.sharedInstance().dismissProgressDialog();
			}
		});
	}


	private void onTicketClicked() {
		Intent intent = new Intent(HelperCenterActivity.this, TicketListActivity.class);

		startActivity(intent);
	}


	private void onHelpCenterDetailClicked(int groupPosition, int childPosition) {
		// goto helper center
		HelperChildItem selItem = categoryList.get(groupPosition).items.get(childPosition);

		if (selItem.isSection)
			return;

		String sectionTitle = categoryList.get(groupPosition).name;
		for (HelperChildItem item : categoryList.get(groupPosition).items) {
			if (item.isSection && item.id == selItem.sectionId) {
				sectionTitle = item.name;
				break;
			}
		}

		Intent intent = new Intent(this, HelperCenterDetailActivity.class);
		intent.putExtra(HelperCenterDetailActivity.EXTRA_ARTICLEID, selItem.id);
		intent.putExtra(HelperCenterDetailActivity.EXTRA_SECTIONNAME, sectionTitle);

		startActivity(intent);
	}


	private void sortCategoryList() {
		Collections.sort(categoryList.get(curCategoryPos).items, new Comparator<HelperChildItem>() {
			@Override
			public int compare(HelperChildItem lhs, HelperChildItem rhs) {
				if (lhs.sortId < rhs.sortId)
					return -1;
				else
					return 1;
			}
		});
	}


	private void onSearhClicked() {
		if (edtSearch.getVisibility() == View.VISIBLE) {
			if (edtSearch.getText().toString().isEmpty()) {
				btnSearch.setImageResource(R.drawable.lb_ic_in_app_search);

				edtSearch.setVisibility(View.GONE);
				searchResultListView.setVisibility(View.GONE);
				lstContent.setVisibility(View.VISIBLE);
			} else {
				edtSearch.setText("");
			}
		} else {
			btnSearch.setImageResource(R.drawable.close);

			edtSearch.setVisibility(View.VISIBLE);
			searchResultListView.setVisibility(View.VISIBLE);
			lstContent.setVisibility(View.GONE);
		}
	}

	private void onPerformSearch() {
		UIManager.sharedInstance().showProgressDialog(this, null, getString(R.string.app_onemoment), true);

		helperProvider = ZendeskConfig.INSTANCE.provider().helpCenterProvider();

		HelpCenterSearch.Builder searchQuery = new HelpCenterSearch.Builder();
		searchQuery.withQuery(edtSearch.getText().toString());
		helperProvider.searchArticles(searchQuery.build(), new ZendeskCallback<List<SearchArticle>>() {
			@Override
			public void onSuccess(List<SearchArticle> searchArticles) {
				UIManager.sharedInstance().dismissProgressDialog();

				searchList.clear();

				int i = 1;
				for (SearchArticle article : searchArticles) {
					HelperChildItem item = new HelperChildItem();
					item.id = article.getArticle().getId();
					item.sectionId = article.getArticle().getSectionId();
					item.name = article.getArticle().getTitle();

					if (article.getSection() != null)
						item.sectionName = article.getSection().getName();
					else
						item.sectionName = "";

					item.sortId = i;
					i++;

					searchList.add(item);
				}

				searchAdapter.setData(searchList);
				searchAdapter.notifyDataSetChanged();
			}

			@Override
			public void onError(ErrorResponse errorResponse) {
				UIManager.sharedInstance().dismissProgressDialog();
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error), getString(R.string.error_network), HelperCenterActivity.this);
			}
		});

	}


	private class ListViewAdapter extends KPExpandableListView.KPExpandableListAdapter {

		private LayoutInflater inflater;

		private List<HelperCategoryItem> items;

		public ListViewAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}

		public void setData(List<HelperCategoryItem> items) {
			this.items = items;
		}

		@Override
		public HelperChildItem getChild(int groupPosition, int childPosition) {
			return items.get(groupPosition).items.get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			HelperChildItem item = getChild(groupPosition, childPosition);

			ChildHolder holder = new ChildHolder();

			if (item.isSection) {
				convertView = inflater.inflate(R.layout.item_helpercenter_section, parent, false);
			} else {
				convertView = inflater.inflate(R.layout.item_helpercenter_child, parent, false);
			}

			holder.title = (KPTextView) convertView.findViewById(R.id.text_title);
			convertView.setTag(holder);

			holder.title.setText(item.name);

			return convertView;
		}

		@Override
		public int getRealChildrenCount(int groupPosition) {
			return items.get(groupPosition).items.size();
		}

		@Override
		public HelperCategoryItem getGroup(int groupPosition) {
			return items.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return items.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			ListViewAdapter.GroupHolder holder;
			HelperCategoryItem item = getGroup(groupPosition);
			if (convertView == null) {
				holder = new ListViewAdapter.GroupHolder();
				convertView = inflater.inflate(R.layout.item_helpercenter_category, parent, false);
				holder.title = (KPTextView) convertView.findViewById(R.id.text_title);
				convertView.setTag(holder);
			} else {
				holder = (ListViewAdapter.GroupHolder) convertView.getTag();
			}

			holder.title.setText(item.name);

			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			return true;
		}

		private class ChildHolder {
			KPTextView title;
		}

		private class GroupHolder {
			KPTextView title;
		}
	}

	private class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private List<HelperChildItem> items;

		@Override
		public int getItemCount() {
			if (items == null)
				return 0;

			return items.size();
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View itemView = getLayoutInflater().inflate(R.layout.item_helpercenter_search, null);

			ItemViewHolder itemViewHolder = new ItemViewHolder(itemView);
			return itemViewHolder;
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
			Object item = items.get(position);
			if (item == null)
				return;

			holder.itemView.setTag(position);

			KPTextView txtArticle = ((ItemViewHolder) holder).txtArticle;
			txtArticle.setText(((HelperChildItem) item).name);

			KPTextView txtSection = ((ItemViewHolder) holder).txtSection;
			txtSection.setText(((HelperChildItem) item).sectionName);
		}


		public void setData(List<HelperChildItem> items) {
			this.items = items;
		}


		public class ItemViewHolder extends RecyclerView.ViewHolder {
			private KPTextView txtArticle = null;
			private KPTextView txtSection = null;

			public ItemViewHolder(final View itemView) {
				super(itemView);

				txtArticle = (KPTextView) itemView.findViewById(R.id.text_article);
				txtSection = (KPTextView) itemView.findViewById(R.id.text_section);
				itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						HelperChildItem item = items.get((int)v.getTag());

						Intent intent = new Intent(HelperCenterActivity.this, HelperCenterDetailActivity.class);
						intent.putExtra(HelperCenterDetailActivity.EXTRA_ARTICLEID, item.id);
						intent.putExtra(HelperCenterDetailActivity.EXTRA_SECTIONNAME, item.sectionName);

						startActivity(intent);
					}
				});
			}
		}
	}
}
