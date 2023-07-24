package org.unicefkidpower.schools.support;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.zendesk.sdk.model.helpcenter.Article;
import com.zendesk.sdk.model.helpcenter.Attachment;
import com.zendesk.sdk.model.helpcenter.AttachmentType;
import com.zendesk.sdk.network.HelpCenterProvider;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;

import org.unicefkidpower.schools.BaseActivity;
import org.unicefkidpower.schools.R;
import org.unicefkidpower.schools.UIManager;
import org.unicefkidpower.schools.helper.AlertDialogWrapper;
import org.unicefkidpower.schools.model.HelperCenterAttachment;
import org.unicefkidpower.schools.ui.KPTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Dayong Li on 3/21/2017.
 */

public class HelperCenterDetailActivity extends BaseActivity {
	private final String				TAG = "HelperCenterDetailActivity";

	public static String				EXTRA_ARTICLEID = "ArticleId";
	public static String				EXTRA_SECTIONNAME = "SectionName";

	private List<HelperCenterAttachment> 	attachList = new ArrayList<HelperCenterAttachment>();

	private KPTextView 					txtTitle;
	private KPTextView 					txtCaption;
	private WebView 					txtContent;
	private KPTextView 					txtAuther;
	private LinearLayout				layoutAttach;

	private long 						articleId = 0;
	private String 						titleName = "";

	private HelpCenterProvider 			helperProvider;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		articleId = getIntent().getLongExtra(EXTRA_ARTICLEID, 0);
		titleName = getIntent().getStringExtra(EXTRA_SECTIONNAME);

		setContentView(R.layout.activity_helpercenter_detail);

		txtTitle = (KPTextView) findViewById(R.id.text_title);
		txtTitle.setText(titleName);

		txtCaption = (KPTextView) findViewById(R.id.text_caption);
		txtContent = (WebView) findViewById(R.id.text_content);
		txtAuther = (KPTextView) findViewById(R.id.text_author);
		layoutAttach = (LinearLayout) findViewById(R.id.layout_attachfile);

