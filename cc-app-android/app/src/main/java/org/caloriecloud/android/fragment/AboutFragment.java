package org.caloriecloud.android.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.caloriecloud.android.R;
import org.caloriecloud.android.util.CCStatics;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.caloriecloud.android.R.id.aboutUsWebView;

public class AboutFragment extends android.app.Fragment{

    public static final String TAG = AboutFragment.class.getSimpleName();

    @BindView(aboutUsWebView)
    WebView mWebView;

    @BindView(R.id.loadingIndicator)
    ContentLoadingProgressBar mLoadingIndicator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, rootView);

        mWebView.loadDataWithBaseURL("file:///android_asset/", CCStatics.aboutUsContent, "text/html", "utf-8", null);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mLoadingIndicator.hide();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                view.getContext().startActivity(i);
                return true;
            }
        });
        return rootView;
    }


}
