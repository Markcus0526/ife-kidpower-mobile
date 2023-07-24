package org.unicefkidpower.kid_power.View.Activities.Onboarding;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;

import org.apache.http.util.ByteArrayBuffer;
import org.unicefkidpower.kid_power.R;
import org.unicefkidpower.kid_power.View.CustomControls.KPHTextView;
import org.unicefkidpower.kid_power.View.Super.SuperActivity;

import java.io.InputStream;

/**
 * Created by Ruifeng Shi on 3/15/2017.
 */

public class AcknowledgementsActivity extends SuperActivity {
	private final int BUFFER_SIZE		= 8192;

	private String htmlContents			= "";
	private String errMessage			= "";
	private KPHTextView txtContents		= null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_acknowledgements);
	}

	@Override
	public void initControls() {
		super.initControls();

		txtContents = (KPHTextView)findViewById(R.id.txt_contents);

		showProgressDialog();
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ByteArrayBuffer buffer = new ByteArrayBuffer(1);
					byte[] buf = new byte[BUFFER_SIZE];
					int readLen = 0;
					InputStream inputStream = getAssets().open("acknowledgements.html");

					while ((readLen = inputStream.read(buf)) >= 0) {
						if (readLen == 0)
							continue;

						buffer.append(buf, 0, readLen);
					}

					byte[] byteArray = buffer.toByteArray();

					htmlContents = new String(byteArray);

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							loadSuccess();
						}
					});
				} catch (Exception ex) {
					ex.printStackTrace();

					errMessage = ex.getMessage();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							loadFailure();
						}
					});
				}
			}
		});
		thread.run();
	}


	private void loadSuccess() {
		dismissProgressDialog();

		if (Build.VERSION.SDK_INT < 24)
			txtContents.setText(Html.fromHtml(htmlContents));
		else
			txtContents.setText(Html.fromHtml(htmlContents, Html.FROM_HTML_MODE_LEGACY));
	}

	private void loadFailure() {
		dismissProgressDialog();
		txtContents.setText(errMessage);
	}
}