		ImageButton btnBack = (ImageButton) findViewById(R.id.image_back);
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		initAriticleInfo();
	}


	@Override
	protected boolean isUseEvent() {
		return false;
	}


	private void initAriticleInfo() {
		UIManager.sharedInstance().showProgressDialog(this, null, getString(R.string.app_onemoment), true);

		helperProvider = ZendeskConfig.INSTANCE.provider().helpCenterProvider();

		helperProvider.getArticle(articleId, new ZendeskCallback<Article>() {
			@Override
			public void onSuccess(Article article) {

				if (article == null)
					return;

				txtTitle.setText(article.getTitle());
				txtCaption.setText(article.getTitle());

				String txt = article.getBody();
				//txt = StringEscapeUtils.escapeHtml4(txt);
				//txt = StringEscapeUtils.unescapeHtml4(txt);
				txtContent.loadData(escapeHTML(txt), "text/html", "UTF-8");

				SimpleDateFormat df = new SimpleDateFormat("MMMM d, yyyy HH:mm");
				txtAuther.setText(article.getAuthor().getName() + " · " + df.format(article.getUpdatedAt()));

				helperProvider.getAttachments(articleId, AttachmentType.BLOCK, new ZendeskCallback<List<Attachment>>() {
					@Override
					public void onSuccess(List<Attachment> attachments) {
						UIManager.sharedInstance().dismissProgressDialog();

						int i = 1;
						for (Attachment attach : attachments) {
							HelperCenterAttachment item = new HelperCenterAttachment();
							item.id = attach.getId();
							item.fileName = attach.getFileName();
							item.size = attach.getSize();
							item.url = attach.getUrl();
							item.contentUrl = attach.getContentUrl();
							item.sortId = i;
							i++;

							attachList.add(item);

							addAttachmentItem(item);
						}
					}

					@Override
					public void onError(ErrorResponse errorResponse) {
						UIManager.sharedInstance().dismissProgressDialog();
						AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error), getString(R.string.error_network), HelperCenterDetailActivity.this);
					}
				});
			}

			@Override
			public void onError(ErrorResponse errorResponse) {
				UIManager.sharedInstance().dismissProgressDialog();
				AlertDialogWrapper.showErrorAlert(getString(R.string.dialog_error), getString(R.string.error_network), HelperCenterDetailActivity.this);
			}
		});


	}

	private void addAttachmentItem(HelperCenterAttachment item) {
		View itemView = LayoutInflater.from(HelperCenterDetailActivity.this).inflate(R.layout.item_helpercenter_attachment, null);
		itemView.setTag(item.sortId);
		layoutAttach.addView(itemView);

		KPTextView txtFileName = (KPTextView) itemView.findViewById(R.id.text_filename);
		txtFileName.setText(item.fileName);
		KPTextView txtFileSize = (KPTextView) itemView.findViewById(R.id.text_filesize);
		txtFileSize.setText(humanReadableByteCount(item.size, true));
		View vwSeperator = (View) itemView.findViewById(R.id.view_seperator);
		if (item.sortId == 1) {
			vwSeperator.setVisibility(View.GONE);
		}

		itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onAttachmentClicked((int)v.getTag());
			}
		});
	}

	private void onAttachmentClicked(int tag) {
		for (HelperCenterAttachment item : attachList) {
			if (item.sortId == tag) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.contentUrl));
				startActivity(intent);
				break;
			}
		}
	}


	private String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}


	public static final String escapeHTML(String s){
		StringBuffer sb = new StringBuffer();
		int n = s.length();
		for (int i = 0; i < n; i++) {
			char c = s.charAt(i);
			switch (c) {
				case '&': sb.append("&amp;"); break;
				//case '"': sb.append("&quot;"); break;
				case 'à': sb.append("&agrave;");break;
				case 'À': sb.append("&Agrave;");break;
				case 'â': sb.append("&acirc;");break;
				case 'Â': sb.append("&Acirc;");break;
				case 'ä': sb.append("&auml;");break;
				case 'Ä': sb.append("&Auml;");break;
				case 'å': sb.append("&aring;");break;
				case 'Å': sb.append("&Aring;");break;
				case 'æ': sb.append("&aelig;");break;
				case 'Æ': sb.append("&AElig;");break;
				case 'ç': sb.append("&ccedil;");break;
				case 'Ç': sb.append("&Ccedil;");break;
				case 'é': sb.append("&eacute;");break;
				case 'É': sb.append("&Eacute;");break;
				case 'è': sb.append("&egrave;");break;
				case 'È': sb.append("&Egrave;");break;
				case 'ê': sb.append("&ecirc;");break;
				case 'Ê': sb.append("&Ecirc;");break;
				case 'ë': sb.append("&euml;");break;
				case 'Ë': sb.append("&Euml;");break;
				case 'ï': sb.append("&iuml;");break;
				case 'Ï': sb.append("&Iuml;");break;
				case 'ô': sb.append("&ocirc;");break;
				case 'Ô': sb.append("&Ocirc;");break;
				case 'ö': sb.append("&ouml;");break;
				case 'Ö': sb.append("&Ouml;");break;
				case 'ø': sb.append("&oslash;");break;
				case 'Ø': sb.append("&Oslash;");break;
				case 'ß': sb.append("&szlig;");break;
				case 'ù': sb.append("&ugrave;");break;
				case 'Ù': sb.append("&Ugrave;");break;
				case 'û': sb.append("&ucirc;");break;
				case 'Û': sb.append("&Ucirc;");break;
				case 'ü': sb.append("&uuml;");break;
				case 'Ü': sb.append("&Uuml;");break;
				case '®': sb.append("&reg;");break;
				case '©': sb.append("&copy;");break;
				case '€': sb.append("&euro;"); break;
				case '“': sb.append("&quot;"); break;
				case '”': sb.append("&quot;"); break;
				case ' ': sb.append("&nbsp;");break;
				// be carefull with this one (non-breaking whitee space)
				//case ' ': sb.append("&nbsp;");break;

				default:  sb.append(c); break;
			}
		}
		return sb.toString();
	}
}
