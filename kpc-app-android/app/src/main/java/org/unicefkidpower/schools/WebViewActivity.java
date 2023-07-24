package org.unicefkidpower.schools;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends BaseActivity {
	public static final String EXTRA_KEY_URL = "EXTRA_KEY_URL";
	private WebView webView;
	private String url = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);

		url = getIntent().getExtras().getString(EXTRA_KEY_URL, "");

		webView = (WebView) findViewById(R.id.wv_main);
		webView.setBackgroundColor(Color.argb(1, 0, 0, 0));
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setDomStorageEnabled(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
		webView.getSettings().setSupportMultipleWindows(false);
		webView.getSettings().setSupportZoom(false);
		if (Build.VERSION.SDK_INT >= 19) {
			// chromium, enable hardware acceleration
			webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			// older android version, disable hardware acceleration
			webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);

		webView.loadUrl(url);

		webView.setWebViewClient(new WebViewClient() {
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
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);

				if (errorCode == WebViewClient.ERROR_HOST_LOOKUP) {
					UIManager.sharedInstance().showToastMessage(WebViewActivity.this,
							getString(R.string.error_network));
					webView.setVisibility(View.INVISIBLE);
				}
			}
		});
	}

	@Override
	protected boolean isUseEvent() {
		return false;
	}
}
