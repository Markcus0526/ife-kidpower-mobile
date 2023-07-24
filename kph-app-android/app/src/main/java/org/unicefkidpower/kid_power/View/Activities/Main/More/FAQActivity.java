package org.unicefkidpower.kid_power.View.Activities.Main.More;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.unicefkidpower.kid_power.Misc.KPHConstants;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;

/**
 * Created by Ruifeng Shi on 11/13/2015.
 */
public class FAQActivity extends SuperActivity {
	// UI Controls
	private WebView			faqWebView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_faq);

		faqWebView = (WebView) findViewById(R.id.wv_faq);
		faqWebView.setBackgroundColor(Color.argb(1, 0, 0, 0));
		faqWebView.getSettings().setJavaScriptEnabled(true);
		faqWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		faqWebView.getSettings().setSupportMultipleWindows(false);
		faqWebView.getSettings().setSupportZoom(false);

		if (Build.VERSION.SDK_INT >= 19) {
			// chromium, enable hardware acceleration
			faqWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			// older android version, disable hardware acceleration
			faqWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		faqWebView.setVerticalScrollBarEnabled(false);
		faqWebView.setHorizontalScrollBarEnabled(false);
		faqWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.startsWith("mailto:")) {
					String mail = url.replaceFirst("mailto:", "");
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("message/rfc822");
					intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mail});

					startActivity(Intent.createChooser(intent, "Send Email"));
					return true;
				}

				return false;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				showProgressDialog();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				dismissProgressDialog();
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);

				if (errorCode == WebViewClient.ERROR_HOST_LOOKUP) {
					showErrorDialog(getString(R.string.network_error_lost_network_connection));
					faqWebView.setVisibility(View.INVISIBLE);
				}
			}
		});

		faqWebView.loadUrl(KPHConstants.URL_FAQ);
	}
}
